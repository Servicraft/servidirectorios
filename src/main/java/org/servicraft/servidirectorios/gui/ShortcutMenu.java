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
        List<Shortcut> shortcuts = DatabaseManager.getShortcuts();
        Inventory inv = Bukkit.createInventory(null, 27, "Directorios");

        int index = 0;
        for (Shortcut sc : shortcuts) {
            if (index >= inv.getSize()) break;
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + sc.getName());
                meta.setLore(java.util.Arrays.asList(ChatColor.GRAY + sc.getDescription()));
                item.setItemMeta(meta);
            }
            inv.setItem(index, item);
            index++;
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
