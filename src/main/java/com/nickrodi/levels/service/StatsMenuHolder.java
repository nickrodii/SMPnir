package com.nickrodi.levels.service;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class StatsMenuHolder implements InventoryHolder {
    private Inventory inventory;

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
