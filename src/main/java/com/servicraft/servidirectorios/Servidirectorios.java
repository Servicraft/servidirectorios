package com.servicraft.servidirectorios;

import org.bukkit.plugin.java.JavaPlugin;
import com.servicraft.servidirectorios.comandos.DirectoriosComando;
import com.servicraft.servidirectorios.utils.ConfigManager;
import com.servicraft.servidirectorios.utils.DatabaseManager;
import com.servicraft.servidirectorios.listeners.MenuListener;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Servidirectorios extends JavaPlugin {

    private static Servidirectorios instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private static Economy economy = null;

    @Override
    public void onEnable() {
        instance = this;
        // Cargar configuración
        this.configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Conectar a la base de datos MySQL
        this.databaseManager = new DatabaseManager();
        databaseManager.connect();

        // Configurar Vault
        if (!setupEconomy()) {
            getLogger().severe("Vault no encontrado. Deshabilitando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Registro del comando
        this.getCommand("directorios").setExecutor(new DirectoriosComando());

        // Registro del Listener
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        getLogger().info("El plugin Servidirectorios ha sido habilitado.");
    }

    @Override
    public void onDisable() {
        // Cerrar conexión a la base de datos
        databaseManager.disconnect();
        getLogger().info("El plugin Servidirectorios ha sido deshabilitado.");
    }

    public static Servidirectorios getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
