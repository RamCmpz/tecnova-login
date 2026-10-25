<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,
                java.time.format.DateTimeFormatter,
                com.ramoncitoacademy.holamundoweb.Usuario,
                com.ramoncitoacademy.holamundoweb.UsuarioRepository"%>
<%!
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    if (usuarios == null) {
        response.sendRedirect(ctx + "/UsuarioServlet");
        return;
    }
    @SuppressWarnings("unchecked")
    List<UsuarioRepository.Opcion> roles =
            (List<UsuarioRepository.Opcion>) request.getAttribute("roles");
    @SuppressWarnings("unchecked")
    List<UsuarioRepository.Opcion> permisos =
            (List<UsuarioRepository.Opcion>) request.getAttribute("permisos");
    Usuario editar = (Usuario) request.getAttribute("editar");
    boolean editando = editar != null;

    String msg = request.getParameter("msg");
    String err = request.getParameter("err");

    String textoMsg = null;
    if ("creado".equals(msg)) textoMsg = "Usuario creado correctamente.";
    else if ("actualizado".equals(msg)) textoMsg = "Usuario actualizado correctamente.";
    else if ("desactivado".equals(msg)) textoMsg = "Usuario desactivado. Ya no podrá iniciar sesión.";
    else if ("activado".equals(msg)) textoMsg = "Usuario activado nuevamente.";
    else if ("desbloqueado".equals(msg)) textoMsg = "Usuario desbloqueado.";

    String textoErr = null;
    if ("duplicado".equals(err)) textoErr = "Ese nombre de usuario ya existe.";
    else if ("invalido".equals(err)) textoErr = "Datos inválidos. Usuario: 3 a 50 caracteres (letras, números, . _ -). Contraseña: de 8 a 72 caracteres.";
    else if ("propio".equals(err)) textoErr = "No puedes desactivarte ni cambiar tu propio rol.";
    else if ("noexiste".equals(err)) textoErr = "El usuario indicado no existe.";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Usuarios</title>
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
        label { font-size: 12px; font-weight: 600; color: #dce6f7; margin-bottom: 6px; }
        .form-control, .form-select {
            background-color: rgba(5, 11, 21, 0.48); color: var(--texto);
            border: 1px solid rgba(148, 163, 184, 0.22); border-radius: 10px;
            font-size: 13px; color-scheme: dark;
        }
        .form-control:focus, .form-select:focus {
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
        .aviso { color: #fcd34d; background: rgba(245, 158, 11, 0.12); border: 1px solid rgba(245, 158, 11, 0.3); }
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
            <a href="<%= ctx %>/UsuarioServlet" class="activo">👥 Usuarios</a>
            <a href="<%= ctx %>/ClienteServlet">🧑‍💼 Clientes</a>
            <a href="#">🚚 Proveedores <span class="pronto">pronto</span></a>
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
        <h1>Gestión de usuarios</h1>
        <p class="sub mb-4">Crea, edita y administra las cuentas de acceso al sistema.</p>

        <% if (textoMsg != null) { %><div class="alerta-ok"><%= h(textoMsg) %></div><% } %>
        <% if (textoErr != null) { %><div class="alerta-err"><%= h(textoErr) %></div><% } %>

        <!-- Formulario crear / editar -->
        <section class="caja mb-4">
            <h2><%= editando ? "Editar usuario" : "Nuevo usuario" %></h2>

            <form method="post" action="<%= ctx %>/UsuarioServlet" autocomplete="off">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= editar.getId() %>">
                <% } %>

                <div class="row g-3">
                    <div class="col-12 col-md-6 col-xl-3">
                        <label for="usuario">Usuario</label>
                        <input type="text" class="form-control" id="usuario" name="usuario"
                               maxlength="50" required
                               pattern="[A-Za-z0-9_.\-]{3,50}"
                               value="<%= editando ? h(editar.getUsuario()) : "" %>"
                               <%= editando ? "readonly" : "" %>>
                    </div>
                    <div class="col-12 col-md-6 col-xl-3">
                        <label for="nombre">Nombre completo</label>
                        <input type="text" class="form-control" id="nombre" name="nombre"
                               maxlength="100" required
                               value="<%= editando ? h(editar.getNombre()) : "" %>">
                    </div>
                    <div class="col-12 col-md-6 col-xl-2">
                        <label for="rolId">Rol</label>
                        <select class="form-select" id="rolId" name="rolId" required>
                            <% for (UsuarioRepository.Opcion o : roles) {
                                   boolean sel = editando && editar.getRolId() != null
                                           && editar.getRolId() == o.getId(); %>
                                <option value="<%= o.getId() %>" <%= sel ? "selected" : "" %>><%= h(o.getNombre()) %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-12 col-md-6 col-xl-2">
                        <label for="permisoId">Permiso</label>
                     <select class="form-select" id="permisoId" name="permisoId" <%= editando ? "required" : "" %>>
    <% if (!editando) { %>
        <option value="" selected>Automático según el rol</option>
    <% } %>
                            <% for (UsuarioRepository.Opcion o : permisos) {
                                   boolean sel = editando && editar.getPermisoId() != null
                                           && editar.getPermisoId() == o.getId(); %>
                                <option value="<%= o.getId() %>" <%= sel ? "selected" : "" %>><%= h(o.getNombre()) %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-12 col-md-6 col-xl-2">
                        <label for="password"><%= editando ? "Nueva contraseña" : "Contraseña" %></label>
                        <input type="password" class="form-control" id="password" name="password"
                               minlength="8" maxlength="72" autocomplete="new-password"
                               <%= editando ? "" : "required" %>>
                        <div class="form-text"><%= editando ? "Déjala vacía para no cambiarla." : "Mínimo 8 caracteres." %></div>
                    </div>
                </div>

                <div class="mt-4 d-flex gap-2">
                    <button type="submit" class="btn btn-azul px-4"><%= editando ? "Guardar cambios" : "Crear usuario" %></button>
                    <% if (editando) { %>
                        <a href="<%= ctx %>/UsuarioServlet" class="btn-mini align-self-center">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- Listado -->
        <section class="caja">
            <h2>Usuarios registrados (<%= usuarios.size() %>)</h2>

            <div class="envoltorio">
                <table class="tabla">
                    <thead>
                        <tr>
                            <th>ID</th><th>Usuario</th><th>Nombre</th><th>Rol</th>
                            <th>Estado</th><th>Acceso</th><th>Último acceso</th><th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% for (Usuario u : usuarios) {
                           boolean activo = "Activo".equalsIgnoreCase(u.getEstado()); %>
                        <tr>
                            <td><%= u.getId() %></td>
                            <td><strong><%= h(u.getUsuario()) %></strong></td>
                            <td><%= h(u.getNombre()) %></td>
                            <td><%= h(u.getRolNombre()) %></td>
                            <td><span class="insignia <%= activo ? "ok" : "mal" %>"><%= h(u.getEstado()) %></span></td>
                            <td>
                                <% if (u.isBloqueado()) { %>
                                    <span class="insignia aviso">Bloqueado</span>
                                <% } else { %>
                                    <span class="insignia ok">Libre</span>
                                <% } %>
                            </td>
                            <td><%= u.getUltimoAcceso() == null ? "—" : u.getUltimoAcceso().toLocalDateTime().format(FMT) %></td>
                            <td>
                                <div class="d-flex flex-wrap gap-1">
                                    <a class="btn-mini" href="<%= ctx %>/UsuarioServlet?accion=editar&id=<%= u.getId() %>">Editar</a>

                                    <% if (u.isBloqueado()) { %>
                                    <form method="post" action="<%= ctx %>/UsuarioServlet" class="d-inline">
                                        <input type="hidden" name="accion" value="desbloquear">
                                        <input type="hidden" name="id" value="<%= u.getId() %>">
                                        <button type="submit" class="btn-mini">Desbloquear</button>
                                    </form>
                                    <% } %>

                                    <% if (activo) { %>
                                    <form method="post" action="<%= ctx %>/UsuarioServlet" class="d-inline"
                                          onsubmit="return confirm('¿Desactivar este usuario? Ya no podrá iniciar sesión.');">
                                        <input type="hidden" name="accion" value="desactivar">
                                        <input type="hidden" name="id" value="<%= u.getId() %>">
                                        <button type="submit" class="btn-mini peligro">Desactivar</button>
                                    </form>
                                    <% } else { %>
                                    <form method="post" action="<%= ctx %>/UsuarioServlet" class="d-inline">
                                        <input type="hidden" name="accion" value="activar">
                                        <input type="hidden" name="id" value="<%= u.getId() %>">
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
