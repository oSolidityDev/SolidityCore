package io.github.abdullahcxd.soliditycore.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves command arguments based on their type
 */
public class ArgumentResolver {

    /**
     * Parse and resolve arguments based on CommandInfo
     */
    public static void resolveArguments(CommandContext context, @NotNull CommandInfo commandInfo, List<String> rawArgs) {
        List<CommandInfo.CommandArgument> argumentDefinitions = commandInfo.getArguments();

        for (int i = 0; i < argumentDefinitions.size() && i < rawArgs.size(); i++) {
            CommandInfo.CommandArgument argDef = argumentDefinitions.get(i);
            String rawValue = rawArgs.get(i);

            try {
                Object parsedValue = parseArgument(rawValue, argDef.getType());
                context.put(argDef.getName(), argDef.getPosition(), parsedValue);
            } catch (ArgumentParseException e) {
                // If parsing fails, store as string and let the command handle it
                context.put(argDef.getName(), argDef.getPosition(), rawValue);
            }
        }
    }

    /**
     * Parse a raw string argument into its proper type
     */
    private static Object parseArgument(String raw, CommandInfo.ArgumentType type) throws ArgumentParseException {
        if (raw == null || raw.isEmpty()) {
            throw new ArgumentParseException("Argument cannot be empty");
        }

        try {
            switch (type) {
                case INTEGER:
                    return Integer.parseInt(raw);

                case DOUBLE:
                    return Double.parseDouble(raw);

                case BOOLEAN:
                    return parseBoolean(raw);

                case PLAYER:
                    Player player = Bukkit.getPlayer(raw);
                    if (player == null) {
                        throw new ArgumentParseException("Player '" + raw + "' is not online");
                    }
                    return player;

                case OFFLINE_PLAYER:
                    return Bukkit.getOfflinePlayer(raw);

                case MATERIAL:
                    Material material = Material.matchMaterial(raw.toUpperCase());
                    if (material == null) {
                        throw new ArgumentParseException("Material '" + raw + "' does not exist");
                    }
                    return material;

                case WORLD:
                    World world = Bukkit.getWorld(raw);
                    if (world == null) {
                        throw new ArgumentParseException("World '" + raw + "' does not exist");
                    }
                    return world;

                default:
                    return raw;
            }
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("Invalid number format: " + raw);
        }
    }

    private static boolean parseBoolean(@NotNull String value) throws ArgumentParseException {
        String lower = value.toLowerCase();
        if (lower.equals("true") || lower.equals("yes") || lower.equals("on") || lower.equals("1")) {
            return true;
        }
        if (lower.equals("false") || lower.equals("no") || lower.equals("off") || lower.equals("0")) {
            return false;
        }
        throw new ArgumentParseException("Invalid boolean value: " + value);
    }

    /**
     * Exception thrown when argument parsing fails
     */
    public static class ArgumentParseException extends Exception {
        public ArgumentParseException(String message) {
            super(message);
        }
    }
}