package com.ramoncitoacademy.holamundoweb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/VentaServlet")
public class VentaServlet extends HttpServlet {

    private static final int MAX_LINEAS = 50;
    private static final int MAX_CANTIDAD = 1_000_000;

    private final VentaRepository repo = new VentaRepository();
    private final ClienteRepository clientes = new ClienteRepository();
    private final ProductoRepository productos = new ProductoRepository();
    private final UsuarioRepository usuarios = new UsuarioRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String actual = usuarioEnSesion(request, response);
        if (actual == null) {
            return;
        }

        try {
            if ("ver".equals(request.getParameter("accion"))) {
                Integer id = aEntero(request.getParameter("id"));
                Venta v = id == null ? null : repo.buscarPorId(id);
                if (v == null) {
                    error(request, response, "La venta indicada no existe.");
                    return;
                }
                request.setAttribute("venta", v);
                request.getRequestDispatcher("/comprobante.jsp").forward(request, response);
                return;
            }

            List<Cliente> clientesActivos = new ArrayList<>();
            for (Cliente c : clientes.listar()) {
                if ("Activo".equalsIgnoreCase(c.getEstado())) {
                    clientesActivos.add(c);
                }
            }
            List<Producto> productosVendibles = new ArrayList<>();
            for (Producto p : productos.listar()) {
                if ("Activo".equalsIgnoreCase(p.getEstado()) && p.getStock() > 0) {
                    productosVendibles.add(p);
                }
            }

            request.setAttribute("ventas", repo.listar());
            request.setAttribute("clientes", clientesActivos);
            request.setAttribute("productos", productosVendibles);
            request.setAttribute("esAdmin", usuarios.esAdministrador(actual));
            request.getRequestDispatcher("/ventas.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Error al consultar ventas.", e);
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
                case "anular":
                    anular(request, response, actual);
                    break;
                default:
                    error(request, response, "Acción no válida.");
            }
        } catch (VentaRepository.VentaException e) {
            error(request, response, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Error al procesar la venta.", e);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response,
                       String actual)
            throws SQLException, VentaRepository.VentaException, IOException {

        Integer idCliente = aEntero(request.getParameter("idCliente"));
        String tipo = request.getParameter("tipoComprobante");
        String[] ids = request.getParameterValues("idProducto");
        String[] cantidades = request.getParameterValues("cantidad");

        if (idCliente == null || ComprobanteStrategy.de(tipo) == null
                || ids == null || cantidades == null
                || ids.length == 0 || ids.length != cantidades.length
                || ids.length > MAX_LINEAS) {
            error(request, response, "Datos de la venta inválidos. Elige un cliente, el tipo de comprobante y al menos un producto.");
            return;
        }

        // Si un producto se repite en el carrito, se suman sus cantidades
        Map<Integer, Integer> items = new TreeMap<>();
        for (int i = 0; i < ids.length; i++) {
            Integer producto = aEntero(ids[i]);
            Integer cantidad = aEntero(cantidades[i]);
            if (producto == null || cantidad == null
                    || cantidad < 1 || cantidad > MAX_CANTIDAD) {
                error(request, response, "Hay una cantidad o un producto inválido en la venta.");
                return;
            }
            items.merge(producto, cantidad, Integer::sum);
        }

        int idVenta = repo.crear(idCliente, actual, items, tipo);
        response.sendRedirect(request.getContextPath()
                + "/VentaServlet?msg=creada&id=" + idVenta);
    }

    private void anular(HttpServletRequest request, HttpServletResponse response,
                        String actual)
            throws SQLException, VentaRepository.VentaException, IOException {

        if (!usuarios.esAdministrador(actual)) {
            error(request, response, "Solo un administrador puede anular ventas.");
            return;
        }
        Integer id = aEntero(request.getParameter("id"));
        if (id == null) {
            error(request, response, "La venta indicada no existe.");
            return;
        }
        repo.anular(id);
        response.sendRedirect(request.getContextPath() + "/VentaServlet?msg=anulada");
    }

    /** Guarda el mensaje en la sesión y vuelve a la lista de ventas. */
    private void error(HttpServletRequest request, HttpServletResponse response,
                       String mensaje) throws IOException {
        request.getSession().setAttribute("ventaError", mensaje);
        response.sendRedirect(request.getContextPath() + "/VentaServlet");
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

    private static Integer aEntero(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
