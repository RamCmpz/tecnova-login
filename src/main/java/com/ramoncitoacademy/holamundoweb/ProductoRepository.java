package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Patrón Repository: todo el SQL de productos y sus catálogos vive aquí. */
public class ProductoRepository {

    /** Par id y nombre para llenar listas desplegables. */
    public static class Opcion {
        private final int id;
        private final String nombre;

        public Opcion(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
    }

    private static final String SELECT_BASE =
            "SELECT p.idProducto, p.codigo, p.nombre, p.descripcion, "
            + "p.idCategoria, c.nombre AS categoria, p.idMarca, m.nombre AS marca, "
            + "p.idUnidadMedida, u.abreviatura, p.precioCompra, p.precioVenta, "
            + "p.stock, p.stockMinimo, p.estado "
            + "FROM producto p "
            + "JOIN categoria c ON c.idCategoria = p.idCategoria "
            + "JOIN marca m ON m.idMarca = p.idMarca "
            + "JOIN unidad_medida u ON u.idUnidadMedida = p.idUnidadMedida ";

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "ORDER BY p.idProducto");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE p.idProducto = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existeCodigo(String codigo) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void crear(Producto p, String registradoPor) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, descripcion, idCategoria, "
                + "idMarca, idUnidadMedida, precioCompra, precioVenta, stock, "
                + "stockMinimo, usuarioRegistro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setInt(4, p.getIdCategoria());
            ps.setInt(5, p.getIdMarca());
            ps.setInt(6, p.getIdUnidadMedida());
            ps.setBigDecimal(7, p.getPrecioCompra());
            ps.setBigDecimal(8, p.getPrecioVenta());
            ps.setInt(9, p.getStock());
            ps.setInt(10, p.getStockMinimo());
            ps.setString(11, registradoPor);
            ps.executeUpdate();
        }
    }

    /** El código no se modifica. */
    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, descripcion = ?, idCategoria = ?, "
                + "idMarca = ?, idUnidadMedida = ?, precioCompra = ?, precioVenta = ?, "
                + "stock = ?, stockMinimo = ? WHERE idProducto = ?";
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getIdCategoria());
            ps.setInt(4, p.getIdMarca());
            ps.setInt(5, p.getIdUnidadMedida());
            ps.setBigDecimal(6, p.getPrecioCompra());
            ps.setBigDecimal(7, p.getPrecioVenta());
            ps.setInt(8, p.getStock());
            ps.setInt(9, p.getStockMinimo());
            ps.setInt(10, p.getIdProducto());
            ps.executeUpdate();
        }
    }

    /** Baja lógica: estado = 'Inactivo'. Reactivar: estado = 'Activo'. */
    public void cambiarEstado(int id, String estado) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE producto SET estado = ? WHERE idProducto = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<Opcion> listarCategorias() throws SQLException {
        return opciones("SELECT idCategoria, nombre FROM categoria "
                + "WHERE estado = 'Activo' ORDER BY nombre");
    }

    public List<Opcion> listarMarcas() throws SQLException {
        return opciones("SELECT idMarca, nombre FROM marca "
                + "WHERE estado = 'Activo' ORDER BY nombre");
    }

    public List<Opcion> listarUnidades() throws SQLException {
        return opciones("SELECT idUnidadMedida, nombre FROM unidad_medida "
                + "WHERE estado = 'Activo' ORDER BY nombre");
    }

    private List<Opcion> opciones(String sql) throws SQLException {
        List<Opcion> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Opcion(rs.getInt(1), rs.getString(2)));
            }
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("idProducto"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setIdCategoria(rs.getInt("idCategoria"));
        p.setCategoria(rs.getString("categoria"));
        p.setIdMarca(rs.getInt("idMarca"));
        p.setMarca(rs.getString("marca"));
        p.setIdUnidadMedida(rs.getInt("idUnidadMedida"));
        p.setUnidad(rs.getString("abreviatura"));
        p.setPrecioCompra(rs.getBigDecimal("precioCompra"));
        p.setPrecioVenta(rs.getBigDecimal("precioVenta"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stockMinimo"));
        p.setEstado(rs.getString("estado"));
        return p;
    }
}
