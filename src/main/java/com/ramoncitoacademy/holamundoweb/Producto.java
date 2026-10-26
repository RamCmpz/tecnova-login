package com.ramoncitoacademy.holamundoweb;

import java.math.BigDecimal;

public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int idCategoria;
    private String categoria;
    private int idMarca;
    private String marca;
    private int idUnidadMedida;
    private String unidad;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int stock;
    private int stockMinimo;
    private String estado;

    public boolean isStockBajo() { return stock <= stockMinimo; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getIdMarca() { return idMarca; }
    public void setIdMarca(int idMarca) { this.idMarca = idMarca; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public int getIdUnidadMedida() { return idUnidadMedida; }
    public void setIdUnidadMedida(int idUnidadMedida) { this.idUnidadMedida = idUnidadMedida; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
