package org.servicraft.servidirectorios;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.servicraft.servidirectorios.commands.DirectoriosCommand;
import org.servicraft.servidirectorios.commands.AdminCommand;
import org.servicraft.servidirectorios.listeners.BuySlotGUIListener;
import org.servicraft.servidirectorios.listeners.BuySlotWeeksGUIListener;
import org.servicraft.servidirectorios.listeners.ShortcutMenuListener;
import org.servicraft.servidirectorios.listeners.EditMenuListener;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.util.Message;
import net.milkbowl.vault.economy.Economy;

public class Servidirectorios extends JavaPlugin {
    
    private static Economy econ = null;
    
    @Override
    public void onEnable() {
        Message.load(this);
        getLogger().info(Message.PLUGIN_ENABLED.get());
        
        // Configurar Vault Economy
        if (!setupEconomy()) {
            getLogger().severe(Message.NO_VAULT.get());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        saveDefaultConfig();

        // Inicializar base de datos
        DatabaseManager.init(this);
        DatabaseManager.cleanupExpiredSlotsPublic();

        // Registrar comandos
        DirectoriosCommand dirCmd = new DirectoriosCommand(this);
        this.getCommand("directorios").setExecutor(dirCmd);
        this.getCommand("directorios").setTabCompleter(dirCmd);
        this.getCommand("servidirectorios").setExecutor(new AdminCommand());
        
        // Registrar listeners para las GUIs
        getServer().getPluginManager().registerEvents(new BuySlotGUIListener(), this);
        getServer().getPluginManager().registerEvents(new BuySlotWeeksGUIListener(), this);
        getServer().getPluginManager().registerEvents(new ShortcutMenuListener(), this);
        getServer().getPluginManager().registerEvents(new EditMenuListener(), this);
        getServer().getPluginManager().registerEvents(new org.servicraft.servidirectorios.listeners.StatsMenuListener(), this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info(Message.PLUGIN_DISABLED.get());
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    public static Economy getEconomy() {
        return econ;
    }
}
