<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String paginaActiva = "inicio";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Panel principal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/tecnova.css" rel="stylesheet">
    <style>
        .tarjeta-mod {
            display: block; height: 100%;
            background: linear-gradient(145deg, rgba(20, 31, 49, 0.96), rgba(12, 19, 32, 0.98));
            border: 1px solid var(--borde); border-radius: 16px;
            padding: 22px; color: var(--texto); text-decoration: none;
            transition: transform 0.2s, border-color 0.2s;
        }
        .tarjeta-mod:hover { transform: translateY(-3px); border-color: rgba(67, 133, 255, 0.5); color: var(--texto); }
        .tarjeta-mod h5 { font-size: 17px; margin: 12px 0 6px; }
        .tarjeta-mod p { color: var(--secundario); font-size: 13px; margin: 0; }
        .icono {
            width: 42px; height: 42px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            font-size: 20px; background: rgba(67, 133, 255, 0.14);
            border: 1px solid rgba(85, 214, 255, 0.28);
        }
    </style>
</head>
<body>

    <%@ include file="/WEB-INF/menu.jspf" %>

    <main class="contenido">
        <h1>Panel principal</h1>
        <p class="sub mb-4">Sistema de gestión de ventas e inventario. Elige un módulo para comenzar.</p>

        <div class="row g-4">
            <div class="col-12 col-md-6 col-xl-4">
                <a href="UsuarioServlet" class="tarjeta-mod">
                    <div class="icono">👥</div>
                    <h5>Usuarios</h5>
                    <p>Crear, editar y administrar cuentas de acceso y roles.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="ClienteServlet" class="tarjeta-mod">
                    <div class="icono">🧑‍💼</div>
                    <h5>Clientes</h5>
                    <p>Registro y mantenimiento de la información de clientes.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="ProveedorServlet" class="tarjeta-mod">
                    <div class="icono">🚚</div>
                    <h5>Proveedores</h5>
                    <p>Datos de contacto y transacciones con proveedores.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="ProductoServlet" class="tarjeta-mod">
                    <div class="icono">📦</div>
                    <h5>Productos</h5>
                    <p>Control de productos, stock y precios.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="VentaServlet" class="tarjeta-mod">
                    <div class="icono">🧾</div>
                    <h5>Ventas</h5>
                    <p>Registro de ventas y emisión de boletas o facturas.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="#" class="tarjeta-mod">
                    <div class="icono">📊</div>
                    <h5>Reportes</h5>
                    <p>Ingresos, salidas, stock y productos más vendidos.</p>
                </a>
            </div>
        </div>
    </main>

</body>
</html>
