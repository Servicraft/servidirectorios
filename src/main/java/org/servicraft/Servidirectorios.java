package org.servicraft.servidirectorios;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.servicraft.servidirectorios.commands.DirectoriosCommand;
import org.servicraft.servidirectorios.listeners.BuySlotGUIListener;
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
        
        // Registrar el comando /directorios
        this.getCommand("directorios").setExecutor(new DirectoriosCommand(this));
        
        // Registrar listener para la GUI de compra
        getServer().getPluginManager().registerEvents(new BuySlotGUIListener(), this);
        
        saveDefaultConfig();
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
