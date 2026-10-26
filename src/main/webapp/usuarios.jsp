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
    if (session.getAttribute("usuario") == null) {
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

    String paginaActiva = "usuarios";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Usuarios</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= ctx %>/css/tecnova.css" rel="stylesheet">
</head>
<body>

    <%@ include file="/WEB-INF/menu.jspf" %>

    <main class="contenido">
        <h1>Gestión de usuarios</h1>
        <p class="sub mb-4">Crea, edita y administra las cuentas de acceso al sistema.</p>

        <% if (textoMsg != null) { %><div class="alerta-ok"><%= h(textoMsg) %></div><% } %>
        <% if (textoErr != null) { %><div class="alerta-err"><%= h(textoErr) %></div><% } %>

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
