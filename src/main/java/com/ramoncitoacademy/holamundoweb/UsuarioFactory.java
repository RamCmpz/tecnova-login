package com.ramoncitoacademy.holamundoweb;

/**
 * Patrón Factory Method: centraliza la creación de objetos Usuario
 * con los valores por defecto que corresponden a cada rol.
 */
public class UsuarioFactory {

    private UsuarioFactory() { }

    public static Usuario crear(String rolNombre, String usuario, String nombre) {
        Usuario u = new Usuario();
        u.setUsuario(usuario);
        u.setNombre(nombre);
        u.setRolNombre(rolNombre);
        u.setPermisoNombre(permisoPorDefecto(rolNombre));
        u.setEstado("Activo");
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        return u;
    }

    public static String permisoPorDefecto(String rolNombre) {
        if (rolNombre == null) {
            return "Ver reportes";
        }
        switch (rolNombre) {
            case "Administrador":
                return "Acceso total";
            case "Vendedor":
                return "Registrar ventas";
            case "Supervisor":
                return "Ver reportes";
            default:
                return "Ver reportes";
        }
    }
}
