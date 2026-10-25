package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Patrón Repository: todo el SQL de proveedores vive en esta clase. */
public class ProveedorRepository {

    private static final String SELECT_BASE =
            "SELECT p.idProveedor, p.idDatosPersonales, p.razonSocial, p.ruc, "
            + "d.nombres, d.apellidos, d.dni, d.correo, d.telefono, d.direccion, "
            + "p.estado "
            + "FROM proveedor p "
            + "JOIN datos_personales d ON d.idDatosPersonales = p.idDatosPersonales ";

    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "ORDER BY p.idProveedor");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Proveedor buscarPorId(int idProveedor) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE p.idProveedor = ?")) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existeDni(String dni) throws SQLException {
        return existe("SELECT 1 FROM datos_personales WHERE dni = ?", dni);
    }

    public boolean existeRuc(String ruc) throws SQLException {
        return existe("SELECT 1 FROM proveedor WHERE ruc = ?", ruc);
    }

    private boolean existe(String sql, String valor) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Guarda datos personales y proveedor en una sola transacción. */
    public void crear(Proveedor p, String registradoPor) throws SQLException {
        String sqlDatos = "INSERT INTO datos_personales (nombres, apellidos, dni, "
                + "correo, telefono, direccion, usuarioRegistro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlProv = "INSERT INTO proveedor (idDatosPersonales, razonSocial, "
                + "ruc, usuarioRegistro) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psD = con.prepareStatement(
                         sqlDatos, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psP = con.prepareStatement(sqlProv)) {

                psD.setString(1, p.getNombres());
                psD.setString(2, p.getApellidos());
                psD.setString(3, p.getDni());
                psD.setString(4, p.getCorreo());
                psD.setString(5, p.getTelefono());
                psD.setString(6, p.getDireccion());
                psD.setString(7, registradoPor);
                psD.executeUpdate();

                int idDatos;
                try (ResultSet keys = psD.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se obtuvo el id de datos personales.");
                    }
                    idDatos = keys.getInt(1);
                }

                psP.setInt(1, idDatos);
                psP.setString(2, p.getRazonSocial());
                psP.setString(3, p.getRuc());
                psP.setString(4, registradoPor);
                psP.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** El DNI y el RUC no se modifican. */
    public void actualizar(Proveedor p) throws SQLException {
        String sqlDatos = "UPDATE datos_personales d "
                + "JOIN proveedor pr ON pr.idDatosPersonales = d.idDatosPersonales "
                + "SET d.nombres = ?, d.apellidos = ?, d.correo = ?, "
                + "d.telefono = ?, d.direccion = ? WHERE pr.idProveedor = ?";
        String sqlProv = "UPDATE proveedor SET razonSocial = ? WHERE idProveedor = ?";

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psD = con.prepareStatement(sqlDatos);
                 PreparedStatement psP = con.prepareStatement(sqlProv)) {

                psD.setString(1, p.getNombres());
                psD.setString(2, p.getApellidos());
                psD.setString(3, p.getCorreo());
                psD.setString(4, p.getTelefono());
                psD.setString(5, p.getDireccion());
                psD.setInt(6, p.getIdProveedor());
                psD.executeUpdate();

                psP.setString(1, p.getRazonSocial());
                psP.setInt(2, p.getIdProveedor());
                psP.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Baja lógica: estado = 'Inactivo'. Reactivar: estado = 'Activo'. */
    public void cambiarEstado(int idProveedor, String estado) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE proveedor SET estado = ? WHERE idProveedor = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idProveedor);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("idProveedor"));
        p.setIdDatosPersonales(rs.getInt("idDatosPersonales"));
        p.setRazonSocial(rs.getString("razonSocial"));
        p.setRuc(rs.getString("ruc"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        p.setDni(rs.getString("dni"));
        p.setCorreo(rs.getString("correo"));
        p.setTelefono(rs.getString("telefono"));
        p.setDireccion(rs.getString("direccion"));
        p.setEstado(rs.getString("estado"));
        return p;
    }
}
