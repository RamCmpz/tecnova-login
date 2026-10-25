package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private final AutenticacionFacade autenticacion = new AutenticacionFacade();

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

        ResultadoLogin resultado;
        try {
            resultado = autenticacion.autenticar(usuario, contrasena);
        } catch (SQLException e) {
            throw new ServletException(
                    "Error al consultar la base de datos.", e);
        }

        if (resultado.getEstado() == ResultadoLogin.Estado.OK) {
            HttpSession sesion = request.getSession();
            sesion.setAttribute("usuario", usuario);
            response.sendRedirect(base + "/inicio.jsp");

        } else if (resultado.getEstado() == ResultadoLogin.Estado.BLOQUEADO) {
            response.sendRedirect(base + "/login.jsp?error=bloqueado");

        } else {
            String destino = base + "/login.jsp?error=credenciales";
            if (resultado.getIntentosRestantes() >= 0) {
                destino += "&restantes=" + resultado.getIntentosRestantes();
            }
            response.sendRedirect(destino);
        }
    }
}
