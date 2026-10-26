<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,
                com.ramoncitoacademy.holamundoweb.Cliente"%>
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
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    if (clientes == null) {
        response.sendRedirect(ctx + "/ClienteServlet");
        return;
    }
    Cliente editar = (Cliente) request.getAttribute("editar");
    boolean editando = editar != null;

    String msg = request.getParameter("msg");
    String err = request.getParameter("err");

    String textoMsg = null;
    if ("creado".equals(msg)) textoMsg = "Cliente registrado correctamente.";
    else if ("actualizado".equals(msg)) textoMsg = "Cliente actualizado correctamente.";
    else if ("desactivado".equals(msg)) textoMsg = "Cliente desactivado.";
    else if ("activado".equals(msg)) textoMsg = "Cliente activado nuevamente.";

    String textoErr = null;
    if ("duplicado".equals(err)) textoErr = "Ya existe un cliente con ese DNI o ese RUC.";
    else if ("invalido".equals(err)) textoErr = "Datos inválidos. El DNI debe tener 8 dígitos, el RUC 11 dígitos (y debe ir junto con la razón social), el correo debe ser válido y el teléfono solo números.";
    else if ("noexiste".equals(err)) textoErr = "El cliente indicado no existe.";

    String paginaActiva = "clientes";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Clientes</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= ctx %>/css/tecnova.css" rel="stylesheet">
</head>
<body>

    <%@ include file="/WEB-INF/menu.jspf" %>

    <main class="contenido">
        <h1>Gestión de clientes</h1>
        <p class="sub mb-4">Registra y mantiene la información de los clientes de TecNova.</p>

        <% if (textoMsg != null) { %><div class="alerta-ok"><%= h(textoMsg) %></div><% } %>
        <% if (textoErr != null) { %><div class="alerta-err"><%= h(textoErr) %></div><% } %>

        <section class="caja mb-4">
            <h2><%= editando ? "Editar cliente" : "Nuevo cliente" %></h2>

            <form method="post" action="<%= ctx %>/ClienteServlet" autocomplete="off">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= editar.getIdCliente() %>">
                <% } %>

                <h3>Datos personales</h3>
                <div class="row g-3 mb-3">
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
                    <div class="col-12 col-md-6 col-xl-2">
                        <label for="dni">DNI</label>
                        <input type="text" class="form-control" id="dni" name="dni"
                               maxlength="8" required pattern="[0-9]{8}" inputmode="numeric"
                               value="<%= editando ? h(editar.getDni()) : "" %>"
                               <%= editando ? "readonly" : "" %>>
                        <div class="form-text"><%= editando ? "El DNI no se puede cambiar." : "8 dígitos." %></div>
                    </div>
                    <div class="col-12 col-md-6 col-xl-2">
                        <label for="telefono">Teléfono</label>
                        <input type="text" class="form-control" id="telefono" name="telefono"
                               maxlength="20" inputmode="tel"
                               value="<%= editando ? h(editar.getTelefono()) : "" %>">
                    </div>
                    <div class="col-12 col-md-6 col-xl-2">
                        <label for="tipoCliente">Tipo</label>
                        <select class="form-select" id="tipoCliente" name="tipoCliente" required>
                            <option value="Minorista" <%= editando && "Minorista".equals(editar.getTipoCliente()) ? "selected" : "" %>>Minorista</option>
                            <option value="Mayorista" <%= editando && "Mayorista".equals(editar.getTipoCliente()) ? "selected" : "" %>>Mayorista</option>
                        </select>
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

                <h3>Datos de facturación (opcional)</h3>
                <div class="row g-3">
                    <div class="col-12 col-md-4">
                        <label for="ruc">RUC</label>
                        <input type="text" class="form-control" id="ruc" name="ruc"
                               maxlength="11" pattern="(10|15|17|20)[0-9]{9}" inputmode="numeric"
                               value="<%= editando ? h(editar.getRuc()) : "" %>">
                        <div class="form-text">11 dígitos. Solo si el cliente pide factura.</div>
                    </div>
                    <div class="col-12 col-md-8">
                        <label for="razonSocial">Razón social</label>
                        <input type="text" class="form-control" id="razonSocial" name="razonSocial"
                               maxlength="120"
                               value="<%= editando ? h(editar.getRazonSocial()) : "" %>">
                        <div class="form-text">Obligatoria si ingresas RUC.</div>
                    </div>
                </div>

                <div class="mt-4 d-flex gap-2">
                    <button type="submit" class="btn btn-azul px-4"><%= editando ? "Guardar cambios" : "Registrar cliente" %></button>
                    <% if (editando) { %>
                        <a href="<%= ctx %>/ClienteServlet" class="btn-mini align-self-center">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </section>

        <section class="caja">
            <h2>Clientes registrados (<%= clientes.size() %>)</h2>

            <div class="envoltorio">
                <table class="tabla">
                    <thead>
                        <tr>
                            <th>ID</th><th>Cliente</th><th>DNI</th><th>RUC</th><th>Teléfono</th>
                            <th>Tipo</th><th>Estado</th><th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% for (Cliente c : clientes) {
                           boolean activo = "Activo".equalsIgnoreCase(c.getEstado()); %>
                        <tr>
                            <td><%= c.getIdCliente() %></td>
                            <td>
                                <strong><%= h(c.getNombres()) %> <%= h(c.getApellidos()) %></strong>
                                <% if (c.getRazonSocial() != null) { %>
                                    <div class="form-text"><%= h(c.getRazonSocial()) %></div>
                                <% } %>
                            </td>
                            <td><%= h(c.getDni()) %></td>
                            <td><%= c.getRuc() == null ? "—" : h(c.getRuc()) %></td>
                            <td><%= c.getTelefono() == null ? "—" : h(c.getTelefono()) %></td>
                            <td><span class="insignia info"><%= h(c.getTipoCliente()) %></span></td>
                            <td><span class="insignia <%= activo ? "ok" : "mal" %>"><%= h(c.getEstado()) %></span></td>
                            <td>
                                <div class="d-flex flex-wrap gap-1">
                                    <a class="btn-mini" href="<%= ctx %>/ClienteServlet?accion=editar&id=<%= c.getIdCliente() %>">Editar</a>

                                    <% if (activo) { %>
                                    <form method="post" action="<%= ctx %>/ClienteServlet" class="d-inline"
                                          onsubmit="return confirm('¿Desactivar este cliente?');">
                                        <input type="hidden" name="accion" value="desactivar">
                                        <input type="hidden" name="id" value="<%= c.getIdCliente() %>">
                                        <button type="submit" class="btn-mini peligro">Desactivar</button>
                                    </form>
                                    <% } else { %>
                                    <form method="post" action="<%= ctx %>/ClienteServlet" class="d-inline">
                                        <input type="hidden" name="accion" value="activar">
                                        <input type="hidden" name="id" value="<%= c.getIdCliente() %>">
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
