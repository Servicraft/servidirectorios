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

public class BuySlotGUI {

    // Mapa para almacenar la página actual de cada jugador
    private static Map<UUID, Integer> playerPages = new HashMap<>();

    public static void open(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(null, 27, Message.PROMOTED_SLOTS_TITLE.get().replace("{page}", String.valueOf(page)));

        // Rango de puestos para cada tipo de moneda
        org.bukkit.plugin.java.JavaPlugin plugin =
                org.bukkit.plugin.java.JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class);
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        int creditStart = cfg.getInt("credit-slots.start");
        int creditEnd = cfg.getInt("credit-slots.end");
        int servStart = cfg.getInt("servidolar-slots.start");
        int servEnd = cfg.getInt("servidolar-slots.end");

        int servSlotsPerPage = servEnd - servStart + 1;

        for (int slot = 0; slot < 26; slot++) {
            boolean isCredit = slot >= creditStart && slot <= creditEnd;
            boolean isServDisplay = slot >= servStart && slot <= servEnd;
            if (!isCredit && !isServDisplay) continue;

            int priceIndex = slot;
            if (isServDisplay && page > 1) {
                priceIndex = slot + servSlotsPerPage * (page - 1);
            }
            double price = cfg.getDouble("slot-prices." + priceIndex, 0.0);
            boolean ocupado = DatabaseManager.isSlotOccupied(priceIndex);
            String owner = DatabaseManager.getSlotOwner(priceIndex);
            int remaining = DatabaseManager.getRemainingDays(priceIndex);

            int displayNumber = slot + 1;
            if (isServDisplay && page > 1) {
                displayNumber = slot + servSlotsPerPage * (page - 1) + 1;
            }

            boolean mine = ocupado && owner != null && owner.equalsIgnoreCase(player.getName());
            Material mat;
            ChatColor color;
            if (!ocupado) {
                mat = Material.LIME_STAINED_GLASS_PANE;
                color = ChatColor.GREEN;
            } else if (mine) {
                mat = Material.ORANGE_STAINED_GLASS_PANE;
                color = ChatColor.GOLD;
            } else {
                mat = Material.RED_STAINED_GLASS_PANE;
                color = ChatColor.RED;
            }
            String nombre = color + Message.SLOT_NAME.get().replace("{number}", String.valueOf(displayNumber));

            List<String> lore = new ArrayList<>();
            if (ocupado) {
                lore.add(ChatColor.GRAY + Message.SLOT_CONTRACT_EXPIRES.get());
                lore.add(ChatColor.GRAY + Message.SLOT_REMAINING_DAYS.get().replace("{days}", String.valueOf(remaining)));
                if (mine) {
                    lore.add(Message.CLICK_TO_EDIT.get());
                }
            } else {
                lore.add(ChatColor.GRAY + Message.SLOT_CLICK_TO_BUY_1.get());
                lore.add(ChatColor.GRAY + Message.SLOT_CLICK_TO_BUY_2.get());
            }
            lore.add(" ");
            lore.add(ChatColor.GRAY + Message.WEEKLY_PRICE.get());
            if (isCredit) {
                String formatted = String.format(java.util.Locale.US, "%.2f", price).replace('.', ',');
                lore.add(ChatColor.AQUA + formatted + " créditos");
            } else {
                String formatted = java.text.NumberFormat.getInstance(java.util.Locale.GERMAN).format(price);
                lore.add(ChatColor.GREEN + "$" + formatted + " servi" + ChatColor.DARK_GREEN + "dólares");
            }

            inv.setItem(slot, buildItem(mat, nombre, lore));
        }

        // Paginación: siguiente o anterior página
        if (page <= 1) {
            inv.setItem(26, buildItem(Material.MAGENTA_GLAZED_TERRACOTTA,
                    ChatColor.WHITE + Message.NEXT_PAGE.get(),
                    null));
        } else {
            inv.setItem(18, buildItem(Material.MAGENTA_GLAZED_TERRACOTTA,
                    ChatColor.WHITE + Message.PREVIOUS_PAGE.get(),
                    null));
        }

        // Llenar espacios vacíos con panel de vidrio verde
        fillEmptySlots(inv, Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + " ");

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
    
    private static int getSlotIndex(int row, int col) {
        return (row - 1) * 9 + (col - 1);
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
    
    public static int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }
}
