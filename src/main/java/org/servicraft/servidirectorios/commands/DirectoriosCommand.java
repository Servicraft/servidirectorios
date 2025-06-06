package org.servicraft.servidirectorios.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.servicraft.servidirectorios.Servidirectorios;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import org.servicraft.servidirectorios.gui.ShortcutMenu;
import org.servicraft.servidirectorios.gui.EditListGUI;
import org.servicraft.servidirectorios.util.Message;

import java.util.ArrayList;
import java.util.List;

public class DirectoriosCommand implements CommandExecutor, TabCompleter {

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            if ("tiendas".startsWith(arg)) completions.add("tiendas");
            if ("comprar".startsWith(arg)) completions.add("comprar");
            if ("editar".startsWith(arg)) completions.add("editar");
        }
        return completions;
    }
}
