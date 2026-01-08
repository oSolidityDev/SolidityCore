package io.github.abdullahcxd.soliditycore.placeholder;

import io.github.abdullahcxd.soliditycore.builders.PlaceholderBuilder;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.prefix.PrefixManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves placeholders for SolidityCore configuration system.
 * <p>
 * Supported placeholder namespaces:
 * <ul>
 *   <li>{@code soliditycore.prefix.<key>} - Gets prefix MiniMessage from PrefixManager</li>
 *   <li>{@code soliditycustom.<key>} - Gets custom field value</li>
 *   <li>{@code config.<path>} - Gets value from main config at path</li>
 * </ul>
 */
@RequiredArgsConstructor
public class PlaceholderResolver implements PlaceholderBuilder.Resolver {

    private final SolidityEditor editor;

    /**
     * Resolves a placeholder key to its value.
     *
     * @param key the placeholder key (without braces)
     * @return the resolved value, or null if not found
     */
    @Override
    public @Nullable String resolve(@NotNull String key) {
        // Split by first dot to get namespace and path
        String[] parts = key.split("\\.", 2);
        
        if (parts.length < 2) {
            return null;
        }

        String namespace = parts[0];
        String path = parts[1];

        return switch (namespace.toLowerCase()) {
            case "soliditycore" -> resolveSolidityCore(path);
            case "soliditycustom" -> resolveCustomField(path);
            case "config" -> resolveConfig(path);
            default -> null;
        };
    }

    /**
     * Resolves {@code soliditycore.*} placeholders.
     */
    private @Nullable String resolveSolidityCore(@NotNull String path) {
        String[] parts = path.split("\\.", 2);
        
        if (parts.length < 2) {
            return null;
        }

        String category = parts[0];
        String key = parts[1];

        if ("prefix".equalsIgnoreCase(category)) {
            return resolvePrefixPlaceholder(key);
        }

        return null;
    }

    /**
     * Resolves {@code soliditycore.prefix.<key>} placeholders.
     * Returns the MiniMessage format of the prefix.
     */
    private @Nullable String resolvePrefixPlaceholder(@NotNull String key) {
        PrefixManager prefixManager = editor.getPrefixManager();
        
        if (prefixManager == null) {
            return null;
        }

        PrefixManager.Prefix prefix = prefixManager.getOrNull(key);
        
        if (prefix == null) {
            return null;
        }

        // Return the MiniMessage format with separator
        return prefix.getMiniMessage() + prefix.getSeparator();
    }

    /**
     * Resolves {@code soliditycustom.<key>} placeholders.
     */
    private @Nullable String resolveCustomField(@NotNull String key) {
        Map<String, Object> customFields = editor.getCustom_fields();
        
        if (customFields == null || !customFields.containsKey(key)) {
            return null;
        }

        Object value = customFields.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Resolves {@code config.<path>} placeholders.
     * Gets value from the main SolidityCore config.
     */
    private @Nullable String resolveConfig(@NotNull String path) {
        if (editor.getCore() == null) {
            return null;
        }

        Object value = editor.getCore().getConfig().get(path);
        return value != null ? value.toString() : null;
    }

    /**
     * Resolves all placeholders in a string.
     *
     * @param text the text to process
     * @return the text with placeholders resolved
     */
    public @NotNull String resolveString(@NotNull String text) {
        if (!text.contains("{") || !text.contains("}")) {
            return text;
        }

        return PlaceholderBuilder.create(text).build(this);
    }

    /**
     * Resolves placeholders in all string values within a configuration section recursively.
     *
     * @param section the configuration section to process
     * @return a map with all placeholders resolved
     */
    public @NotNull Map<String, Object> resolveSection(@NotNull ConfigurationSection section) {
        Map<String, Object> resolved = new HashMap<>();

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (value instanceof String stringValue) {
                resolved.put(key, resolveString(stringValue));
            } else if (value instanceof ConfigurationSection subSection) {
                resolved.put(key, resolveSection(subSection));
            } else {
                resolved.put(key, value);
            }
        }

        return resolved;
    }

    /**
     * Creates a default resolver instance using the singleton editor.
     *
     * @return a new resolver instance
     */
    public static @NotNull PlaceholderResolver createDefault() {
        return new PlaceholderResolver(SolidityEditor.getInstance());
    }
}