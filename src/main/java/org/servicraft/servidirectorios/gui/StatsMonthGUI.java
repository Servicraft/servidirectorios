package org.servicraft.servidirectorios.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.util.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsMonthGUI {
    private static final Map<UUID, Integer> currentSlot = new HashMap<>();
    private static final Map<UUID, Integer> currentYear = new HashMap<>();
    private static final Map<UUID, Map<Integer, Integer>> monthMap = new HashMap<>();

    public static void open(Player player, int slotIndex, int year) {
        currentSlot.put(player.getUniqueId(), slotIndex);
        currentYear.put(player.getUniqueId(), year);
        boolean since = StatsYearGUI.isSincePurchase(player);
        Map<Integer, Integer> data = DatabaseManager.getClicksPerMonth(slotIndex, year, since ? DatabaseManager.getSlotPurchaseTime(slotIndex) : 0L);
        int size = Math.max(9, ((data.size() - 1) / 9 + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, Message.STATS_MONTHS_TITLE.get().replace("{year}", String.valueOf(year)));
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta m = glass.getItemMeta();
            if (m != null) {
                m.setDisplayName(" ");
                glass.setItemMeta(m);
            }
            inv.setItem(i, glass);
        }
        Map<Integer, Integer> slotMap = new HashMap<>();
        int index = 0;
        for (Map.Entry<Integer, Integer> e : data.entrySet()) {
            ItemStack chest = new ItemStack(Material.CHEST);
            ItemMeta meta = chest.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.WHITE + String.valueOf(e.getKey()));
                meta.setLore(java.util.Collections.singletonList(Message.CLICKS_COUNT.get().replace("{count}", String.valueOf(e.getValue()))));
                chest.setItemMeta(meta);
            }
            inv.setItem(index, chest);
            slotMap.put(index, e.getKey());
            index++;
        }
        ItemStack toggle = new ItemStack(since ? Material.RED_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE);
        ItemMeta tm = toggle.getItemMeta();
        if (tm != null) {
            tm.setDisplayName(since ? Message.TOGGLE_SINCE_PURCHASE.get() : Message.TOGGLE_ALL_TIME.get());
            toggle.setItemMeta(tm);
        }
        if (inv.getSize() > 8) inv.setItem(inv.getSize() - 2, toggle); else inv.setItem(7, toggle);

        ItemStack back = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(Message.STATS_BACK.get());
            back.setItemMeta(bm);
        }
        if (inv.getSize() > 8) inv.setItem(inv.getSize() - 1, back); else inv.setItem(8, back);

        monthMap.put(player.getUniqueId(), slotMap);
        player.openInventory(inv);
    }

    public static int getSlotIndex(Player player) {
        return currentSlot.getOrDefault(player.getUniqueId(), -1);
    }

    public static int getYear(Player player) {
        return currentYear.getOrDefault(player.getUniqueId(), 0);
    }

    public static Integer getMonth(Player player, int slot) {
        Map<Integer, Integer> map = monthMap.get(player.getUniqueId());
        if (map == null) return null;
        return map.get(slot);
    }
}
