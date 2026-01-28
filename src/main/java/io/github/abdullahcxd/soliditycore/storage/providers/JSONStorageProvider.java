package io.github.abdullahcxd.soliditycore.storage.providers;

import io.github.abdullahcxd.soliditycore.storage.AbstractStorageProvider;
import io.github.abdullahcxd.soliditycore.storage.StorageException;
import io.github.abdullahcxd.soliditycore.storage.StorageType;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON storage provider implementation using org.json library.
 * Stores data in JSON files in the plugin's data folder.
 */
public class JSONStorageProvider extends AbstractStorageProvider {

    private File storageDirectory;
    private final Map<String, JSONObject> cache;
    private boolean autoSave;
    private String directoryPath;

    /**
     * Creates a new JSON storage provider.
     *
     * @param logger Logger instance for this provider
     */
    public JSONStorageProvider(@NotNull Logger logger) {
        super("JSON", StorageType.JSON, logger);
        this.cache = new HashMap<>();
    }

    @Override
    protected void doInitialize(FileConfiguration configuration) throws Exception {
        // Get configuration
        this.directoryPath = getConfigValue("storage.json.directory", "data");
        this.autoSave = getConfigValue("storage.json.auto-save", true);

        // Create storage directory
        File pluginDataFolder = new File(configuration.getName()).getParentFile();
        this.storageDirectory = new File(pluginDataFolder, directoryPath);

        if (!storageDirectory.exists()) {
            if (!storageDirectory.mkdirs()) {
                throw new StorageException("Failed to create storage directory: " + storageDirectory.getAbsolutePath());
            }
        }

        logger.info("JSON storage directory: " + storageDirectory.getAbsolutePath());
    }

    @Override
    protected void doStart() throws Exception {
        // Load existing JSON files into cache
        File[] files = storageDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    JSONObject json = new JSONObject(content);
                    String key = file.getName().replace(".json", "");
                    cache.put(key, json);
                    logger.fine("Loaded JSON file: " + key);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to load JSON file: " + file.getName(), e);
                }
            }
            logger.info("Loaded " + cache.size() + " JSON files into cache");
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
     * Gets a JSON object by key.
     *
     * @param key The key (file name without .json extension)
     * @return The JSON object, or null if not found
     */
    public JSONObject get(@NotNull String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        return cache.get(key);
    }

    /**
     * Saves a JSON object with the given key.
     *
     * @param key  The key (file name without .json extension)
     * @param data The JSON object to save
     * @throws StorageException if save fails
     */
    public void save(@NotNull String key, @NotNull JSONObject data) throws StorageException {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        cache.put(key, data);

        if (autoSave) {
            writeToFile(key, data);
        }
    }

    /**
     * Saves a JSON array with the given key.
     *
     * @param key  The key (file name without .json extension)
     * @param data The JSON array to save
     * @throws StorageException if save fails
     */
    public void save(@NotNull String key, @NotNull JSONArray data) throws StorageException {
        JSONObject wrapper = new JSONObject();
        wrapper.put("data", data);
        save(key, wrapper);
    }

    /**
     * Deletes a JSON file by key.
     *
     * @param key The key (file name without .json extension)
     * @return true if the file was deleted
     */
    public boolean delete(@NotNull String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        cache.remove(key);

        File file = new File(storageDirectory, key + ".json");
        if (file.exists()) {
            return file.delete();
        }
        return false;
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

        for (Map.Entry<String, JSONObject> entry : cache.entrySet()) {
            try {
                writeToFile(entry.getKey(), entry.getValue());
                saved++;
            } catch (StorageException e) {
                logger.log(Level.WARNING, "Failed to save JSON file: " + entry.getKey(), e);
                failed++;
            }
        }

        logger.info(String.format("Saved JSON files: %d succeeded, %d failed", saved, failed));
    }

    /**
     * Reloads all JSON files from disk.
     *
     * @throws StorageException if reload fails
     */
    public void reload() throws StorageException {
        cache.clear();
        try {
            doStart();
        } catch (Exception e) {
            throw new StorageException("Failed to reload JSON files", e);
        }
    }

    /**
     * Gets all cached keys.
     *
     * @return Set of all keys
     */
    public java.util.Set<String> getKeys() {
        return new java.util.HashSet<>(cache.keySet());
    }

    /**
     * Writes a JSON object to a file.
     *
     * @param key  The key (file name)
     * @param data The JSON object
     * @throws StorageException if write fails
     */
    private void writeToFile(String key, JSONObject data) throws StorageException {
        File file = new File(storageDirectory, key + ".json");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data.toString(2)); // Pretty print with 2-space indent
            logger.fine("Saved JSON file: " + key);
        } catch (IOException e) {
            throw new StorageException("Failed to write JSON file: " + key, e);
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
}