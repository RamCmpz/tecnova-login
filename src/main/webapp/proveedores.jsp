<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,
                com.ramoncitoacademy.holamundoweb.Proveedor"%>
<%!
    static String h(Object o) {
        if (o == null) return "";
        return String.valueOf(o).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String ctx = request.getContextPath();
    String usuarioSesion = (String) session.getAttribute("usuario");
    if (usuarioSesion == null) {
        response.sendRedirect(ctx + "/login.jsp");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    if (proveedores == null) {
        response.sendRedirect(ctx + "/ProveedorServlet");
        return;
    }
    Proveedor editar = (Proveedor) request.getAttribute("editar");
    boolean editando = editar != null;

    String msg = request.getParameter("msg");
    String err = request.getParameter("err");

    String textoMsg = null;
    if ("creado".equals(msg)) textoMsg = "Proveedor registrado correctamente.";
    else if ("actualizado".equals(msg)) textoMsg = "Proveedor actualizado correctamente.";
    else if ("desactivado".equals(msg)) textoMsg = "Proveedor desactivado.";
    else if ("activado".equals(msg)) textoMsg = "Proveedor activado nuevamente.";

    String textoErr = null;
    if ("duplicado".equals(err)) textoErr = "Ya existe un registro con ese DNI o ese RUC.";
    else if ("invalido".equals(err)) textoErr = "Datos inválidos. El RUC debe tener 11 dígitos (empieza con 10, 15, 17 o 20), el DNI 8 dígitos, y el correo y teléfono deben ser válidos.";
    else if ("noexiste".equals(err)) textoErr = "El proveedor indicado no existe.";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Proveedores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --panel: #101827;
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
            background: var(--panel); border-right: 1px solid var(--borde);
            padding: 24px 16px; display: flex; flex-direction: column;
        }
        .logo-texto { font-size: 26px; font-weight: 800; letter-spacing: -1px; padding: 0 10px 22px; }
        .logo-texto span { color: var(--celeste); }
        .menu a {
            display: flex; align-items: center; justify-content: space-between;
            color: var(--secundario); text-decoration: none;
            padding: 11px 12px; border-radius: 10px; font-size: 14px; margin-bottom: 4px;
            transition: background 0.2s, color 0.2s;
        }
        .menu a:hover, .menu a.activo { background: rgba(67, 133, 255, 0.14); color: var(--texto); }
        .menu a.activo { border: 1px solid rgba(67, 133, 255, 0.28); }
        .pronto {
            font-size: 10px; color: var(--celeste);
            border: 1px solid rgba(85, 214, 255, 0.35);
            border-radius: 20px; padding: 1px 8px;
        }
        .usuario-box { margin-top: auto; border-top: 1px solid var(--borde); padding-top: 16px; font-size: 13px; color: var(--secundario); }
        .usuario-box strong { color: var(--texto); display: block; margin-bottom: 10px; }
        .btn-azul {
            color: #fff; font-weight: 600; font-size: 13px;
            border: 1px solid rgba(122, 177, 255, 0.45);
            background: linear-gradient(100deg, #2865e8, #347ff5);
        }
        .btn-azul:hover { color: #fff; filter: brightness(1.13); }
        .btn-salir { width: 100%; }
        .contenido { margin-left: 250px; padding: 34px 38px; }
        .contenido h1 { font-size: 28px; letter-spacing: -0.8px; }
        .sub { color: var(--secundario); font-size: 14px; }

        .caja {
            background: linear-gradient(145deg, rgba(20, 31, 49, 0.96), rgba(12, 19, 32, 0.98));
            border: 1px solid var(--borde); border-radius: 16px; padding: 22px;
        }
        .caja h2 { font-size: 17px; margin-bottom: 16px; }
        .caja h3 { font-size: 12px; color: var(--celeste); text-transform: uppercase; letter-spacing: 1px; margin: 4px 0 0; }
        label { font-size: 12px; font-weight: 600; color: #dce6f7; margin-bottom: 6px; }
        .form-control {
            background-color: rgba(5, 11, 21, 0.48); color: var(--texto);
            border: 1px solid rgba(148, 163, 184, 0.22); border-radius: 10px;
            font-size: 13px; color-scheme: dark;
        }
        .form-control:focus {
            background-color: rgba(5, 11, 21, 0.7); color: var(--texto);
            border-color: var(--azul); box-shadow: 0 0 0 3px rgba(67, 133, 255, 0.12);
        }
        .form-control[readonly] { opacity: 0.65; }
        .form-text { color: var(--secundario); font-size: 11px; }

        .tabla { width: 100%; font-size: 13px; color: var(--texto); border-collapse: collapse; }
        .tabla th {
            color: var(--secundario); font-weight: 600; font-size: 11px;
            text-transform: uppercase; letter-spacing: 0.6px;
            padding: 10px 12px; border-bottom: 1px solid var(--borde); white-space: nowrap;
        }
        .tabla td { padding: 12px; border-bottom: 1px solid rgba(148, 163, 184, 0.08); vertical-align: middle; }
        .tabla tr:last-child td { border-bottom: none; }
        .insignia { font-size: 11px; font-weight: 600; border-radius: 20px; padding: 3px 10px; white-space: nowrap; }
        .ok { color: #6ee7b7; background: rgba(16, 185, 129, 0.12); border: 1px solid rgba(16, 185, 129, 0.3); }
        .mal { color: #fca5a5; background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); }
        .btn-mini {
            font-size: 12px; padding: 4px 10px; border-radius: 8px;
            color: var(--texto); background: transparent;
            border: 1px solid var(--borde); text-decoration: none; display: inline-block;
        }
        .btn-mini:hover { background: rgba(67, 133, 255, 0.14); color: var(--texto); }
        .btn-mini.peligro:hover { background: rgba(239, 68, 68, 0.14); }
        .alerta-ok, .alerta-err { border-radius: 10px; padding: 11px 14px; font-size: 13px; margin-bottom: 18px; }
        .alerta-ok { color: #6ee7b7; background: rgba(16, 185, 129, 0.10); border: 1px solid rgba(16, 185, 129, 0.3); }
        .alerta-err { color: #fca5a5; background: rgba(239, 68, 68, 0.10); border: 1px solid rgba(239, 68, 68, 0.3); }
        .envoltorio { overflow-x: auto; }
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
            <a href="<%= ctx %>/inicio.jsp">🏠 Inicio</a>
            <a href="<%= ctx %>/UsuarioServlet">👥 Usuarios</a>
            <a href="<%= ctx %>/ClienteServlet">🧑‍💼 Clientes</a>
            <a href="<%= ctx %>/ProveedorServlet" class="activo">🚚 Proveedores</a>
            <a href="#">📦 Productos <span class="pronto">pronto</span></a>
            <a href="#">🧾 Ventas <span class="pronto">pronto</span></a>
            <a href="#">📊 Reportes <span class="pronto">pronto</span></a>
        </nav>

        <div class="usuario-box">
            Sesión iniciada como
            <strong><%= h(usuarioSesion) %></strong>
            <a class="btn btn-azul btn-salir" href="<%= ctx %>/CerrarSesionServlet">Cerrar sesión</a>
        </div>
    </aside>

    <main class="contenido">
        <h1>Gestión de proveedores</h1>
        <p class="sub mb-4">Registra proveedores y a su persona de contacto.</p>

        <% if (textoMsg != null) { %><div class="alerta-ok"><%= h(textoMsg) %></div><% } %>
        <% if (textoErr != null) { %><div class="alerta-err"><%= h(textoErr) %></div><% } %>

        <section class="caja mb-4">
            <h2><%= editando ? "Editar proveedor" : "Nuevo proveedor" %></h2>

            <form method="post" action="<%= ctx %>/ProveedorServlet" autocomplete="off">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= editar.getIdProveedor() %>">
                <% } %>

                <h3>Empresa</h3>
                <div class="row g-3 mb-3">
                    <div class="col-12 col-md-8">
                        <label for="razonSocial">Razón social</label>
                        <input type="text" class="form-control" id="razonSocial" name="razonSocial"
                               maxlength="120" required
                               value="<%= editando ? h(editar.getRazonSocial()) : "" %>">
                    </div>
                    <div class="col-12 col-md-4">
                        <label for="ruc">RUC</label>
                        <input type="text" class="form-control" id="ruc" name="ruc"
                               maxlength="11" required pattern="(10|15|17|20)[0-9]{9}" inputmode="numeric"
                               value="<%= editando ? h(editar.getRuc()) : "" %>"
                               <%= editando ? "readonly" : "" %>>
                        <div class="form-text"><%= editando ? "El RUC no se puede cambiar." : "11 dígitos." %></div>
                    </div>
                </div>

                <h3>Persona de contacto</h3>
                <div class="row g-3">
                    <div class="col-12 col-md-6 col-xl-3">
                        <label for="nombres">Nombres</label>
                        <input type="text" class="form-control" id="nombres" name="nombres"
                               maxlength="80" required
                               value="<%= editando ? h(editar.getNombres()) : "" %>">
                    </div>
                    <div class="col-12 col-md-6 col-xl-3">
                        <label for="apellidos">Apellidos</label>
                        <input type="text" class="form-control" id="apellidos" name="apellidos"
                               maxlength="80" required
                               value="<%= editando ? h(editar.getApellidos()) : "" %>">
                    </div>
                    <div class="col-12 col-md-6 col-xl-3">
                        <label for="dni">DNI</label>
                        <input type="text" class="form-control" id="dni" name="dni"
                               maxlength="8" required pattern="[0-9]{8}" inputmode="numeric"
                               value="<%= editando ? h(editar.getDni()) : "" %>"
                               <%= editando ? "readonly" : "" %>>
                        <div class="form-text"><%= editando ? "El DNI no se puede cambiar." : "8 dígitos." %></div>
                    </div>
                    <div class="col-12 col-md-6 col-xl-3">
                        <label for="telefono">Teléfono</label>
                        <input type="text" class="form-control" id="telefono" name="telefono"
                               maxlength="20" inputmode="tel"
                               value="<%= editando ? h(editar.getTelefono()) : "" %>">
                    </div>
                    <div class="col-12 col-md-6">
                        <label for="correo">Correo electrónico</label>
                        <input type="email" class="form-control" id="correo" name="correo"
                               maxlength="80"
                               value="<%= editando ? h(editar.getCorreo()) : "" %>">
                    </div>
                    <div class="col-12 col-md-6">
                        <label for="direccion">Dirección</label>
                        <input type="text" class="form-control" id="direccion" name="direccion"
                               maxlength="150"
                               value="<%= editando ? h(editar.getDireccion()) : "" %>">
                    </div>
                </div>

                <div class="mt-4 d-flex gap-2">
                    <button type="submit" class="btn btn-azul px-4"><%= editando ? "Guardar cambios" : "Registrar proveedor" %></button>
                    <% if (editando) { %>
                        <a href="<%= ctx %>/ProveedorServlet" class="btn-mini align-self-center">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </section>

        <section class="caja">
            <h2>Proveedores registrados (<%= proveedores.size() %>)</h2>

            <div class="envoltorio">
                <table class="tabla">
                    <thead>
                        <tr>
                            <th>ID</th><th>Razón social</th><th>RUC</th><th>Contacto</th>
                            <th>Teléfono</th><th>Correo</th><th>Estado</th><th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% for (Proveedor p : proveedores) {
                           boolean activo = "Activo".equalsIgnoreCase(p.getEstado()); %>
                        <tr>
                            <td><%= p.getIdProveedor() %></td>
                            <td><strong><%= h(p.getRazonSocial()) %></strong></td>
                            <td><%= h(p.getRuc()) %></td>
                            <td><%= h(p.getNombres()) %> <%= h(p.getApellidos()) %></td>
                            <td><%= p.getTelefono() == null ? "—" : h(p.getTelefono()) %></td>
                            <td><%= p.getCorreo() == null ? "—" : h(p.getCorreo()) %></td>
                            <td><span class="insignia <%= activo ? "ok" : "mal" %>"><%= h(p.getEstado()) %></span></td>
                            <td>
                                <div class="d-flex flex-wrap gap-1">
                                    <a class="btn-mini" href="<%= ctx %>/ProveedorServlet?accion=editar&id=<%= p.getIdProveedor() %>">Editar</a>

                                    <% if (activo) { %>
                                    <form method="post" action="<%= ctx %>/ProveedorServlet" class="d-inline"
                                          onsubmit="return confirm('¿Desactivar este proveedor?');">
                                        <input type="hidden" name="accion" value="desactivar">
                                        <input type="hidden" name="id" value="<%= p.getIdProveedor() %>">
                                        <button type="submit" class="btn-mini peligro">Desactivar</button>
                                    </form>
                                    <% } else { %>
                                    <form method="post" action="<%= ctx %>/ProveedorServlet" class="d-inline">
                                        <input type="hidden" name="accion" value="activar">
                                        <input type="hidden" name="id" value="<%= p.getIdProveedor() %>">
                                        <button type="submit" class="btn-mini">Activar</button>
                                    </form>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

</body>
</html>
