package com.nickrodi.nir.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class StatsMenuService {
    private static final int MENU_SIZE = 27;
    private static final Component TITLE = Component.text("Statistics", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD);

    private static final int SLOT_PLAYTIME = 10;
    private static final int SLOT_MINING = 11;
    private static final int SLOT_COMBAT = 12;
    private static final int SLOT_MOBS = 13;
    private static final int SLOT_COLLECTIONS = 14;
    private static final int SLOT_EXPLORATION = 15;
    private static final int SLOT_EXTRA = 16;
    private static final int SLOT_BACK = 22;

    private final Map<Integer, StatsSection> slotMap = Map.of(
            SLOT_PLAYTIME, StatsSection.PLAYTIME,
            SLOT_MINING, StatsSection.MINING,
            SLOT_COMBAT, StatsSection.COMBAT,
            SLOT_MOBS, StatsSection.MOBS,
            SLOT_COLLECTIONS, StatsSection.COLLECTIONS,
            SLOT_EXPLORATION, StatsSection.EXPLORATION,
            SLOT_EXTRA, StatsSection.EXTRA
    );

    public void openFor(Player player) {
        Inventory menu = buildMenu();
        player.openInventory(menu);
    }

    public StatsSection getSectionForSlot(int slot) {
        return slotMap.get(slot);
    }

    public boolean isBackSlot(int slot) {
        return slot == SLOT_BACK;
    }

    private Inventory buildMenu() {
        StatsMenuHolder holder = new StatsMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, MENU_SIZE, TITLE);
        holder.setInventory(inventory);

        inventory.setItem(SLOT_PLAYTIME, button(Material.CLOCK, "Playtime"));
        inventory.setItem(SLOT_MINING, button(Material.IRON_PICKAXE, "Mining"));
        inventory.setItem(SLOT_COMBAT, button(Material.IRON_SWORD, "Combat"));
        inventory.setItem(SLOT_MOBS, button(Material.HAY_BLOCK, "Mobs"));
        inventory.setItem(SLOT_COLLECTIONS, button(Material.BOOK, "Collections"));
        inventory.setItem(SLOT_EXPLORATION, button(Material.GRASS_BLOCK, "Exploration"));
        inventory.setItem(SLOT_EXTRA, button(Material.MAGMA_CREAM, "Extra"));
        inventory.setItem(SLOT_BACK, button(Material.BARRIER, "Back", NamedTextColor.RED));
        return inventory;
    }

    private ItemStack button(Material material, String label) {
        return button(material, label, NamedTextColor.GRAY);
    }

    private ItemStack button(Material material, String label, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(label, color, TextDecoration.BOLD));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}
