package org.servicraft.servidirectorios.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import org.servicraft.servidirectorios.gui.BuySlotWeeksGUI;
import org.servicraft.servidirectorios.gui.EditSlotGUI;
import org.servicraft.servidirectorios.util.Message;

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
                if (displayName.contains(Message.NEXT_PAGE.get())) {
                    int nextPage = currentPage + 1;
                    player.sendMessage(Message.CHANGING_PAGE.get().replace("{page}", String.valueOf(nextPage)));
                    BuySlotGUI.open(player, nextPage);
                } else if (displayName.contains(Message.PREVIOUS_PAGE.get())) {
                    int prevPage = Math.max(1, currentPage - 1);
                    player.sendMessage(Message.RETURNING_PAGE.get().replace("{page}", String.valueOf(prevPage)));
                    BuySlotGUI.open(player, prevPage);
                }
                return;
            }
            
            // Abrir el selector de semanas solo en puestos disponibles o editar si es tuyo
            if (meta.hasLore() && meta.getLore().stream().anyMatch(line -> line.contains(Message.SLOT_CLICK_TO_BUY_1.get()) || line.contains(Message.CLICK_TO_EDIT.get()))) {
                int slot = event.getRawSlot();
              
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

                String owner = org.servicraft.servidirectorios.database.DatabaseManager.getSlotOwner(priceIndex);
                if (owner != null && owner.equalsIgnoreCase(player.getName())) {
                    org.servicraft.servidirectorios.model.Shortcut sc = org.servicraft.servidirectorios.database.DatabaseManager.getOwnedShortcuts(owner).get(priceIndex);
                    org.servicraft.servidirectorios.gui.EditSlotGUI.open(player, priceIndex, sc);
                } else {
                    BuySlotWeeksGUI.open(player, cost, credit, priceIndex);
                }
            }
        }
    }
}
