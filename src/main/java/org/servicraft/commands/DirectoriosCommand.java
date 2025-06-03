package org.servicraft.servidirectorios.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.servicraft.servidirectorios.Servidirectorios;
import org.servicraft.servidirectorios.gui.BuySlotGUI;

public class DirectoriosCommand implements CommandExecutor {

    private final Servidirectorios plugin;

    public DirectoriosCommand(Servidirectorios plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Solo los jugadores pueden usar este comando
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // User Story #1: Menú de atajos (placeholder)
            player.sendMessage(ChatColor.GREEN + "Abriendo menú de atajos...");
            // Aquí se implementará la UI de atajos personalizada.
            return true;
        }

        if (args[0].equalsIgnoreCase("tiendas")) {
            // Subcomando /directorios tiendas: abrir el menú de tiendas
            player.sendMessage(ChatColor.AQUA + "Abriendo menú de tiendas...");
            // Aquí se delegará a la lógica del menú de tiendas.
            return true;
        }
        
        if (args[0].equalsIgnoreCase("comprar")) {
            // Subcomando /directorios comprar: abrir el menú para comprar directorios
            BuySlotGUI.open(player, 1);
            return true;
        }

        player.sendMessage(ChatColor.RED + "Subcomando desconocido. Usa /directorios, /directorios tiendas o /directorios comprar.");
        return true;
    }
}
