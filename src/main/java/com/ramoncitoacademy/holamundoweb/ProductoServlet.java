package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ProductoServlet")
public class ProductoServlet extends HttpServlet {

    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9\\-]{3,20}$");
    private static final BigDecimal PRECIO_MAX = new BigDecimal("99999999.99");

    private final ProductoRepository repo = new ProductoRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (usuarioEnSesion(request, response) == null) {
            return;
        }

        try {
            if ("editar".equals(request.getParameter("accion"))) {
                Integer id = aEntero(request.getParameter("id"), 1, Integer.MAX_VALUE);
                if (id != null) {
                    request.setAttribute("editar", repo.buscarPorId(id));
                }
            }
            request.setAttribute("productos", repo.listar());
            request.setAttribute("categorias", repo.listarCategorias());
            request.setAttribute("marcas", repo.listarMarcas());
            request.setAttribute("unidades", repo.listarUnidades());
            request.getRequestDispatcher("/productos.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Error al consultar productos.", e);
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
            redirigir(request, response, "err=invalido");
        } catch (SQLException e) {
            throw new ServletException("Error al guardar el producto.", e);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response,
                       String actual) throws SQLException, IOException {

        Producto p = leerFormulario(request);
        String codigo = recortar(request.getParameter("codigo"));

        if (p == null || codigo == null || !CODIGO.matcher(codigo).matches()) {
            redirigir(request, response, "err=invalido");
            return;
        }
        codigo = codigo.toUpperCase();
        p.setCodigo(codigo);

        if (repo.existeCodigo(codigo)) {
            redirigir(request, response, "err=duplicado");
            return;
        }

        repo.crear(p, actual);
        redirigir(request, response, "msg=creado");
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Integer id = aEntero(request.getParameter("id"), 1, Integer.MAX_VALUE);
        Producto p = leerFormulario(request);

        if (id == null || p == null) {
            redirigir(request, response, "err=invalido");
            return;
        }
        if (repo.buscarPorId(id) == null) {
            redirigir(request, response, "err=noexiste");
            return;
        }

        p.setIdProducto(id);
        repo.actualizar(p);
        redirigir(request, response, "msg=actualizado");
    }

    private void cambiarEstado(String accion, HttpServletRequest request,
                               HttpServletResponse response)
            throws SQLException, IOException {

        Integer id = aEntero(request.getParameter("id"), 1, Integer.MAX_VALUE);
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
    private Producto leerFormulario(HttpServletRequest request) {
        String nombre = recortar(request.getParameter("nombre"));
        String descripcion = recortar(request.getParameter("descripcion"));
        Integer idCategoria = aEntero(request.getParameter("idCategoria"), 1, Integer.MAX_VALUE);
        Integer idMarca = aEntero(request.getParameter("idMarca"), 1, Integer.MAX_VALUE);
        Integer idUnidad = aEntero(request.getParameter("idUnidadMedida"), 1, Integer.MAX_VALUE);
        BigDecimal precioCompra = aPrecio(request.getParameter("precioCompra"));
        BigDecimal precioVenta = aPrecio(request.getParameter("precioVenta"));
        Integer stock = aEntero(request.getParameter("stock"), 0, 1_000_000);
        Integer stockMinimo = aEntero(request.getParameter("stockMinimo"), 0, 1_000_000);

        if (nombre == null || nombre.length() > 120
                || (descripcion != null && descripcion.length() > 255)
                || idCategoria == null || idMarca == null || idUnidad == null
                || precioCompra == null || precioVenta == null
                || stock == null || stockMinimo == null) {
            return null;
        }

        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setIdCategoria(idCategoria);
        p.setIdMarca(idMarca);
        p.setIdUnidadMedida(idUnidad);
        p.setPrecioCompra(precioCompra);
        p.setPrecioVenta(precioVenta);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
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
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?" + consulta);
    }

    /** Recorta; devuelve null si queda vacío. */
    private static String recortar(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer aEntero(String s, int min, int max) {
        try {
            int v = Integer.parseInt(s.trim());
            return (v < min || v > max) ? null : v;
        } catch (Exception e) {
            return null;
        }
    }

    /** Precio entre 0 y 99 999 999,99 con máximo 2 decimales. */
    private static BigDecimal aPrecio(String s) {
        try {
            BigDecimal v = new BigDecimal(s.trim());
            if (v.signum() < 0 || v.scale() > 2 || v.compareTo(PRECIO_MAX) > 0) {
                return null;
            }
            return v;
        } catch (Exception e) {
            return null;
        }
    }
}
