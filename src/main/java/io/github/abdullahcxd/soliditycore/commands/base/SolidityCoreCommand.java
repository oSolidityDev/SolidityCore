package io.github.abdullahcxd.soliditycore.commands.base;

import io.github.abdullahcxd.soliditycore.commands.BaseCommand;
import io.github.abdullahcxd.soliditycore.commands.CommandContext;
import io.github.abdullahcxd.soliditycore.commands.CommandInfo;
import io.github.abdullahcxd.soliditycore.screen.InventoryManager;
import io.github.abdullahcxd.soliditycore.screen.menus.SolidityCoreMenu;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SolidityCoreCommand extends BaseCommand {
    @Override
    public void initialize() {

    }

    @Override
    public CommandInfo getCommandInfo() {
        return CommandInfo.create()
                .name("solidity")
                .description("The main SolidityCore plugin command")
                .player(true)
                .permission("soliditycore.admin")
                .build();
    }

    @Override
    public void execute(@NotNull CommandContext context) {
        Player player = context.getPlayerSender();

        SenderUtils.sendPrefixed(player, "<green>Opening Solidity Menu</green>h");

        InventoryManager.open(player, new SolidityCoreMenu());
    }
}
