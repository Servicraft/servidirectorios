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

import java.util.*;

public class ShortcutMenu {

    private static final Set<UUID> adminViewers = new HashSet<>();
    private static final Map<UUID, Integer> playerPages = new HashMap<>();
    private static final Map<UUID, Map<Integer, Shortcut>> playerShortcutMap = new HashMap<>();
    private static final Map<UUID, Map<Integer, Integer>> playerSlotIndexMap = new HashMap<>();

    public static void open(Player player) {
        open(player, 1);
    }

    public static void open(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);

        Map<Integer, Shortcut> all = DatabaseManager.getActiveShortcutMap();
        Inventory inv = Bukkit.createInventory(null, 27, org.servicraft.servidirectorios.util.Message.DIRECTORIES_TITLE.get());

        org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.plugin.java.JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class).getConfig();
        int creditStart = cfg.getInt("credit-slots.start");
        int creditEnd = cfg.getInt("credit-slots.end");
        int servStart = cfg.getInt("servidolar-slots.start");
        int servEnd = cfg.getInt("servidolar-slots.end");
        int servSlotsPerPage = servEnd - servStart + 1;

        Map<Integer, Shortcut> currentMap = new HashMap<>();
        Map<Integer, Integer> slotIndexMap = new HashMap<>();

        for (int slot = 0; slot < 26; slot++) {
            int index = slot;
            if (slot >= servStart && slot <= servEnd && page > 1) {
                index = slot + servSlotsPerPage * (page - 1);
            }
            Shortcut sc = all.get(index);
            if (sc == null) continue;

            ItemStack item = new ItemStack(sc.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + sc.getName());
                List<String> lore = Arrays.stream(sc.getDescription().split("\\n"))
                        .map(s -> ChatColor.GRAY + s)
                        .collect(java.util.stream.Collectors.toList());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            currentMap.put(slot, sc);
            slotIndexMap.put(slot, index);
        }

        ItemStack magenta = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
        ItemMeta mm = magenta.getItemMeta();
        if (mm != null) {
            if (page <= 1) {
                mm.setDisplayName(ChatColor.WHITE + org.servicraft.servidirectorios.util.Message.NEXT_PAGE.get());
            } else {
                mm.setDisplayName(ChatColor.WHITE + org.servicraft.servidirectorios.util.Message.PREVIOUS_PAGE.get());
            }
            magenta.setItemMeta(mm);
        }

        if (page <= 1) {
            inv.setItem(26, magenta);
        } else {
            inv.setItem(18, magenta);
        }

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

        playerShortcutMap.put(player.getUniqueId(), currentMap);
        playerSlotIndexMap.put(player.getUniqueId(), slotIndexMap);
        player.openInventory(inv);
    }

    public static int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }

    public static Shortcut getShortcut(Player player, int slot) {
        Map<Integer, Shortcut> map = playerShortcutMap.get(player.getUniqueId());
        if (map == null) return null;
        return map.get(slot);
    }

    public static int getSlotIndex(Player player, int slot) {
        Map<Integer, Integer> map = playerSlotIndexMap.get(player.getUniqueId());
        if (map == null) return slot;
        return map.getOrDefault(slot, slot);
    }

    public static void openAdmin(Player player) {
        adminViewers.add(player.getUniqueId());
        open(player, 1);
    }

    public static boolean isAdminViewer(Player player) {
        return adminViewers.contains(player.getUniqueId());
    }

    public static void clearAdmin(Player player) {
        adminViewers.remove(player.getUniqueId());
    }
}
