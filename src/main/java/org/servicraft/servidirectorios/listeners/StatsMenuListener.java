package org.servicraft.servidirectorios.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.servicraft.servidirectorios.gui.*;
import org.servicraft.servidirectorios.util.Message;

public class StatsMenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.equals(Message.STATS_TITLE.get())) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            int slotIndex = StatsListGUI.getSlotIndex(player, slot);
            if (slotIndex >= 0) {
                StatsYearGUI.open(player, slotIndex);
            }
            return;
        }
        if (title.equals(Message.STATS_YEARS_TITLE.get())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            int slot = event.getRawSlot();
            if (item.getType() == Material.BLACK_GLAZED_TERRACOTTA) {
                StatsListGUI.open(player);
                return;
            }
            if (item.getType() == Material.RED_STAINED_GLASS_PANE || item.getType() == Material.LIME_STAINED_GLASS_PANE) {
                StatsYearGUI.open(player, StatsYearGUI.getSlotIndex(player), StatsYearGUI.toggle(player));
                return;
            }
            Integer year = StatsYearGUI.getYear(player, slot);
            if (year != null) {
                StatsMonthGUI.open(player, StatsYearGUI.getSlotIndex(player), year);
            }
            return;
        }
        if (title.startsWith(Message.STATS_MONTHS_TITLE.get().split("{",2)[0])) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            int slot = event.getRawSlot();
            if (item.getType() == Material.BLACK_GLAZED_TERRACOTTA) {
                StatsYearGUI.open(player, StatsMonthGUI.getSlotIndex(player));
                return;
            }
            if (item.getType() == Material.RED_STAINED_GLASS_PANE || item.getType() == Material.LIME_STAINED_GLASS_PANE) {
                StatsMonthGUI.open(player, StatsMonthGUI.getSlotIndex(player), StatsMonthGUI.getYear(player));
                return;
            }
            Integer month = StatsMonthGUI.getMonth(player, slot);
            if (month != null) {
                StatsWeekGUI.open(player, StatsMonthGUI.getSlotIndex(player), StatsMonthGUI.getYear(player), month);
            }
            return;
        }
        if (title.startsWith(Message.STATS_WEEKS_TITLE.get().split("{",2)[0])) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            if (item.getType() == Material.BLACK_GLAZED_TERRACOTTA) {
                StatsMonthGUI.open(player, StatsWeekGUI.getSlotIndex(player), StatsWeekGUI.getYear(player));
                return;
            }
            if (item.getType() == Material.RED_STAINED_GLASS_PANE || item.getType() == Material.LIME_STAINED_GLASS_PANE) {
                StatsWeekGUI.open(player, StatsWeekGUI.getSlotIndex(player), StatsWeekGUI.getYear(player), StatsWeekGUI.getMonth(player));
            }
        }
    }
}
