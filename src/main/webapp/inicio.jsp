
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(
            request.getContextPath() + "/login.jsp"
        );
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Inicio</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            min-height: 100vh;
            font-family: "Segoe UI", Arial, sans-serif;
            color: #f2f6ff;
            background: linear-gradient(135deg, #080d18, #0d1628 55%, #080d18);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
        }

        .tarjeta {
            width: 100%;
            max-width: 650px;
            padding: 42px;
            border: 1px solid rgba(148, 163, 184, 0.18);
            border-radius: 22px;
            background: #101827;
            box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
        }

        .marca {
            color: #55d6ff;
            font-size: 28px;
            font-weight: 800;
            margin-bottom: 32px;
        }

        h1 {
            font-size: 32px;
            margin-bottom: 14px;
        }

        p {
            color: #94a3b8;
            line-height: 1.7;
            margin-bottom: 24px;
        }

        .estado {
            padding: 18px;
            margin-bottom: 25px;
            border: 1px solid rgba(85, 214, 255, 0.2);
            border-radius: 12px;
            background: rgba(67, 133, 255, 0.08);
        }

        .boton {
            display: inline-block;
            padding: 12px 20px;
            color: white;
            text-decoration: none;
            font-weight: 600;
            border-radius: 10px;
            background: linear-gradient(100deg, #2865e8, #347ff5);
        }

        .boton:hover {
            filter: brightness(1.12);
        }

        @media (max-width: 480px) {
            .tarjeta {
                padding: 28px 22px;
            }

            h1 {
                font-size: 26px;
            }
        }
    </style>
</head>
<body>
    <main class="tarjeta">
        <div class="marca">TecNova</div>

        <h1>¡Bienvenido al sistema!</h1>

        <p>
            Has iniciado sesión correctamente.
            Desde aquí podrás acceder a las funciones
            del sistema de gestión de ventas e inventario.
        </p>

        <div class="estado">
            Sesión iniciada como:
            <strong><%= session.getAttribute("usuario") %></strong>
        </div>

        <p>
            El módulo de inventario y ventas se implementará
            en los siguientes pasos del proyecto.
        </p>

        <a class="boton"
           href="<%= request.getContextPath() %>/CerrarSesionServlet">
            Cerrar sesión
        </a>
    </main>
</body>
</html>
