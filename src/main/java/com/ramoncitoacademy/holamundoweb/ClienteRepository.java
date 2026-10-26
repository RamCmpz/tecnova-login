package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Patrón Repository: todo el SQL de clientes vive en esta clase. */
public class ClienteRepository {

    private static final String SELECT_BASE =
            "SELECT c.idCliente, c.idDatosPersonales, d.nombres, d.apellidos, "
            + "d.dni, d.correo, d.telefono, d.direccion, c.tipoCliente, "
            + "c.ruc, c.razonSocial, c.estado "
            + "FROM cliente c "
            + "JOIN datos_personales d ON d.idDatosPersonales = c.idDatosPersonales ";

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "ORDER BY c.idCliente");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE c.idCliente = ?")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existeDni(String dni) throws SQLException {
        return existe("SELECT 1 FROM datos_personales WHERE dni = ?", dni);
    }

    public boolean existeRuc(String ruc) throws SQLException {
        return existe("SELECT 1 FROM cliente WHERE ruc = ?", ruc);
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

    /** Guarda datos personales y cliente en una sola transacción. */
    public void crear(Cliente c, String registradoPor) throws SQLException {
        String sqlDatos = "INSERT INTO datos_personales (nombres, apellidos, dni, "
                + "correo, telefono, direccion, usuarioRegistro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCliente = "INSERT INTO cliente (idDatosPersonales, tipoCliente, "
                + "ruc, razonSocial, usuarioRegistro) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psD = con.prepareStatement(
                         sqlDatos, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psC = con.prepareStatement(sqlCliente)) {

                psD.setString(1, c.getNombres());
                psD.setString(2, c.getApellidos());
                psD.setString(3, c.getDni());
                psD.setString(4, c.getCorreo());
                psD.setString(5, c.getTelefono());
                psD.setString(6, c.getDireccion());
                psD.setString(7, registradoPor);
                psD.executeUpdate();

                int idDatos;
                try (ResultSet keys = psD.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se obtuvo el id de datos personales.");
                    }
                    idDatos = keys.getInt(1);
                }

                psC.setInt(1, idDatos);
                psC.setString(2, c.getTipoCliente());
                psC.setString(3, c.getRuc());
                psC.setString(4, c.getRazonSocial());
                psC.setString(5, registradoPor);
                psC.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** El DNI no se modifica. Actualiza datos personales y datos de cliente en una transacción. */
    public void actualizar(Cliente c) throws SQLException {
        String sqlDatos = "UPDATE datos_personales d "
                + "JOIN cliente cl ON cl.idDatosPersonales = d.idDatosPersonales "
                + "SET d.nombres = ?, d.apellidos = ?, d.correo = ?, "
                + "d.telefono = ?, d.direccion = ? WHERE cl.idCliente = ?";
        String sqlCliente = "UPDATE cliente SET tipoCliente = ?, ruc = ?, "
                + "razonSocial = ? WHERE idCliente = ?";

        try (Connection con = ConexionDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psD = con.prepareStatement(sqlDatos);
                 PreparedStatement psC = con.prepareStatement(sqlCliente)) {

                psD.setString(1, c.getNombres());
                psD.setString(2, c.getApellidos());
                psD.setString(3, c.getCorreo());
                psD.setString(4, c.getTelefono());
                psD.setString(5, c.getDireccion());
                psD.setInt(6, c.getIdCliente());
                psD.executeUpdate();

                psC.setString(1, c.getTipoCliente());
                psC.setString(2, c.getRuc());
                psC.setString(3, c.getRazonSocial());
                psC.setInt(4, c.getIdCliente());
                psC.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Baja lógica: estado = 'Inactivo'. Reactivar: estado = 'Activo'. */
    public void cambiarEstado(int idCliente, String estado) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE cliente SET estado = ? WHERE idCliente = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("idCliente"));
        c.setIdDatosPersonales(rs.getInt("idDatosPersonales"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setDni(rs.getString("dni"));
        c.setCorreo(rs.getString("correo"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setTipoCliente(rs.getString("tipoCliente"));
        c.setRuc(rs.getString("ruc"));
        c.setRazonSocial(rs.getString("razonSocial"));
        c.setEstado(rs.getString("estado"));
        return c;
    }
}
