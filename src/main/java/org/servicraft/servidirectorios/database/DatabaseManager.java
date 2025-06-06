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

import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.servicraft.servidirectorios.model.Shortcut;

public class DatabaseManager {

    private static Connection connection;
    private static String dbType;

    public static void init(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "h2");
        dbType = type.toLowerCase();
        if (dbType.equals("mysql")) {
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
                "icon VARCHAR(32) DEFAULT 'CHEST'," +
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
                "expires BIGINT NOT NULL," +
                "owner VARCHAR(32) NOT NULL" +
                ")";
        connection.createStatement().executeUpdate(shortcuts);
        connection.createStatement().executeUpdate(slots);
        try {
            connection.createStatement().executeUpdate("ALTER TABLE slots ADD COLUMN IF NOT EXISTS owner VARCHAR(32) NOT NULL DEFAULT ''");
        } catch (SQLException ignore) {}
        try {
            connection.createStatement().executeUpdate("ALTER TABLE shortcuts ADD COLUMN IF NOT EXISTS icon VARCHAR(32) DEFAULT 'CHEST'");
        } catch (SQLException ignore) {}
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
                        loc,
                        Material.valueOf(rs.getString("icon")));
                list.add(sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int createShortcut(String name, String description, Location loc, Material icon) {
        if (connection == null) return -1;
        String sql = "INSERT INTO shortcuts(name, description, icon, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, icon.name());
            ps.setString(4, loc.getWorld().getName());
            ps.setDouble(5, loc.getX());
            ps.setDouble(6, loc.getY());
            ps.setDouble(7, loc.getZ());
            ps.setFloat(8, loc.getYaw());
            ps.setFloat(9, loc.getPitch());
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

    public static String getSlotOwner(int slotIndex) {
        cleanExpiredSlots();
        if (connection == null) return null;
        String sql = "SELECT owner FROM slots WHERE slot_index = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, slotIndex);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("owner");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.Map<Integer, Shortcut> getOwnedShortcuts(String player) {
        cleanExpiredSlots();
        java.util.Map<Integer, Shortcut> map = new java.util.LinkedHashMap<>();
        if (connection == null) return map;
        String sql = "SELECT s.slot_index, sc.* FROM slots s JOIN shortcuts sc ON s.shortcut_id = sc.id WHERE s.owner = ? AND s.expires > ? ORDER BY s.slot_index";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, player);
            ps.setLong(2, System.currentTimeMillis());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location loc = new Location(Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch"));
                Shortcut sc = new Shortcut(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        loc,
                        Material.valueOf(rs.getString("icon")));
                map.put(rs.getInt("slot_index"), sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void updateShortcutBySlot(int slotIndex, String name, String description, Location loc, Material icon) {
        if (connection == null) return;
        String getId = "SELECT shortcut_id FROM slots WHERE slot_index = ?";
        try (PreparedStatement ps = connection.prepareStatement(getId)) {
            ps.setInt(1, slotIndex);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String sql = "UPDATE shortcuts SET name=?, description=?, icon=?, world=?, x=?, y=?, z=?, yaw=?, pitch=? WHERE id=?";
                try (PreparedStatement ps2 = connection.prepareStatement(sql)) {
                    ps2.setString(1, name);
                    ps2.setString(2, description);
                    ps2.setString(3, icon.name());
                    ps2.setString(4, loc.getWorld().getName());
                    ps2.setDouble(5, loc.getX());
                    ps2.setDouble(6, loc.getY());
                    ps2.setDouble(7, loc.getZ());
                    ps2.setFloat(8, loc.getYaw());
                    ps2.setFloat(9, loc.getPitch());
                    ps2.setInt(10, id);
                    ps2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void purchaseSlot(int slotIndex, int weeks, String playerName, Location loc) {
        if (connection == null) return;
        int shortcutId = createShortcut(playerName, "Tienda de " + playerName, loc, Material.CHEST);
        if (shortcutId == -1) return;
        long expires = System.currentTimeMillis() + weeks * 7L * 24L * 60L * 60L * 1000L;
        String sql;
        if ("mysql".equals(dbType)) {
            sql = "INSERT INTO slots(slot_index, shortcut_id, expires, owner) VALUES(?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE shortcut_id=VALUES(shortcut_id), expires=VALUES(expires), owner=VALUES(owner)";
        } else {
            sql = "MERGE INTO slots KEY(slot_index) VALUES(?,?,?,?)";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, slotIndex);
            ps.setInt(2, shortcutId);
            ps.setLong(3, expires);
            ps.setString(4, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Shortcut> getActiveShortcuts() {
        return new ArrayList<>(getActiveShortcutMap().values());
    }

    public static java.util.Map<Integer, Shortcut> getActiveShortcutMap() {
        cleanExpiredSlots();
        java.util.Map<Integer, Shortcut> map = new java.util.LinkedHashMap<>();
        if (connection == null) return map;
        String sql = "SELECT s.slot_index, sc.* FROM slots s JOIN shortcuts sc ON s.shortcut_id = sc.id " +
                "WHERE s.expires > ? ORDER BY s.slot_index";
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
                        loc,
                        Material.valueOf(rs.getString("icon")));
                map.put(rs.getInt("slot_index"), sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void extendSlot(int slotIndex, int weeks) {
        if (connection == null) return;
        String sql = "UPDATE slots SET expires = expires + ? WHERE slot_index = ?";
        long add = weeks * 7L * 24L * 60L * 60L * 1000L;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, add);
            ps.setInt(2, slotIndex);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateIconBySlot(int slotIndex, Material icon) {
        if (connection == null) return;
        String getId = "SELECT shortcut_id FROM slots WHERE slot_index = ?";
        try (PreparedStatement ps = connection.prepareStatement(getId)) {
            ps.setInt(1, slotIndex);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                try (PreparedStatement ps2 = connection.prepareStatement("UPDATE shortcuts SET icon=? WHERE id=?")) {
                    ps2.setString(1, icon.name());
                    ps2.setInt(2, id);
                    ps2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSlot(int slotIndex) {
        if (connection == null) return;
        String select = "SELECT shortcut_id FROM slots WHERE slot_index = ?";
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setInt(1, slotIndex);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                try (PreparedStatement ps2 = connection.prepareStatement("DELETE FROM slots WHERE slot_index = ?")) {
                    ps2.setInt(1, slotIndex);
                    ps2.executeUpdate();
                }
                try (PreparedStatement ps3 = connection.prepareStatement("DELETE FROM shortcuts WHERE id = ?")) {
                    ps3.setInt(1, id);
                    ps3.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
