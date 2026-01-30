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

public class CollectionsMenuService {
    private static final int MENU_SIZE = 27;
    private static final Component TITLE = Component.text("Collections", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD);

    private static final int SLOT_BESTIARY = 10;
    private static final int SLOT_ENCHANTS = 11;
    private static final int SLOT_FISHING = 12;
    private static final int SLOT_BIOMES = 14;
    private static final int SLOT_NIGHT = 15;
    private static final int SLOT_DISCS = 16;
    private static final int SLOT_BACK = 22;

    private final Map<Integer, StatsSection> slotMap = Map.of(
            SLOT_BESTIARY, StatsSection.BESTIARY,
            SLOT_ENCHANTS, StatsSection.ENCHANTS,
            SLOT_FISHING, StatsSection.FISHING,
            SLOT_BIOMES, StatsSection.BIOMES,
            SLOT_NIGHT, StatsSection.NIGHT,
            SLOT_DISCS, StatsSection.DISCS
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
        CollectionsMenuHolder holder = new CollectionsMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, MENU_SIZE, TITLE);
        holder.setInventory(inventory);

        inventory.setItem(SLOT_BESTIARY, button(Material.BONE, "Bestiary"));
        inventory.setItem(SLOT_ENCHANTS, button(Material.ENCHANTED_BOOK, "Enchants"));
        inventory.setItem(SLOT_FISHING, button(Material.FISHING_ROD, "Fishing"));
        inventory.setItem(SLOT_BIOMES, button(Material.PINK_PETALS, "Biomes"));
        inventory.setItem(SLOT_NIGHT, button(Material.SLIME_BLOCK, "Night Events"));
        inventory.setItem(SLOT_DISCS, button(Material.MUSIC_DISC_13, "Discs"));
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
