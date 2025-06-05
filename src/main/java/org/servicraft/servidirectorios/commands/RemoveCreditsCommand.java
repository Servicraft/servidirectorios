package org.servicraft.servidirectorios.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.leaderos.plugin.api.LeaderOSAPI;
import net.leaderos.plugin.api.handlers.UpdateCacheEvent;
import net.leaderos.plugin.helpers.ChatUtil;
import net.leaderos.shared.helpers.MoneyUtil;
import net.leaderos.shared.helpers.Placeholder;
import net.leaderos.shared.modules.credit.enums.UpdateType;
import org.bukkit.Bukkit;
import org.servicraft.servidirectorios.util.Message;

public class RemoveCreditsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public RemoveCreditsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("removecredits")) {
            if (args.length != 2) {
                sender.sendMessage(Message.USAGE_REMOVECREDITS.get());
                return false;
            }

            String targetPlayerName = args[0];
            double amount;

            try {
                amount = MoneyUtil.parseDouble(Double.parseDouble(args[1]));
            } catch (NumberFormatException e) {
                sender.sendMessage(Message.INVALID_CREDIT_AMOUNT.get());
                return false;
            }

            if (amount <= 0) {
                sender.sendMessage(Message.AMOUNT_GREATER_ZERO.get());
                return false;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean isRemoved = LeaderOSAPI.getCreditManager().remove(targetPlayerName, amount);

                if (isRemoved) {
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(
                        new UpdateCacheEvent(targetPlayerName, amount, UpdateType.REMOVE)
                    ));

                    sender.sendMessage(ChatUtil.replacePlaceholders(
                        Message.CREDITS_REMOVED_SENDER.get(),
                        new Placeholder("{amount}", MoneyUtil.format(amount)),
                        new Placeholder("{target}", targetPlayerName)
                    ));

                    Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
                    if (targetPlayer != null) {
                        targetPlayer.sendMessage(ChatUtil.replacePlaceholders(
                            Message.CREDITS_REMOVED_TARGET.get(),
                            new Placeholder("{amount}", MoneyUtil.format(amount))
                        ));
                    }
                } else {
                    sender.sendMessage(Message.CREDITS_REMOVE_FAILED.get());
                }
            });
            return true;
        }
        return false;
    }
}
