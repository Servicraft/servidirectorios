package org.servicraft.servidirectorios.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.servicraft.servidirectorios.Servidirectorios;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import net.milkbowl.vault.economy.Economy;

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
                // Determinar la fila (1-based) del item a partir del slot
                int slot = event.getRawSlot();
                int row = (slot / 9) + 1;
                
                // Obtener el costo desde la lore; se asume que la línea de costo es la segunda línea
                String costLine = "";
                if (meta.hasLore() && meta.getLore().size() > 1) {
                    costLine = meta.getLore().get(1);
                }
                // Se espera un formato: "Costo semanal: X créditos" o "Costo semanal: X servidólares"
                double cost = 0;
                try {
                    String[] parts = costLine.split(":");
                    if (parts.length >= 2) {
                        String costPart = parts[1].trim().split(" ")[0];
                        costPart = costPart.replace(",", "."); // Asegurar formato decimal
                        cost = Double.parseDouble(costPart);
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(ChatColor.RED + "Error al procesar el costo del puesto.");
                    return;
                }
                
                // Procesar el pago según el sistema de moneda
                if (row == 1) {
                    // Pago con créditos utilizando LeaderOS
                    player.sendMessage(ChatColor.GREEN + "Procesando compra con créditos...");
                    final Player targetPlayer = player;
                    final double transactionCost = cost;
                    Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> {
                        boolean success;
                        try {
                            // Se quitan créditos directamente utilizando la API de LeaderOS
                            success = net.leaderos.plugin.api.LeaderOSAPI.getCreditManager().remove(targetPlayer.getName(), transactionCost);
                        } catch (Exception e) {
                            e.printStackTrace();
                            success = false;
                        }
                        if (success) {
                            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> {
                                // Aquí se debe actualizar el estado del puesto en MySQL y actualizar la GUI (cambiar a estado ocupado)
                                targetPlayer.sendMessage(ChatColor.GREEN + "Compra exitosa. Se te han descontado " + transactionCost + " créditos.");
                            });
                        } else {
                            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("servidirectorios"), () -> {
                                targetPlayer.sendMessage(ChatColor.RED + "No tienes suficientes créditos o ocurrió un error.");
                            });
                        }
                    });
                } else if (row == 2 || row == 3) {
                    // Pago con servidólares utilizando Vault
                    player.sendMessage(ChatColor.GREEN + "Procesando compra con servidólares...");
                    Economy economy = Servidirectorios.getEconomy();
                    if (economy == null) {
                        player.sendMessage(ChatColor.RED + "El sistema de economía no está disponible.");
                        return;
                    }
                    if (economy.getBalance(player) >= cost) {
                        net.milkbowl.vault.economy.EconomyResponse response = economy.withdrawPlayer(player, cost);
                        if (response.transactionSuccess()) {
                            // Actualizar el estado del puesto en MySQL (placeholder) y la GUI
                            player.sendMessage(ChatColor.GREEN + "Compra exitosa. Se te han descontado " + cost + " servidólares.");
                        } else {
                            player.sendMessage(ChatColor.RED + "No se pudo completar la transacción.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "No tienes suficientes servidólares.");
                    }
                }
            }
        }
    }
}
