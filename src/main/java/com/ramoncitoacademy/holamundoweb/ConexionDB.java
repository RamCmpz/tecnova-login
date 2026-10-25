
package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

private static final String URL =
        System.getenv("DB_URL") != null
        ? System.getenv("DB_URL")
        : "jdbc:mysql://localhost:3306/tecnova_db?serverTimezone=UTC";

private static final String USER =
        System.getenv("DB_USER") != null
        ? System.getenv("DB_USER")
        : "root";

private static final String PASSWORD =
        System.getenv("DB_PASSWORD") != null
        ? System.getenv("DB_PASSWORD")
        : "ram123";

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontró el controlador de MySQL.", e
            );
        }

        if (URL == null || URL.isBlank()
                || USER == null || USER.isBlank()
                || PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "Faltan configurar DB_URL, DB_USER o DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
