package com.servicraft.servidirectorios.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.servicraft.servidirectorios.Servidirectorios;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private Servidirectorios plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(Servidirectorios plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
