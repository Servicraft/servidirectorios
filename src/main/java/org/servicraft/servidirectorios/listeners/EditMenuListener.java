package org.servicraft.servidirectorios.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import org.servicraft.servidirectorios.gui.EditListGUI;
import org.servicraft.servidirectorios.gui.EditSlotGUI;
import org.servicraft.servidirectorios.model.Shortcut;
import org.servicraft.servidirectorios.util.Message;

import java.util.HashMap;
import java.util.Map;

public class EditMenuListener implements Listener {
    private final Map<Player, Integer> renameMap = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.equals(Message.EDIT_DIRECTORIES_TITLE.get())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.CHEST) {
                int index = event.getRawSlot();
                Map<Integer, Shortcut> owned = DatabaseManager.getOwnedShortcuts(player.getName());
                int slotIndex = (Integer) owned.keySet().toArray()[index];
                EditSlotGUI.open(player, slotIndex, owned.get(slotIndex));
            }
        } else if (title.equals(Message.EDIT_MENU_TITLE.get())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            int slot = event.getRawSlot();
            int slotIndex = EditSlotGUI.getEditingSlot(player);
            if (slot == 11) {
                renameMap.put(player, slotIndex);
                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "Ingresa nombre|descripcion en el chat");
            } else if (slot == 13) {
                DatabaseManager.updateShortcutBySlot(slotIndex, DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex).getName(), DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex).getDescription(), player.getLocation());
                EditSlotGUI.open(player, slotIndex, DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex));
            } else if (slot == 14) {
                Shortcut sc = DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex);
                player.teleport(sc.getLocation());
            } else if (slot == 16) {
                BuySlotGUI.open(player, BuySlotGUI.getCurrentPage(player));
            } else if (slot == 26) {
                BuySlotGUI.open(player, 1);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (renameMap.containsKey(player)) {
            int slotIndex = renameMap.remove(player);
            event.setCancelled(true);
            String msg = ChatColor.translateAlternateColorCodes('&', event.getMessage());
            String[] parts = msg.split("\\|", 2);
            String name = parts[0];
            String desc = parts.length > 1 ? parts[1] : "";
            Shortcut sc = DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex);
            DatabaseManager.updateShortcutBySlot(slotIndex, name, desc, sc.getLocation());
            player.sendMessage(ChatColor.GREEN + "Actualizado");
            EditSlotGUI.open(player, slotIndex, DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex));
        }
    }
}
