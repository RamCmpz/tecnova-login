package com.ramoncitoacademy.holamundoweb;

import java.sql.Timestamp;

public class Usuario {

    private long id;
    private String usuario;
    private String nombre;
    private Integer rolId;
    private String rolNombre;
    private Integer permisoId;
    private String permisoNombre;
    private boolean bloqueado;
    private int intentosFallidos;
    private String estado;
    private Timestamp ultimoAcceso;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getRolId() { return rolId; }
    public void setRolId(Integer rolId) { this.rolId = rolId; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public Integer getPermisoId() { return permisoId; }
    public void setPermisoId(Integer permisoId) { this.permisoId = permisoId; }

    public String getPermisoNombre() { return permisoNombre; }
    public void setPermisoNombre(String permisoNombre) { this.permisoNombre = permisoNombre; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(Timestamp ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
