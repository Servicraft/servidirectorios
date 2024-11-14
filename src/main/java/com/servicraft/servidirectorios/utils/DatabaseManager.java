package com.servicraft.servidirectorios.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import com.servicraft.servidirectorios.Servidirectorios;

public class DatabaseManager {

    private Connection connection;

    public void connect() {
        String host = Servidirectorios.getInstance().getConfig().getString("database.host");
        String port = Servidirectorios.getInstance().getConfig().getString("database.port");
        String database = Servidirectorios.getInstance().getConfig().getString("database.database");
        String username = Servidirectorios.getInstance().getConfig().getString("database.username");
        String password = Servidirectorios.getInstance().getConfig().getString("database.password");

        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                username,
                password
            );
            Bukkit.getLogger().info("Conectado a la base de datos MySQL.");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("No se pudo conectar a la base de datos MySQL.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                Bukkit.getLogger().info("Desconectado de la base de datos MySQL.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
