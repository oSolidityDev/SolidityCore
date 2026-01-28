package io.github.abdullahcxd.soliditycore.storage.providers;

import io.github.abdullahcxd.soliditycore.storage.AbstractStorageProvider;
import io.github.abdullahcxd.soliditycore.storage.StorageException;
import io.github.abdullahcxd.soliditycore.storage.StorageType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * YAML storage provider implementation using Bukkit's configuration API.
 * Stores data in YAML files in the plugin's data folder.
 */
public class YAMLStorageProvider extends AbstractStorageProvider {

    private File storageDirectory;
    private final Map<String, YamlConfiguration> cache;
    private boolean autoSave;
    private String directoryPath;
    private boolean createDefaults;

    /**
     * Creates a new YAML storage provider.
     *
     * @param logger Logger instance for this provider
     */
    public YAMLStorageProvider(@NotNull Logger logger) {
        super("YAML", StorageType.YAML, logger);
        this.cache = new HashMap<>();
    }

    @Override
    protected void doInitialize(FileConfiguration configuration) throws Exception {
        // Get configuration
        this.directoryPath = getConfigValue("storage.yaml.directory", "data");
        this.autoSave = getConfigValue("storage.yaml.auto-save", true);
        this.createDefaults = getConfigValue("storage.yaml.create-defaults", false);

        // Create storage directory
        File pluginDataFolder = new File(configuration.getName()).getParentFile();
        this.storageDirectory = new File(pluginDataFolder, directoryPath);

        if (!storageDirectory.exists()) {
            if (!storageDirectory.mkdirs()) {
                throw new StorageException("Failed to create storage directory: " + storageDirectory.getAbsolutePath());
            }
        }

        logger.info("YAML storage directory: " + storageDirectory.getAbsolutePath());
    }

