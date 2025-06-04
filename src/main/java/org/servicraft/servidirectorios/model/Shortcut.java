package org.servicraft.servidirectorios.model;

import org.bukkit.Location;

public class Shortcut {

    private final int id;
    private final String name;
    private final String description;
    private final Location location;

    public Shortcut(int id, String name, String description, Location location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
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
}
