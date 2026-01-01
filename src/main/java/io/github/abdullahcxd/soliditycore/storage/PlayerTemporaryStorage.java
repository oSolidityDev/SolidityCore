
package io.github.abdullahcxd.soliditycore.storage;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerTemporaryStorage implements Storage<Object> {

    private final UUID playerId;
    private final Map<String, Object> dataMap = new HashMap<>();

    public PlayerTemporaryStorage(Player player) {
        this.playerId = player.getUniqueId();
    }

    public PlayerTemporaryStorage(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public Storage<Object> set(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        dataMap.put(key, value);
        return this;
    }

    @Override
    public Object get(String key) {
        return dataMap.get(key);
    }

    @Override
    public Object get(String key, Object defaultValue) {
        return dataMap.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean has(String key) {
        return dataMap.containsKey(key);
    }

    @Override
    public Object remove(String key) {
        return dataMap.remove(key);
    }

    @Override
    public void clear() {
        dataMap.clear();
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    /**
     * Get a typed value from storage
     */
    @SuppressWarnings("unchecked")
    public <T> T getTyped(String key, Class<T> type) {
        Object value = get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Get a typed value with default
     */
    public <T> T getTyped(String key, Class<T> type, T defaultValue) {
        T value = getTyped(key, type);
        return value != null ? value : defaultValue;
    }

    // Convenience methods for common types
    public String getString(String key) {
        return getTyped(key, String.class);
    }

    public String getString(String key, String defaultValue) {
        return getTyped(key, String.class, defaultValue);
    }

    public Integer getInt(String key) {
        return getTyped(key, Integer.class);
    }

    public int getInt(String key, int defaultValue) {
        return getTyped(key, Integer.class, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return getTyped(key, Boolean.class);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getTyped(key, Boolean.class, defaultValue);
    }
}