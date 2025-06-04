package org.servicraft.servidirectorios;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.servicraft.servidirectorios.commands.DirectoriosCommand;
import org.servicraft.servidirectorios.commands.CreateShortcutCommand;
import org.servicraft.servidirectorios.listeners.BuySlotGUIListener;
import org.servicraft.servidirectorios.listeners.ShortcutMenuListener;
import org.servicraft.servidirectorios.database.DatabaseManager;
import net.milkbowl.vault.economy.Economy;

public class Servidirectorios extends JavaPlugin {
    
    private static Economy econ = null;
    
    @Override
    public void onEnable() {
        getLogger().info("Servidirectorios habilitado.");
        
        // Configurar Vault Economy
        if (!setupEconomy()) {
            getLogger().severe("Vault no encontrado! Deshabilitando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        saveDefaultConfig();

        // Inicializar base de datos
        DatabaseManager.init(this);

        // Registrar comandos
        this.getCommand("directorios").setExecutor(new DirectoriosCommand(this));
        this.getCommand("createshortcut").setExecutor(new CreateShortcutCommand());
        
        // Registrar listeners para las GUIs
        getServer().getPluginManager().registerEvents(new BuySlotGUIListener(), this);
        getServer().getPluginManager().registerEvents(new ShortcutMenuListener(), this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Servidirectorios deshabilitado.");
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
