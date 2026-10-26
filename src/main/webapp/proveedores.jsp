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
    if (session.getAttribute("usuario") == null) {
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

    String paginaActiva = "proveedores";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Proveedores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= ctx %>/css/tecnova.css" rel="stylesheet">
</head>
<body>

    <%@ include file="/WEB-INF/menu.jspf" %>

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
