package org.servicraft.servidirectorios.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.servicraft.servidirectorios.gui.StatsMenu;
import org.servicraft.servidirectorios.util.Message;

public class StatsMenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.equals(Message.STATS_YEARS_TITLE.get())) {
            event.setCancelled(true);
            Integer year = StatsMenu.getYear(player, event.getRawSlot());
            if (year != null) {
                StatsMenu.openMonths(player, year);
            }
        } else if (title.startsWith(Message.STATS_MONTHS_TITLE.get().split("{year}")[0])) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 26) {
                StatsMenu.openYears(player);
                return;
            }
            Integer month = StatsMenu.getMonth(player, slot);
            if (month != null) {
                // no further action
            }
        }
    }
}
