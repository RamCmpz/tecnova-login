package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Patrón Repository: concentra el acceso a datos de la tabla usuarios.
 * El servlet no conoce SQL; solo llama a estos métodos.
 */
public class UsuarioRepository {

    /** Elemento simple para llenar listas desplegables (roles, permisos). */
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
            "SELECT u.id, u.usuario, u.nombre, u.rol_id, r.nombre_rol, "
            + "u.permiso_id, p.nombre_permiso, u.bloqueado, "
            + "u.intentosFallidos, u.estado, u.ultimoAcceso "
            + "FROM usuarios u "
            + "LEFT JOIN rol r ON r.rol_id = u.rol_id "
            + "LEFT JOIN permiso p ON p.permiso_id = u.permiso_id ";

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "ORDER BY u.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Usuario buscarPorId(long id) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE u.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existeUsuario(String usuario) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void crear(String usuario, String nombre, String passwordPlano,
                      int rolId, int permisoId, String registradoPor)
            throws SQLException {
        String hash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
        String sql = "INSERT INTO usuarios (usuario, password_hash, nombre, "
                + "rol_id, permiso_id, usuarioRegistro) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, hash);
            ps.setString(3, nombre);
            ps.setInt(4, rolId);
            ps.setInt(5, permisoId);
            ps.setString(6, registradoPor);
            ps.executeUpdate();
        }
    }

    /** Si passwordNueva es null, la contraseña no se modifica. */
    public void actualizar(long id, String nombre, int rolId, int permisoId,
                           String passwordNueva) throws SQLException {
        boolean cambiaClave = passwordNueva != null;
        String sql = cambiaClave
                ? "UPDATE usuarios SET nombre = ?, rol_id = ?, permiso_id = ?, "
                  + "password_hash = ? WHERE id = ?"
                : "UPDATE usuarios SET nombre = ?, rol_id = ?, permiso_id = ? "
                  + "WHERE id = ?";
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, rolId);
            ps.setInt(3, permisoId);
            if (cambiaClave) {
                ps.setString(4, BCrypt.hashpw(passwordNueva, BCrypt.gensalt(10)));
                ps.setLong(5, id);
            } else {
                ps.setLong(4, id);
            }
            ps.executeUpdate();
        }
    }

    /** Baja lógica: estado = 'Inactivo'. Reactivar: estado = 'Activo'. */
    public void cambiarEstado(long id, String estado) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE usuarios SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void desbloquear(long id) throws SQLException {
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE usuarios SET bloqueado = 0, intentosFallidos = 0 "
                     + "WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public boolean esAdministrador(String usuario) throws SQLException {
        String sql = "SELECT 1 FROM usuarios u "
                + "JOIN rol r ON r.rol_id = u.rol_id "
                + "WHERE u.usuario = ? AND r.nombre_rol = 'Administrador' "
                + "AND u.estado = 'Activo'";
        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Opcion> listarRoles() throws SQLException {
        return listarOpciones("SELECT rol_id, nombre_rol FROM rol "
                + "WHERE estado = 'Activo' ORDER BY nombre_rol");
    }

    public List<Opcion> listarPermisos() throws SQLException {
        return listarOpciones("SELECT permiso_id, nombre_permiso FROM permiso "
                + "WHERE estado = 'Activo' ORDER BY nombre_permiso");
    }

    private List<Opcion> listarOpciones(String sql) throws SQLException {
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

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsuario(rs.getString("usuario"));
        u.setNombre(rs.getString("nombre"));

        int rolId = rs.getInt("rol_id");
        u.setRolId(rs.wasNull() ? null : rolId);
        u.setRolNombre(rs.getString("nombre_rol"));

        int permisoId = rs.getInt("permiso_id");
        u.setPermisoId(rs.wasNull() ? null : permisoId);
        u.setPermisoNombre(rs.getString("nombre_permiso"));

        u.setBloqueado(rs.getBoolean("bloqueado"));
        u.setIntentosFallidos(rs.getInt("intentosFallidos"));
        u.setEstado(rs.getString("estado"));
        u.setUltimoAcceso(rs.getTimestamp("ultimoAcceso"));
        return u;
    }
}
