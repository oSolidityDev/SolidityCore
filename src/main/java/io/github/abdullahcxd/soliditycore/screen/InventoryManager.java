package io.github.abdullahcxd.soliditycore.screen;

import io.github.abdullahcxd.soliditycore.screen.dialogs.ConfirmationScreen;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InventoryManager {

    public static void open(Player player, @NotNull InventoryScreen screen) {

        screen.initialize();
        screen.open(player);

    }

}
