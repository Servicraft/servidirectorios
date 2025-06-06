package org.servicraft.servidirectorios.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.servicraft.servidirectorios.Servidirectorios;
import org.servicraft.servidirectorios.gui.BuySlotWeeksGUI;
import net.milkbowl.vault.economy.Economy;
import org.servicraft.servidirectorios.util.Message;

public class BuySlotWeeksGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Comprar puesto")) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        if (slot == 11) {
            BuySlotWeeksGUI.decrementWeeks(player, inv);
        } else if (slot == 15) {
            BuySlotWeeksGUI.incrementWeeks(player, inv);
        } else if (slot == 13) {
            int weeks = BuySlotWeeksGUI.getWeeks(player);
            double price = BuySlotWeeksGUI.getPrice(player) * weeks;
            boolean credit = BuySlotWeeksGUI.isCredit(player);
            int slotIndex = BuySlotWeeksGUI.getSlot(player);
            if (credit) {
                player.sendMessage(Message.PROCESSING_CREDITS.get());
                final Player target = player;
                final double amount = price;
                org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(org.bukkit.Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> {
                    boolean success;
                    try {
                        success = net.leaderos.plugin.api.LeaderOSAPI.getCreditManager().remove(target.getName(), amount);
                    } catch (Exception e) {
                        e.printStackTrace();
                        success = false;
                    }
                    if (success) {
                        if (target.getName().equalsIgnoreCase(org.servicraft.servidirectorios.database.DatabaseManager.getSlotOwner(slotIndex))) {
                            org.servicraft.servidirectorios.database.DatabaseManager.extendSlot(slotIndex, weeks);
                        } else {
                            org.servicraft.servidirectorios.database.DatabaseManager.purchaseSlot(slotIndex, weeks, target.getName(), target.getLocation());
                        }
                        org.bukkit.Bukkit.getScheduler().runTask(org.bukkit.Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> target.sendMessage(Message.BUY_SUCCESS.get()));
                    } else {
                        org.bukkit.Bukkit.getScheduler().runTask(org.bukkit.Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> target.sendMessage(Message.NOT_ENOUGH_CREDITS.get()));
                    }
                });
            } else {
                Economy econ = Servidirectorios.getEconomy();
                if (econ == null) {
                    player.sendMessage(Message.ECONOMY_NOT_AVAILABLE.get());
                    return;
                }
                if (econ.getBalance(player) >= price) {
                    net.milkbowl.vault.economy.EconomyResponse resp = econ.withdrawPlayer(player, price);
                    if (resp.transactionSuccess()) {
                        if (player.getName().equalsIgnoreCase(org.servicraft.servidirectorios.database.DatabaseManager.getSlotOwner(slotIndex))) {
                            org.servicraft.servidirectorios.database.DatabaseManager.extendSlot(slotIndex, weeks);
                        } else {
                            org.servicraft.servidirectorios.database.DatabaseManager.purchaseSlot(slotIndex, weeks, player.getName(), player.getLocation());
                        }
                        player.sendMessage(Message.BUY_SUCCESS.get());
                    } else {
                        player.sendMessage(Message.TRANSACTION_FAILED.get());
                    }
                } else {
                    player.sendMessage(Message.NOT_ENOUGH_MONEY.get());
                }
            }
            player.closeInventory();
        }
    }
}
