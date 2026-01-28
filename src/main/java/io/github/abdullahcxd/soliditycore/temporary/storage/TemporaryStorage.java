package io.github.abdullahcxd.soliditycore.temporary.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generic storage interface for key-value data management.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface TemporaryStorage<K, V> {

    /**
     * Stores a value with the specified key.
     *
     * @param key   the key
     * @param value the value to store
     * @return this storage instance for chaining
     */
    @NotNull TemporaryStorage<K, V> set(@NotNull K key, @Nullable V value);

    /**
     * Retrieves a value by key.
     *
     * @param key the key
     * @return the value, or null if not found
     */
    @Nullable V get(@NotNull K key);

    /**
     * Retrieves a value by key, with a default fallback.
     *
     * @param key          the key
     * @param defaultValue the default value if key not found
     * @return the value or default
     */
    @NotNull V getOrDefault(@NotNull K key, @NotNull V defaultValue);

    /**
     * Retrieves a value by key, computing it if absent.
     *
     * @param key      the key
     * @param supplier the supplier to compute the value
     * @return the value (existing or computed)
     */
    @NotNull V computeIfAbsent(@NotNull K key, @NotNull Supplier<V> supplier);

    /**
     * Retrieves a value by key, computing it from the key if absent.
     *
     * @param key             the key
     * @param mappingFunction the function to compute the value from the key
     * @return the value (existing or computed)
     */
    @NotNull V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> mappingFunction);

    /**
     * Retrieves a value as an Optional.
     *
     * @param key the key
     * @return Optional containing the value if present
     */
    @NotNull Optional<V> getOptional(@NotNull K key);

    /**
     * Checks if a key exists in storage.
     *
     * @param key the key
     * @return true if key exists
     */
    boolean has(@NotNull K key);

    /**
     * Removes a value by key.
     *
     * @param key the key
     * @return the removed value, or null if not found
     */
    @Nullable V remove(@NotNull K key);

    /**
     * Clears all stored data.
     */
    void clear();

    /**
     * Gets the number of stored entries.
     *
     * @return the size
     */
    int size();

    /**
     * Checks if storage is empty.
     *
     * @return true if empty
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Gets all keys in storage.
     *
     * @return set of keys
     */
    @NotNull Set<K> keys();

    /**
     * Gets all stored data as a map.
     *
     * @return immutable copy of the data
     */
    @NotNull Map<K, V> asMap();

    /**
     * Sets multiple key-value pairs at once.
     *
     * @param data the data to store
     * @return this storage instance for chaining
     */
    @NotNull TemporaryStorage<K, V> putAll(@NotNull Map<K, V> data);
}