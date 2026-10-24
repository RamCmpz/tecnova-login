
package com.ramoncitoacademy.holamundoweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL =
            "jdbc:mysql://localhost:3306/tecnova_db?serverTimezone=UTC";

    private static final String USER = "root";

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontró el controlador de MySQL.", e
            );
        }

        if (PASSWORD == null || PASSWORD.isEmpty()) {
            throw new SQLException(
                    "Falta configurar la variable DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
