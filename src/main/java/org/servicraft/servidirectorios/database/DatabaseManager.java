package org.servicraft.servidirectorios.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            // Use absolute path to satisfy H2 requirement for explicit file locations
            String url = "jdbc:h2:" + new File(dataFolder, fileName).getAbsolutePath();
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
        String slots = "CREATE TABLE IF NOT EXISTS slots (" +
                "slot_index INT PRIMARY KEY," +
                "shortcut_id INT NOT NULL," +
                "expires BIGINT NOT NULL" +
                ")";
        connection.createStatement().executeUpdate(shortcuts);
        connection.createStatement().executeUpdate(slots);
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

    public static int createShortcut(String name, String description, Location loc) {
        if (connection == null) return -1;
        String sql = "INSERT INTO shortcuts(name, description, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setFloat(7, loc.getYaw());
            ps.setFloat(8, loc.getPitch());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void cleanExpiredSlots() {
        if (connection == null) return;
        String sql = "DELETE FROM slots WHERE expires <= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cleanupExpiredSlotsPublic() {
        cleanExpiredSlots();
    }

    public static boolean isSlotOccupied(int slotIndex) {
        cleanExpiredSlots();
        if (connection == null) return false;
        String sql = "SELECT 1 FROM slots WHERE slot_index = ? AND expires > ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, slotIndex);
            ps.setLong(2, System.currentTimeMillis());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getRemainingDays(int slotIndex) {
        cleanExpiredSlots();
        if (connection == null) return 0;
        String sql = "SELECT expires FROM slots WHERE slot_index = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, slotIndex);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long expires = rs.getLong("expires");
                long diff = expires - System.currentTimeMillis();
                return (int) Math.ceil(diff / 86400000.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void purchaseSlot(int slotIndex, int weeks, String playerName, Location loc) {
        if (connection == null) return;
        int shortcutId = createShortcut(playerName, "Tienda de " + playerName, loc);
        if (shortcutId == -1) return;
        long expires = System.currentTimeMillis() + weeks * 7L * 24L * 60L * 60L * 1000L;
        String sql = "INSERT OR REPLACE INTO slots(slot_index, shortcut_id, expires) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, slotIndex);
            ps.setInt(2, shortcutId);
            ps.setLong(3, expires);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Shortcut> getActiveShortcuts() {
        cleanExpiredSlots();
        List<Shortcut> list = new ArrayList<>();
        if (connection == null) return list;
        String sql = "SELECT sc.* FROM slots s JOIN shortcuts sc ON s.shortcut_id = sc.id WHERE s.expires > ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
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
}
