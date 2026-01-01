package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.commands.BaseCommand;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import io.github.abdullahcxd.soliditycore.listener.SolidityListener;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class SolidityPlugin extends JavaPlugin {

    public abstract SolidityMetadata getSolidityMetadata();

    public abstract void load();
    public abstract void enable();
    public abstract void disable();

    /**
     * If you have multiple configurations, you can override this to reload multiple configurations with the use of one command
     */
    public void reloadConfigurations() {
        reloadConfig();
    }

    @Override
    public void onLoad() {
        SolidityEditor.getInstance().registerPluginMeta(this);

        ConsoleCommandSender console = getConsoleCommandSender();
        SenderUtils.sendPrefixed(console, SenderUtils.separator(32));
        SenderUtils.newline(console);
        SenderUtils.sendPrefixed(console,
                "<green>Loading Solidity Plugin <gold>" +
                        getSolidityMetadata().getPluginName() +
                        "</gold> version <gold>" +
                        getSolidityMetadata().getPluginVersion() +
                        "</gold></green>");
        SenderUtils.newline(console);
        SenderUtils.sendPrefixed(console, SenderUtils.separator(32));

        load();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        logEnable();
        enable();
    }

    @Override
    public void onDisable() {
        disable();
        logDisable();
        super.onDisable();
    }

    private void logEnable() {
        SenderUtils.sendPrefixed(getConsoleCommandSender(),
                "<green>Enabled plugin <gold>" +
                        getSolidityMetadata().getPluginName() + "</gold></green>");
    }

    private void logDisable() {
        SenderUtils.sendPrefixed(getConsoleCommandSender(),
                "<red>Disabled plugin <gold>" +
                        getSolidityMetadata().getPluginName() + "</gold></red>");
    }

    public ConsoleCommandSender getConsoleCommandSender() {
        return getServer().getConsoleSender();
    }

    public boolean hasDependencies() {
        return !getSolidityMetadata().getDependencies().isEmpty();
    }

    public boolean checkDependenciesLoaded() {
        boolean allLoaded = true;
        for (String dep : getSolidityMetadata().getDependencies()) {
            if (!SolidityEditor.getInstance().isPluginLoaded(dep)) {
                SenderUtils.error(getConsoleCommandSender(),
                        "Dependency not loaded: " + dep);
                allLoaded = false;
            }
        }
        return allLoaded;
    }

    public void registerCommand(BaseCommand command) {

        if (command == null) {
            throw new SolidityException("Cannot resolve a null command from plugin " + getSolidityMetadata().getPluginName());
        }

        command.initialize();

        PluginCommand pluginCommand = getCommand(command.getCommandInfo().getName());

        if (pluginCommand == null) {
            throw new SolidityException("Command with the name of /" + command.getCommandInfo().getName() + " wasn't found in the plugin.yml for " + getDescription().getName());
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

    }

    public void registerListener(@NotNull SolidityListener listener) {
        listener.initialize(this);
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
