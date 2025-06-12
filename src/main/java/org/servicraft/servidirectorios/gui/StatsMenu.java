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

import java.util.*;

public class StatsMenu {
    private static final Map<UUID, Map<Integer, Integer>> yearSlots = new HashMap<>();
    private static final Map<UUID, Map<Integer, Integer>> monthSlots = new HashMap<>();
    private static final Map<UUID, Integer> currentYear = new HashMap<>();

    public static void openYears(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Message.STATS_YEARS_TITLE.get());
        Map<Integer, Integer> map = new HashMap<>();
        int index = 0;
        for (int year : DatabaseManager.getYears()) {
            int clicks = DatabaseManager.getYearTotal(year);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + Message.STATS_YEAR_NAME.get().replace("{year}", String.valueOf(year)));
                meta.setLore(Collections.singletonList(ChatColor.GRAY + Message.STATS_CLICKS.get().replace("{clicks}", String.valueOf(clicks))));
                item.setItemMeta(meta);
            }
            inv.setItem(index, item);
            map.put(index, year);
            index++;
        }
        fill(inv);
        yearSlots.put(player.getUniqueId(), map);
        player.openInventory(inv);
    }

    public static void openMonths(Player player, int year) {
        Inventory inv = Bukkit.createInventory(null, 27, Message.STATS_MONTHS_TITLE.get().replace("{year}", String.valueOf(year)));
        Map<Integer, Integer> map = new HashMap<>();
        int start = year == 2025 ? 6 : 1;
        int slot = 0;
        for (int m = start; m <= 12; m++) {
            int clicks = DatabaseManager.getClicks(year, m);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String monthName = Message.valueOf("MONTH_" + m).get();
                meta.setDisplayName(ChatColor.GREEN + monthName);
                meta.setLore(Collections.singletonList(ChatColor.GRAY + Message.STATS_CLICKS.get().replace("{clicks}", String.valueOf(clicks))));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            map.put(slot, m);
            slot++;
        }
        ItemStack back = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
        ItemMeta meta = back.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.STATS_BACK.get());
            back.setItemMeta(meta);
        }
        inv.setItem(26, back);
        fill(inv);
        monthSlots.put(player.getUniqueId(), map);
        currentYear.put(player.getUniqueId(), year);
        player.openInventory(inv);
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta m = glass.getItemMeta();
                if (m != null) {
                    m.setDisplayName(" ");
                    glass.setItemMeta(m);
                }
                inv.setItem(i, glass);
            }
        }
    }

    public static Integer getYear(Player player, int slot) {
        Map<Integer, Integer> map = yearSlots.get(player.getUniqueId());
        if (map == null) return null;
        return map.get(slot);
    }

    public static Integer getMonth(Player player, int slot) {
        Map<Integer, Integer> map = monthSlots.get(player.getUniqueId());
        if (map == null) return null;
        return map.get(slot);
    }

    public static int getCurrentYear(Player player) {
        return currentYear.getOrDefault(player.getUniqueId(), 2025);
    }

    public static void clear(Player player) {
        yearSlots.remove(player.getUniqueId());
        monthSlots.remove(player.getUniqueId());
        currentYear.remove(player.getUniqueId());
    }
}
