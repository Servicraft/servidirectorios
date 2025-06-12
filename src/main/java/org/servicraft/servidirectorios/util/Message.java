package org.servicraft.servidirectorios.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public enum Message {
    PLUGIN_ENABLED("plugin-enabled", "Servidirectorios habilitado."),
    NO_VAULT("no-vault", "Vault no encontrado! Deshabilitando plugin."),
    PLUGIN_DISABLED("plugin-disabled", "Servidirectorios deshabilitado."),
    ONLY_PLAYERS("only-players", "Este comando solo puede ser usado por jugadores."),
    UNKNOWN_SUBCOMMAND("unknown-subcommand", "&cSubcomando desconocido. Usa /directorios, /directorios tiendas o /directorios comprar."),
    USAGE_CREATE_SHORTCUT("usage-create-shortcut", "Uso: /createshortcut <nombre> <descripcion>"),
    SHORTCUT_CREATED("shortcut-created", "Shortcut creado: {name}"),
    USAGE_REMOVECREDITS("usage-removecredits", "Uso: /removecredits <jugador> <cantidad>"),
    INVALID_CREDIT_AMOUNT("invalid-credit-amount", "Por favor, introduce una cantidad válida de créditos."),
    AMOUNT_GREATER_ZERO("amount-greater-zero", "La cantidad debe ser mayor a 0."),
    CREDITS_REMOVED_SENDER("credits-removed-sender", "Créditos quitados exitosamente."),
    CREDITS_REMOVED_TARGET("credits-removed-target", "Se te han quitado {amount} créditos."),
    CREDITS_REMOVE_FAILED("credits-remove-failed", "No se pudo quitar los créditos. Verifica que el jugador esté disponible o tenga suficientes créditos."),
    PROMOTED_SLOTS_TITLE("promoted-slots-title", "Puestos promocionados - Página {page}"),
    SLOT_NAME("slot-name", "Puesto {number}"),
    SLOT_CONTRACT_EXPIRES("slot-contract-expires", "Contrato expira en"),
    SLOT_REMAINING_DAYS("slot-remaining-days", "{days} días"),
    SLOT_CLICK_TO_BUY_1("slot-click-to-buy-1", "¡Haz clic para comprar"),
    SLOT_CLICK_TO_BUY_2("slot-click-to-buy-2", "este puesto!"),
    WEEKLY_PRICE("weekly-price", "Valor semanal:"),
    NEXT_PAGE("next-page", "Siguiente página"),
    PREVIOUS_PAGE("previous-page", "Página anterior"),
    BUY_SLOT_TITLE("buy-slot-title", "Comprar puesto"),
    DECREASE_WEEK("decrease-week", "Reducir 1 semana"),
    INCREASE_WEEK("increase-week", "Incrementar una semana"),
    PAY("pay", "Pagar"),
    ORDER_SUMMARY("order-summary", "Resumen del pedido:"),
    WEEKS_LINE("weeks-line", "{weeks} semanas"),
    BUY_SUCCESS("buy-success", "Compra exitosa."),
    TRANSACTION_FAILED("transaction-failed", "No se pudo completar la transacción."),
    NOT_ENOUGH_MONEY("not-enough-money", "No tienes suficientes servidólares."),
    PROCESSING_CREDITS("processing-credits", "Procesando compra con créditos..."),
    NOT_ENOUGH_CREDITS("not-enough-credits", "No tienes suficientes créditos o ocurrió un error."),
    ECONOMY_NOT_AVAILABLE("economy-not-available", "El sistema de economía no está disponible."),
    TELEPORTED_TO("teleported-to", "Teletransportado a {name}"),
    CHANGING_PAGE("changing-page", "Cambiando a la página {page} de puestos promocionados..."),
    RETURNING_PAGE("returning-page", "Volviendo a la página {page} de puestos promocionados..."),
    DIRECTORIES_TITLE("directories-title", "Directorios"),
    EDIT_DIRECTORIES_TITLE("edit-directories-title", "Editar directorios"),
    EDIT_MENU_TITLE("edit-menu-title", "Editar directorio"),
    EDIT_ICON("edit-icon", "&eÍcono"),
    EDIT_NAME_DESCRIPTION("edit-name-description", "&fNombre y descripción"),
    EDIT_PLACE("edit-place", "&fLugar"),
    GO_TO_PLACE("go-to-place", "&fIr al lugar"),
    BUY_MORE_DAYS("buy-more-days", "&aComprar más días"),
    REMAINING_DAYS("remaining-days", "&7{days} días restantes"),
    BACK_TO_PROMOTED("back-to-promoted", "&fVolver a los puestos promocionados"),
    CLICK_TO_EDIT("click-to-edit", "&7Haz clic para editar"),
    ENTER_TITLE("enter-title", "&aEscribe el título o 'cancelar' para cancelar"),
    ENTER_LORE("enter-lore", "&aEscribe hasta 5 líneas separadas por | o 'cancelar'"),
    UPDATED("updated", "Actualizado"),
    EDIT_CANCELLED("edit-cancelled", "Edición cancelada."),
    ADMIN_DELETE_REFUND("admin-delete-refund", "&eEliminar y devolver dinero al propietario"),
    DIRECTORY_REMOVED("directory-removed", "Directorio eliminado y se devolvió {amount} al propietario."),
    DIRECTORY_REMOVED_OWNER("directory-removed-owner", "Tu directorio fue eliminado y se te devolvió {amount}."),

    STATS_TITLE("stats-title", "Estadísticas"),
    STATS_YEARS_TITLE("stats-years-title", "Años"),
    STATS_MONTHS_TITLE("stats-months-title", "Meses de {year}"),
    STATS_WEEKS_TITLE("stats-weeks-title", "Semanas de {month}/{year}"),
    CLICK_TO_VIEW("click-to-view", "&fHaz clic para ver"),
    CLICKS_COUNT("clicks-count", "&6Clics: &e{count}"),
    TOGGLE_ALL_TIME("toggle-all-time", "&aDesde siempre"),
    TOGGLE_SINCE_PURCHASE("toggle-since-purchase", "&cDesde compra"),
    STATS_BACK("stats-back", "&fVolver");

    private static YamlConfiguration config;
    private final String path;
    private final String def;

    Message(String path, String def) {
        this.path = path;
        this.def = def;
    }

    public static void load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String get() {
        if (config == null) return ChatColor.translateAlternateColorCodes('&', def);
        String val = config.getString(path, def);
        return ChatColor.translateAlternateColorCodes('&', val);
    }
}
