package org.servicraft.servidirectorios.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.*;

public class BuySlotWeeksGUI {

    private static final Map<UUID, Integer> playerWeeks = new HashMap<>();
    private static final Map<UUID, Double> playerPrice = new HashMap<>();
    private static final Map<UUID, Boolean> playerCredit = new HashMap<>();
    private static final Map<UUID, Integer> playerSlot = new HashMap<>();

    public static void open(Player player, double price, boolean credit, int slotIndex) {
        playerWeeks.put(player.getUniqueId(), 1);
        playerPrice.put(player.getUniqueId(), price);
        playerCredit.put(player.getUniqueId(), credit);
        playerSlot.put(player.getUniqueId(), slotIndex);

        Inventory inv = Bukkit.createInventory(null, 27, "Comprar puesto");
        inv.setItem(10, buildItem(Material.IRON_NUGGET, ChatColor.RED + "Reducir 1 semana", null));
        inv.setItem(12, buildPayItem(player));
        inv.setItem(14, buildItem(Material.GOLD_NUGGET, ChatColor.GREEN + "Incrementar una semana", null));

        fillEmptySlots(inv, Material.BLACK_STAINED_GLASS_PANE, " ");

        player.openInventory(inv);
    }

    private static ItemStack buildItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillEmptySlots(Inventory inv, Material material, String displayName) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);
                    item.setItemMeta(meta);
                }
                inv.setItem(i, item);
            }
        }
    }

    private static ItemStack buildPayItem(Player player) {
        int weeks = playerWeeks.getOrDefault(player.getUniqueId(), 1);
        double price = playerPrice.getOrDefault(player.getUniqueId(), 0.0);
        boolean credit = playerCredit.getOrDefault(player.getUniqueId(), false);
        double total = price * weeks;

        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Pagar");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Resumen del pedido:");
            lore.add(" ");
            lore.add(ChatColor.GRAY + String.valueOf(weeks) + " semanas");
            if (credit) {
                String formatted = String.format(Locale.US, "%.2f", total).replace('.', ',');
                lore.add(ChatColor.AQUA + formatted + " créditos");
            } else {
                String formatted = NumberFormat.getInstance(Locale.GERMAN).format(total);
                lore.add(ChatColor.GREEN + "$" + formatted + ChatColor.GREEN + " servi" + ChatColor.DARK_GREEN + "dólares");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void incrementWeeks(Player player, Inventory inv) {
        int weeks = playerWeeks.getOrDefault(player.getUniqueId(), 1);
        weeks++;
        playerWeeks.put(player.getUniqueId(), weeks);
        inv.setItem(12, buildPayItem(player));
    }

    public static void decrementWeeks(Player player, Inventory inv) {
        int weeks = playerWeeks.getOrDefault(player.getUniqueId(), 1);
        if (weeks > 1) {
            weeks--;
            playerWeeks.put(player.getUniqueId(), weeks);
            inv.setItem(12, buildPayItem(player));
        }
    }

    public static int getWeeks(Player player) {
        return playerWeeks.getOrDefault(player.getUniqueId(), 1);
    }

    public static double getPrice(Player player) {
        return playerPrice.getOrDefault(player.getUniqueId(), 0.0);
    }

    public static boolean isCredit(Player player) {
        return playerCredit.getOrDefault(player.getUniqueId(), false);
    }

    public static int getSlot(Player player) {
        return playerSlot.getOrDefault(player.getUniqueId(), -1);
    }
}
