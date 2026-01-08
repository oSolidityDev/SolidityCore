package io.github.abdullahcxd.soliditycore.builders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A lightweight placeholder builder for resolving {@code {placeholders}} in strings.
 * <p>
 * Supports:
 * <ul>
 *   <li>Static placeholders via {@link #addPlaceholder(String, String)}</li>
 *   <li>Dynamic resolution via {@link Resolver}</li>
 *   <li>Multiple occurrences (all are replaced)</li>
 *   <li>Nested keys like {@code {soliditycore.prefix.plugin}}</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * String result = PlaceholderBuilder.create("Hello {user}, welcome to {server}!")
 *     .addPlaceholder("server", "SolidityCore")
 *     .build(key -> {
 *         if (key.equals("user")) return "Abdullah";
 *         return null;
 *     });
 * }</pre>
 */
@Getter
public final class PlaceholderBuilder {

    /**
     * Creates a new {@link PlaceholderBuilder} with the given template content.
     *
     * @param content the string containing placeholders
     * @return a new builder instance
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull PlaceholderBuilder create(@NotNull String content) {
        return new PlaceholderBuilder(content);
    }

    /**
     * Resolves placeholders that were not statically defined.
     */
    @FunctionalInterface
    public interface Resolver {

        /**
         * Resolves a placeholder key.
         *
         * @param key placeholder name without braces
         * @return replacement value, or {@code null} to keep the placeholder
         */
        @Nullable String resolve(@NotNull String key);
    }

    /**
     * Represents a static placeholder entry.
     */
    @Getter
    @AllArgsConstructor
    public static final class Placeholder {
        private final String key;
        private final String value;
    }

    private final String template;
    private final Map<String, String> placeholders = new HashMap<>();

    private PlaceholderBuilder(String template) {
        this.template = Objects.requireNonNull(template, "template");
    }

    /**
     * Clears all registered static placeholders.
     *
     * @return this builder instance
     */
    public PlaceholderBuilder clear() {
        this.placeholders.clear();
        return this;
    }

    /**
     * Adds or overrides a static placeholder.
     *
     * @param key   placeholder key (without braces)
     * @param value replacement value
     * @return this builder instance
     */
    public PlaceholderBuilder addPlaceholder(@NotNull String key, @NotNull String value) {
        this.placeholders.put(key, value);
        return this;
    }

    /**
     * Adds multiple static placeholders from a map.
     *
     * @param placeholders map of key-value pairs
     * @return this builder instance
     */
    public PlaceholderBuilder addPlaceholders(@NotNull Map<String, String> placeholders) {
        this.placeholders.putAll(placeholders);
        return this;
    }

    /**
     * Builds the final string, resolving placeholders.
     *
     * <p>
     * Resolution order:
     * <ol>
     *   <li>Static placeholders</li>
     *   <li>Dynamic resolver</li>
     *   <li>Unresolved placeholders remain unchanged</li>
     * </ol>
     *
     * @param resolver optional dynamic resolver
     * @return resolved string
     */
    public @NotNull String build(@Nullable Resolver resolver) {

        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < template.length()) {

            char c = template.charAt(i);

            if (c == '{') {
                int end = template.indexOf('}', i + 1);

                if (end != -1) {
                    String key = template.substring(i + 1, end);

                    // 1️⃣ Static placeholder
                    String replacement = placeholders.get(key);

                    // 2️⃣ Dynamic resolver
                    if (replacement == null && resolver != null) {
                        replacement = resolver.resolve(key);
                    }

                    // 3️⃣ Fallback
                    result.append(
                            replacement != null ? replacement : '{' + key + '}'
                    );

                    i = end + 1;
                    continue;
                }
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    /**
     * Builds the final string using only static placeholders.
     *
     * @return resolved string
     */
    public @NotNull String build() {
        return build(null);
    }

    /**
     * Checks if the template contains any placeholders.
     *
     * @return true if placeholders exist
     */
    public boolean hasPlaceholders() {
        return template.contains("{") && template.contains("}");
    }

    /**
     * Extracts all placeholder keys from the template.
     *
     * @return set of placeholder keys (without braces)
     */
    public @NotNull Set<String> extractPlaceholderKeys() {
        Set<String> keys = new HashSet<>();
        int i = 0;

        while (i < template.length()) {
            char c = template.charAt(i);

            if (c == '{') {
                int end = template.indexOf('}', i + 1);
                if (end != -1) {
                    String key = template.substring(i + 1, end);
                    keys.add(key);
                    i = end + 1;
                    continue;
                }
            }
            i++;
        }

        return keys;
    }
}