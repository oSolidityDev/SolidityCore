package io.github.abdullahcxd.soliditycore.screen;

import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class PaginatedInventoryScreen extends InventoryScreen {

    private int currentPage = 0;
    private final List<ItemStack> items = new ArrayList<>();

    private int previousButtonSlot;
    private int nextButtonSlot;
    private int[] contentSlots;

    private ItemStack previousButton;
    private ItemStack nextButton;
    private ItemStack previousButtonDisabled;
    private ItemStack nextButtonDisabled;

    public PaginatedInventoryScreen(String title, int rows) {
        super(title, rows);

        this.previousButtonSlot = (rows * 9) - 9; // Bottom left
        this.nextButtonSlot = (rows * 9) - 1; // Bottom right
        this.contentSlots = calculateContentSlots(rows);

        initializeButtons();
    }

    /**
     * Calculate content slots (excludes bottom row by default)
     */
    @Contract(pure = true)
    private int @NotNull [] calculateContentSlots(int rows) {
        int totalSlots = (rows - 1) * 9; // Exclude bottom row for navigation
        int[] slots = new int[totalSlots];
        for (int i = 0; i < totalSlots; i++) {
            slots[i] = i;
        }
        return slots;
    }

    /**
     * Initialize default navigation buttons
     */
    private void initializeButtons() {
        previousButton = createButton(Material.ARROW, "&aPrevious Page");
        nextButton = createButton(Material.ARROW, "&aNext Page");
        previousButtonDisabled = createButton(Material.BARRIER, "&cNo Previous Page");
        nextButtonDisabled = createButton(Material.BARRIER, "&cNo Next Page");
    }

    private @NotNull ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageBuilder.fromMiniMessage(name).build());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Set custom content slots for pagination
     */
    public void setContentSlots(int... slots) {
        this.contentSlots = slots;
    }

    /**
     * Set custom navigation button slots
     */
    public void setNavigationSlots(int previousSlot, int nextSlot) {
        this.previousButtonSlot = previousSlot;
        this.nextButtonSlot = nextSlot;
    }

    /**
     * Set custom navigation button items
     */
    public void setNavigationButtons(ItemStack previous, ItemStack next,
                                     ItemStack previousDisabled, ItemStack nextDisabled) {
        this.previousButton = previous;
        this.nextButton = next;
        this.previousButtonDisabled = previousDisabled;
        this.nextButtonDisabled = nextDisabled;
    }

    /**
     * Add an item to the pagination list
     */
    public void addItem(ItemStack item) {
        items.add(item);
    }

    /**
     * Add multiple items to the pagination list
     */
    public void addItems(List<ItemStack> items) {
        this.items.addAll(items);
    }

    /**
     * Clear all paginated items
     */
    public void clearItems() {
        items.clear();
        currentPage = 0;
    }

    /**
     * Get the total number of pages
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / contentSlots.length);
    }

    /**
     * Check if there is a next page
     */
    public boolean hasNextPage() {
        return currentPage < getTotalPages() - 1;
    }

    /**
     * Check if there is a previous page
     */
    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    /**
     * Go to the next page
     */
    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
            renderPage();
        }
    }

    /**
     * Go to the previous page
     */
    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
            renderPage();
        }
    }

    /**
     * Go to a specific page
     */
    public void goToPage(int page) {
        if (page >= 0 && page < getTotalPages()) {
            currentPage = page;
            renderPage();
        }
    }

    /**
     * Render the current page
     */
    public void renderPage() {
        // Clear content slots
        for (int slot : contentSlots) {
            removeItem(slot);
        }

        // Calculate start and end indices
        int startIndex = currentPage * contentSlots.length;
        int endIndex = Math.min(startIndex + contentSlots.length, items.size());

        // Place items on the page
        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            setItem(contentSlots[slotIndex], items.get(i));
        }

        // Update navigation buttons
        updateNavigationButtons();

        // Call hook for additional rendering
        onPageRender(currentPage);
    }

    /**
     * Update navigation buttons based on current page
     */
    private void updateNavigationButtons() {
        if (hasPreviousPage()) {
            setItem(previousButtonSlot, previousButton);
        } else {
            setItem(previousButtonSlot, previousButtonDisabled);
        }

        if (hasNextPage()) {
            setItem(nextButtonSlot, nextButton);
        } else {
            setItem(nextButtonSlot, nextButtonDisabled);
        }
    }

    @Override
    public void onClick(Player player, int position, ItemStack item, ClickType clickType) {
        // Handle navigation
        if (position == previousButtonSlot) {
            previousPage();
            return;
        }

        if (position == nextButtonSlot) {
            nextPage();
            return;
        }

        // Find the actual item index
        for (int i = 0; i < contentSlots.length; i++) {
            if (contentSlots[i] == position) {
                int itemIndex = (currentPage * contentSlots.length) + i;
                if (itemIndex < items.size()) {
                    onPageItemClick(player, itemIndex, items.get(itemIndex));
                }
                return;
            }
        }
    }

    /**
     * Called when the page is rendered (override for custom behavior)
     */
    protected void onPageRender(int page) {}

    /**
     * Called when a paginated item is clicked
     */
    protected abstract void onPageItemClick(Player player, int index, ItemStack item);

}