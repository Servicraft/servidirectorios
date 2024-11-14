package com.servicraft.servidirectorios.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import com.servicraft.servidirectorios.Servidirectorios;
import com.servicraft.servidirectorios.utils.ConfigManager;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TiendasMenu {

    private Player player;
    private int page;
    private Inventory inventory;
    private ConfigManager configManager;

    public TiendasMenu(Player player) {
        this.player = player;
        this.page = 0;
        this.configManager = Servidirectorios.getInstance().getConfigManager();
    }

    public void open() {
        // Crear el inventario
        inventory = Bukkit.createInventory(null, 54, "§aTiendas");

        // Agregar ítems al inventario
        setupMenu();

        // Abrir el inventario al jugador
        player.openInventory(inventory);
    }

    private void setupMenu() {
        // Obtener configuración
        boolean creditSlotsStay = configManager.getConfig().getBoolean("credit-slots-stay-on-page-switch");
        int creditStart = configManager.getConfig().getInt("credit-slots.start");
        int creditEnd = configManager.getConfig().getInt("credit-slots.end");
        int servidolarStart = configManager.getConfig().getInt("servidolar-slots.start");
        int servidolarEnd = configManager.getConfig().getInt("servidolar-slots.end");

        // Agregar puestos de créditos
        if (creditSlotsStay || page == 0) {
            for (int i = creditStart; i <= creditEnd; i++) {
                int slotIndex = i; // Ajustar según sea necesario
                ItemStack item = createSlotItem(i, "credit");
                inventory.setItem(slotIndex, item);
            }
        }

        // Agregar puestos de servidólares
        int itemsPerPage = 36; // Ajustar según el diseño del inventario
        int startIndex = page * itemsPerPage + servidolarStart;
        int endIndex = Math.min(startIndex + itemsPerPage, servidolarEnd);

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex + 18; // Ajustar según sea necesario
            ItemStack item = createSlotItem(i, "servidolar");
            inventory.setItem(slotIndex, item);
        }

        // Botón de navegación (Shulker Box Púrpura)
        ItemStack navigationItem = new ItemStack(Material.PURPLE_SHULKER_BOX);
        ItemMeta navMeta = navigationItem.getItemMeta();
        navMeta.setDisplayName("§dPágina siguiente");
        navigationItem.setItemMeta(navMeta);
        inventory.setItem(53, navigationItem);
    }

    private ItemStack createSlotItem(int slotNumber, String currencyType) {
        boolean isPurchased = isSlotPurchased(slotNumber);
        ItemStack item;

        if (isPurchased) {
            item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6Puesto #" + slotNumber);
            List<String> lore = new ArrayList<>();
            lore.add("§aEste puesto está comprado.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        } else {
            item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§5Puesto disponible");
            List<String> lore = new ArrayList<>();
            double price = configManager.getConfig().getDouble("slot-prices." + slotNumber);
            lore.add("§ePrecio: " + price + " " + (currencyType.equals("credit") ? "créditos" : "servidólares"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private boolean isSlotPurchased(int slotNumber) {
        // Aquí implementas la lógica para verificar si el puesto está comprado en la base de datos
        // Retorna true si está comprado, false si no
        return false;
    }
}
