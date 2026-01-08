package io.github.abdullahcxd.soliditycore.editor;

import io.github.abdullahcxd.soliditycore.SolidityCore;
import io.github.abdullahcxd.soliditycore.SolidityMetadata;
import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import io.github.abdullahcxd.soliditycore.placeholder.PlaceholderResolver;
import io.github.abdullahcxd.soliditycore.prefix.PrefixManager;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SolidityEditor {

    @Getter
    private static final SolidityEditor instance = new SolidityEditor();

    private final Map<String, SolidityMetadata> metadata;
    private SolidityCore core;
    private PrefixManager prefixManager;
    private PlaceholderResolver placeholderResolver;

    private List<String> blacklistedPlugins;
    private final Map<String, ConfigurationSection> attachedConfigurations;

    private final Map<String, Object> custom_fields = new HashMap<>();

    private SolidityEditor() {
        this.metadata = new HashMap<>();
        this.attachedConfigurations = new HashMap<>();
    }

    public void registerPluginMeta(@NotNull SolidityPlugin plugin) {
        this.metadata.put(plugin.getSolidityPluginName(), plugin.getSolidityMetadata());
    }

    public void unregisterPluginMeta(@NotNull SolidityPlugin plugin) {
        this.metadata.remove(plugin.getSolidityPluginName());
    }

    public Map<String, String> getPluginsVersion() {
        return metadata.values().stream()
                .collect(Collectors.toMap(SolidityMetadata::getPluginName, SolidityMetadata::getPluginVersion));
    }

    public Optional<SolidityMetadata> getPlugin(@NotNull String name) {
        return metadata.values().stream()
                .filter(m -> m.getPluginName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<String> getAuthors(@NotNull String name) {
        return getPlugin(name)
                .map(SolidityMetadata::getAuthors)
                .orElse(Collections.emptyList());
    }

    public void broadcastPluginInfo() {
        metadata.values().forEach(meta -> {
            System.out.println(meta.getPluginName() + " v" + meta.getPluginVersion()
                    + " by " + String.join(", ", meta.getAuthors()));
        });
    }

    public int reloadAll() {
        int reloaded = 0;

        reloadNecessaryStuff();
        reloadAttachedConfigurations();
        reloadCustomFields();

        for (SolidityMetadata meta : metadata.values()) {
            Optional<SolidityPlugin> pluginOptional = core.getServer().getPluginManager()
                    .getPlugin(meta.getPluginName()) instanceof SolidityPlugin sp ? Optional.of(sp) : Optional.empty();

            if (pluginOptional.isPresent()) {
                SolidityPlugin plugin = pluginOptional.get();
                if (isBlacklisted(plugin)) {
                    disablePlugin(plugin);
                    continue;
                }

                try {
                    plugin.reloadConfigurations(); // reload the plugin configuration
                    reloaded++;
                } catch (Exception e) {
                    throw new SolidityException(e);
                }
            } else {
                System.err.println("Plugin " + meta.getPluginName() + " not found or not a SolidityPlugin.");
            }
        }

        return reloaded;
    }

    public void initialize() {
        prefixManager = new PrefixManager(SenderUtils.SOLIDITY_PREFIX);

        prefixManager.register("solidity", SenderUtils.SOLIDITY_PREFIX);
        prefixManager.register("info", PrefixManager.Presets.info());
        prefixManager.register("success", PrefixManager.Presets.success());
        prefixManager.register("error", PrefixManager.Presets.error());
        prefixManager.register("warning", PrefixManager.Presets.warning());
        prefixManager.register("debug", PrefixManager.Presets.debug());
        prefixManager.register("system", PrefixManager.Presets.system());

        // Initialize the placeholder resolver
        placeholderResolver = new PlaceholderResolver(this);
    }

    public boolean isBlacklisted(String name) {
        return blacklistedPlugins.contains(name);
    }

    public boolean isBlacklisted(@NotNull SolidityPlugin plugin) {
        return isBlacklisted(plugin.getSolidityPluginName());
    }

    public void registerSenderPrefix(String keyId, String prefix) {
        prefixManager.register(keyId, prefix);
    }

    public void reloadAttachedConfigurations() {
        ConfigurationSection attachedConfigSection = getCore().getConfig().getConfigurationSection("plugins.attachedConfigurations");
        if (attachedConfigSection == null) throw new SolidityException("Attached configuration section must be included under the SolidityCore config file in the plugins section!");

        this.attachedConfigurations.clear();

        Set<String> keys = attachedConfigSection.getKeys(false);
        for (String key : keys) {
            reloadAttachedConfiguration(key);
        }
    }

    public void reloadAttachedConfiguration(String pluginName) {
        ConfigurationSection attachedConfigSection = getCore().getConfig().getConfigurationSection("plugins.attachedConfigurations");
        if (attachedConfigSection == null) throw new SolidityException("Attached configuration section must be included under the SolidityCore config file in the plugins section!");

        reloadAttachedConfiguration(attachedConfigSection, pluginName);
    }

    public void reloadAttachedConfiguration(@NotNull ConfigurationSection attachedConfigSection, String pluginName) {
        ConfigurationSection section = attachedConfigSection.getConfigurationSection(pluginName);
        if (section == null) return;

        this.attachedConfigurations.put(pluginName, section);
    }

    public void reloadCustomFields() {
        ConfigurationSection customFieldsSection =
                getCore().getConfig().getConfigurationSection("custom_fields");

        if (customFieldsSection == null)
            throw new SolidityException(
                    "Custom Fields section must be included under the SolidityCore config file!"
            );

        this.custom_fields.clear();

        for (String key : customFieldsSection.getKeys(false)) {
            Object value = customFieldsSection.get(key);

            if (value instanceof String || value instanceof Number) {
                this.custom_fields.put(key, value);
            }
        }
    }

    public void setCore(@NotNull SolidityCore core) {
        core.reloadConfig();
        this.core = core;

        reloadNecessaryStuff();
        reloadAttachedConfigurations();
        reloadCustomFields();
    }

    public void disablePlugin(SolidityPlugin plugin) {
        getPluginManager().disablePlugin(plugin);
        unregisterPluginMeta(plugin);
    }

    public PluginManager getPluginManager() {
        return Bukkit.getPluginManager();
    }

    private void reloadNecessaryStuff() {
        this.blacklistedPlugins = core.getConfig().getStringList("plugins.blacklisted");
    }

    public boolean isPluginLoaded(@NotNull String name) {
        return getPlugin(name).isPresent();
    }

    public ConfigurationSection getAttachedConfigurationFor(String pluginName) {
        return this.attachedConfigurations.get(pluginName);
    }

    /**
     * Gets a value from attached configuration with placeholder resolution.
     *
     * @param pluginName the plugin name
     * @param path       the configuration path
     * @return the resolved value, or null if not found
     */
    public @Nullable Object getAttachedConfigurationValue(String pluginName, String path) {
        ConfigurationSection section = getAttachedConfigurationFor(pluginName);

        if (section == null) {
            return null;
        }

        Object value = section.get(path);

        // Resolve placeholders if it's a string
        if (value instanceof String stringValue && placeholderResolver != null) {
            return placeholderResolver.resolveString(stringValue);
        }

        return value;
    }

    /**
     * Gets a resolved map of all values in an attached configuration.
     * All string values will have their placeholders resolved.
     *
     * @param pluginName the plugin name
     * @return map of resolved values, or empty map if section not found
     */
    public @NotNull Map<String, Object> getResolvedAttachedConfiguration(String pluginName) {
        ConfigurationSection section = getAttachedConfigurationFor(pluginName);

        if (section == null) {
            return Collections.emptyMap();
        }

        if (placeholderResolver == null) {
            // Return raw values if resolver not initialized
            Map<String, Object> raw = new HashMap<>();
            section.getKeys(false).forEach(key -> raw.put(key, section.get(key)));
            return raw;
        }

        return placeholderResolver.resolveSection(section);
    }

    /**
     * Adds a custom field programmatically.
     * This can be used by plugins to register their own placeholder values.
     *
     * @param key   the field key
     * @param value the field value (must be String or Number)
     * @throws IllegalArgumentException if value is not String or Number
     */
    public void addCustomField(@NotNull String key, @NotNull Object value) {
        if (!(value instanceof String || value instanceof Number)) {
            throw new IllegalArgumentException("Custom field value must be a String or Number, got: " + value.getClass().getName());
        }
        this.custom_fields.put(key, value);
    }

    /**
     * Removes a custom field.
     *
     * @param key the field key
     * @return the removed value, or null if not found
     */
    public @Nullable Object removeCustomField(@NotNull String key) {
        return this.custom_fields.remove(key);
    }

    /**
     * Resolves placeholders in any string using the placeholder resolver.
     *
     * @param text the text to resolve
     * @return the resolved text
     */
    public @NotNull String resolvePlaceholders(@NotNull String text) {
        if (placeholderResolver == null) {
            return text;
        }
        return placeholderResolver.resolveString(text);
    }
}