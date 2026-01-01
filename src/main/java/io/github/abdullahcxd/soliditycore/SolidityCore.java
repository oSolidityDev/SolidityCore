package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.commands.BaseCommand;
import io.github.abdullahcxd.soliditycore.commands.base.SolidityCoreCommand;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SolidityCore extends JavaPlugin {

    @Override
    public void onLoad() {
        SolidityEditor.getInstance().setCore(this);
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

        registerCommand(new SolidityCoreCommand());

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

    public ConsoleCommandSender getConsoleCommandSender() {
        return getServer().getConsoleSender();
    }

    public void registerCommand(BaseCommand command) {

        if (command == null) {
            throw new SolidityException("Cannot resolve a null command from plugin SolidityCore");
        }

        command.initialize();

        PluginCommand pluginCommand = getCommand(command.getCommandInfo().getName());

        if (pluginCommand == null) {
            throw new SolidityException("Command with the name of /" + command.getCommandInfo().getName() + " wasn't found in the plugin.yml for " + getDescription().getName());
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

    }

    /**
     * Fetches all registered SolidityPlugins
     * This requires that all plugins extending SolidityPlugin register themselves in SolidityEditor
     */
    private SolidityPlugin[] getRegisteredPlugins() {
        return SolidityEditor.getInstance()
                .getMetadata()
                .stream()
                .map(meta -> (SolidityPlugin) getServer().getPluginManager().getPlugin(meta.getPluginName()))
                .filter(Objects::nonNull)
                .toArray(SolidityPlugin[]::new);
    }
}
