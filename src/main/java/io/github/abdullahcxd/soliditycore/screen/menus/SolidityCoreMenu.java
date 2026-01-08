package io.github.abdullahcxd.soliditycore.screen.menus;

import io.github.abdullahcxd.soliditycore.builders.ItemBuilder;
import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.screen.InventoryScreen;
import io.github.abdullahcxd.soliditycore.screen.dialogs.ConfirmationScreen;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SolidityCoreMenu extends InventoryScreen {

    private static final String title = "<gradient:#6A5ACD:#8A2BE2>SolidityCore Menu</gradient>";
    private static final int rows = 3;

    public SolidityCoreMenu() {
        super(title, rows);
    }

    @Override
    public void initialize() {
        ItemStack glassPane = ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .name(MessageBuilder.fromMiniMessage(" ").build()) // empty name
                .build();
        fillEmpty(glassPane);

        ItemStack reloadButton = ItemBuilder.of(Material.PAPER)
                .name(MessageBuilder.fromMiniMessage("<green>Reload Configurations</green>").build())
                .lore(MessageBuilder.fromMiniMessage("<gray>Click to reload plugin configs</gray>").build())
                .build();

        setItem(13, reloadButton, (player, slot, item, clickType) -> {
            ConfirmationScreen.confirm(
                    player,
                    "<green>Configuration Reload</green>",
                    "<gold>You are about to reload configurations, do you want to reload?</gold>",
                    (plr) -> {
                        int reloaded = SolidityEditor.getInstance().reloadAll();
                        SenderUtils.sendPrefixed(player, "<green>Configurations reloaded for <gold>" + reloaded + "</gold> plugins and Core plugin</green>");
                    },
                    (plr) -> {
                        SenderUtils.sendPrefixed(player, "<red>Denied configuration reload, no configurations was reloaded</red>");
                    }
            );
        });
    }

    @Override
    public void onClick(Player player, int slot, ItemStack item, ClickType clickType) {
        // All click actions handled in setItem above
    }
}
