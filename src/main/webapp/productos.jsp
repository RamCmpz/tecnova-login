<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,
                com.ramoncitoacademy.holamundoweb.Producto,
                com.ramoncitoacademy.holamundoweb.ProductoRepository"%>
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
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    if (productos == null) {
        response.sendRedirect(ctx + "/ProductoServlet");
        return;
    }
    @SuppressWarnings("unchecked")
    List<ProductoRepository.Opcion> categorias = (List<ProductoRepository.Opcion>) request.getAttribute("categorias");
    @SuppressWarnings("unchecked")
    List<ProductoRepository.Opcion> marcas = (List<ProductoRepository.Opcion>) request.getAttribute("marcas");
    @SuppressWarnings("unchecked")
    List<ProductoRepository.Opcion> unidades = (List<ProductoRepository.Opcion>) request.getAttribute("unidades");

    Producto editar = (Producto) request.getAttribute("editar");
    boolean editando = editar != null;

    String msg = request.getParameter("msg");
    String err = request.getParameter("err");

    String textoMsg = null;
    if ("creado".equals(msg)) textoMsg = "Producto registrado correctamente.";
    else if ("actualizado".equals(msg)) textoMsg = "Producto actualizado correctamente.";
    else if ("desactivado".equals(msg)) textoMsg = "Producto desactivado.";
    else if ("activado".equals(msg)) textoMsg = "Producto activado nuevamente.";

    String textoErr = null;
    if ("duplicado".equals(err)) textoErr = "Ya existe un producto con ese código.";
    else if ("invalido".equals(err)) textoErr = "Datos inválidos. Revisa el código (3 a 20 letras, números o guiones), los precios (máximo 2 decimales) y el stock (números enteros).";
    else if ("noexiste".equals(err)) textoErr = "El producto indicado no existe.";

    String paginaActiva = "productos";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TecNova | Productos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= ctx %>/css/tecnova.css" rel="stylesheet">
