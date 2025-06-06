package org.servicraft.servidirectorios.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.servicraft.servidirectorios.gui.ShortcutMenu;
import org.servicraft.servidirectorios.gui.EditSlotGUI;
import org.servicraft.servidirectorios.model.Shortcut;
import org.servicraft.servidirectorios.util.Message;

public class ShortcutMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Message.DIRECTORIES_TITLE.get())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= event.getInventory().getSize()) {
                return;
            }

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            if (item.getType() == Material.MAGENTA_GLAZED_TERRACOTTA) {
                int page = ShortcutMenu.getCurrentPage(player);
                if (item.getItemMeta() != null && item.getItemMeta().getDisplayName().contains(Message.NEXT_PAGE.get())) {
                    ShortcutMenu.open(player, page + 1);
                } else if (item.getItemMeta() != null && item.getItemMeta().getDisplayName().contains(Message.PREVIOUS_PAGE.get())) {
                    ShortcutMenu.open(player, Math.max(1, page - 1));
                }
                return;
            }

            Shortcut sc = ShortcutMenu.getShortcut(player, slot);
            if (sc != null) {
                player.closeInventory();
                if (ShortcutMenu.isAdminViewer(player)) {
                    int index = ShortcutMenu.getSlotIndex(player, slot);
                    ShortcutMenu.clearAdmin(player);
                    org.servicraft.servidirectorios.gui.EditSlotGUI.openAdmin(player, index, sc);
                } else {
                    player.teleport(sc.getLocation());
                    player.sendMessage(Message.TELEPORTED_TO.get().replace("{name}", sc.getName()));
                }
            }
        }
    }
}
