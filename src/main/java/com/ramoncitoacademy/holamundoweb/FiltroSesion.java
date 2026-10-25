package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.util.Set;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtro que protege todo el sistema: si no hay sesión iniciada,
 * redirige al login. Solo dejan pasar las rutas públicas.
 */
@WebFilter("/*")
public class FiltroSesion implements Filter {

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/", "/index.jsp", "/login.jsp",
            "/LoginServlet", "/CerrarSesionServlet"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ruta = request.getRequestURI()
                .substring(request.getContextPath().length());

        boolean publica = RUTAS_PUBLICAS.contains(ruta);

        HttpSession sesion = request.getSession(false);
        boolean logueado = sesion != null
                && sesion.getAttribute("usuario") != null;

        if (publica || logueado) {
            // Las páginas privadas no se guardan en caché del navegador
            if (!publica) {
                response.setHeader("Cache-Control",
                        "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
            }
            chain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
