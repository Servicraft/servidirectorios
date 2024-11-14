package com.servicraft.servidirectorios.comandos;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.entity.Player;
import com.servicraft.servidirectorios.gui.TiendasMenu;

public class DirectoriosComando implements CommandExecutor {

    private final Permission usePermission = new Permission("servidirectorios.use");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(usePermission)) {
            player.sendMessage("No tienes permisos para usar este comando.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("tiendas")) {
            // Abrir el menú de tiendas
            new TiendasMenu(player).open();
            return true;
        } else {
            player.sendMessage("Uso correcto: /directorios tiendas");
            return true;
        }
    }
}
