package org.servicraft.servidirectorios.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.leaderos.plugin.api.LeaderOSAPI;
import net.leaderos.plugin.api.handlers.UpdateCacheEvent;
import net.leaderos.shared.helpers.ChatUtil;
import net.leaderos.shared.helpers.MoneyUtil;
import net.leaderos.shared.helpers.Placeholder;
import org.bukkit.Bukkit;

public class RemoveCreditsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public RemoveCreditsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("removecredits")) {
            if (args.length != 2) {
                sender.sendMessage("Uso: /removecredits <jugador> <cantidad>");
                return false;
            }

            String targetPlayerName = args[0];
            double amount;

            try {
                amount = MoneyUtil.parseDouble(Double.parseDouble(args[1]));
            } catch (NumberFormatException e) {
                sender.sendMessage("Por favor, introduce una cantidad válida de créditos.");
                return false;
            }

            if (amount <= 0) {
                sender.sendMessage("La cantidad debe ser mayor a 0.");
                return false;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean isRemoved = LeaderOSAPI.getCreditManager().remove(targetPlayerName, amount);

                if (isRemoved) {
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(
                        new UpdateCacheEvent(targetPlayerName, amount, UpdateCacheEvent.UpdateType.REMOVE)
                    ));

                    sender.sendMessage(ChatUtil.replacePlaceholders(
                        "Créditos quitados exitosamente.",
                        new Placeholder("{amount}", MoneyUtil.format(amount)),
                        new Placeholder("{target}", targetPlayerName)
                    ));

                    Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
                    if (targetPlayer != null) {
                        targetPlayer.sendMessage(ChatUtil.replacePlaceholders(
                            "Se te han quitado {amount} créditos.",
                            new Placeholder("{amount}", MoneyUtil.format(amount))
                        ));
                    }
                } else {
                    sender.sendMessage("No se pudo quitar los créditos. Verifica que el jugador esté disponible o tenga suficientes créditos.");
                }
            });
            return true;
        }
        return false;
    }
}
