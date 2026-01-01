package io.github.abdullahcxd.soliditycore.screen.dialogs;

import io.github.abdullahcxd.soliditycore.screen.InventoryManager;
import io.github.abdullahcxd.soliditycore.screen.InventoryScreen;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * A simple confirmation screen for yes/no decisions
 */
public class ConfirmationScreen extends InventoryScreen {

    private final String questionText;
    private final Consumer<Player> onConfirm;
    private final Consumer<Player> onDeny;
    private final ItemStack confirmItem;
    private final ItemStack denyItem;
    private final ItemStack questionItem;

    private ConfirmationScreen(@NotNull Player viewer,
                               @NotNull String title,
                               @NotNull String questionText,
                               @NotNull Consumer<Player> onConfirm,
                               @NotNull Consumer<Player> onDeny) {
        super(title, 3);
        this.questionText = questionText;
        this.onConfirm = onConfirm;
        this.onDeny = onDeny;

        // Create confirm button
        this.confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        
        // Create deny button
        this.denyItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        
        // Create question item
        this.questionItem = new ItemStack(Material.PAPER);
        
        initialize();
    }

    @Override
    public void initialize() {
        // Fill background with black glass
        ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < getInventory().getSize(); i++) {
            setItem(i, background);
        }

        // Question item in center with custom text
        setItem(13, questionItem, 
            "<yellow><bold>Are you sure?</bold>",
            List.of("<gray>" + questionText),
            null);

        // Confirm buttons (left side)
        for (int i : new int[]{10, 11, 12, 19, 20, 21}) {
            setItem(i, confirmItem,
                "<green><bold>✔ CONFIRM</bold>",
                List.of("<gray>Click to confirm this action"),
                (player, slot, item, clickType) -> {
                    player.closeInventory();
                    onConfirm.accept(player);
                });
        }

        // Deny buttons (right side)
        for (int i : new int[]{14, 15, 16, 23, 24, 25}) {
            setItem(i, denyItem,
                "<red><bold>✖ CANCEL</bold>",
                List.of("<gray>Click to cancel this action"),
                (player, slot, item, clickType) -> {
                    player.closeInventory();
                    onDeny.accept(player);
                });
        }
    }

    @Override
    public void onClick(Player player, int slot, ItemStack item, ClickType clickType) {
        // Individual click actions are handled per-item
    }

    @Override
    public void onClose(Player player) {
        // Cleanup handled by parent class
    }

    /**
     * Creates and opens a confirmation screen with custom title
     */
    public static void confirm(@NotNull Player player,
                              @NotNull String title,
                              @NotNull String question,
                              @NotNull Consumer<Player> onConfirm,
                              @NotNull Consumer<Player> onDeny) {
        InventoryManager.open(player, new ConfirmationScreen(player, title, question, onConfirm, onDeny));
    }

    /**
     * Creates and opens a confirmation screen with default title
     */
    public static void confirm(@NotNull Player player,
                              @NotNull String question,
                              @NotNull Consumer<Player> onConfirm,
                              @NotNull Consumer<Player> onDeny) {
        confirm(player, "<red><bold>Confirm Action", question, onConfirm, onDeny);
    }

    /**
     * Creates and opens a confirmation screen with only confirm callback
     */
    public static void confirm(@NotNull Player player,
                              @NotNull String question,
                              @NotNull Consumer<Player> onConfirm) {
        confirm(player, question, onConfirm, p -> {});
    }

    /**
     * Creates a dangerous action confirmation (red theme)
     */
    public static void confirmDangerous(@NotNull Player player,
                                       @NotNull String question,
                                       @NotNull Consumer<Player> onConfirm,
                                       @NotNull Consumer<Player> onDeny) {
        confirm(player, "<dark_red><bold>⚠ WARNING ⚠", question, onConfirm, onDeny);
    }

    /**
     * Creates a dangerous action confirmation with only confirm callback
     */
    public static void confirmDangerous(@NotNull Player player,
                                       @NotNull String question,
                                       @NotNull Consumer<Player> onConfirm) {
        confirmDangerous(player, question, onConfirm, p -> {});
    }
}