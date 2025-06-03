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
        
        // Hardcodeamos algunos items de ejemplo según la lógica del JSON
        
        // Slot 1: posición [1,1] → índice 0 (créditos)
        setSlot(inv, 1, 1, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Puesto 1", Arrays.asList(
                ChatColor.GRAY + "Contrato expira en: 9 días",
                ChatColor.BLUE + "Costo semanal: 1,25 créditos"
        ));
        
        // Slot 2: posición [1,2] → índice 1 (créditos)
        setSlot(inv, 1, 2, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Puesto 2", Arrays.asList(
                ChatColor.GRAY + "Contrato expira en: 2 días",
                ChatColor.BLUE + "Costo semanal: 1,15 créditos"
        ));
        
        // Slot 3: posición [1,3] → índice 2 (créditos, disponible)
        setSlot(inv, 1, 3, Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Puesto 3 - ¡Disponible!", Arrays.asList(
                ChatColor.YELLOW + "Haz clic para comprar este puesto!",
                ChatColor.BLUE + "Costo semanal: 1,10 créditos"
        ));
        
        // Slot 4: posición [2,3] → índice 11 (servidólares, disponible)
        setSlot(inv, 2, 3, Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Puesto 11", Arrays.asList(
                ChatColor.YELLOW + "Haz clic para comprar este puesto!",
                ChatColor.GREEN + "Costo semanal: 7000 servidólares"
        ));
        
        // Pagination: posición [6,9] → índice 53
        setSlot(inv, 6, 9, Material.MAGENTA_GLAZED_TERRACOTTA, ChatColor.LIGHT_PURPLE + "Haz clic para ver más puestos (todos en servidólares)", null);
        
        // Llenar espacios vacíos con panel de vidrio verde
        fillEmptySlots(inv, Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Panel de vidrio verde, representa espacios libres");
        
        player.openInventory(inv);
    }
    
    private static void setSlot(Inventory inv, int row, int col, Material material, String displayName, List<String> lore) {
        int index = getSlotIndex(row, col);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        inv.setItem(index, item);
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
