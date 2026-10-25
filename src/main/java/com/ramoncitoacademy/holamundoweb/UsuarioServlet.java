package com.ramoncitoacademy.holamundoweb;

import java.util.List;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Objects;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UsuarioServlet")
public class UsuarioServlet extends HttpServlet {

    private static final Pattern PATRON_USUARIO =
            Pattern.compile("^[A-Za-z0-9_.-]{3,50}$");

    private final UsuarioRepository repo = new UsuarioRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (usuarioAutorizado(request, response) == null) {
            return;
        }

        try {
            if ("editar".equals(request.getParameter("accion"))) {
                Long id = aLong(request.getParameter("id"));
                if (id != null) {
                    request.setAttribute("editar", repo.buscarPorId(id));
                }
            }
            request.setAttribute("usuarios", repo.listar());
            request.setAttribute("roles", repo.listarRoles());
            request.setAttribute("permisos", repo.listarPermisos());
            request.getRequestDispatcher("/usuarios.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Error al consultar usuarios.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String actual = usuarioAutorizado(request, response);
        if (actual == null) {
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        try {
            switch (accion) {
                case "crear":
                    crear(request, response, actual);
                    break;
                case "actualizar":
                    actualizar(request, response, actual);
                    break;
                case "desactivar":
                case "activar":
                case "desbloquear":
                    cambiar(accion, request, response, actual);
                    break;
                default:
                    redirigir(request, response, "err=invalido");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            redirigir(request, response, "err=invalido");
        } catch (SQLException e) {
            throw new ServletException("Error al guardar el usuario.", e);
        }
    }

    // ---------- Acciones ----------

        private void crear(HttpServletRequest request, HttpServletResponse response,
                       String actual) throws SQLException, IOException {

        String usuario = recortar(request.getParameter("usuario"));
        String nombre = recortar(request.getParameter("nombre"));
        String password = request.getParameter("password");
        Integer rolId = aEntero(request.getParameter("rolId"));
        Integer permisoId = aEntero(request.getParameter("permisoId")); // null = automático

        if (usuario == null || !PATRON_USUARIO.matcher(usuario).matches()
                || nombre == null || nombre.isEmpty() || nombre.length() > 100
                || !passwordValida(password)
                || rolId == null) {
            redirigir(request, response, "err=invalido");
            return;
        }

        String rolNombre = nombreDe(repo.listarRoles(), rolId);
        if (rolNombre == null) {
            redirigir(request, response, "err=invalido");
            return;
        }

        // Factory Method: crea el usuario con los valores por defecto del rol
        Usuario nuevo = UsuarioFactory.crear(rolNombre, usuario, nombre);

        if (permisoId == null) {
            permisoId = idDe(repo.listarPermisos(), nuevo.getPermisoNombre());
            if (permisoId == null) {
                redirigir(request, response, "err=invalido");
                return;
            }
        }

        if (repo.existeUsuario(nuevo.getUsuario())) {
            redirigir(request, response, "err=duplicado");
            return;
        }

        repo.crear(nuevo.getUsuario(), nuevo.getNombre(), password,
                rolId, permisoId, actual);
        redirigir(request, response, "msg=creado");
    }

    private static String nombreDe(List<UsuarioRepository.Opcion> lista, int id) {
        for (UsuarioRepository.Opcion o : lista) {
            if (o.getId() == id) {
                return o.getNombre();
            }
        }
        return null;
    }

    private static Integer idDe(List<UsuarioRepository.Opcion> lista, String nombre) {
        for (UsuarioRepository.Opcion o : lista) {
            if (o.getNombre().equals(nombre)) {
                return o.getId();
            }
        }
        return null;
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response,
                            String actual) throws SQLException, IOException {

        Long id = aLong(request.getParameter("id"));
        String nombre = recortar(request.getParameter("nombre"));
        Integer rolId = aEntero(request.getParameter("rolId"));
        Integer permisoId = aEntero(request.getParameter("permisoId"));
        String password = request.getParameter("password");

        boolean cambiaClave = password != null && !password.isEmpty();

        if (id == null || nombre == null || nombre.isEmpty() || nombre.length() > 100
                || rolId == null || permisoId == null
                || (cambiaClave && !passwordValida(password))) {
            redirigir(request, response, "err=invalido");
            return;
        }

        Usuario objetivo = repo.buscarPorId(id);
        if (objetivo == null) {
            redirigir(request, response, "err=noexiste");
            return;
        }

        // Evita que el administrador se quite su propio rol y se bloquee solo
        if (objetivo.getUsuario().equals(actual)
                && !Objects.equals(rolId, objetivo.getRolId())) {
            redirigir(request, response, "err=propio");
            return;
        }

        repo.actualizar(id, nombre, rolId, permisoId, cambiaClave ? password : null);
        redirigir(request, response, "msg=actualizado");
    }

    private void cambiar(String accion, HttpServletRequest request,
                         HttpServletResponse response, String actual)
            throws SQLException, IOException {

        Long id = aLong(request.getParameter("id"));
        Usuario objetivo = id == null ? null : repo.buscarPorId(id);

        if (objetivo == null) {
            redirigir(request, response, "err=noexiste");
            return;
        }

        if ("desactivar".equals(accion)) {
            if (objetivo.getUsuario().equals(actual)) {
                redirigir(request, response, "err=propio");
                return;
            }
            repo.cambiarEstado(id, "Inactivo");
            redirigir(request, response, "msg=desactivado");

        } else if ("activar".equals(accion)) {
            repo.cambiarEstado(id, "Activo");
            redirigir(request, response, "msg=activado");

        } else {
            repo.desbloquear(id);
            redirigir(request, response, "msg=desbloqueado");
        }
    }

    // ---------- Utilidades ----------

    /** Devuelve el usuario en sesión si es administrador; si no, responde y devuelve null. */
    private String usuarioAutorizado(HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession sesion = request.getSession(false);
        String usuario = sesion == null ? null : (String) sesion.getAttribute("usuario");

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return null;
        }

        try {
            if (!repo.esAdministrador(usuario)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Solo los administradores pueden gestionar usuarios.");
                return null;
            }
        } catch (SQLException e) {
            throw new ServletException("Error al verificar permisos.", e);
        }
        return usuario;
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response,
                           String consulta) throws IOException {
        response.sendRedirect(request.getContextPath() + "/UsuarioServlet?" + consulta);
    }

    private static boolean passwordValida(String p) {
        return p != null && p.length() >= 8 && p.length() <= 72;
    }

    private static String recortar(String s) {
        return s == null ? null : s.trim();
    }

    private static Integer aEntero(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Long aLong(String s) {
        try {
            return Long.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
