package org.servicraft.servidirectorios.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.model.Shortcut;
import org.servicraft.servidirectorios.util.Message;

import java.util.Map;

public class EditListGUI {
    public static void open(Player player) {
        Map<Integer, Shortcut> owned = DatabaseManager.getOwnedShortcuts(player.getName());
        int size = ((owned.size() - 1) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, Math.max(9, size), Message.EDIT_DIRECTORIES_TITLE.get());
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = glass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(" ");
                glass.setItemMeta(meta);
            }
            inv.setItem(i, glass);
        }
        int index = 0;
        for (Map.Entry<Integer, Shortcut> e : owned.entrySet()) {
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(e.getValue().getName());
                meta.setLore(java.util.Arrays.asList(e.getValue().getDescription()));
                item.setItemMeta(meta);
            }
            inv.setItem(index, item);
            index++;
        }
        player.openInventory(inv);
    }
}
