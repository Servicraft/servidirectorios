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

    private static final Map<UUID, Integer> playerPages = new HashMap<>();
    private static final Map<UUID, Map<Integer, Shortcut>> playerShortcutMap = new HashMap<>();

    public static void open(Player player) {
        open(player, 1);
    }

    public static void open(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);

        Map<Integer, Shortcut> all = DatabaseManager.getActiveShortcutMap();
        List<Shortcut> list = new ArrayList<>(all.values());

        Inventory inv = Bukkit.createInventory(null, 27,
                org.servicraft.servidirectorios.util.Message.DIRECTORIES_TITLE.get());

        Map<Integer, Shortcut> currentMap = new HashMap<>();

        int start = (page - 1) * 26;
        for (int i = 0; i < 26 && start + i < list.size(); i++) {
            int slot = i;
            if (page == 2 && slot >= 18) slot++; // leave slot 18 for navigation

            Shortcut sc = list.get(start + i);
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
        }

        ItemStack magenta = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
        ItemMeta mm = magenta.getItemMeta();
        if (mm != null) {
            if (page == 1) {
                mm.setDisplayName(ChatColor.WHITE + org.servicraft.servidirectorios.util.Message.NEXT_PAGE.get());
            } else {
                mm.setDisplayName(ChatColor.WHITE + org.servicraft.servidirectorios.util.Message.PREVIOUS_PAGE.get());
            }
            magenta.setItemMeta(mm);
        }

        if (page == 1 && list.size() > 26) {
            inv.setItem(26, magenta);
        } else if (page == 2) {
            inv.setItem(18, magenta);
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

        playerShortcutMap.put(player.getUniqueId(), currentMap);
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
}
