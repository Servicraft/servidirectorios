package org.servicraft.servidirectorios.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.servicraft.servidirectorios.database.DatabaseManager;

public class CreateShortcutCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /createshortcut <nombre> <descripcion>");
            return true;
        }
        Player player = (Player) sender;
        String name = args[0];
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String desc = sb.toString().trim();
        Location loc = player.getLocation();
        DatabaseManager.createShortcut(name, desc, loc);
        player.sendMessage(ChatColor.GREEN + "Shortcut creado: " + name);
        return true;
    }
}
