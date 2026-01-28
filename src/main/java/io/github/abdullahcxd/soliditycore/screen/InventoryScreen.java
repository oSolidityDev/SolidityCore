package io.github.abdullahcxd.soliditycore.screen;

import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
import io.github.abdullahcxd.soliditycore.temporary.storage.TemporaryPlayerStorage;
import io.github.abdullahcxd.soliditycore.temporary.storage.TemporaryStorageManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class InventoryScreen implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final String title;
    private final int rows;
    private final Map<Integer, ClickableItem> itemMap = new HashMap<>();
    private final Map<UUID, TemporaryPlayerStorage> viewerStorages = new HashMap<>();

    private boolean registered = false;

    public InventoryScreen(String title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6");
        }

        this.title = title;
        this.rows = rows;

        // Convert MiniMessage title to legacy format for Bukkit
        this.inventory = Bukkit.createInventory(this, rows * 9,
                MessageBuilder.fromMiniMessage(title).build());
    }

    /**
     * Initialize the GUI - set up items, etc.
     */
    public abstract void initialize(Player target);

    /**
     * Called when the inventory is closed
     */
    public void onClose(Player player) {}

    /**
     * Called when an item is clicked
     */
    public abstract void onClick(Player player, int slot, ItemStack item, ClickType clickType);

    /**
     * Set an item with a click action
     */
    public void setItem(int slot, @NotNull ItemStack item, ClickAction action) {
        if (slot < 0 || slot >= inventory.getSize()) {
            throw new IllegalArgumentException("Slot out of bounds: " + slot);
        }

        ClickableItem clickableItem = new ClickableItem(item, action);
        itemMap.put(slot, clickableItem);
        inventory.setItem(slot, item);
    }

    /**
     * Set an item without a click action
     */
    public void setItem(int slot, @NotNull ItemStack item) {
        setItem(slot, item, null);
    }

    /**
     * Set an item with MiniMessage name and lore
     */
    public void setItem(int slot, @NotNull ItemStack item, @NotNull String name, @NotNull List<String> lore, ClickAction action) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageBuilder.fromMiniMessage(name).build());
            meta.lore(lore.stream().map(MessageBuilder::fromMiniMessage).map(MessageBuilder::build).toList());
            item.setItemMeta(meta);
        }
        setItem(slot, item, action);
    }

    /**
     * Remove an item from a slot
     */
    public void removeItem(int slot) {
        itemMap.remove(slot);
        inventory.setItem(slot, null);
    }

    /**
     * Clear all items
     */
    public void clear() {
        itemMap.clear();
        inventory.clear();
    }

    /**
     * Fill empty slots with an item
     */
    public void fillEmpty(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                setItem(i, item);
            }
        }
    }

    /**
     * Fill border with an item
     */
    public void fillBorder(ItemStack item) {
        int size = inventory.getSize();
        int width = 9;

        // Top row
        for (int i = 0; i < width; i++) {
            setItem(i, item);
        }

        // Bottom row
        for (int i = size - width; i < size; i++) {
            setItem(i, item);
        }

        // Sides
        for (int i = width; i < size - width; i += width) {
            setItem(i, item);
            setItem(i + width - 1, item);
        }
    }

    /**
     * Open the inventory for a player
     */
    public void open(Player player) {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, SolidityEditor.getInstance().getCore());
            registered = true;
        }

        TemporaryPlayerStorage storage = TemporaryStorageManager.getOrCreate(player);
        viewerStorages.put(player.getUniqueId(), storage);

        player.openInventory(inventory);
    }

    /**
     * Close the inventory for all viewers
     */
    public void closeAll() {
        inventory.getViewers().forEach(viewer -> {
            if (viewer instanceof Player) {
                viewer.closeInventory();
            }
        });
    }

    /**
     * Update the inventory for all viewers
     */
    public void update() {
        inventory.getViewers().forEach(viewer -> {
            if (viewer instanceof Player) {
                ((Player) viewer).updateInventory();
            }
        });
    }

    /**
     * Get storage for a specific viewer
     */
    public TemporaryPlayerStorage getStorage(@NotNull Player player) {
        return viewerStorages.get(player.getUniqueId());
    }

    /**
     * Unregister events and cleanup
     */
    public void destroy() {
        closeAll();
        viewerStorages.clear();
        itemMap.clear();

        if (registered) {
            HandlerList.unregisterAll(this);
            registered = false;
        }
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof InventoryScreen)) return;
        if (!event.getInventory().equals(this.inventory)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(this.inventory)) return;

        int slot = event.getSlot();
        ItemStack item = event.getCurrentItem();

        if (item == null) return;

        ClickType clickType = ClickType.fromBukkit(event.getClick());

        // Execute the click action if one exists
        ClickableItem clickableItem = itemMap.get(slot);
        if (clickableItem != null && clickableItem.getAction() != null) {
            clickableItem.getAction().onClick(player, slot, item, clickType);
        }

        // Call the abstract method
        onClick(player, slot, item, clickType);
    }

    @EventHandler
    public void onItemDrag(@NotNull InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof InventoryScreen)) return;
        if (!event.getInventory().equals(this.inventory)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof InventoryScreen)) return;
        if (!event.getInventory().equals(this.inventory)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        viewerStorages.remove(player.getUniqueId());
        onClose(player);

        if (inventory.getViewers().isEmpty()) {
            destroy();
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Getter
    public static class ClickableItem {
        private final ItemStack item;
        private final ClickAction action;

        public ClickableItem(ItemStack item, ClickAction action) {
            this.item = item;
            this.action = action;
        }
    }

    @FunctionalInterface
    public interface ClickAction {
        void onClick(Player player, int slot, ItemStack item, ClickType clickType);
    }

    public enum ClickType {
        LEFT,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        MIDDLE,
        DROP,
        CONTROL_DROP,
        DOUBLE_CLICK,
        OTHER;

        @Contract(pure = true)
        public static ClickType fromBukkit(org.bukkit.event.inventory.@NotNull ClickType bukkitType) {
            return switch (bukkitType) {
                case LEFT -> LEFT;
                case RIGHT -> RIGHT;
                case SHIFT_LEFT -> SHIFT_LEFT;
                case SHIFT_RIGHT -> SHIFT_RIGHT;
                case MIDDLE -> MIDDLE;
                case DROP -> DROP;
                case CONTROL_DROP -> CONTROL_DROP;
                case DOUBLE_CLICK -> DOUBLE_CLICK;
                default -> OTHER;
            };
        }
    }
}