    @Override
    protected void doStart() throws Exception {
        // Load existing YAML files into cache
        File[] files = storageDirectory.listFiles((dir, name) -> 
            name.endsWith(".yml") || name.endsWith(".yaml"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    String key = file.getName().replaceAll("\\.(yml|yaml)$", "");
                    cache.put(key, yaml);
                    logger.fine("Loaded YAML file: " + key);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to load YAML file: " + file.getName(), e);
                }
            }
            logger.info("Loaded " + cache.size() + " YAML files into cache");
        }
    }

    @Override
    protected void doClose() {
        // Save all cached data
        saveAll();
        cache.clear();
    }

    @Override
    public boolean testConnection() {
        if (!isActive()) {
            return false;
        }
        return storageDirectory != null && storageDirectory.exists() && storageDirectory.canWrite();
    }

    /**
     * Gets a YAML configuration by key.
     * Creates a new configuration if it doesn't exist and createDefaults is enabled.
     *
     * @param key The key (file name without extension)
     * @return The YAML configuration, or null if not found and createDefaults is false
     */
    @Nullable
    public YamlConfiguration get(@NotNull String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        YamlConfiguration config = cache.get(key);
        
        if (config == null && createDefaults) {
            config = new YamlConfiguration();
            cache.put(key, config);
            logger.fine("Created new YAML configuration: " + key);
        }
        
        return config;
    }

    /**
     * Gets or creates a YAML configuration by key.
     *
     * @param key The key (file name without extension)
     * @return The YAML configuration (never null)
     */
    @NotNull
    public YamlConfiguration getOrCreate(@NotNull String key) {
        YamlConfiguration config = get(key);
        if (config == null) {
            config = new YamlConfiguration();
            cache.put(key, config);
        }
        return config;
    }

    /**
     * Saves a YAML configuration with the given key.
     *
     * @param key    The key (file name without extension)
     * @param config The YAML configuration to save
     * @throws StorageException if save fails
     */
    public void save(@NotNull String key, @NotNull YamlConfiguration config) throws StorageException {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        cache.put(key, config);

        if (autoSave) {
            writeToFile(key, config);
        }
    }

    /**
     * Sets a value in a YAML configuration and optionally saves it.
     *
     * @param key   The key (file name without extension)
     * @param path  The configuration path
     * @param value The value to set
     * @throws StorageException if save fails
     */
    public void set(@NotNull String key, @NotNull String path, @Nullable Object value) throws StorageException {
        YamlConfiguration config = getOrCreate(key);
        config.set(path, value);
        
        if (autoSave) {
            writeToFile(key, config);
        }
    }

    /**
     * Gets a value from a YAML configuration.
     *
     * @param key  The key (file name without extension)
     * @param path The configuration path
     * @return The value, or null if not found
     */
    @Nullable
    public Object getValue(@NotNull String key, @NotNull String path) {
        YamlConfiguration config = get(key);
        return config != null ? config.get(path) : null;
    }

    /**
     * Gets a value from a YAML configuration with a default.
     *
     * @param key          The key (file name without extension)
     * @param path         The configuration path
     * @param defaultValue The default value
     * @param <T>          The value type
     * @return The value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(@NotNull String key, @NotNull String path, @NotNull T defaultValue) {
        YamlConfiguration config = get(key);
        if (config == null) {
            return defaultValue;
        }
        
        Object value = config.get(path);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return (T) value;
        } catch (ClassCastException e) {
            logger.warning("Type mismatch for " + key + "." + path + ", returning default");
            return defaultValue;
        }
    }

    /**
     * Gets a configuration section from a YAML configuration.
     *
     * @param key  The key (file name without extension)
     * @param path The configuration path
     * @return The configuration section, or null if not found
     */
    @Nullable
    public ConfigurationSection getSection(@NotNull String key, @NotNull String path) {
        YamlConfiguration config = get(key);
        return config != null ? config.getConfigurationSection(path) : null;
    }

    /**
     * Deletes a YAML file by key.
     *
     * @param key The key (file name without extension)
     * @return true if the file was deleted
     */
    public boolean delete(@NotNull String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        cache.remove(key);

        // Try both .yml and .yaml extensions
        File ymlFile = new File(storageDirectory, key + ".yml");
        File yamlFile = new File(storageDirectory, key + ".yaml");
        
        boolean deleted = false;
        if (ymlFile.exists()) {
            deleted = ymlFile.delete();
        }
        if (yamlFile.exists()) {
            deleted = yamlFile.delete() || deleted;
        }
        
        return deleted;
    }

    /**
     * Checks if a key exists in storage.
     *
     * @param key The key to check
     * @return true if the key exists
     */
    public boolean exists(@NotNull String key) {
        return cache.containsKey(key);
    }

    /**
     * Saves all cached data to disk.
     */
    public void saveAll() {
        int saved = 0;
        int failed = 0;

        for (Map.Entry<String, YamlConfiguration> entry : cache.entrySet()) {
            try {
                writeToFile(entry.getKey(), entry.getValue());
                saved++;
            } catch (StorageException e) {
                logger.log(Level.WARNING, "Failed to save YAML file: " + entry.getKey(), e);
                failed++;
            }
        }

        logger.info(String.format("Saved YAML files: %d succeeded, %d failed", saved, failed));
    }

    /**
     * Reloads all YAML files from disk.
     *
     * @throws StorageException if reload fails
     */
    public void reload() throws StorageException {
        cache.clear();
        try {
            doStart();
        } catch (Exception e) {
            throw new StorageException("Failed to reload YAML files", e);
        }
    }

    /**
     * Reloads a specific YAML file from disk.
     *
     * @param key The key to reload
     * @throws StorageException if reload fails
     */
    public void reload(@NotNull String key) throws StorageException {
        File ymlFile = new File(storageDirectory, key + ".yml");
        File yamlFile = new File(storageDirectory, key + ".yaml");
        
        File file = ymlFile.exists() ? ymlFile : (yamlFile.exists() ? yamlFile : null);
        
        if (file == null) {
            throw new StorageException("YAML file not found: " + key);
        }
        
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            cache.put(key, yaml);
            logger.fine("Reloaded YAML file: " + key);
        } catch (Exception e) {
            throw new StorageException("Failed to reload YAML file: " + key, e);
        }
    }

    /**
     * Gets all cached keys.
     *
     * @return Set of all keys
     */
    public Set<String> getKeys() {
        return new java.util.HashSet<>(cache.keySet());
    }

    /**
     * Writes a YAML configuration to a file.
     * Uses .yml extension by default.
     *
     * @param key    The key (file name)
     * @param config The YAML configuration
     * @throws StorageException if write fails
     */
    private void writeToFile(String key, YamlConfiguration config) throws StorageException {
        File file = new File(storageDirectory, key + ".yml");

        try {
            config.save(file);
            logger.fine("Saved YAML file: " + key);
        } catch (IOException e) {
            throw new StorageException("Failed to write YAML file: " + key, e);
        }
    }

    /**
     * Gets the storage directory.
     *
     * @return The storage directory
     */
    public File getStorageDirectory() {
        return storageDirectory;
    }

    /**
     * Checks if auto-save is enabled.
     *
     * @return true if auto-save is enabled
     */
    public boolean isAutoSave() {
        return autoSave;
    }

    /**
     * Sets auto-save mode.
     *
     * @param autoSave true to enable auto-save
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    /**
     * Checks if default configurations are created automatically.
     *
     * @return true if defaults are created
     */
    public boolean isCreateDefaults() {
        return createDefaults;
    }

    /**
     * Sets whether to create default configurations automatically.
     *
     * @param createDefaults true to create defaults
     */
    public void setCreateDefaults(boolean createDefaults) {
        this.createDefaults = createDefaults;
    }
}