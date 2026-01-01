package io.github.abdullahcxd.soliditycore.builders;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Deprecated: When using message builder, use MessageBuilder instead for compatibility with paper plugins using the MessageBuilder#toLegacy
 *
 */
@Deprecated(since = "1.0.0")
public class LegacyMessageBuilder {

    @Contract("_ -> new")
    public static @NotNull String colorize(String content) {
        return ChatColor.translateAlternateColorCodes('&', content);
    }

}
