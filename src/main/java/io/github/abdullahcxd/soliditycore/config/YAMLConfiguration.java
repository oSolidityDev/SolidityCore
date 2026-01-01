package io.github.abdullahcxd.soliditycore.config;

import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class YAMLConfiguration {

    private final SolidityPlugin plugin;
    private final File file;
    private final String fileName;
    private FileConfiguration config;

    /**
     * Create a new YAML configuration file
     * @param plugin The plugin instance
     * @param fileName The name of the file (e.g., "config.yml")
     */
    public YAMLConfiguration(@NotNull SolidityPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);

        // Create the file if it doesn't exist
        if (!file.exists()) {
            saveDefault();
        }

        reload();
    }

    /**
     * Create a new YAML configuration file in a subdirectory
     * @param plugin The plugin instance
     * @param directory The subdirectory (e.g., "data")
     * @param fileName The name of the file (e.g., "players.yml")
     */
    public YAMLConfiguration(SolidityPlugin plugin, @NotNull File directory, String fileName) {
        this.plugin = plugin;
        if (!directory.exists())
            directory.mkdir();
        this.fileName = new File(directory, fileName).getPath();
        this.file = new File(plugin.getDataFolder(), this.fileName);

        // Create parent directories if they don't exist
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // Create the file if it doesn't exist
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + fileName, e);
            }
        }

        reload();
    }

    /**
     * Reload the configuration from disk
     */
    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            config.setDefaults(defaultConfig);
        }
    }

    /**
     * Save the configuration to disk
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + fileName, e);
        }
    }

    /**
     * Save the default configuration from resources
     */
    public void saveDefault() {
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    /**
     * Delete the configuration file
     */
    public boolean delete() {
        return file.delete();
    }

    /**
     * Check if the file exists
     */
    public boolean exists() {
        return file.exists();
    }

    public Object get(String path) {
        return config.get(path);
    }

    public Object get(String path, Object def) {
        return config.get(path, def);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public List<?> getList(String path) {
        return config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return config.getList(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public List<Integer> getIntegerList(String path) {
        return config.getIntegerList(path);
    }

    public List<Boolean> getBooleanList(String path) {
        return config.getBooleanList(path);
    }

    public List<Double> getDoubleList(String path) {
        return config.getDoubleList(path);
    }

    public List<Long> getLongList(String path) {
        return config.getLongList(path);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public boolean isSet(String path) {
        return config.isSet(path);
    }

    public Set<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }

    public void addDefault(String path, Object value) {
        config.addDefault(path, value);
    }
}
