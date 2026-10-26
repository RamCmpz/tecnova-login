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

@WebServlet("/PedidoServlet")
public class PedidoServlet extends HttpServlet {

    private static final int MAX_LINEAS = 50;
    private static final int MAX_CANTIDAD = 1_000_000;

    private final PedidoRepository repo = new PedidoRepository();
    private final ClienteRepository clientes = new ClienteRepository();
    private final ProductoRepository productos = new ProductoRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (usuarioEnSesion(request, response) == null) {
            return;
        }

        try {
            if ("ver".equals(request.getParameter("accion"))) {
                Integer id = aEntero(request.getParameter("id"));
                Pedido p = id == null ? null : repo.buscarPorId(id);
                if (p == null) {
                    error(request, response, "El pedido indicado no existe.");
                    return;
                }
                request.setAttribute("ver", p);
            }

            List<Cliente> clientesActivos = new ArrayList<>();
            for (Cliente c : clientes.listar()) {
                if ("Activo".equalsIgnoreCase(c.getEstado())) {
                    clientesActivos.add(c);
                }
            }
            List<Producto> productosActivos = new ArrayList<>();
            for (Producto p : productos.listar()) {
                if ("Activo".equalsIgnoreCase(p.getEstado())) {
                    productosActivos.add(p);
                }
            }

            request.setAttribute("pedidos", repo.listar());
            request.setAttribute("clientes", clientesActivos);
            request.setAttribute("productos", productosActivos);
            request.getRequestDispatcher("/pedidos.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Error al consultar pedidos.", e);
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
                case "convertir":
                    convertir(request, response, actual);
                    break;
                case "cancelar":
                    cancelar(request, response);
                    break;
                default:
                    error(request, response, "Acción no válida.");
            }
        } catch (VentaRepository.VentaException e) {
            error(request, response, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Error al procesar el pedido.", e);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response,
                       String actual)
            throws SQLException, VentaRepository.VentaException, IOException {

        Integer idCliente = aEntero(request.getParameter("idCliente"));
        String[] ids = request.getParameterValues("idProducto");
        String[] cantidades = request.getParameterValues("cantidad");

        if (idCliente == null || ids == null || cantidades == null
                || ids.length == 0 || ids.length != cantidades.length
                || ids.length > MAX_LINEAS) {
            error(request, response, "Datos del pedido inválidos. Elige un cliente y al menos un producto.");
            return;
        }

        // Si un producto se repite en el carrito, se suman sus cantidades
        Map<Integer, Integer> items = new TreeMap<>();
        for (int i = 0; i < ids.length; i++) {
            Integer producto = aEntero(ids[i]);
            Integer cantidad = aEntero(cantidades[i]);
            if (producto == null || cantidad == null
                    || cantidad < 1 || cantidad > MAX_CANTIDAD) {
                error(request, response, "Hay una cantidad o un producto inválido en el pedido.");
                return;
            }
            items.merge(producto, cantidad, Integer::sum);
        }

        int idPedido = repo.crear(idCliente, actual, items);
        response.sendRedirect(request.getContextPath()
                + "/PedidoServlet?msg=creado&id=" + idPedido);
    }

    private void convertir(HttpServletRequest request, HttpServletResponse response,
                           String actual)
            throws SQLException, VentaRepository.VentaException, IOException {

        Integer id = aEntero(request.getParameter("id"));
        if (id == null) {
            error(request, response, "El pedido indicado no existe.");
            return;
        }
        int idVenta = repo.convertir(id, actual);
        response.sendRedirect(request.getContextPath()
                + "/PedidoServlet?msg=convertido&idVenta=" + idVenta);
    }

    private void cancelar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, VentaRepository.VentaException, IOException {

        Integer id = aEntero(request.getParameter("id"));
        if (id == null) {
            error(request, response, "El pedido indicado no existe.");
            return;
        }
        repo.cancelar(id);
        response.sendRedirect(request.getContextPath() + "/PedidoServlet?msg=cancelado");
    }

    /** Guarda el mensaje en la sesión y vuelve a la lista de pedidos. */
    private void error(HttpServletRequest request, HttpServletResponse response,
                       String mensaje) throws IOException {
        request.getSession().setAttribute("pedidoError", mensaje);
        response.sendRedirect(request.getContextPath() + "/PedidoServlet");
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
