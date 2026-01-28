package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.actionbar.ActionBarManager;
import io.github.abdullahcxd.soliditycore.commands.BaseCommand;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.exception.DeprecationException;
import io.github.abdullahcxd.soliditycore.listener.SolidityListener;
import io.github.abdullahcxd.soliditycore.storage.StorageManager;
import io.github.abdullahcxd.soliditycore.storage.StorageType;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import lombok.Getter;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Base class for all Solidity-based plugins.
 * Provides lifecycle management, storage integration, and common utilities.
 */
@Getter
public abstract class SolidityPlugin extends JavaPlugin {

    private StorageManager storageManager;

    /**
     * Gets the metadata for this Solidity plugin.
     * Must be implemented by subclasses to provide plugin information.
     *
     * @return The plugin metadata (must not be null)
     */
    @NotNull
    public abstract SolidityMetadata getSolidityMetadata();

    /**
     * Called during the plugin load phase.
     * Override this to perform initialization that doesn't depend on other plugins.
     */
    public abstract void load();

    /**
     * Called during the plugin enable phase.
     * Override this to perform initialization that may depend on other plugins.
     */
    public abstract void enable();

    /**
     * Called during the plugin disable phase.
     * Override this to perform cleanup and save data.
     */
    public abstract void disable();

    /**
     * Reloads plugin configurations.
     * Override this if you have multiple configuration files to reload.
     * Default implementation reloads the main config and attached configurations.
     */
    public void reloadConfigurations() {
        try {
            reloadConfig();
            SolidityEditor.getInstance().reloadAttachedConfiguration(this.getSolidityPluginName());
            getLogger().info("Configurations reloaded successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload configurations", e);
            SenderUtils.error(getConsoleCommandSender(), "Failed to reload configurations: " + e.getMessage());
        }
    }

    @Override
    public final void onLoad() {
        try {
            // Register with SolidityEditor
            SolidityEditor.getInstance().registerPluginMeta(this);
            SolidityEditor.getInstance().registerSenderPrefix(
                    getSolidityMetadata().getPluginName(),
                    getSolidityMetadata().getPluginLoggerPrefix()
            );

            // Log loading message
            logLoadStart();

            // Call user implementation
            load();

            // Initialize storage providers if configured
            if (storageManager != null) {
                storageManager.loadProviders();
            }

            getLogger().info("Plugin loaded successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load plugin", e);
            throw new RuntimeException("Plugin load failed", e);
        }
    }

    @Override
    public final void onEnable() {
        try {
            // Initialize ActionBar manager
            ActionBarManager.initialize(this);

            // Log enable message
            logEnable();

            // Check dependencies if any
            if (hasDependencies() && !checkDependenciesLoaded()) {
                getLogger().severe("Required dependencies are not loaded. Disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // Call user implementation
            enable();

            // Start storage providers if configured
            if (storageManager != null) {
                storageManager.enableProviders();
            }

            getLogger().info("Plugin enabled successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onDisable() {
        try {
            // Close storage providers first
            if (storageManager != null) {
                storageManager.closeProviders();
            }

            // Call user implementation
            disable();

            // Log disable message
            logDisable();

            getLogger().info("Plugin disabled successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin disable", e);
        }
    }

    /**
     * Logs the plugin loading start message.
     */
    private void logLoadStart() {
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
    }

    /**
     * Logs the plugin enable message.
     */
    private void logEnable() {
        SenderUtils.sendWithPrefix(getConsoleCommandSender(), getSolidityPluginName(),
                "<green>Enabled plugin <gold>" +
                        getSolidityMetadata().getPluginName() + "</gold></green>");
    }

    /**
     * Logs the plugin disable message.
     */
    private void logDisable() {
        SenderUtils.sendWithPrefix(getConsoleCommandSender(), getSolidityPluginName(),
                "<red>Disabled plugin <gold>" +
                        getSolidityMetadata().getPluginName() + "</gold></red>");
    }

    /**
     * Gets the console command sender.
     *
     * @return The console sender
     */
    @NotNull
    public ConsoleCommandSender getConsoleCommandSender() {
        return getServer().getConsoleSender();
    }

    /**
     * Checks if this plugin has dependencies.
     *
     * @return true if dependencies are declared
     */
    public boolean hasDependencies() {
        return getSolidityMetadata().getDependencies() != null &&
                !getSolidityMetadata().getDependencies().isEmpty();
    }

    /**
     * Checks if all declared dependencies are loaded.
     *
     * @return true if all dependencies are loaded
     */
    public boolean checkDependenciesLoaded() {
        boolean allLoaded = true;
        for (String dep : getSolidityMetadata().getDependencies()) {
            if (!SolidityEditor.getInstance().isPluginLoaded(dep)) {
                SenderUtils.error(getConsoleCommandSender(),
                        "Dependency not loaded: " + dep);
                getLogger().severe("Required dependency not loaded: " + dep);
                allLoaded = false;
            }
        }
        return allLoaded;
    }

    /**
     * Registers a command to the server.
     *
     * @param command Command to be registered
     * @deprecated in favor of CommandManager#registerCommand
     */
    @Deprecated(forRemoval = true, since = "0.0.3")
    public void registerCommand(BaseCommand command) {
        throw new DeprecationException(
                "registerCommand",
                DeprecationException.DeprecatedType.Method,
                "The method was removed in favor of CommandManager#registerCommand"
        );
    }

    /**
     * Registers a listener with the plugin.
     *
     * @param listener The listener to register (must not be null)
     */
    public void registerListener(@NotNull SolidityListener listener) {

        try {
            listener.initialize(this);
            getServer().getPluginManager().registerEvents(listener, this);
            getLogger().info("Registered listener: " + listener.getClass().getSimpleName());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register listener: " +
                    listener.getClass().getSimpleName(), e);
        }
    }

    /**
     * Gets the plugin name from metadata.
     *
     * @return The plugin name
     */
    @NotNull
    public String getSolidityPluginName() {
        return getSolidityMetadata().getPluginName();
    }

    /**
     * Gets the attached configuration for this plugin.
     *
     * @return The attached configuration section, or null if not found
     */
    @Nullable
    public ConfigurationSection getAttachedConfiguration() {
        return SolidityEditor.getInstance().getAttachedConfigurationFor(this.getSolidityPluginName());
    }

    /**
     * Initializes the storage manager with the specified storage type.
     * This should be called in the {@link #load()} method before any providers are registered.
     *
     * @param type The storage type to use (must not be null)
     * @throws IllegalArgumentException if type is null
     * @throws IllegalStateException    if storage manager is already initialized
     */
    public void initializeStorageManager(@NotNull StorageType type) {

        if (storageManager != null) {
            throw new IllegalStateException("Storage manager is already initialized");
        }

        storageManager = new StorageManager(this, type);
        getLogger().info("Initialized storage manager with type: " + type.getDisplayName());
    }

    /**
     * Checks if the storage manager is initialized.
     *
     * @return true if storage manager is initialized
     */
    public boolean hasStorageManager() {
        return storageManager != null;
    }
}