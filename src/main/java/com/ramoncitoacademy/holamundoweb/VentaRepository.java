package com.ramoncitoacademy.holamundoweb;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Patrón Repository: todo el SQL de ventas y comprobantes vive aquí. */
public class VentaRepository {

    /** Error de regla de negocio (stock insuficiente, cliente inactivo, etc.). */
    public static class VentaException extends Exception {
        public VentaException(String mensaje) {
            super(mensaje);
        }
    }

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final String TIPO = "Boleta";
    private static final String SERIE = "B001";
    private static final int MAX_CANTIDAD = 1_000_000;

    private static final String SELECT_BASE =
            "SELECT v.idVenta, v.idCliente, v.fechaVenta, v.subtotal, v.igv, v.total, "
            + "v.estado, co.tipo, co.serie, co.numero, "
            + "d.nombres, d.apellidos, d.dni, u.usuario "
            + "FROM venta v "
            + "JOIN comprobante co ON co.idVenta = v.idVenta "
            + "JOIN cliente c ON c.idCliente = v.idCliente "
            + "JOIN datos_personales d ON d.idDatosPersonales = c.idDatosPersonales "
            + "JOIN usuarios u ON u.id = v.idUsuario ";

    // ---------- Lectura ----------

    public List<Venta> listar() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "ORDER BY v.idVenta DESC LIMIT 100");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Devuelve la venta con su detalle, o null si no existe. */
    public Venta buscarPorId(int idVenta) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection()) {
            Venta v;
            try (PreparedStatement ps = con.prepareStatement(
                    SELECT_BASE + "WHERE v.idVenta = ?")) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    v = mapear(rs);
                }
            }

            String sqlDetalle = "SELECT dv.idDetalle, dv.idProducto, p.codigo, p.nombre, "
                    + "dv.cantidad, dv.precioUnitario, dv.subtotal "
                    + "FROM detalle_venta dv "
                    + "JOIN producto p ON p.idProducto = dv.idProducto "
                    + "WHERE dv.idVenta = ? ORDER BY dv.idDetalle";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleVenta d = new DetalleVenta();
                        d.setIdDetalle(rs.getInt("idDetalle"));
                        d.setIdProducto(rs.getInt("idProducto"));
                        d.setCodigo(rs.getString("codigo"));
                        d.setProducto(rs.getString("nombre"));
                        d.setCantidad(rs.getInt("cantidad"));
                        d.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                        d.setSubtotal(rs.getBigDecimal("subtotal"));
                        v.getDetalles().add(d);
                    }
                }
            }
            return v;
        }
    }

    // ---------- Crear venta ----------

    /**
     * Registra una venta con su boleta en una transacción propia.
     * Los precios se toman de la base de datos, nunca del formulario.
     *
     * @param items mapa idProducto -> cantidad
     * @return el id de la venta creada
     */
    public int crear(int idCliente, String usuario, Map<Integer, Integer> items)
            throws SQLException, VentaException {

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                int idVenta = crearEnTransaccion(con, idCliente, usuario, items, TIPO, SERIE);
                con.commit();
                return idVenta;
            } catch (SQLException | VentaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Misma lógica de crear(), pero dentro de una transacción ya abierta.
     * No hace commit ni rollback: eso lo decide quien llama. Así otro proceso
     * (por ejemplo, convertir un pedido) puede unirla a su propia transacción.
     */
    public int crearEnTransaccion(Connection con, int idCliente, String usuario,
                                  Map<Integer, Integer> items, String tipo, String serie)
            throws SQLException, VentaException {

        if (items == null || items.isEmpty()) {
            throw new VentaException("Agrega al menos un producto a la venta.");
        }
        // Orden fijo por idProducto: evita bloqueos cruzados entre ventas simultáneas
        Map<Integer, Integer> ordenados = new TreeMap<>(items);

        // 1. Cliente activo
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT estado FROM cliente WHERE idCliente = ?")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"Activo".equalsIgnoreCase(rs.getString(1))) {
                    throw new VentaException("El cliente no existe o está inactivo.");
                }
            }
        }

        // 2. Usuario que registra la venta
        long idUsuario;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM usuarios WHERE usuario = ? AND estado = 'Activo'")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new VentaException("El usuario de la sesión no es válido.");
                }
                idUsuario = rs.getLong(1);
            }
        }

        // 3. Productos: bloquea cada fila, valida stock y toma el precio real
        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT codigo, nombre, precioVenta, stock, estado "
                + "FROM producto WHERE idProducto = ? FOR UPDATE")) {
            for (Map.Entry<Integer, Integer> e : ordenados.entrySet()) {
                int cantidad = e.getValue();
                if (cantidad < 1 || cantidad > MAX_CANTIDAD) {
                    throw new VentaException("La cantidad de un producto no es válida.");
                }
                ps.setInt(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"Activo".equalsIgnoreCase(rs.getString("estado"))) {
                        throw new VentaException("Un producto no existe o está inactivo.");
                    }
                    String nombre = rs.getString("nombre");
                    int stock = rs.getInt("stock");
                    if (stock < cantidad) {
                        throw new VentaException("Stock insuficiente de \"" + nombre
                                + "\": quedan " + stock + " y se pidieron " + cantidad + ".");
                    }

                    BigDecimal precio = rs.getBigDecimal("precioVenta");
                    BigDecimal subtotalLinea = precio.multiply(BigDecimal.valueOf(cantidad));

                    DetalleVenta d = new DetalleVenta();
                    d.setIdProducto(e.getKey());
                    d.setCantidad(cantidad);
                    d.setPrecioUnitario(precio);
                    d.setSubtotal(subtotalLinea);
                    detalles.add(d);

                    total = total.add(subtotalLinea);
                }
            }
        }

        // 4. Totales: el precio incluye IGV (18 %)
        BigDecimal subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);

        // 5. Cabecera de la venta
        int idVenta;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO venta (idCliente, idUsuario, subtotal, igv, total) "
                + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            ps.setLong(2, idUsuario);
            ps.setBigDecimal(3, subtotal);
            ps.setBigDecimal(4, igv);
            ps.setBigDecimal(5, total);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el id de la venta.");
                }
                idVenta = keys.getInt(1);
            }
        }

        // 6. Detalle y descuento de stock
        try (PreparedStatement psDet = con.prepareStatement(
                     "INSERT INTO detalle_venta (idVenta, idProducto, cantidad, "
                     + "precioUnitario, subtotal) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement psStock = con.prepareStatement(
                     "UPDATE producto SET stock = stock - ? "
                     + "WHERE idProducto = ? AND stock >= ?")) {
            for (DetalleVenta d : detalles) {
                psDet.setInt(1, idVenta);
                psDet.setInt(2, d.getIdProducto());
                psDet.setInt(3, d.getCantidad());
                psDet.setBigDecimal(4, d.getPrecioUnitario());
                psDet.setBigDecimal(5, d.getSubtotal());
                psDet.executeUpdate();

                psStock.setInt(1, d.getCantidad());
                psStock.setInt(2, d.getIdProducto());
                psStock.setInt(3, d.getCantidad());
                if (psStock.executeUpdate() != 1) {
                    throw new VentaException("No se pudo descontar el stock de un producto.");
                }
            }
        }

        // 7. Número de comprobante: sin repetidos ni saltos
        int numero;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ultimoNumero FROM serie_comprobante "
                + "WHERE tipo = ? AND serie = ? FOR UPDATE")) {
            ps.setString(1, tipo);
            ps.setString(2, serie);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new VentaException("No hay una serie configurada para " + tipo + ".");
                }
                numero = rs.getInt(1) + 1;
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE serie_comprobante SET ultimoNumero = ? "
                + "WHERE tipo = ? AND serie = ?")) {
            ps.setInt(1, numero);
            ps.setString(2, tipo);
            ps.setString(3, serie);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO comprobante (idVenta, tipo, serie, numero) "
                + "VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, idVenta);
            ps.setString(2, tipo);
            ps.setString(3, serie);
            ps.setInt(4, numero);
            ps.executeUpdate();
        }

        return idVenta;
    }

    // ---------- Anular venta ----------

    /** Marca la venta como Anulada y devuelve el stock, todo en una transacción. */
    public void anular(int idVenta) throws SQLException, VentaException {
        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT estado FROM venta WHERE idVenta = ? FOR UPDATE")) {
                    ps.setInt(1, idVenta);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new VentaException("La venta no existe.");
                        }
                        if (!"Emitida".equalsIgnoreCase(rs.getString(1))) {
                            throw new VentaException("La venta ya está anulada.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE venta SET estado = 'Anulada' WHERE idVenta = ?")) {
                    ps.setInt(1, idVenta);
                    ps.executeUpdate();
                }

                try (PreparedStatement sel = con.prepareStatement(
                             "SELECT idProducto, cantidad FROM detalle_venta "
                             + "WHERE idVenta = ? ORDER BY idProducto");
                     PreparedStatement upd = con.prepareStatement(
                             "UPDATE producto SET stock = stock + ? WHERE idProducto = ?")) {
                    sel.setInt(1, idVenta);
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            upd.setInt(1, rs.getInt("cantidad"));
                            upd.setInt(2, rs.getInt("idProducto"));
                            upd.executeUpdate();
                        }
                    }
                }

                con.commit();

            } catch (SQLException | VentaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- Utilidades ----------

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("idVenta"));
        v.setIdCliente(rs.getInt("idCliente"));
        v.setFechaVenta(rs.getTimestamp("fechaVenta"));
        v.setSubtotal(rs.getBigDecimal("subtotal"));
        v.setIgv(rs.getBigDecimal("igv"));
        v.setTotal(rs.getBigDecimal("total"));
        v.setEstado(rs.getString("estado"));
        v.setTipoComprobante(rs.getString("tipo"));
        v.setSerie(rs.getString("serie"));
        v.setNumero(rs.getInt("numero"));
        v.setClienteNombre(rs.getString("nombres") + " " + rs.getString("apellidos"));
        v.setClienteDni(rs.getString("dni"));
        v.setUsuario(rs.getString("usuario"));
        return v;
    }
}
