package org.servicraft.servidirectorios.listeners;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.model.Shortcut;
import org.servicraft.servidirectorios.util.Message;

import java.util.List;

public class ShortcutMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Message.DIRECTORIES_TITLE.get())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            String name = ChatColor.stripColor(meta.getDisplayName());
            java.util.Map<Integer, Shortcut> shortcuts = DatabaseManager.getActiveShortcutMap();
            for (Shortcut sc : shortcuts.values()) {
                if (sc.getName().equalsIgnoreCase(name)) {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.teleport(sc.getLocation());
                    player.sendMessage(Message.TELEPORTED_TO.get().replace("{name}", sc.getName()));
                    return;
                }
            }
        }
    }
}
