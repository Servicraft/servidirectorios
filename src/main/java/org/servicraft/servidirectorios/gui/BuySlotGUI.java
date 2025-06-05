package org.servicraft.servidirectorios.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BuySlotGUI {

    // Mapa para almacenar la página actual de cada jugador
    private static Map<UUID, Integer> playerPages = new HashMap<>();

    public static void open(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(null, 54, "Puestos promocionados - Página " + page);

        // Rango de puestos para cada tipo de moneda
        org.bukkit.plugin.java.JavaPlugin plugin =
                org.bukkit.plugin.java.JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class);
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        int creditStart = cfg.getInt("credit-slots.start");
        int creditEnd = cfg.getInt("credit-slots.end");
        int servStart = cfg.getInt("servidolar-slots.start");
        int servEnd = cfg.getInt("servidolar-slots.end");

        // Ejemplo simple de ocupación: los dos primeros puestos de créditos ocupados
        for (int slot = 0; slot < inv.getSize(); slot++) {
            boolean isCredit = slot >= creditStart && slot <= creditEnd;
            boolean isServ = slot >= servStart && slot <= servEnd;
            if (!isCredit && !isServ) continue;

            double price = cfg.getDouble("slot-prices." + slot, 0.0);
            boolean ocupado = isCredit && (slot == creditStart || slot == creditStart + 1);

            Material mat = ocupado ? Material.RED_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
            String nombre = (ocupado ? ChatColor.RED : ChatColor.GREEN) + "Puesto " + (slot + 1);
            if (!ocupado) nombre += " - ¡Disponible!";

            List<String> lore = new ArrayList<>();
            if (ocupado) {
                lore.add(ChatColor.GRAY + "Contrato expira en: " + (slot == creditStart ? 9 : 2) + " días");
            } else {
                lore.add(ChatColor.YELLOW + "Haz clic para comprar este puesto!");
            }
            ChatColor color = isCredit ? ChatColor.BLUE : ChatColor.GREEN;
            String moneda = isCredit ? "créditos" : "servidólares";
            lore.add(color + "Valor semanal: " + price + " " + moneda);

            inv.setItem(slot, buildItem(mat, nombre, lore));
        }

        // Pagination: posición [6,9] → índice 53
        inv.setItem(53, buildItem(Material.MAGENTA_GLAZED_TERRACOTTA,
                ChatColor.LIGHT_PURPLE + "Haz clic para ver más puestos (todos en servidólares)",
                null));

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
