package org.servicraft.servidirectorios.model;

import org.bukkit.Location;
import org.bukkit.Material;

public class Shortcut {

    private final int id;
    private final String name;
    private final String description;
    private final Location location;
    private final Material icon;

    public Shortcut(int id, String name, String description, Location location, Material icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Location getLocation() {
        return location;
    }

    public Material getIcon() {
        return icon;
    }
}
