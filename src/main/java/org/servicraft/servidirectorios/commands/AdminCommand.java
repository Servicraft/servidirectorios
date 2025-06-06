package org.servicraft.servidirectorios.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.servicraft.servidirectorios.gui.ShortcutMenu;
import org.servicraft.servidirectorios.util.Message;

public class AdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.ONLY_PLAYERS.get());
            return true;
        }

        Player player = (Player) sender;
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("servidirectorios.admin")) {
                player.sendMessage(org.bukkit.ChatColor.RED + Message.UNKNOWN_SUBCOMMAND.get());
                return true;
            }
            ShortcutMenu.openAdmin(player);
            return true;
        }

        player.sendMessage(org.bukkit.ChatColor.RED + Message.UNKNOWN_SUBCOMMAND.get());
        return true;
    }
}
