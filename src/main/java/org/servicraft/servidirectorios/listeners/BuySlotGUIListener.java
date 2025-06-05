package org.servicraft.servidirectorios.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import org.servicraft.servidirectorios.gui.BuySlotWeeksGUI;

public class BuySlotGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Verificar que el inventario sea el de puestos promocionados
        if (event.getView().getTitle().startsWith("Puestos promocionados")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                return;
            }
            String displayName = meta.getDisplayName();
            
            // Si se hace clic en el botón de paginación (azulejo magenta)
            if (clickedItem.getType() == Material.MAGENTA_GLAZED_TERRACOTTA) {
                int currentPage = BuySlotGUI.getCurrentPage(player);
                if (displayName.contains("Siguiente página")) {
                    int nextPage = currentPage + 1;
                    player.sendMessage(ChatColor.AQUA + "Cambiando a la página " + nextPage + " de puestos promocionados...");
                    BuySlotGUI.open(player, nextPage);
                } else if (displayName.contains("Página anterior")) {
                    int prevPage = Math.max(1, currentPage - 1);
                    player.sendMessage(ChatColor.AQUA + "Volviendo a la página " + prevPage + " de puestos promocionados...");
                    BuySlotGUI.open(player, prevPage);
                }
                return;
            }
            
            // Procesar la compra si el item posee la acción de compra (se busca la línea "Haz clic para comprar este puesto!")
            if (displayName.contains("¡Disponible!") || (meta.hasLore() && meta.getLore().stream().anyMatch(line -> line.contains("Haz clic para comprar este puesto!")))) {
                int slot = event.getRawSlot();
                int row = (slot / 9) + 1;

                org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.plugin.java.JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class).getConfig();
                int creditStart = cfg.getInt("credit-slots.start");
                int creditEnd = cfg.getInt("credit-slots.end");
                int servStart = cfg.getInt("servidolar-slots.start");
                int servEnd = cfg.getInt("servidolar-slots.end");

                int priceIndex = slot;
                int page = BuySlotGUI.getCurrentPage(player);
                int servSlotsPerPage = servEnd - servStart + 1;
                if (slot >= servStart && slot <= servEnd && page > 1) {
                    priceIndex = slot + servSlotsPerPage * (page - 1);
                }

                double cost = cfg.getDouble("slot-prices." + priceIndex, 0.0);
                boolean credit = slot >= creditStart && slot <= creditEnd;

                BuySlotWeeksGUI.open(player, cost, credit);
            }
        }
    }
}
