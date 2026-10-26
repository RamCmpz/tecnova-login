<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,
                java.util.Locale,
                java.time.format.DateTimeFormatter,
                com.ramoncitoacademy.holamundoweb.Pedido,
                com.ramoncitoacademy.holamundoweb.DetalleVenta,
                com.ramoncitoacademy.holamundoweb.Cliente,
                com.ramoncitoacademy.holamundoweb.Producto"%>
<%!
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static String h(Object o) {
        if (o == null) return "";
        return String.valueOf(o).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String m(java.math.BigDecimal b) {
        return b == null ? "0.00" : String.format(Locale.US, "%.2f", b);
    }

    static String claseEstado(String estado) {
        if ("Pendiente".equalsIgnoreCase(estado)) return "aviso";
        if ("Convertido".equalsIgnoreCase(estado)) return "ok";
        return "mal";
    }
%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String ctx = request.getContextPath();
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(ctx + "/login.jsp");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    if (pedidos == null) {
        response.sendRedirect(ctx + "/PedidoServlet");
        return;
    }
    @SuppressWarnings("unchecked")
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    @SuppressWarnings("unchecked")
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    Pedido ver = (Pedido) request.getAttribute("ver");
    boolean puedePedir = !clientes.isEmpty() && !productos.isEmpty();

    String flash = (String) session.getAttribute("pedidoError");
    session.removeAttribute("pedidoError");

    String msg = request.getParameter("msg");
    String textoMsg = null;
    String linkBoleta = null;
    if ("creado".equals(msg)) {
        textoMsg = "Pedido registrado correctamente. Queda pendiente hasta convertirlo en venta.";
    } else if ("convertido".equals(msg)) {
        textoMsg = "Pedido convertido en venta. Se generó la boleta y se descontó el stock.";
        try {
            linkBoleta = ctx + "/VentaServlet?accion=ver&id="
                    + Integer.parseInt(request.getParameter("idVenta"));
        } catch (Exception e) {
            linkBoleta = null;
        }
    } else if ("cancelado".equals(msg)) {
        textoMsg = "Pedido cancelado.";
    }

    String paginaActiva = "pedidos";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Pedidos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= ctx %>/css/tecnova.css" rel="stylesheet">
    <style>
        .resumen { max-width: 340px; margin-left: auto; font-size: 14px; }
        .resumen div { display: flex; justify-content: space-between; padding: 4px 0; color: var(--secundario); }
        .resumen .total { color: var(--texto); font-weight: 700; font-size: 18px; border-top: 1px solid var(--borde); margin-top: 6px; padding-top: 10px; }
    </style>
</head>
<body>

    <%@ include file="/WEB-INF/menu.jspf" %>

    <main class="contenido">
        <h1>Solicitudes de pedido</h1>
        <p class="sub mb-4">Registra lo que pide el cliente y conviértelo en venta cuando confirme. El pedido no reserva stock.</p>

        <% if (textoMsg != null) { %>
            <div class="alerta-ok">
                <%= h(textoMsg) %>
                <% if (linkBoleta != null) { %>
                    <a href="<%= linkBoleta %>" class="btn-mini ms-2">Ver boleta</a>
                <% } %>
            </div>
        <% } %>
        <% if (flash != null) { %><div class="alerta-err"><%= h(flash) %></div><% } %>

        <% if (ver != null) { %>
        <section class="caja mb-4">
            <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
                <h2><%= h(ver.getCodigo()) %> · <%= h(ver.getClienteNombre()) %></h2>
                <a href="<%= ctx %>/PedidoServlet" class="btn-mini">Cerrar detalle</a>
            </div>
            <p class="sub">
                <span class="insignia <%= claseEstado(ver.getEstado()) %>"><%= h(ver.getEstado()) %></span>
                &nbsp; <%= ver.getFechaPedido().toLocalDateTime().format(FMT) %> · registrado por <%= h(ver.getUsuario()) %>
                <% if (ver.getNumeroVenta() != null) { %>
                    · boleta <a href="<%= ctx %>/VentaServlet?accion=ver&id=<%= ver.getIdVenta() %>"><%= h(ver.getNumeroVenta()) %></a>
                <% } %>
            </p>
            <div class="envoltorio">
                <table class="tabla">
                    <thead>
                        <tr><th>Cód.</th><th>Producto</th><th>Cant.</th><th>P. unit.</th><th>Importe</th></tr>
                    </thead>
                    <tbody>
                    <% for (DetalleVenta d : ver.getDetalles()) { %>
                        <tr>
                            <td><%= h(d.getCodigo()) %></td>
                            <td><%= h(d.getProducto()) %></td>
                            <td><%= d.getCantidad() %></td>
                            <td>S/ <%= m(d.getPrecioUnitario()) %></td>
                            <td>S/ <%= m(d.getSubtotal()) %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <div class="resumen mt-3">
                <div><span>Subtotal (sin IGV)</span><span>S/ <%= m(ver.getSubtotal()) %></span></div>
                <div><span>IGV (18 %)</span><span>S/ <%= m(ver.getIgv()) %></span></div>
                <div class="total"><span>Total</span><span>S/ <%= m(ver.getTotal()) %></span></div>
                <% if ("Pendiente".equalsIgnoreCase(ver.getEstado())) { %>
                <div class="form-text text-end">Al convertir se cobrará el precio vigente de cada producto.</div>
                <% } %>
            </div>
        </section>
        <% } %>

        <section class="caja mb-4">
            <h2>Nuevo pedido</h2>

            <% if (!puedePedir) { %>
                <div class="alerta-err mb-0">Para registrar un pedido necesitas al menos un cliente activo y un producto activo.</div>
            <% } else { %>

            <select id="catalogo" hidden>
                <% for (Producto p : productos) { %>
                    <option value="<%= p.getIdProducto() %>"
                            data-precio="<%= m(p.getPrecioVenta()) %>"><%= h(p.getCodigo()) %> · <%= h(p.getNombre()) %> (S/ <%= m(p.getPrecioVenta()) %> · stock <%= p.getStock() %>)</option>
                <% } %>
            </select>

            <form method="post" action="<%= ctx %>/PedidoServlet" autocomplete="off">
                <input type="hidden" name="accion" value="crear">

                <div class="row g-3 mb-3">
                    <div class="col-12 col-lg-6">
                        <label for="idCliente">Cliente</label>
                        <select class="form-select" id="idCliente" name="idCliente" required>
                            <% for (Cliente c : clientes) { %>
                                <option value="<%= c.getIdCliente() %>"><%= h(c.getNombres()) %> <%= h(c.getApellidos()) %> · DNI <%= h(c.getDni()) %></option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <h3>Productos solicitados</h3>
                <div class="envoltorio mt-2">
                    <table class="tabla">
                        <thead>
                            <tr><th>Producto</th><th>Cantidad</th><th>Importe</th><th></th></tr>
                        </thead>
                        <tbody id="lineas"></tbody>
                    </table>
                </div>

                <button type="button" id="agregar" class="btn-mini mt-2">+ Agregar producto</button>

                <div class="resumen mt-3">
                    <div><span>Subtotal (sin IGV)</span><span id="tSub">S/ 0.00</span></div>
                    <div><span>IGV (18 %)</span><span id="tIgv">S/ 0.00</span></div>
                    <div class="total"><span>Total</span><span id="tTotal">S/ 0.00</span></div>
                    <div class="form-text text-end">Estimado. El servidor recalcula con los precios reales.</div>
                </div>

                <div class="mt-4">
                    <button type="submit" id="guardar" class="btn btn-azul px-4" disabled>Registrar pedido</button>
                </div>
            </form>

            <% } %>
        </section>

        <section class="caja">
            <h2>Últimos pedidos (<%= pedidos.size() %>)</h2>

            <div class="envoltorio">
                <table class="tabla">
                    <thead>
                        <tr>
                            <th>Pedido</th><th>Fecha</th><th>Cliente</th><th>Registró</th>
                            <th>Total</th><th>Estado</th><th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% for (Pedido p : pedidos) {
                           boolean pendiente = "Pendiente".equalsIgnoreCase(p.getEstado()); %>
                        <tr>
                            <td><strong><%= h(p.getCodigo()) %></strong></td>
                            <td><%= p.getFechaPedido().toLocalDateTime().format(FMT) %></td>
                            <td><%= h(p.getClienteNombre()) %></td>
                            <td><%= h(p.getUsuario()) %></td>
                            <td>S/ <%= m(p.getTotal()) %></td>
                            <td>
                                <span class="insignia <%= claseEstado(p.getEstado()) %>"><%= h(p.getEstado()) %></span>
                                <% if (p.getNumeroVenta() != null) { %>
                                    <div class="form-text"><%= h(p.getNumeroVenta()) %></div>
                                <% } %>
                            </td>
                            <td>
                                <div class="d-flex flex-wrap gap-1">
                                    <a class="btn-mini" href="<%= ctx %>/PedidoServlet?accion=ver&id=<%= p.getIdPedido() %>">Ver</a>

                                    <% if (pendiente) { %>
                                    <form method="post" action="<%= ctx %>/PedidoServlet" class="d-inline"
                                          onsubmit="return confirm('¿Convertir este pedido en venta? Se descontará el stock y se generará la boleta.');">
                                        <input type="hidden" name="accion" value="convertir">
                                        <input type="hidden" name="id" value="<%= p.getIdPedido() %>">
                                        <button type="submit" class="btn-mini">Convertir en venta</button>
                                    </form>
                                    <form method="post" action="<%= ctx %>/PedidoServlet" class="d-inline"
                                          onsubmit="return confirm('¿Cancelar este pedido?');">
                                        <input type="hidden" name="accion" value="cancelar">
                                        <input type="hidden" name="id" value="<%= p.getIdPedido() %>">
                                        <button type="submit" class="btn-mini peligro">Cancelar</button>
                                    </form>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                    <% if (pedidos.isEmpty()) { %>
                        <tr><td colspan="7" style="color: var(--secundario);">Aún no hay pedidos registrados.</td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <script>
    (function () {
        var catalogo = document.getElementById('catalogo');
        var cuerpo = document.getElementById('lineas');
        var agregar = document.getElementById('agregar');
        var guardar = document.getElementById('guardar');
        if (!catalogo || !cuerpo) { return; }

        function fmt(n) { return 'S/ ' + n.toFixed(2); }

        function recalcular() {
            var total = 0;
            var filas = cuerpo.querySelectorAll('tr');
            filas.forEach(function (tr) {
                var sel = tr.querySelector('select');
                var qty = tr.querySelector('input');
                var opt = sel.options[sel.selectedIndex];
                var precio = parseFloat(opt.dataset.precio);
                var cantidad = parseInt(qty.value, 10) || 0;
                var importe = precio * cantidad;
                tr.querySelector('.importe').textContent = fmt(importe);
                total += importe;
            });
            var subtotal = total / 1.18;
            document.getElementById('tSub').textContent = fmt(subtotal);
            document.getElementById('tIgv').textContent = fmt(total - subtotal);
            document.getElementById('tTotal').textContent = fmt(total);
            guardar.disabled = filas.length === 0;
        }

        agregar.addEventListener('click', function () {
            var tr = document.createElement('tr');
            tr.innerHTML =
                '<td><select class="form-select" name="idProducto" required>' + catalogo.innerHTML + '</select></td>' +
                '<td style="width:130px"><input type="number" class="form-control" name="cantidad" min="1" max="1000000" step="1" value="1" required></td>' +
                '<td class="importe">S/ 0.00</td>' +
                '<td><button type="button" class="btn-mini peligro quitar">Quitar</button></td>';
            cuerpo.appendChild(tr);
            recalcular();
        });

        cuerpo.addEventListener('input', recalcular);
        cuerpo.addEventListener('change', recalcular);
        cuerpo.addEventListener('click', function (e) {
            if (e.target.classList.contains('quitar')) {
                e.target.closest('tr').remove();
                recalcular();
            }
        });

        agregar.click();
    })();
    </script>

</body>
</html>
