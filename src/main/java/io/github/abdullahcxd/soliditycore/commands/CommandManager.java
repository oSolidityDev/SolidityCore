package io.github.abdullahcxd.soliditycore.commands;

import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.exception.DeprecationException;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.logging.Level;

@UtilityClass
public class CommandManager {

    private static CommandMap commandMap;
    private static boolean nmsWarningShown = false;

    /**
     * Registers a command for a SolidityPlugin
     *
     * @param solidityPlugin The plugin instance
     * @param command The command to register
     */
    public static void registerCommand(@NotNull SolidityPlugin solidityPlugin, @NotNull BaseCommand command) {
        command.setSolidityPlugin(solidityPlugin);
        command.initialize();
        registerCommand((JavaPlugin) solidityPlugin, command);
    }

    /**
     * Registers a command for any JavaPlugin (standard registration via plugin.yml)
     *
     * @param plugin The plugin instance
     * @param command The command to register
     * @throws SolidityException if command is null or not found in plugin.yml
     */
    public static void registerCommand(@NotNull JavaPlugin plugin, @NotNull BaseCommand command) {

        PluginCommand pluginCommand = plugin.getCommand(command.getCommandInfo().getName());

        if (pluginCommand == null) {
            throw new SolidityException(
                    "Command with the name of /" + command.getCommandInfo().getName() +
                            " wasn't found in the plugin.yml for " + plugin.getDescription().getName()
            );
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    /**
     * Registers a command dynamically without plugin.yml using NMS/Reflection.
     *
     * <p><b>⚠️ WARNING:</b> This method uses reflection to access internal Bukkit/Spigot APIs.
     * It may break across different server versions or implementations (Paper, Spigot, etc.).
     * Use with caution and always test on your target server version.</p>
     *
     * <p><b>Recommended:</b> Use {@link #registerCommand(JavaPlugin, BaseCommand)} with
     * plugin.yml definitions for better compatibility and stability.</p>
     *
     * @param plugin The plugin instance
     * @param command The command to register
     * @param fallbackPrefix Fallback prefix for the command (usually plugin name)
     * @return true if registration was successful, false otherwise
     */
    public static boolean registerCommandDynamic(@NotNull JavaPlugin plugin,
                                                 @NotNull BaseCommand command,
                                                 @NotNull String fallbackPrefix) {

        showNmsWarning(plugin);

        try {
            // Set plugin first if it's a SolidityPlugin
            if (plugin instanceof SolidityPlugin) {
                command.setSolidityPlugin((SolidityPlugin) plugin);
            }

            command.initialize();

            // Get CommandMap
            CommandMap map = getCommandMap();
            if (map == null) {
                plugin.getLogger().severe("Failed to get CommandMap for dynamic command registration");
                return false;
            }

            // Create PluginCommand
            PluginCommand pluginCommand = createPluginCommand(
                    command.getCommandInfo().getName(),
                    plugin
            );

            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);

            if (command.getCommandInfo().getDescription() != null) {
                pluginCommand.setDescription(command.getCommandInfo().getDescription());
            }

            if (command.getCommandInfo().getUsage() != null) {
                pluginCommand.setUsage(command.getCommandInfo().getUsage());
            }

            if (command.getCommandInfo().getAliases() != null &&
                    !command.getCommandInfo().getAliases().isEmpty()) {
                pluginCommand.setAliases(command.getCommandInfo().getAliases());
            }

            if (command.getCommandInfo().getPermission() != null) {
                pluginCommand.setPermission(command.getCommandInfo().getPermission());
            }

            // Register to CommandMap
            map.register(fallbackPrefix, pluginCommand);

            plugin.getLogger().info("Dynamically registered command: /" + command.getCommandInfo().getName());
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to dynamically register command: " + command.getCommandInfo().getName(), e);
            return false;
        }
    }

    /**
     * Registers a command dynamically with plugin name as fallback prefix
     */
    public static boolean registerCommandDynamic(@NotNull JavaPlugin plugin, @NotNull BaseCommand command) {
        return registerCommandDynamic(plugin, command, plugin.getName().toLowerCase());
    }

    /**
     * Unregisters a command dynamically
     *
     * <p><b>⚠️ WARNING:</b> This method uses reflection. See {@link #registerCommandDynamic} for details.</p>
     *
     * @param commandName The name of the command to unregister
     * @return true if unregistration was successful
     */
    public static boolean unregisterCommand(@NotNull String commandName) {
        try {
            CommandMap map = getCommandMap();
            if (map == null) return false;

            Command command = map.getCommand(commandName);
            if (command == null) return false;

            command.unregister(map);

            // Remove from known commands map
            Field knownCommandsField = map.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Command> knownCommands =
                    (java.util.Map<String, Command>) knownCommandsField.get(map);

            knownCommands.remove(commandName);
            knownCommands.remove(commandName.toLowerCase());

            // Remove aliases
            for (String alias : command.getAliases()) {
                knownCommands.remove(alias);
                knownCommands.remove(alias.toLowerCase());
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the server's CommandMap using reflection
     *
     * @return CommandMap or null if failed
     */
    private static CommandMap getCommandMap() {
        if (commandMap != null) {
            return commandMap;
        }

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            return commandMap;
        } catch (Exception e) {
            throw new SolidityException(e);
        }
    }

    /**
     * Creates a PluginCommand instance using reflection
     *
     * @param name Command name
     * @param plugin Plugin owner
     * @return PluginCommand or null if failed
     */
    private static PluginCommand createPluginCommand(@NotNull String name, @NotNull JavaPlugin plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(
                    String.class,
                    org.bukkit.plugin.Plugin.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            throw new SolidityException(e);
        }
    }

    /**
     * Shows a warning about NMS/Reflection usage (only once per server session)
     */
    private static void showNmsWarning(@NotNull JavaPlugin plugin) {
        if (nmsWarningShown) return;

        plugin.getLogger().warning("╔════════════════════════════════════════════════════════════════╗");
        plugin.getLogger().warning("║           DYNAMIC COMMAND REGISTRATION WARNING                 ║");
        plugin.getLogger().warning("╠════════════════════════════════════════════════════════════════╣");
        plugin.getLogger().warning("║ You are using dynamic command registration via reflection.     ║");
        plugin.getLogger().warning("║                                                                ║");
        plugin.getLogger().warning("║ ⚠️  This method accesses internal Bukkit/Spigot APIs and      ║");
        plugin.getLogger().warning("║     may break across different server versions.               ║");
        plugin.getLogger().warning("║                                                                ║");
        plugin.getLogger().warning("║ Compatibility issues may occur on:                            ║");
        plugin.getLogger().warning("║  • Major Minecraft version updates                            ║");
        plugin.getLogger().warning("║  • Different server implementations (Paper, Purpur, etc.)     ║");
        plugin.getLogger().warning("║  • Server software updates                                    ║");
        plugin.getLogger().warning("║                                                                ║");
        plugin.getLogger().warning("║ 📋 RECOMMENDATION:                                             ║");
        plugin.getLogger().warning("║ Define commands in plugin.yml for better stability            ║");
        plugin.getLogger().warning("╚════════════════════════════════════════════════════════════════╝");

        nmsWarningShown = true;
    }

    /**
     * Checks if a command is registered
     *
     * @param commandName The command name to check
     * @return true if the command is registered
     */
    public static boolean isCommandRegistered(@NotNull String commandName) {
        CommandMap map = getCommandMap();
        if (map == null) return false;
        return map.getCommand(commandName) != null;
    }

    /**
     * Gets information about NMS compatibility
     *
     * @return Compatibility info string
     */
    public static @NotNull String getCompatibilityInfo() {
        String serverVersion = Bukkit.getVersion();
        String bukkitVersion = Bukkit.getBukkitVersion();

        return String.format(
                "Server: %s | Bukkit: %s | CommandMap: %s",
                serverVersion,
                bukkitVersion,
                getCommandMap() != null ? "Available" : "Unavailable"
        );
    }
}