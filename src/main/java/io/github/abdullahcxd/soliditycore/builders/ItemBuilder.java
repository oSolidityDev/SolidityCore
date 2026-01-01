package io.github.abdullahcxd.soliditycore.builders;

import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for creating ItemStacks safely and cleanly.
 */
public final class ItemBuilder {

    private final ItemStack stack;
    private final ItemMeta meta;

    private ItemBuilder(@NotNull Material material, int amount) {
        this.stack = new ItemStack(material, amount);

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            throw new SolidityException("Material has no ItemMeta: " + material);
        }

        this.meta = meta;
    }


    public static @NotNull ItemBuilder of(@NotNull Material material) {
        return new ItemBuilder(material, 1);
    }

    public static @NotNull ItemBuilder of(@NotNull Material material, int amount) {
        return new ItemBuilder(material, amount);
    }


    public ItemBuilder editMeta(@NotNull Consumer<ItemMeta> consumer) {
        try {
            consumer.accept(meta);
            return this;
        } catch (Exception e) {
            throw new SolidityException("Failed to edit ItemMeta", e);
        }
    }


    public ItemBuilder name(@NotNull Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder lore(@NotNull List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public ItemBuilder lore(Component... lines) {
        meta.lore(List.of(lines));
        return this;
    }

    public ItemBuilder addLore(@NotNull Component line) {
        List<Component> lore = meta.lore();
        if (lore == null) lore = new ArrayList<>();
        lore.add(line);
        meta.lore(lore);
        return this;
    }

    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }


    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public @NotNull ItemStack build() {
        stack.setItemMeta(meta);
        return stack.clone(); // defensive copy
    }
}
