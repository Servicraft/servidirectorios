package com.servicraft.servidirectorios.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.servicraft.servidirectorios.Servidirectorios;
import com.servicraft.servidirectorios.gui.TiendasMenu;
import net.leaderos.plugin.api.LeaderOSAPI;

import org.bukkit.Material;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§aTiendas")) return;

        event.setCancelled(true); // Prevenir que los jugadores tomen los ítems

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        // Lógica para manejar los clicks en los ítems
        if (clickedItem.getType() == Material.PURPLE_SHULKER_BOX) {
            // Navegar a la siguiente página
            player.closeInventory();
            TiendasMenu menu = new TiendasMenu(player);
            // Aquí incrementas la página actual antes de abrir el menú
            // menu.setPage(menu.getPage() + 1);
            menu.open();
        } else if (clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            // Intentar comprar el puesto
            int slotNumber = event.getSlot(); // Ajustar según cómo almacenes el número de puesto
            handlePurchase(player, slotNumber);
        } else if (clickedItem.getType() == Material.CHEST) {
            // Verificar si el jugador es el propietario
            int slotNumber = event.getSlot(); // Ajustar según sea necesario
            if (isOwner(player, slotNumber)) {
                // Abrir opciones de edición
                player.sendMessage("Abriendo opciones de edición para tu puesto.");
            } else {
                player.sendMessage("Este puesto está ocupado y no te pertenece.");
            }
        }
    }

    private void handlePurchase(Player player, int slotNumber) {
        // Obtener precio y tipo de moneda
        double price = Servidirectorios.getInstance().getConfigManager().getConfig().getDouble("slot-prices." + slotNumber);
        String currencyType = getCurrencyType(slotNumber);

        if (currencyType.equals("credit")) {
            // Usar LeaderOS para procesar el pago con créditos
            boolean success = LeaderOSAPI.getCreditManager().remove(player.getName(), price);
            if (success) {
                // Registrar la compra en la base de datos
                registerPurchase(player, slotNumber);
                player.sendMessage("Has comprado el puesto #" + slotNumber + " con " + price + " créditos.");
            } else {
                player.sendMessage("No tienes suficientes créditos.");
            }
        } else {
            // Usar Vault para procesar el pago con servidólares
            if (Servidirectorios.getEconomy().withdrawPlayer(player, price).transactionSuccess()) {
                // Registrar la compra en la base de datos
                registerPurchase(player, slotNumber);
                player.sendMessage("Has comprado el puesto #" + slotNumber + " con " + price + " servidólares.");
            } else {
                player.sendMessage("No tienes suficientes servidólares.");
            }
        }

        // Actualizar el inventario para reflejar la compra
        player.closeInventory();
        new TiendasMenu(player).open();
    }

    private String getCurrencyType(int slotNumber) {
        int creditStart = Servidirectorios.getInstance().getConfigManager().getConfig().getInt("credit-slots.start");
        int creditEnd = Servidirectorios.getInstance().getConfigManager().getConfig().getInt("credit-slots.end");
        if (slotNumber >= creditStart && slotNumber <= creditEnd) {
            return "credit";
        } else {
            return "servidolar";
        }
    }

    private boolean isOwner(Player player, int slotNumber) {
        // Verificar en la base de datos si el jugador es el propietario del puesto
        return false;
    }

    private void registerPurchase(Player player, int slotNumber) {
        // Registrar la compra en la base de datos MySQL
    }
}