</head>
<body>

    <%@ include file="/WEB-INF/menu.jspf" %>

    <main class="contenido">
        <h1>Gestión de productos</h1>
        <p class="sub mb-4">Controla el catálogo, los precios y el stock de TecNova.</p>

        <% if (textoMsg != null) { %><div class="alerta-ok"><%= h(textoMsg) %></div><% } %>
        <% if (textoErr != null) { %><div class="alerta-err"><%= h(textoErr) %></div><% } %>

        <section class="caja mb-4">
            <h2><%= editando ? "Editar producto" : "Nuevo producto" %></h2>

            <form method="post" action="<%= ctx %>/ProductoServlet" autocomplete="off">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= editar.getIdProducto() %>">
                <% } %>

                <h3>Datos del producto</h3>
                <div class="row g-3 mb-3">
                    <div class="col-12 col-md-4 col-xl-2">
                        <label for="codigo">Código</label>
                        <input type="text" class="form-control" id="codigo" name="codigo"
                               maxlength="20" required pattern="[A-Za-z0-9\-]{3,20}"
                               value="<%= editando ? h(editar.getCodigo()) : "" %>"
                               <%= editando ? "readonly" : "" %>>
                        <div class="form-text"><%= editando ? "No se puede cambiar." : "Ej.: LAP-003" %></div>
                    </div>
                    <div class="col-12 col-md-8 col-xl-4">
                        <label for="nombre">Nombre</label>
                        <input type="text" class="form-control" id="nombre" name="nombre"
                               maxlength="120" required
                               value="<%= editando ? h(editar.getNombre()) : "" %>">
                    </div>
                    <div class="col-12 col-xl-6">
                        <label for="descripcion">Descripción</label>
                        <input type="text" class="form-control" id="descripcion" name="descripcion"
                               maxlength="255"
                               value="<%= editando ? h(editar.getDescripcion()) : "" %>">
                    </div>
                    <div class="col-12 col-md-4">
                        <label for="idCategoria">Categoría</label>
                        <select class="form-select" id="idCategoria" name="idCategoria" required>
                            <% for (ProductoRepository.Opcion o : categorias) { %>
                                <option value="<%= o.getId() %>" <%= editando && editar.getIdCategoria() == o.getId() ? "selected" : "" %>><%= h(o.getNombre()) %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label for="idMarca">Marca</label>
                        <select class="form-select" id="idMarca" name="idMarca" required>
                            <% for (ProductoRepository.Opcion o : marcas) { %>
                                <option value="<%= o.getId() %>" <%= editando && editar.getIdMarca() == o.getId() ? "selected" : "" %>><%= h(o.getNombre()) %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label for="idUnidadMedida">Unidad de medida</label>
                        <select class="form-select" id="idUnidadMedida" name="idUnidadMedida" required>
                            <% for (ProductoRepository.Opcion o : unidades) { %>
                                <option value="<%= o.getId() %>" <%= editando && editar.getIdUnidadMedida() == o.getId() ? "selected" : "" %>><%= h(o.getNombre()) %></option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <h3>Precios y stock</h3>
                <div class="row g-3">
                    <div class="col-6 col-xl-3">
                        <label for="precioCompra">Precio de compra (S/)</label>
                        <input type="number" class="form-control" id="precioCompra" name="precioCompra"
                               min="0" max="99999999.99" step="0.01" required
                               value="<%= editando ? editar.getPrecioCompra().toPlainString() : "" %>">
                    </div>
                    <div class="col-6 col-xl-3">
                        <label for="precioVenta">Precio de venta (S/)</label>
                        <input type="number" class="form-control" id="precioVenta" name="precioVenta"
                               min="0" max="99999999.99" step="0.01" required
                               value="<%= editando ? editar.getPrecioVenta().toPlainString() : "" %>">
                    </div>
                    <div class="col-6 col-xl-3">
                        <label for="stock">Stock actual</label>
                        <input type="number" class="form-control" id="stock" name="stock"
                               min="0" max="1000000" step="1" required
                               value="<%= editando ? String.valueOf(editar.getStock()) : "0" %>">
                    </div>
                    <div class="col-6 col-xl-3">
                        <label for="stockMinimo">Stock mínimo</label>
                        <input type="number" class="form-control" id="stockMinimo" name="stockMinimo"
                               min="0" max="1000000" step="1" required
                               value="<%= editando ? String.valueOf(editar.getStockMinimo()) : "0" %>">
                        <div class="form-text">Debajo de este número se marca como stock bajo.</div>
                    </div>
                </div>

                <div class="mt-4 d-flex gap-2">
                    <button type="submit" class="btn btn-azul px-4"><%= editando ? "Guardar cambios" : "Registrar producto" %></button>
                    <% if (editando) { %>
                        <a href="<%= ctx %>/ProductoServlet" class="btn-mini align-self-center">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </section>

        <section class="caja">
            <h2>Productos registrados (<%= productos.size() %>)</h2>

            <div class="envoltorio">
                <table class="tabla">
                    <thead>
                        <tr>
                            <th>Código</th><th>Producto</th><th>Categoría</th><th>Marca</th>
                            <th>P. compra</th><th>P. venta</th><th>Stock</th><th>Estado</th><th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% for (Producto p : productos) {
                           boolean activo = "Activo".equalsIgnoreCase(p.getEstado()); %>
                        <tr>
                            <td><strong><%= h(p.getCodigo()) %></strong></td>
                            <td><%= h(p.getNombre()) %></td>
                            <td><span class="insignia info"><%= h(p.getCategoria()) %></span></td>
                            <td><%= h(p.getMarca()) %></td>
                            <td>S/ <%= String.format(java.util.Locale.US, "%.2f", p.getPrecioCompra()) %></td>
                            <td>S/ <%= String.format(java.util.Locale.US, "%.2f", p.getPrecioVenta()) %></td>
                            <td>
                                <%= p.getStock() %> <%= h(p.getUnidad()) %>
                                <% if (p.isStockBajo()) { %>
                                    <span class="insignia aviso">Stock bajo</span>
                                <% } %>
                            </td>
                            <td><span class="insignia <%= activo ? "ok" : "mal" %>"><%= h(p.getEstado()) %></span></td>
                            <td>
                                <div class="d-flex flex-wrap gap-1">
                                    <a class="btn-mini" href="<%= ctx %>/ProductoServlet?accion=editar&id=<%= p.getIdProducto() %>">Editar</a>

                                    <% if (activo) { %>
                                    <form method="post" action="<%= ctx %>/ProductoServlet" class="d-inline"
                                          onsubmit="return confirm('¿Desactivar este producto?');">
                                        <input type="hidden" name="accion" value="desactivar">
                                        <input type="hidden" name="id" value="<%= p.getIdProducto() %>">
                                        <button type="submit" class="btn-mini peligro">Desactivar</button>
                                    </form>
                                    <% } else { %>
                                    <form method="post" action="<%= ctx %>/ProductoServlet" class="d-inline">
                                        <input type="hidden" name="accion" value="activar">
                                        <input type="hidden" name="id" value="<%= p.getIdProducto() %>">
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
