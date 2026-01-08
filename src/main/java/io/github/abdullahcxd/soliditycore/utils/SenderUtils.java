package io.github.abdullahcxd.soliditycore.utils;

import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.prefix.PrefixManager;
import io.github.abdullahcxd.soliditycore.prefix.PrefixManager.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Utility methods for sending formatted messages to CommandSenders.
 */
public final class SenderUtils {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static PrefixManager prefixManager;

    public static final Prefix SOLIDITY_PREFIX = PrefixManager.Presets.solidity();

    static {
        prefixManager = SolidityEditor.getInstance().getPrefixManager();
    }

    private SenderUtils() {}

    /**
     * Gets the global PrefixManager instance
     */
    public static @NotNull PrefixManager getPrefixManager() {
        return prefixManager;
    }

    /**
     * Sets a custom PrefixManager
     */
    public static void setPrefixManager(@NotNull PrefixManager manager) {
        prefixManager = manager;
    }


    public static void send(@NotNull CommandSender sender,
                            @NotNull String miniMessage) {
        sender.sendMessage(MINI.deserialize(miniMessage));
    }

    public static void send(@NotNull CommandSender sender,
                            @NotNull Component component) {
        sender.sendMessage(component);
    }

    public static void send(@NotNull Collection<? extends CommandSender> senders,
                            @NotNull Component component) {
        for (CommandSender sender : senders) {
            sender.sendMessage(component);
        }
    }


    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull String miniMessage) {
        sender.sendMessage(SOLIDITY_PREFIX.append(miniMessage));
    }

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull Component component) {
        sender.sendMessage(SOLIDITY_PREFIX.append(component));
    }


    /**
     * Sends a message with a registered prefix
     *
     * @param sender The recipient
     * @param prefixKey The registered prefix key
     * @param miniMessage The message in MiniMessage format
     */
    public static void sendWithPrefix(@NotNull CommandSender sender,
                                      @NotNull String prefixKey,
                                      @NotNull String miniMessage) {
        Prefix prefix = prefixManager.get(prefixKey);
        sender.sendMessage(prefix.append(miniMessage));
    }

    /**
     * Sends a component with a registered prefix
     */
    public static void sendWithPrefix(@NotNull CommandSender sender,
                                      @NotNull String prefixKey,
                                      @NotNull Component component) {
        Prefix prefix = prefixManager.get(prefixKey);
        sender.sendMessage(prefix.append(component));
    }


    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull Prefix prefix,
                                    @NotNull String miniMessage) {
        sender.sendMessage(prefix.append(miniMessage));
    }

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull Prefix prefix,
                                    @NotNull Component component) {
        sender.sendMessage(prefix.append(component));
    }


    public static void send(@NotNull CommandSender sender,
                            @NotNull MessageBuilder builder) {
        sender.sendMessage(builder.build());
    }

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull MessageBuilder builder) {
        sender.sendMessage(SOLIDITY_PREFIX.append(builder.build()));
    }

    public static void sendWithPrefix(@NotNull CommandSender sender,
                                      @NotNull String prefixKey,
                                      @NotNull MessageBuilder builder) {
        Prefix prefix = prefixManager.get(prefixKey);
        sender.sendMessage(prefix.append(builder.build()));
    }

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull Prefix prefix,
                                    @NotNull MessageBuilder builder) {
        sender.sendMessage(prefix.append(builder.build()));
    }


    public static void success(@NotNull CommandSender sender,
                               @NotNull String message) {
        sendWithPrefix(sender, "success", message);
    }

    public static void error(@NotNull CommandSender sender,
                             @NotNull String message) {
        sendWithPrefix(sender, "error", message);
    }

    public static void warning(@NotNull CommandSender sender,
                               @NotNull String message) {
        sendWithPrefix(sender, "warning", message);
    }

    public static void info(@NotNull CommandSender sender,
                            @NotNull String message) {
        sendWithPrefix(sender, "info", message);
    }

    public static void debug(@NotNull CommandSender sender,
                             @NotNull String message) {
        sendWithPrefix(sender, "debug", message);
    }


    public static void console(@NotNull String miniMessage) {
        Bukkit.getConsoleSender().sendMessage(MINI.deserialize(miniMessage));
    }

    public static void consolePrefixed(@NotNull String miniMessage) {
        Bukkit.getConsoleSender().sendMessage(SOLIDITY_PREFIX.append(miniMessage));
    }

    public static void consoleWithPrefix(@NotNull String prefixKey,
                                         @NotNull String miniMessage) {
        Prefix prefix = prefixManager.get(prefixKey);
        Bukkit.getConsoleSender().sendMessage(prefix.append(miniMessage));
    }

    public static void consolePrefixed(@NotNull Prefix prefix,
                                       @NotNull String miniMessage) {
        Bukkit.getConsoleSender().sendMessage(prefix.append(miniMessage));
    }


    /**
     * Sends the same message to multiple recipients
     */
    public static void broadcast(@NotNull Collection<? extends CommandSender> senders,
                                 @NotNull String miniMessage) {
        Component message = MINI.deserialize(miniMessage);
        for (CommandSender sender : senders) {
            sender.sendMessage(message);
        }
    }

    /**
     * Broadcasts with a prefix
     */
    public static void broadcastPrefixed(@NotNull Collection<? extends CommandSender> senders,
                                         @NotNull String miniMessage) {
        Component message = SOLIDITY_PREFIX.append(miniMessage);
        for (CommandSender sender : senders) {
            sender.sendMessage(message);
        }
    }

    /**
     * Broadcasts with a registered prefix key
     */
    public static void broadcastWithPrefix(@NotNull Collection<? extends CommandSender> senders,
                                           @NotNull String prefixKey,
                                           @NotNull String miniMessage) {
        Prefix prefix = prefixManager.get(prefixKey);
        Component message = prefix.append(miniMessage);
        for (CommandSender sender : senders) {
            sender.sendMessage(message);
        }
    }


    public static void newline(@NotNull CommandSender sender) {
        sender.sendMessage(Component.empty());
    }

    @Contract(pure = true)
    public static @NotNull Component separator(int length) {
        return MINI.deserialize(
                "<dark_gray><strikethrough>" + " ".repeat(length) + "</strikethrough>"
        );
    }

    /**
     * Creates a title-style header with separators
     */
    public static void sendHeader(@NotNull CommandSender sender,
                                  @NotNull String title,
                                  int separatorLength) {
        sender.sendMessage(separator(separatorLength));
        sendPrefixed(sender, title);
        sender.sendMessage(separator(separatorLength));
    }

    /**
     * Creates a header with custom prefix
     */
    public static void sendHeader(@NotNull CommandSender sender,
                                  @NotNull String prefixKey,
                                  @NotNull String title,
                                  int separatorLength) {
        sender.sendMessage(separator(separatorLength));
        sendWithPrefix(sender, prefixKey, title);
        sender.sendMessage(separator(separatorLength));
    }
}