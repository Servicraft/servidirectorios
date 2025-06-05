package org.servicraft.servidirectorios.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.servicraft.servidirectorios.model.Shortcut;
import org.bukkit.Location;
import org.bukkit.Bukkit;

public class DatabaseManager {

    private static Connection connection;

    public static void init(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "h2");
        if (type.equalsIgnoreCase("mysql")) {
            String host = config.getString("database.mysql.host");
            String port = config.getString("database.mysql.port");
            String db = config.getString("database.mysql.database");
            String user = config.getString("database.mysql.username");
            String pass = config.getString("database.mysql.password");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true";
            try {
                connection = DriverManager.getConnection(url, user, pass);
                createTables();
            } catch (SQLException e) {
                e.printStackTrace();
                connection = null;
            }
        } else {
            String fileName = config.getString("database.file", "database");
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            String url = "jdbc:h2:" + new File(dataFolder, fileName).getPath();
            try {
                // Cargar el driver manualmente para evitar problemas de "No suitable driver"
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection(url, "sa", "");
                createTables();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                connection = null;
            }
        }
    }

    private static void createTables() throws SQLException {
        if (connection == null) return;
        String shortcuts = "CREATE TABLE IF NOT EXISTS shortcuts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(32) NOT NULL," +
                "description VARCHAR(100)," +
                "world VARCHAR(32) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL" +
                ")";
        connection.createStatement().executeUpdate(shortcuts);
    }

    public static boolean isConnected() {
        return connection != null;
    }

    public static List<Shortcut> getShortcuts() {
        List<Shortcut> list = new ArrayList<>();
        if (connection == null) return list;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM shortcuts")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location loc = new Location(Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch"));
                Shortcut sc = new Shortcut(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        loc);
                list.add(sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void createShortcut(String name, String description, Location loc) {
        if (connection == null) return;
        String sql = "INSERT INTO shortcuts(name, description, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setFloat(7, loc.getYaw());
            ps.setFloat(8, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
