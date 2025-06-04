package org.servicraft.servidirectorios.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.model.Shortcut;

import java.util.List;

public class ShortcutMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Directorios")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            String name = ChatColor.stripColor(meta.getDisplayName());
            List<Shortcut> shortcuts = DatabaseManager.getShortcuts();
            for (Shortcut sc : shortcuts) {
                if (sc.getName().equalsIgnoreCase(name)) {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.teleport(sc.getLocation());
                    player.sendMessage(ChatColor.AQUA + "Teletransportado a " + sc.getName());
                    return;
                }
            }
        }
    }
}
