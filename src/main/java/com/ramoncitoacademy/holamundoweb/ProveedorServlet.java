package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ProveedorServlet")
public class ProveedorServlet extends HttpServlet {

    private static final Pattern DNI = Pattern.compile("^[0-9]{8}$");
    private static final Pattern RUC = Pattern.compile("^(10|15|17|20)[0-9]{9}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+ \\-]{6,20}$");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ProveedorRepository repo = new ProveedorRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (usuarioEnSesion(request, response) == null) {
            return;
        }

        try {
            if ("editar".equals(request.getParameter("accion"))) {
                Integer id = aEntero(request.getParameter("id"));
                if (id != null) {
                    request.setAttribute("editar", repo.buscarPorId(id));
                }
            }
            request.setAttribute("proveedores", repo.listar());
            request.getRequestDispatcher("/proveedores.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Error al consultar proveedores.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String actual = usuarioEnSesion(request, response);
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
                    actualizar(request, response);
                    break;
                case "desactivar":
                case "activar":
                    cambiarEstado(accion, request, response);
                    break;
                default:
                    redirigir(request, response, "err=invalido");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            redirigir(request, response, "err=duplicado");
        } catch (SQLException e) {
            throw new ServletException("Error al guardar el proveedor.", e);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response,
                       String actual) throws SQLException, IOException {

        Proveedor p = leerFormulario(request);
        String dni = recortar(request.getParameter("dni"));
        String ruc = recortar(request.getParameter("ruc"));

        if (p == null
                || dni == null || !DNI.matcher(dni).matches()
                || ruc == null || !RUC.matcher(ruc).matches()) {
            redirigir(request, response, "err=invalido");
            return;
        }
        p.setDni(dni);
        p.setRuc(ruc);

        if (repo.existeDni(dni) || repo.existeRuc(ruc)) {
            redirigir(request, response, "err=duplicado");
            return;
        }

        repo.crear(p, actual);
        redirigir(request, response, "msg=creado");
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Integer id = aEntero(request.getParameter("id"));
        Proveedor p = leerFormulario(request);

        if (id == null || p == null) {
            redirigir(request, response, "err=invalido");
            return;
        }
        if (repo.buscarPorId(id) == null) {
            redirigir(request, response, "err=noexiste");
            return;
        }

        p.setIdProveedor(id);
        repo.actualizar(p);
        redirigir(request, response, "msg=actualizado");
    }

    private void cambiarEstado(String accion, HttpServletRequest request,
                               HttpServletResponse response)
            throws SQLException, IOException {

        Integer id = aEntero(request.getParameter("id"));
        if (id == null || repo.buscarPorId(id) == null) {
            redirigir(request, response, "err=noexiste");
            return;
        }

        if ("desactivar".equals(accion)) {
            repo.cambiarEstado(id, "Inactivo");
            redirigir(request, response, "msg=desactivado");
        } else {
            repo.cambiarEstado(id, "Activo");
            redirigir(request, response, "msg=activado");
        }
    }

    /** Lee y valida los campos comunes. Devuelve null si algo es inválido. */
    private Proveedor leerFormulario(HttpServletRequest request) {
        String razon = recortar(request.getParameter("razonSocial"));
        String nombres = recortar(request.getParameter("nombres"));
        String apellidos = recortar(request.getParameter("apellidos"));
        String correo = recortar(request.getParameter("correo"));
        String telefono = recortar(request.getParameter("telefono"));
        String direccion = recortar(request.getParameter("direccion"));

        if (razon == null || razon.length() > 120
                || nombres == null || nombres.length() > 80
                || apellidos == null || apellidos.length() > 80
                || (correo != null && (correo.length() > 80
                        || !CORREO.matcher(correo).matches()))
                || (telefono != null && !TELEFONO.matcher(telefono).matches())
                || (direccion != null && direccion.length() > 150)) {
            return null;
        }

        Proveedor p = new Proveedor();
        p.setRazonSocial(razon);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setCorreo(correo);
        p.setTelefono(telefono);
        p.setDireccion(direccion);
        return p;
    }

    private String usuarioEnSesion(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        String usuario = sesion == null ? null : (String) sesion.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
        return usuario;
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response,
                           String consulta) throws IOException {
        response.sendRedirect(request.getContextPath() + "/ProveedorServlet?" + consulta);
    }

    /** Recorta; devuelve null si queda vacío. */
    private static String recortar(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer aEntero(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
