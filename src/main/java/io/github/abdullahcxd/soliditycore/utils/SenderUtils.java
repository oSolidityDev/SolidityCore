package io.github.abdullahcxd.soliditycore.utils;

import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
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

    /** SolidityCore default prefix (MiniMessage format) */
    private static final String SOLIDITY_PREFIX_MM =
                    "<bold><gradient:#6A5ACD:#8A2BE2>Solidity</gradient>" +
                    "<white>Core</white></bold>" +
                    " <gray>»</gray> ";

    /** Pre-built prefix component */
    private static final Component SOLIDITY_PREFIX =
            MINI.deserialize(SOLIDITY_PREFIX_MM);

    private SenderUtils() {}

    /* ---------------- BASIC SEND ---------------- */

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

    /* ---------------- PREFIXED ---------------- */

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull String miniMessage) {
        sender.sendMessage(
                SOLIDITY_PREFIX.append(MINI.deserialize(miniMessage))
        );
    }

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull Component component) {
        sender.sendMessage(
                SOLIDITY_PREFIX.append(component)
        );
    }

    /* ---------------- BUILDER INTEGRATION ---------------- */

    public static void send(@NotNull CommandSender sender,
                            @NotNull MessageBuilder builder) {
        sender.sendMessage(builder.build());
    }

    public static void sendPrefixed(@NotNull CommandSender sender,
                                    @NotNull MessageBuilder builder) {
        sender.sendMessage(
                SOLIDITY_PREFIX.append(builder.build())
        );
    }

    /* ---------------- COMMON TYPES ---------------- */

    public static void success(@NotNull CommandSender sender,
                               @NotNull String message) {
        sendPrefixed(sender,
                "<green>✔</green> <gray>" + message + "</gray>"
        );
    }

    public static void error(@NotNull CommandSender sender,
                             @NotNull String message) {
        sendPrefixed(sender,
                "<red>✖</red> <gray>" + message + "</gray>"
        );
    }

    public static void warning(@NotNull CommandSender sender,
                               @NotNull String message) {
        sendPrefixed(sender,
                "<yellow>⚠</yellow> <gray>" + message + "</gray>"
        );
    }

    /* ---------------- CONSOLE ---------------- */

    public static void console(@NotNull String miniMessage) {
        Bukkit.getConsoleSender().sendMessage(MINI.deserialize(miniMessage));
    }

    public static void consolePrefixed(@NotNull String miniMessage) {
        Bukkit.getConsoleSender().sendMessage(
                SOLIDITY_PREFIX.append(MINI.deserialize(miniMessage))
        );
    }

    /* ---------------- UTIL ---------------- */

    public static void newline(@NotNull CommandSender sender) {
        sendPrefixed(sender, "");
    }

    @Contract(pure = true)
    public static @NotNull Component separator(int length) {
        return MINI.deserialize(
                "<dark_gray><strikethrough>" + " ".repeat(length) + "</strikethrough>"
        );
    }
}
