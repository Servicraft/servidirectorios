package org.servicraft.servidirectorios.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.servicraft.servidirectorios.Servidirectorios;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import org.servicraft.servidirectorios.gui.ShortcutMenu;
import org.servicraft.servidirectorios.gui.EditListGUI;
import org.servicraft.servidirectorios.util.Message;

public class DirectoriosCommand implements CommandExecutor {

    private final Servidirectorios plugin;

    public DirectoriosCommand(Servidirectorios plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Solo los jugadores pueden usar este comando
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.ONLY_PLAYERS.get());
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            ShortcutMenu.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("tiendas")) {
            BuySlotGUI.open(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("comprar")) {
            // Subcomando /directorios comprar: abrir el menú para comprar directorios
            BuySlotGUI.open(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("editar")) {
            EditListGUI.open(player);
            return true;
        }

        player.sendMessage(ChatColor.RED + Message.UNKNOWN_SUBCOMMAND.get());
        return true;
    }
}
