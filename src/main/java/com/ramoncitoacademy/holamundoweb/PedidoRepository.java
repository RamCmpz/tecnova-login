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
import com.ramoncitoacademy.holamundoweb.VentaRepository.VentaException;

/** Patrón Repository: todo el SQL de pedidos (solicitudes) vive aquí. */
public class PedidoRepository {

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final int MAX_CANTIDAD = 1_000_000;

    private final VentaRepository ventas = new VentaRepository();

    private static final String SELECT_BASE =
            "SELECT p.idPedido, p.idCliente, p.fechaPedido, p.subtotal, p.igv, p.total, "
            + "p.estado, p.idVenta, co.serie, co.numero, "
            + "d.nombres, d.apellidos, d.dni, u.usuario "
            + "FROM pedido p "
            + "JOIN cliente c ON c.idCliente = p.idCliente "
            + "JOIN datos_personales d ON d.idDatosPersonales = c.idDatosPersonales "
            + "JOIN usuarios u ON u.id = p.idUsuario "
            + "LEFT JOIN comprobante co ON co.idVenta = p.idVenta ";

    // ---------- Lectura ----------

    public List<Pedido> listar() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "ORDER BY p.idPedido DESC LIMIT 100");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Devuelve el pedido con su detalle, o null si no existe. */
    public Pedido buscarPorId(int idPedido) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection()) {
            Pedido p;
            try (PreparedStatement ps = con.prepareStatement(
                    SELECT_BASE + "WHERE p.idPedido = ?")) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    p = mapear(rs);
                }
            }

            String sqlDetalle = "SELECT dp.idDetalle, dp.idProducto, pr.codigo, pr.nombre, "
                    + "dp.cantidad, dp.precioUnitario, dp.subtotal "
                    + "FROM detalle_pedido dp "
                    + "JOIN producto pr ON pr.idProducto = dp.idProducto "
                    + "WHERE dp.idPedido = ? ORDER BY dp.idDetalle";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idPedido);
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
                        p.getDetalles().add(d);
                    }
                }
            }
            return p;
        }
    }

    // ---------- Crear pedido ----------

    /**
     * Registra una solicitud de pedido. No descuenta ni reserva stock.
     * Los precios se toman de la base de datos, nunca del formulario.
     *
     * @param items mapa idProducto -> cantidad
     * @return el id del pedido creado
     */
    public int crear(int idCliente, String usuario, Map<Integer, Integer> items)
            throws SQLException, VentaException {

        if (items == null || items.isEmpty()) {
            throw new VentaException("Agrega al menos un producto al pedido.");
        }
        Map<Integer, Integer> ordenados = new TreeMap<>(items);

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
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

                // 2. Usuario que registra el pedido
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

                // 3. Productos activos y su precio vigente
                List<DetalleVenta> detalles = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT precioVenta, estado FROM producto WHERE idProducto = ?")) {
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

                // 5. Cabecera
                int idPedido;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO pedido (idCliente, idUsuario, subtotal, igv, total) "
                        + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idCliente);
                    ps.setLong(2, idUsuario);
                    ps.setBigDecimal(3, subtotal);
                    ps.setBigDecimal(4, igv);
                    ps.setBigDecimal(5, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se obtuvo el id del pedido.");
                        }
                        idPedido = keys.getInt(1);
                    }
                }

                // 6. Detalle
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO detalle_pedido (idPedido, idProducto, cantidad, "
                        + "precioUnitario, subtotal) VALUES (?, ?, ?, ?, ?)")) {
                    for (DetalleVenta d : detalles) {
                        ps.setInt(1, idPedido);
                        ps.setInt(2, d.getIdProducto());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.setBigDecimal(5, d.getSubtotal());
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return idPedido;

            } catch (SQLException | VentaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- Convertir en venta ----------

    /**
     * Convierte un pedido pendiente en venta (con su boleta) en UNA sola
     * transacción: si algo falla, no se crea la venta y el pedido sigue Pendiente.
     *
     * @return el id de la venta creada
     */
    public int convertir(int idPedido, String usuario)
            throws SQLException, VentaException {

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Bloquea el pedido y verifica que siga pendiente
                int idCliente;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT idCliente, estado FROM pedido WHERE idPedido = ? FOR UPDATE")) {
                    ps.setInt(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new VentaException("El pedido no existe.");
                        }
                        if (!"Pendiente".equalsIgnoreCase(rs.getString("estado"))) {
                            throw new VentaException("El pedido ya no está pendiente.");
                        }
                        idCliente = rs.getInt("idCliente");
                    }
                }

                // 2. Lee los productos del pedido
                Map<Integer, Integer> items = new TreeMap<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT idProducto, cantidad FROM detalle_pedido WHERE idPedido = ?")) {
                    ps.setInt(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            items.merge(rs.getInt("idProducto"), rs.getInt("cantidad"), Integer::sum);
                        }
                    }
                }

                // 3. Crea la venta dentro de esta misma transacción
                int idVenta = ventas.crearEnTransaccion(con, idCliente, usuario, items,
                        "Boleta", "B001");

                // 4. Marca el pedido como convertido
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE pedido SET estado = 'Convertido', idVenta = ? WHERE idPedido = ?")) {
                    ps.setInt(1, idVenta);
                    ps.setInt(2, idPedido);
                    ps.executeUpdate();
                }

                con.commit();
                return idVenta;

            } catch (SQLException | VentaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- Cancelar ----------

    public void cancelar(int idPedido) throws SQLException, VentaException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE pedido SET estado = 'Cancelado' "
                     + "WHERE idPedido = ? AND estado = 'Pendiente'")) {
            ps.setInt(1, idPedido);
            if (ps.executeUpdate() != 1) {
                throw new VentaException("Solo se pueden cancelar pedidos pendientes.");
            }
        }
    }

    // ---------- Utilidades ----------

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("idPedido"));
        p.setIdCliente(rs.getInt("idCliente"));
        p.setFechaPedido(rs.getTimestamp("fechaPedido"));
        p.setSubtotal(rs.getBigDecimal("subtotal"));
        p.setIgv(rs.getBigDecimal("igv"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setEstado(rs.getString("estado"));

        int idVenta = rs.getInt("idVenta");
        p.setIdVenta(rs.wasNull() ? null : idVenta);

        String serie = rs.getString("serie");
        p.setSerie(serie);
        int numero = rs.getInt("numero");
        p.setNumero(rs.wasNull() ? null : numero);

        p.setClienteNombre(rs.getString("nombres") + " " + rs.getString("apellidos"));
        p.setClienteDni(rs.getString("dni"));
        p.setUsuario(rs.getString("usuario"));
        return p;
    }
}
