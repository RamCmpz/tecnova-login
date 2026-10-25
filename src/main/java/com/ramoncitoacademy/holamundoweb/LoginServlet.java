package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final int MAX_INTENTOS = 3;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String usuario = request.getParameter("usuario");
        String contrasena = request.getParameter("contrasena");
        String base = request.getContextPath();

        if (usuario == null || contrasena == null
                || usuario.trim().isEmpty()
                || contrasena.isEmpty()) {
            response.sendRedirect(base + "/login.jsp?error=campos");
            return;
        }
        usuario = usuario.trim();

        String sqlBuscar = "SELECT id, password_hash, intentosFallidos, "
                + "bloqueado, estado FROM usuarios WHERE usuario = ?";

        try (Connection con = ConexionDB.getInstance().getConnection();
             PreparedStatement buscar = con.prepareStatement(sqlBuscar)) {

            buscar.setString(1, usuario);

            try (ResultSet rs = buscar.executeQuery()) {

                // Usuario inexistente: mismo mensaje que clave incorrecta
                if (!rs.next()) {
                    response.sendRedirect(base + "/login.jsp?error=credenciales");
                    return;
                }

                long id = rs.getLong("id");
                String hash = rs.getString("password_hash");
                int intentos = rs.getInt("intentosFallidos");
                boolean bloqueado = rs.getBoolean("bloqueado");
                String estado = rs.getString("estado");

                // Cuenta bloqueada o inactiva
                if (bloqueado || !"Activo".equalsIgnoreCase(estado)) {
                    response.sendRedirect(base + "/login.jsp?error=bloqueado");
                    return;
                }

                // Contraseña correcta
                if (BCrypt.checkpw(contrasena, hash)) {
                    try (PreparedStatement ok = con.prepareStatement(
                            "UPDATE usuarios SET intentosFallidos = 0, "
                            + "ultimoAcceso = NOW() WHERE id = ?")) {
                        ok.setLong(1, id);
                        ok.executeUpdate();
                    }
                    HttpSession sesion = request.getSession();
                    sesion.setAttribute("usuario", usuario);
                    response.sendRedirect(base + "/inicio.jsp");
                    return;
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
                    response.sendRedirect(base + "/login.jsp?error=bloqueado");
                } else {
                    response.sendRedirect(base + "/login.jsp?error=credenciales&restantes="
                            + (MAX_INTENTOS - intentos));
                }
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al consultar la base de datos.", e);
        }
    }
}
