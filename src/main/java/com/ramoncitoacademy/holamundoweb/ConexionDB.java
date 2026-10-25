package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // Única instancia de la clase (Singleton)
    private static ConexionDB instancia;

    private final String url;
    private final String user;
    private final String password;

    // Constructor privado: nadie más puede hacer "new ConexionDB()"
    private ConexionDB() {
        this.url = leer("DB_URL",
                "jdbc:mysql://localhost:3306/tecnova_db?serverTimezone=UTC");
        this.user = leer("DB_USER", "root");
        this.password = leer("DB_PASSWORD", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "No se encontró el controlador de MySQL.", e);
        }
    }

    // Punto de acceso global a la única instancia
    public static synchronized ConexionDB getInstance() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    // Entrega una conexión nueva cada vez (más seguro con Aiven)
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Compatibilidad con el código anterior (PruebaConexion, etc.)
    public static Connection conectar() throws SQLException {
        return getInstance().getConnection();
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return (valor != null && !valor.isBlank()) ? valor : porDefecto;
    }
}
