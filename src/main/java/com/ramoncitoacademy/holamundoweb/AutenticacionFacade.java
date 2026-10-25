package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Patrón Facade: ofrece un único método sencillo (autenticar) que
 * esconde la consulta a la BD, la verificación BCrypt, el conteo de
 * intentos fallidos y el bloqueo de la cuenta.
 */
public class AutenticacionFacade {

    private static final int MAX_INTENTOS = 3;

    public ResultadoLogin autenticar(String usuario, String contrasena)
            throws SQLException {

        String sqlBuscar = "SELECT id, password_hash, intentosFallidos, "
                + "bloqueado, estado FROM usuarios WHERE usuario = ?";

        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement buscar = con.prepareStatement(sqlBuscar)) {

            buscar.setString(1, usuario);

            try (ResultSet rs = buscar.executeQuery()) {

                // Usuario inexistente: mismo resultado que clave incorrecta
                if (!rs.next()) {
                    return ResultadoLogin.credencialesInvalidas(-1);
                }

                long id = rs.getLong("id");
                String hash = rs.getString("password_hash");
                int intentos = rs.getInt("intentosFallidos");
                boolean bloqueado = rs.getBoolean("bloqueado");
                String estado = rs.getString("estado");

                // Cuenta bloqueada o inactiva
                if (bloqueado || !"Activo".equalsIgnoreCase(estado)) {
                    return ResultadoLogin.bloqueado();
                }

                // Contraseña correcta
                if (BCrypt.checkpw(contrasena, hash)) {
                    try (PreparedStatement ok = con.prepareStatement(
                            "UPDATE usuarios SET intentosFallidos = 0, "
                            + "ultimoAcceso = NOW() WHERE id = ?")) {
                        ok.setLong(1, id);
                        ok.executeUpdate();
                    }
                    return ResultadoLogin.ok();
                }

                // Contraseña incorrecta: suma intento y bloquea al llegar a 3
                intentos++;
                boolean quedaBloqueado = intentos >= MAX_INTENTOS;

                try (PreparedStatement fallo = con.prepareStatement(
                        "UPDATE usuarios SET intentosFallidos = ?, "
                        + "bloqueado = ? WHERE id = ?")) {
                    fallo.setInt(1, intentos);
                    fallo.setBoolean(2, quedaBloqueado);
                    fallo.setLong(3, id);
                    fallo.executeUpdate();
                }

                if (quedaBloqueado) {
                    return ResultadoLogin.bloqueado();
                }
                return ResultadoLogin.credencialesInvalidas(MAX_INTENTOS - intentos);
            }
        }
    }
}
