package org.servicraft.servidirectorios.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.model.Shortcut;
import org.servicraft.servidirectorios.util.Message;

public class ShortcutMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Message.DIRECTORIES_TITLE.get())) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= event.getInventory().getSize()) {
                return;
            }
            java.util.Map<Integer, Shortcut> shortcuts = DatabaseManager.getActiveShortcutMap();
            Shortcut sc = shortcuts.get(slot);
            if (sc != null) {
                Player player = (Player) event.getWhoClicked();
                player.closeInventory();
                player.teleport(sc.getLocation());
                player.sendMessage(Message.TELEPORTED_TO.get().replace("{name}", sc.getName()));
            }
        }
    }
}
