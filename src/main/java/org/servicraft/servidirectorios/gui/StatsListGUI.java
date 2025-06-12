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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsListGUI {
    private static final Map<UUID, Map<Integer, Integer>> playerSlotMap = new HashMap<>();

    public static void open(Player player) {
        Map<Integer, Shortcut> owned = DatabaseManager.getOwnedShortcuts(player.getName());
        int size = ((owned.size() - 1) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, Math.max(9, size), Message.STATS_TITLE.get());
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = glass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(" ");
                glass.setItemMeta(meta);
            }
            inv.setItem(i, glass);
        }
        Map<Integer, Integer> slotMap = new HashMap<>();
        int index = 0;
        for (Map.Entry<Integer, Shortcut> e : owned.entrySet()) {
            ItemStack item = new ItemStack(e.getValue().getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(e.getValue().getName());
                meta.setLore(java.util.Collections.singletonList(Message.CLICK_TO_VIEW.get()));
                item.setItemMeta(meta);
            }
            inv.setItem(index, item);
            slotMap.put(index, e.getKey());
            index++;
        }
        playerSlotMap.put(player.getUniqueId(), slotMap);
        player.openInventory(inv);
    }

    public static int getSlotIndex(Player player, int slot) {
        Map<Integer, Integer> map = playerSlotMap.get(player.getUniqueId());
        if (map == null) return -1;
        return map.getOrDefault(slot, -1);
    }
}
