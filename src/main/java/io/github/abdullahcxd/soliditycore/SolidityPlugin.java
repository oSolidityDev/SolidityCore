package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.actionbar.ActionBarManager;
import io.github.abdullahcxd.soliditycore.commands.BaseCommand;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.exception.DeprecationException;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import io.github.abdullahcxd.soliditycore.listener.SolidityListener;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
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
        SolidityEditor.getInstance().reloadAttachedConfiguration(this.getSolidityPluginName());
    }

    @Override
    public void onLoad() {
        SolidityEditor.getInstance().registerPluginMeta(this);

        SolidityEditor.getInstance().registerSenderPrefix(
                getSolidityMetadata().getPluginName(),
                getSolidityMetadata().getPluginLoggerPrefix()
        );

        ConsoleCommandSender console = getConsoleCommandSender();
        SenderUtils.sendWithPrefix(console, getSolidityPluginName(), SenderUtils.separator(32));
        SenderUtils.newline(console);
        SenderUtils.sendWithPrefix(console,
                getSolidityPluginName(),
                "<green>Loading Solidity Plugin <gold>" +
                        getSolidityMetadata().getPluginName() +
                        "</gold> version <gold>" +
                        getSolidityMetadata().getPluginVersion() +
                        "</gold></green>");
        SenderUtils.newline(console);
        SenderUtils.sendWithPrefix(console, getSolidityPluginName(), SenderUtils.separator(32));

        load();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ActionBarManager.initialize(this);
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
        SenderUtils.sendWithPrefix(getConsoleCommandSender(), getSolidityPluginName(),
                "<green>Enabled plugin <gold>" +
                        getSolidityMetadata().getPluginName() + "</gold></green>");
    }

    private void logDisable() {
        SenderUtils.sendWithPrefix(getConsoleCommandSender(), getSolidityPluginName(),
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

    /**
     * Register's a command to the server
     * @deprecated in favor of CommandManager#registerCommand, to simplify duplications and to add more features
     * @param command Command to be registered
     */
    @Deprecated(forRemoval = true, since = "0.0.3")
    public void registerCommand(BaseCommand command) {

        throw new DeprecationException(
                "registerCommand",
                DeprecationException.DeprecatedType.Method,
                "The method was removed in favor of CommandManager#registerCommand"
        );

    }

    public void registerListener(@NotNull SolidityListener listener) {
        listener.initialize(this);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public String getSolidityPluginName() {
        return getSolidityMetadata().getPluginName();
    }

    public ConfigurationSection getAttachedConfiguration() {
        return SolidityEditor.getInstance().getAttachedConfigurationFor(this.getSolidityPluginName());
    }
}
