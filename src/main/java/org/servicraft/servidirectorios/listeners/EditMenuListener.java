package org.servicraft.servidirectorios.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.servicraft.servidirectorios.database.DatabaseManager;
import org.servicraft.servidirectorios.gui.BuySlotGUI;
import org.servicraft.servidirectorios.gui.BuySlotWeeksGUI;
import org.servicraft.servidirectorios.gui.EditListGUI;
import org.servicraft.servidirectorios.gui.EditSlotGUI;
import org.servicraft.servidirectorios.model.Shortcut;
import org.servicraft.servidirectorios.util.Message;

import java.util.HashMap;
import java.util.Map;

public class EditMenuListener implements Listener {
    private final Map<Player, Integer> renameTitle = new HashMap<>();
    private final Map<Player, Integer> renameLore = new HashMap<>();
    private final Map<Player, String> tempName = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.equals(Message.EDIT_DIRECTORIES_TITLE.get())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && !" ".equals(meta.getDisplayName())) {
                    int index = event.getRawSlot();
                    Map<Integer, Shortcut> owned = DatabaseManager.getOwnedShortcuts(player.getName());
                    if (index < owned.size()) {
                        int slotIndex = (Integer) owned.keySet().toArray()[index];
                        EditSlotGUI.open(player, slotIndex, owned.get(slotIndex));
                    }
                }
            }
        } else if (title.equals(Message.EDIT_MENU_TITLE.get())) {
            boolean top = event.getRawSlot() < event.getInventory().getSize();
            if (top) event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            int slot = event.getRawSlot();
            int slotIndex = EditSlotGUI.getEditingSlot(player);
            if (slot == 10) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    ItemStack newIcon = new ItemStack(cursor.getType());
                    ItemMeta m = newIcon.getItemMeta();
                    if (m != null) {
                        m.setDisplayName(Message.EDIT_ICON.get());
                        newIcon.setItemMeta(m);
                    }
                    event.getInventory().setItem(10, newIcon);
                    DatabaseManager.updateIconBySlot(slotIndex, cursor.getType());
                }
            } else if (slot == 11) {
                renameTitle.put(player, slotIndex);
                player.closeInventory();
                player.sendMessage(Message.ENTER_TITLE.get());
            } else if (slot == 13) {
                Shortcut sc = DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex);
                DatabaseManager.updateShortcutBySlot(slotIndex, sc.getName(), sc.getDescription(), player.getLocation(), sc.getIcon());
                EditSlotGUI.open(player, slotIndex, DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex));
            } else if (slot == 14) {
                Shortcut sc = DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex);
                player.teleport(sc.getLocation());
            } else if (slot == 16) {
                org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.plugin.java.JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class).getConfig();
                int creditStart = cfg.getInt("credit-slots.start");
                int creditEnd = cfg.getInt("credit-slots.end");
                double price = cfg.getDouble("slot-prices." + slotIndex, 0.0);
                boolean credit = slotIndex >= creditStart && slotIndex <= creditEnd;
                BuySlotWeeksGUI.open(player, price, credit, slotIndex);
            } else if (slot == 22 && EditSlotGUI.isAdminEditor(player)) {
                String owner = DatabaseManager.getSlotOwner(slotIndex);
                int days = DatabaseManager.getRemainingDays(slotIndex);
                org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.plugin.java.JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class).getConfig();
                double price = cfg.getDouble("slot-prices." + slotIndex, 0.0);
                double refund = price * days / 7.0;
                net.milkbowl.vault.economy.Economy econ = org.servicraft.servidirectorios.Servidirectorios.getEconomy();
                if (econ != null && owner != null) {
                    econ.depositPlayer(org.bukkit.Bukkit.getOfflinePlayer(owner), refund);
                }
                if (owner != null) {
                    org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayerExact(owner);
                    if (target != null) {
                        target.sendMessage(Message.DIRECTORY_REMOVED_OWNER.get().replace("{amount}", String.format(java.util.Locale.US, "%.2f", refund)));
                    }
                }
                DatabaseManager.deleteSlot(slotIndex);
                player.sendMessage(Message.DIRECTORY_REMOVED.get().replace("{amount}", String.format(java.util.Locale.US, "%.2f", refund)));
                EditSlotGUI.clearAdmin(player);
                BuySlotGUI.open(player, 1);
            } else if (slot == 26) {
                BuySlotGUI.open(player, 1);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (renameTitle.containsKey(player)) {
            int slotIndex = renameTitle.remove(player);
            event.setCancelled(true);
            String msg = colorize(event.getMessage());
            if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("cancelar")) {
                player.sendMessage(Message.EDIT_CANCELLED.get());
                return;
            }
            tempName.put(player, msg);
            renameLore.put(player, slotIndex);
            player.sendMessage(Message.ENTER_LORE.get());
        } else if (renameLore.containsKey(player)) {
            int slotIndex = renameLore.remove(player);
            event.setCancelled(true);
            String msg = colorize(event.getMessage());
            if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("cancelar")) {
                tempName.remove(player);
                player.sendMessage(Message.EDIT_CANCELLED.get());
                return;
            }
            String[] loreParts = msg.split("\\|");
            if (loreParts.length > 5) {
                loreParts = java.util.Arrays.copyOfRange(loreParts, 0, 5);
            }
            String desc = String.join("\n", loreParts);
            String name = tempName.remove(player);
            Shortcut sc = DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex);
            DatabaseManager.updateShortcutBySlot(slotIndex, name, desc, sc.getLocation(), sc.getIcon());
            player.sendMessage(Message.UPDATED.get());
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(org.servicraft.servidirectorios.Servidirectorios.class),
                    () -> EditSlotGUI.open(player, slotIndex, DatabaseManager.getOwnedShortcuts(player.getName()).get(slotIndex)));
        }
    }

    private String colorize(String text) {
        String result = ChatColor.translateAlternateColorCodes('&', text);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("#[a-fA-F0-9]{6}").matcher(result);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String color = m.group();
            m.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of(color) + "");
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
