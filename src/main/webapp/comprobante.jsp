<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Locale,
                java.time.format.DateTimeFormatter,
                com.ramoncitoacademy.holamundoweb.Venta,
                com.ramoncitoacademy.holamundoweb.DetalleVenta"%>
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
    Venta v = (Venta) request.getAttribute("venta");
    if (v == null) {
        response.sendRedirect(ctx + "/VentaServlet");
        return;
    }
    boolean anulada = !"Emitida".equalsIgnoreCase(v.getEstado());
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Boleta <%= h(v.getNumeroCompleto()) %> | TecNova</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; padding: 24px 12px; background: #e9edf3; font-family: "Segoe UI", Arial, sans-serif; color: #1b2433; }
        .boleta { position: relative; max-width: 720px; margin: 0 auto; background: #fff; border-radius: 10px; padding: 34px 38px; box-shadow: 0 8px 30px rgba(0,0,0,0.12); }
        .cabecera { display: flex; justify-content: space-between; gap: 20px; border-bottom: 2px solid #1b2433; padding-bottom: 16px; }
        .marca { font-size: 28px; font-weight: 800; letter-spacing: -1px; }
        .marca span { color: #2f7bf5; }
        .marca small { display: block; font-size: 12px; font-weight: 400; color: #5c6b82; letter-spacing: 0; }
        .numero { text-align: center; border: 2px solid #1b2433; border-radius: 8px; padding: 10px 16px; min-width: 210px; }
        .numero strong { display: block; font-size: 13px; }
        .numero span { font-size: 20px; font-weight: 700; }
        .datos { margin: 18px 0; font-size: 14px; line-height: 1.8; }
        .datos b { display: inline-block; min-width: 90px; }
        table { width: 100%; border-collapse: collapse; font-size: 14px; }
        th { text-align: left; background: #f0f3f8; padding: 9px 10px; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
        td { padding: 9px 10px; border-bottom: 1px solid #e6eaf1; }
        .der { text-align: right; }
        .totales { margin: 18px 0 0 auto; width: 280px; font-size: 14px; }
        .totales div { display: flex; justify-content: space-between; padding: 4px 0; }
        .totales .gran { font-size: 18px; font-weight: 700; border-top: 2px solid #1b2433; margin-top: 6px; padding-top: 8px; }
        .pie { margin-top: 28px; text-align: center; font-size: 12px; color: #7a889e; }
        .sello { position: absolute; top: 45%; left: 50%; transform: translate(-50%, -50%) rotate(-18deg); font-size: 80px; font-weight: 800; color: rgba(214, 48, 49, 0.22); border: 8px solid rgba(214, 48, 49, 0.22); padding: 0 24px; border-radius: 12px; pointer-events: none; }
        .acciones { max-width: 720px; margin: 0 auto 14px; display: flex; gap: 10px; }
        .acciones a, .acciones button { font: inherit; font-size: 14px; cursor: pointer; padding: 9px 18px; border-radius: 8px; border: 1px solid #b9c3d3; background: #fff; color: #1b2433; text-decoration: none; }
        .acciones button { background: #2f7bf5; border-color: #2f7bf5; color: #fff; font-weight: 600; }
        @media print {
            body { background: #fff; padding: 0; }
            .acciones { display: none; }
            .boleta { box-shadow: none; max-width: none; padding: 0; }
        }
    </style>
</head>
<body>

    <div class="acciones">
        <button type="button" onclick="window.print()">Imprimir</button>
        <a href="<%= ctx %>/VentaServlet">Volver a ventas</a>
    </div>

    <div class="boleta">
        <% if (anulada) { %><div class="sello">ANULADA</div><% } %>

        <div class="cabecera">
            <div class="marca">Tec<span>Nova</span><small>Tienda de tecnología · Lima, Perú</small></div>
            <div class="numero">
                <strong>BOLETA DE VENTA</strong>
                <span><%= h(v.getNumeroCompleto()) %></span>
            </div>
        </div>

        <div class="datos">
            <div><b>Fecha:</b> <%= v.getFechaVenta().toLocalDateTime().format(FMT) %></div>
            <div><b>Cliente:</b> <%= h(v.getClienteNombre()) %></div>
            <div><b>DNI:</b> <%= h(v.getClienteDni()) %></div>
            <div><b>Vendedor:</b> <%= h(v.getUsuario()) %></div>
        </div>

        <table>
            <thead>
                <tr><th>Cód.</th><th>Descripción</th><th class="der">Cant.</th><th class="der">P. unit.</th><th class="der">Importe</th></tr>
            </thead>
            <tbody>
            <% for (DetalleVenta d : v.getDetalles()) { %>
                <tr>
                    <td><%= h(d.getCodigo()) %></td>
                    <td><%= h(d.getProducto()) %></td>
                    <td class="der"><%= d.getCantidad() %></td>
                    <td class="der">S/ <%= m(d.getPrecioUnitario()) %></td>
                    <td class="der">S/ <%= m(d.getSubtotal()) %></td>
                </tr>
            <% } %>
            </tbody>
        </table>

        <div class="totales">
            <div><span>Subtotal (sin IGV)</span><span>S/ <%= m(v.getSubtotal()) %></span></div>
            <div><span>IGV (18 %)</span><span>S/ <%= m(v.getIgv()) %></span></div>
            <div class="gran"><span>TOTAL</span><span>S/ <%= m(v.getTotal()) %></span></div>
        </div>

        <div class="pie">Gracias por su compra · Documento generado por el sistema TecNova (proyecto académico)</div>
    </div>

</body>
</html>
