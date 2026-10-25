<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String usuarioSesion = (String) session.getAttribute("usuario");
    if (usuarioSesion == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String nombreSeguro = usuarioSesion.replace("&", "&amp;")
            .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Panel principal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --fondo: #080d18;
            --panel: #101827;
            --panel2: #141f31;
            --azul: #4385ff;
            --celeste: #55d6ff;
            --texto: #f2f6ff;
            --secundario: #94a3b8;
            --borde: rgba(148, 163, 184, 0.18);
        }
        body {
            min-height: 100vh;
            font-family: "Segoe UI", Arial, sans-serif;
            color: var(--texto);
            background:
                radial-gradient(ellipse at 15% 10%, rgba(31, 83, 166, 0.30), transparent 42%),
                linear-gradient(135deg, #080d18, #0d1628 55%, #080d18);
            background-attachment: fixed;
        }
        .sidebar {
            position: fixed; top: 0; left: 0; bottom: 0; width: 250px;
            background: var(--panel);
            border-right: 1px solid var(--borde);
            padding: 24px 16px;
            display: flex; flex-direction: column;
        }
        .logo-texto { font-size: 26px; font-weight: 800; letter-spacing: -1px; padding: 0 10px 22px; }
        .logo-texto span { color: var(--celeste); }
        .menu a {
            display: flex; align-items: center; justify-content: space-between;
            color: var(--secundario); text-decoration: none;
            padding: 11px 12px; border-radius: 10px;
            font-size: 14px; margin-bottom: 4px;
            transition: background 0.2s, color 0.2s;
        }
        .menu a:hover, .menu a.activo {
            background: rgba(67, 133, 255, 0.14); color: var(--texto);
        }
        .menu a.activo { border: 1px solid rgba(67, 133, 255, 0.28); }
        .pronto {
            font-size: 10px; color: var(--celeste);
            border: 1px solid rgba(85, 214, 255, 0.35);
            border-radius: 20px; padding: 1px 8px;
        }
        .usuario-box {
            margin-top: auto; border-top: 1px solid var(--borde); padding-top: 16px;
            font-size: 13px; color: var(--secundario);
        }
        .usuario-box strong { color: var(--texto); display: block; margin-bottom: 10px; }
        .btn-salir {
            width: 100%; color: #fff; font-weight: 600; font-size: 13px;
            border: 1px solid rgba(122, 177, 255, 0.45);
            background: linear-gradient(100deg, #2865e8, #347ff5);
        }
        .btn-salir:hover { color: #fff; filter: brightness(1.13); }
        .contenido { margin-left: 250px; padding: 34px 38px; }
        .contenido h1 { font-size: 28px; letter-spacing: -0.8px; }
        .sub { color: var(--secundario); font-size: 14px; }
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
        @media (max-width: 800px) {
            .sidebar { position: static; width: 100%; border-right: none; border-bottom: 1px solid var(--borde); }
            .contenido { margin-left: 0; padding: 24px 18px; }
        }
    </style>
</head>
<body>

    <aside class="sidebar">
        <div class="logo-texto">Tec<span>Nova</span></div>

        <nav class="menu">
            <a href="inicio.jsp" class="activo">🏠 Inicio</a>
            <a href="UsuarioServlet">👥 Usuarios</a>
            <a href="#">🧑‍💼 Clientes <span class="pronto">pronto</span></a>
            <a href="#">🚚 Proveedores <span class="pronto">pronto</span></a>
            <a href="#">📦 Productos <span class="pronto">pronto</span></a>
            <a href="#">🧾 Ventas <span class="pronto">pronto</span></a>
            <a href="#">📊 Reportes <span class="pronto">pronto</span></a>
        </nav>

        <div class="usuario-box">
            Sesión iniciada como
            <strong><%= nombreSeguro %></strong>
            <a class="btn btn-salir" href="${pageContext.request.contextPath}/CerrarSesionServlet">Cerrar sesión</a>
        </div>
    </aside>

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
                <a href="#" class="tarjeta-mod">
                    <div class="icono">🧑‍💼</div>
                    <h5>Clientes</h5>
                    <p>Registro y mantenimiento de la información de clientes.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="#" class="tarjeta-mod">
                    <div class="icono">🚚</div>
                    <h5>Proveedores</h5>
                    <p>Datos de contacto y transacciones con proveedores.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="#" class="tarjeta-mod">
                    <div class="icono">📦</div>
                    <h5>Productos</h5>
                    <p>Control de productos, stock y precios.</p>
                </a>
            </div>
            <div class="col-12 col-md-6 col-xl-4">
                <a href="#" class="tarjeta-mod">
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
