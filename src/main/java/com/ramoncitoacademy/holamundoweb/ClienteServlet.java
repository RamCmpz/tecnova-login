package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ClienteServlet")
public class ClienteServlet extends HttpServlet {

    private static final Pattern DNI = Pattern.compile("^[0-9]{8}$");
    private static final Pattern RUC = Pattern.compile("^(10|15|17|20)[0-9]{9}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+ \\-]{6,20}$");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Set<String> TIPOS = Set.of("Minorista", "Mayorista");

    private final ClienteRepository repo = new ClienteRepository();

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
            request.setAttribute("clientes", repo.listar());
            request.getRequestDispatcher("/clientes.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Error al consultar clientes.", e);
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
            throw new ServletException("Error al guardar el cliente.", e);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response,
                       String actual) throws SQLException, IOException {

        Cliente c = leerFormulario(request);
        String dni = recortar(request.getParameter("dni"));

        if (c == null || dni == null || !DNI.matcher(dni).matches()) {
            redirigir(request, response, "err=invalido");
            return;
        }
        c.setDni(dni);

        if (repo.existeDni(dni) || (c.getRuc() != null && repo.existeRuc(c.getRuc()))) {
            redirigir(request, response, "err=duplicado");
            return;
        }

        repo.crear(c, actual);
        redirigir(request, response, "msg=creado");
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Integer id = aEntero(request.getParameter("id"));
        Cliente c = leerFormulario(request);

        if (id == null || c == null) {
            redirigir(request, response, "err=invalido");
            return;
        }
        if (repo.buscarPorId(id) == null) {
            redirigir(request, response, "err=noexiste");
            return;
        }

        c.setIdCliente(id);
        repo.actualizar(c);
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
    private Cliente leerFormulario(HttpServletRequest request) {
        String nombres = recortar(request.getParameter("nombres"));
        String apellidos = recortar(request.getParameter("apellidos"));
        String correo = recortar(request.getParameter("correo"));
        String telefono = recortar(request.getParameter("telefono"));
        String direccion = recortar(request.getParameter("direccion"));
        String tipo = recortar(request.getParameter("tipoCliente"));
        String ruc = recortar(request.getParameter("ruc"));
        String razonSocial = recortar(request.getParameter("razonSocial"));

        if (nombres == null || nombres.length() > 80
                || apellidos == null || apellidos.length() > 80
                || (correo != null && (correo.length() > 80
                        || !CORREO.matcher(correo).matches()))
                || (telefono != null && !TELEFONO.matcher(telefono).matches())
                || (direccion != null && direccion.length() > 150)
                || tipo == null || !TIPOS.contains(tipo)) {
            return null;
        }

        // RUC y razón social van juntos: o se llenan los dos, o ninguno
        if ((ruc == null) != (razonSocial == null)) {
            return null;
        }
        if (ruc != null && (!RUC.matcher(ruc).matches() || razonSocial.length() > 120)) {
            return null;
        }

        Cliente c = new Cliente();
        c.setNombres(nombres);
        c.setApellidos(apellidos);
        c.setCorreo(correo);
        c.setTelefono(telefono);
        c.setDireccion(direccion);
        c.setTipoCliente(tipo);
        c.setRuc(ruc);
        c.setRazonSocial(razonSocial);
        return c;
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
        response.sendRedirect(request.getContextPath() + "/ClienteServlet?" + consulta);
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
