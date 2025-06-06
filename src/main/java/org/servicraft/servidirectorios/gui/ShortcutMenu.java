package org.servicraft.servidirectorios.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.model.Shortcut;

import java.util.List;

public class ShortcutMenu {

    public static void open(Player player) {
        java.util.Map<Integer, Shortcut> shortcuts = DatabaseManager.getActiveShortcutMap();
        Inventory inv = Bukkit.createInventory(null, 27, org.servicraft.servidirectorios.util.Message.DIRECTORIES_TITLE.get());

        for (java.util.Map.Entry<Integer, Shortcut> entry : shortcuts.entrySet()) {
            int slot = entry.getKey();
            if (slot >= inv.getSize()) continue;
            Shortcut sc = entry.getValue();
            ItemStack item = new ItemStack(sc.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + sc.getName());
                java.util.List<String> lore = java.util.Arrays.stream(sc.getDescription().split("\\n"))
                        .map(s -> ChatColor.GRAY + s)
                        .collect(java.util.stream.Collectors.toList());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
        }

        // fill rest with decorative glass
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = glass.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(" ");
                    glass.setItemMeta(meta);
                }
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }
}
