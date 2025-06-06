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
import org.servicraft.servidirectorios.util.Message;

import java.util.HashMap;
import java.util.Map;

public class EditSlotGUI {
    private static final Map<java.util.UUID, Integer> current = new HashMap<>();
    private static final java.util.Set<java.util.UUID> adminEditors = new java.util.HashSet<>();

    public static void open(Player player, int slotIndex, Shortcut sc) {
        open(player, slotIndex, sc, false);
    }

    public static void openAdmin(Player player, int slotIndex, Shortcut sc) {
        adminEditors.add(player.getUniqueId());
        open(player, slotIndex, sc, true);
    }

    private static void open(Player player, int slotIndex, Shortcut sc, boolean admin) {
        current.put(player.getUniqueId(), slotIndex);
        Inventory inv = Bukkit.createInventory(null, 27, Message.EDIT_MENU_TITLE.get());
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta m = glass.getItemMeta();
            if (m != null) {
                m.setDisplayName(" ");
                glass.setItemMeta(m);
            }
            inv.setItem(i, glass);
        }
        ItemStack icon = new ItemStack(sc.getIcon());
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.EDIT_ICON.get());
            icon.setItemMeta(meta);
        }
        inv.setItem(10, icon);

        ItemStack name = new ItemStack(Material.OAK_SIGN);
        meta = name.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.EDIT_NAME_DESCRIPTION.get());
            name.setItemMeta(meta);
        }
        inv.setItem(11, name);

        ItemStack compass = new ItemStack(Material.COMPASS);
        meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.EDIT_PLACE.get());
            String coords = sc.getLocation().getBlockX()+","+sc.getLocation().getBlockY()+","+sc.getLocation().getBlockZ();
            meta.setLore(java.util.Arrays.asList(ChatColor.GRAY + coords));
            compass.setItemMeta(meta);
        }
        inv.setItem(13, compass);

        ItemStack stick = new ItemStack(Material.STICK);
        meta = stick.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.GO_TO_PLACE.get());
            stick.setItemMeta(meta);
        }
        inv.setItem(14, stick);

        ItemStack buyMore = new ItemStack(Material.EXPERIENCE_BOTTLE);
        meta = buyMore.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.BUY_MORE_DAYS.get());
            int days = DatabaseManager.getRemainingDays(slotIndex);
            meta.setLore(java.util.Arrays.asList(Message.REMAINING_DAYS.get().replace("{days}", String.valueOf(days))));
            buyMore.setItemMeta(meta);
        }
        inv.setItem(16, buyMore);

        if (admin) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            meta = barrier.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Message.ADMIN_DELETE_REFUND.get());
                barrier.setItemMeta(meta);
            }
            inv.setItem(22, barrier);
        }

        ItemStack back = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
        meta = back.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.BACK_TO_PROMOTED.get());
            back.setItemMeta(meta);
        }
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    public static int getEditingSlot(Player player) {
        return current.getOrDefault(player.getUniqueId(), -1);
    }

    public static boolean isAdminEditor(Player player) {
        return adminEditors.contains(player.getUniqueId());
    }

    public static void clearAdmin(Player player) {
        adminEditors.remove(player.getUniqueId());
    }
}
