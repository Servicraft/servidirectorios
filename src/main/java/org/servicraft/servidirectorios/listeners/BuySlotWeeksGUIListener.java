package org.servicraft.servidirectorios.listeners;

import org.bukkit.ChatColor;
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
        if (slot == 10) {
            BuySlotWeeksGUI.decrementWeeks(player, inv);
        } else if (slot == 14) {
            BuySlotWeeksGUI.incrementWeeks(player, inv);
        } else if (slot == 12) {
            int weeks = BuySlotWeeksGUI.getWeeks(player);
            double price = BuySlotWeeksGUI.getPrice(player) * weeks;
            boolean credit = BuySlotWeeksGUI.isCredit(player);
            int slotIndex = BuySlotWeeksGUI.getSlot(player);
            if (credit) {
                player.sendMessage(ChatColor.GREEN + "Procesando compra con créditos...");
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
                        org.servicraft.servidirectorios.database.DatabaseManager.purchaseSlot(slotIndex, weeks, target.getName(), target.getLocation());
                        org.bukkit.Bukkit.getScheduler().runTask(org.bukkit.Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> target.sendMessage(ChatColor.GREEN + "Compra exitosa."));
                    } else {
                        org.bukkit.Bukkit.getScheduler().runTask(org.bukkit.Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> target.sendMessage(ChatColor.RED + "No tienes suficientes créditos o ocurrió un error."));
                    }
                });
            } else {
                Economy econ = Servidirectorios.getEconomy();
                if (econ == null) {
                    player.sendMessage(ChatColor.RED + "El sistema de economía no está disponible.");
                    return;
                }
                if (econ.getBalance(player) >= price) {
                    net.milkbowl.vault.economy.EconomyResponse resp = econ.withdrawPlayer(player, price);
                    if (resp.transactionSuccess()) {
                        org.servicraft.servidirectorios.database.DatabaseManager.purchaseSlot(slotIndex, weeks, player.getName(), player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Compra exitosa.");
                    } else {
                        player.sendMessage(ChatColor.RED + "No se pudo completar la transacción.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "No tienes suficientes servidólares.");
                }
            }
            player.closeInventory();
        }
    }
}
