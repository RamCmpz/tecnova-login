
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

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String usuario = request.getParameter("usuario");
        String contrasena = request.getParameter("contrasena");

        if (usuario == null || contrasena == null
                || usuario.trim().isEmpty()
                || contrasena.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp?error=campos");
            return;
        }

        String sql =
                "SELECT password_hash FROM usuarios WHERE usuario = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, usuario.trim());

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    String hash = resultado.getString("password_hash");

                    if (BCrypt.checkpw(contrasena, hash)) {
                        HttpSession sesion = request.getSession();
                        sesion.setAttribute("usuario", usuario.trim());

                        response.sendRedirect(
                                request.getContextPath() + "/inicio.jsp");
                        return;
                    }
                }
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=credenciales");

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al consultar la base de datos.", e);
        }
    }
}
