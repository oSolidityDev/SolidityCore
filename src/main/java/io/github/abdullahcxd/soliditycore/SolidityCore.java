package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.actionbar.ActionBarManager;
import io.github.abdullahcxd.soliditycore.commands.CommandManager;
import io.github.abdullahcxd.soliditycore.commands.base.SolidityCoreCommand;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SolidityCore extends JavaPlugin {

    @Override
    public void onLoad() {
        saveDefaultConfig();
        SolidityEditor.getInstance().setCore(this);
        SolidityEditor.getInstance().initialize();

        ConsoleCommandSender console = getConsoleCommandSender();

        SenderUtils.sendPrefixed(console, SenderUtils.separator(32));
        SenderUtils.newline(console);
        SenderUtils.sendPrefixed(console, "<green>Loading SolidityCore version <gold>"
                + getPluginMeta().getVersion() + "</gold></green>");
        SenderUtils.newline(console);
        SenderUtils.sendPrefixed(console, SenderUtils.separator(32));
    }

    @Override
    public void onEnable() {
        ConsoleCommandSender console = getConsoleCommandSender();

        CommandManager.registerCommand(this, new SolidityCoreCommand());

        // Check dependencies for all registered plugins
        for (SolidityPlugin plugin : getRegisteredPlugins()) {
            if (plugin.hasDependencies() && !plugin.checkDependenciesLoaded()) {
                SenderUtils.error(console, "Plugin " + plugin.getSolidityMetadata().getPluginName()
                        + " has missing dependencies! It will be disabled.");
                getServer().getPluginManager().disablePlugin(plugin);
            }
        }

        // Optionally log all loaded plugins
        SenderUtils.sendPrefixed(console, "<green>All plugins loaded successfully!</green>");
        SolidityEditor.getInstance().broadcastPluginInfo();
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender console = getConsoleCommandSender();
        SenderUtils.sendPrefixed(console, "<red>Shutting down SolidityCore...</red>");
    }

    public @NotNull ConsoleCommandSender getConsoleCommandSender() {
        return getServer().getConsoleSender();
    }

    /**
     * Fetches all registered SolidityPlugins
     * This requires that all plugins extending SolidityPlugin register themselves in SolidityEditor
     */
    private SolidityPlugin @NotNull [] getRegisteredPlugins() {
        return SolidityEditor.getInstance()
                .getMetadata()
                .values()
                .stream()
                .map(meta -> (SolidityPlugin) getServer().getPluginManager().getPlugin(meta.getPluginName()))
                .filter(Objects::nonNull)
                .toArray(SolidityPlugin[]::new);
    }
}
