package com.nickrodi.nir.service;

import com.nickrodi.nir.util.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BoardService {
    private final JavaPlugin plugin;
    private final File file;
    private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();
    private List<Component> pages = new ArrayList<>();
    private final Set<UUID> editors = new HashSet<>();

    public BoardService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "board.yml");
    }

    public void load() {
        if (!file.exists()) {
            pages = new ArrayList<>();
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> raw = config.getStringList("pages");
        List<Component> loaded = new ArrayList<>();
        for (String json : raw) {
            try {
                loaded.add(serializer.deserialize(json));
            } catch (Exception ignored) {
            }
        }
        pages = loaded;
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<String> raw = new ArrayList<>();
        for (Component page : pages) {
            raw.add(serializer.serialize(page));
        }
        config.set("pages", raw);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save board.yml", e);
        }
    }

    public void openFor(Player player) {
        editors.add(player.getUniqueId());
        ItemStack book = buildBoardBook();
        ItemStack existing = findBoardBook(player);
        if (existing != null) {
            existing.setItemMeta(book.getItemMeta());
        } else if (!player.getInventory().addItem(book).isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
        }
        try {
            player.openBook(book);
        } catch (IllegalArgumentException ignored) {
            player.sendMessage("Board book added to your inventory. Right-click to edit.");
        }
    }

    public void updateFrom(BookMeta meta) {
        if (meta == null) {
            return;
        }
        pages = new ArrayList<>(meta.pages());
        save();
    }

    public boolean isEditor(UUID uuid) {
        return editors.contains(uuid);
    }

    public void clearEditor(UUID uuid) {
        editors.remove(uuid);
    }

    private ItemStack buildBoardBook() {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (pages.isEmpty()) {
            meta.pages(List.of(Component.text("")));
        } else {
            meta.pages(pages);
        }
        meta.getPersistentDataContainer().set(Keys.BOARD_BOOK, PersistentDataType.BYTE, (byte) 1);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack findBoardBook(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.WRITABLE_BOOK) {
                continue;
            }
            if (!(item.getItemMeta() instanceof BookMeta meta)) {
                continue;
            }
            if (meta.getPersistentDataContainer().has(Keys.BOARD_BOOK, PersistentDataType.BYTE)) {
                return item;
            }
        }
        return null;
    }
}
