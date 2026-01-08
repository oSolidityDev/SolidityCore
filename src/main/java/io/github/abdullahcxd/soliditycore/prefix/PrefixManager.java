package io.github.abdullahcxd.soliditycore.prefix;

import lombok.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages prefixes for plugins and systems.
 * Allows registration, retrieval, and caching of message prefixes.
 */
@RequiredArgsConstructor
public final class PrefixManager {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    /** Default separator between prefix and message */
    private static final String DEFAULT_SEPARATOR = " <gray>»</gray> ";

    /** Registered prefixes by plugin/system name */
    private final Map<String, Prefix> prefixes = new ConcurrentHashMap<>();

    /** Default prefix for unregistered systems */
    @Getter @Setter
    @NonNull
    private Prefix defaultPrefix;

    /**
     * Registers a prefix for a plugin or system
     */
    public @NotNull Prefix register(@NotNull String key, @NotNull Prefix prefix) {
        prefixes.put(key.toLowerCase(), prefix);
        return prefix;
    }

    /**
     * Registers a prefix from MiniMessage format
     */
    public @NotNull Prefix register(@NotNull String key, @NotNull String miniMessage) {
        return register(key, Prefix.of(miniMessage));
    }

    /**
     * Registers a prefix with custom separator
     */
    public @NotNull Prefix register(@NotNull String key,
                                    @NotNull String miniMessage,
                                    @NotNull String separator) {
        return register(key, Prefix.of(miniMessage, separator));
    }

    /**
     * Gets a registered prefix by key
     */
    public @NotNull Prefix get(@NotNull String key) {
        return prefixes.getOrDefault(key.toLowerCase(), defaultPrefix);
    }

    /**
     * Gets a registered prefix, or null if not found
     */
    public @Nullable Prefix getOrNull(@NotNull String key) {
        return prefixes.get(key.toLowerCase());
    }

    /**
     * Checks if a prefix is registered
     */
    public boolean has(@NotNull String key) {
        return prefixes.containsKey(key.toLowerCase());
    }

    /**
     * Unregisters a prefix
     */
    public @Nullable Prefix unregister(@NotNull String key) {
        return prefixes.remove(key.toLowerCase());
    }

    /**
     * Clears all registered prefixes
     */
    public void clear() {
        prefixes.clear();
    }

    /**
     * Gets the number of registered prefixes
     */
    public int size() {
        return prefixes.size();
    }

    /**
     * Represents a message prefix with optional separator
     */
    @Getter
    @ToString
    @EqualsAndHashCode
    public static final class Prefix {
        private final String miniMessage;
        private final String separator;

        @Getter(AccessLevel.NONE)
        private final Component component;

        @Getter(AccessLevel.NONE)
        private final Component componentWithSeparator;

        private Prefix(@NotNull String miniMessage, @NotNull String separator) {
            this.miniMessage = miniMessage;
            this.separator = separator;
            this.component = MINI.deserialize(miniMessage);
            this.componentWithSeparator = MINI.deserialize(miniMessage + separator);
        }

        /**
         * Creates a prefix with default separator
         */
        public static @NotNull Prefix of(@NotNull String miniMessage) {
            return new Prefix(miniMessage, DEFAULT_SEPARATOR);
        }

        /**
         * Creates a prefix with custom separator
         */
        public static @NotNull Prefix of(@NotNull String miniMessage, @NotNull String separator) {
            return new Prefix(miniMessage, separator);
        }

        /**
         * Creates a prefix from Component (no separator by default)
         */
        public static @NotNull Prefix of(@NotNull Component component) {
            return new Prefix(MINI.serialize(component), DEFAULT_SEPARATOR);
        }

        /**
         * Gets the prefix as a Component (without separator)
         */
        public @NotNull Component asComponent() {
            return component;
        }

        /**
         * Gets the prefix with separator as a Component
         */
        public @NotNull Component asComponentWithSeparator() {
            return componentWithSeparator;
        }

        /**
         * Appends a message to this prefix
         */
        public @NotNull Component append(@NotNull String message) {
            return componentWithSeparator.append(MINI.deserialize(message));
        }

        /**
         * Appends a component to this prefix
         */
        public @NotNull Component append(@NotNull Component component) {
            return componentWithSeparator.append(component);
        }

        /**
         * Creates a new prefix with a different separator
         */
        public @NotNull Prefix withSeparator(@NotNull String newSeparator) {
            return new Prefix(this.miniMessage, newSeparator);
        }

        /**
         * Creates a builder for gradual prefix construction
         */
        public static @NotNull Builder builder() {
            return new Builder();
        }

        /**
         * Builder for creating complex prefixes
         */
        public static final class Builder {
            private final StringBuilder miniMessage = new StringBuilder();
            private String separator = DEFAULT_SEPARATOR;

            /**
             * Adds raw MiniMessage text
             */
            public @NotNull Builder append(@NotNull String miniMessage) {
                this.miniMessage.append(miniMessage);
                return this;
            }

            /**
             * Adds bold text
             */
            public @NotNull Builder bold(@NotNull String text) {
                this.miniMessage.append("<bold>").append(text).append("</bold>");
                return this;
            }

            /**
             * Adds italic text
             */
            public @NotNull Builder italic(@NotNull String text) {
                this.miniMessage.append("<italic>").append(text).append("</italic>");
                return this;
            }

            /**
             * Adds colored text
             */
            public @NotNull Builder color(@NotNull String color, @NotNull String text) {
                this.miniMessage.append("<").append(color).append(">")
                        .append(text)
                        .append("</").append(color).append(">");
                return this;
            }

            /**
             * Adds gradient text
             */
            public @NotNull Builder gradient(@NotNull String from,
                                             @NotNull String to,
                                             @NotNull String text) {
                this.miniMessage.append("<gradient:")
                        .append(from).append(":")
                        .append(to).append(">")
                        .append(text)
                        .append("</gradient>");
                return this;
            }

            /**
             * Sets custom separator
             */
            public @NotNull Builder separator(@NotNull String separator) {
                this.separator = separator;
                return this;
            }

            /**
             * Builds the prefix
             */
            public @NotNull Prefix build() {
                return new Prefix(miniMessage.toString(), separator);
            }
        }
    }

    /**
     * Common prefix presets
     */
    @UtilityClass
    public static class Presets {

        public static @NotNull Prefix solidity() {
            return Prefix.of(
                    "<bold><gradient:#6A5ACD:#8A2BE2>Solidity</gradient><white>Core</white></bold>"
            );
        }

        public static @NotNull Prefix info() {
            return Prefix.of("<blue>Info</blue>");
        }

        public static @NotNull Prefix success() {
            return Prefix.of("<green>✔ Success</green>");
        }

        public static @NotNull Prefix error() {
            return Prefix.of("<red>✖ Error</red>");
        }

        public static @NotNull Prefix warning() {
            return Prefix.of("<yellow>⚠ Warning</yellow>");
        }

        public static @NotNull Prefix debug() {
            return Prefix.of("<gray>[DEBUG]</gray>");
        }

        public static @NotNull Prefix system() {
            return Prefix.of("<dark_gray>[System]</dark_gray>");
        }

        public static @NotNull Prefix custom(@NotNull String name, @NotNull String color) {
            return Prefix.of("<" + color + ">" + name + "</" + color + ">");
        }
    }
}