package io.github.abdullahcxd.soliditycore.temporary.storage;

import org.jetbrains.annotations.NotNull;

/**
 * Type-safe storage key wrapper for better type inference and compile-time safety.
 *
 * @param <T> the value type associated with this key
 */
public class TemporaryStorageKey<T> {

    private final String key;
    private final Class<T> type;

    private TemporaryStorageKey(@NotNull String key, @NotNull Class<T> type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Creates a new storage key.
     *
     * @param key  the string key
     * @param type the value type class
     * @param <T>  the type parameter
     * @return a new StorageKey
     */
    public static <T> @NotNull TemporaryStorageKey<T> of(@NotNull String key, @NotNull Class<T> type) {
        return new TemporaryStorageKey<>(key, type);
    }

    /**
     * Creates a namespaced storage key.
     *
     * @param namespace the namespace (e.g., plugin name)
     * @param key       the key
     * @param type      the value type class
     * @param <T>       the type parameter
     * @return a new namespaced StorageKey
     */
    public static <T> @NotNull TemporaryStorageKey<T> namespaced(@NotNull String namespace, @NotNull String key, @NotNull Class<T> type) {
        return new TemporaryStorageKey<>(namespace + ":" + key, type);
    }

    /**
     * Gets the string representation of this key.
     */
    public @NotNull String getKey() {
        return key;
    }

    /**
     * Gets the type associated with this key.
     */
    public @NotNull Class<T> getType() {
        return type;
    }

    /**
     * Gets a value from storage using this typed key.
     */
    public T get(@NotNull TemporaryTypedStorage<String> storage) {
        return storage.getTyped(key, type);
    }

    /**
     * Gets a value with a default using this typed key.
     */
    public T get(@NotNull TemporaryTypedStorage<String> storage, @NotNull T defaultValue) {
        return storage.getTyped(key, type, defaultValue);
    }

    /**
     * Sets a value in storage using this typed key.
     */
    public void set(@NotNull TemporaryTypedStorage<String> storage, @NotNull T value) {
        storage.set(key, value);
    }

    /**
     * Removes a value from storage using this typed key.
     */
    public T remove(@NotNull TemporaryTypedStorage<String> storage) {
        return storage.getTyped(key, type);
    }

    /**
     * Checks if this key exists in storage.
     */
    public boolean exists(@NotNull TemporaryTypedStorage<String> storage) {
        return storage.has(key);
    }

    @Override
    public String toString() {
        return "StorageKey{key='" + key + "', type=" + type.getSimpleName() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemporaryStorageKey<?> that = (TemporaryStorageKey<?>) o;
        return key.equals(that.key) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }


    public static @NotNull TemporaryStorageKey<String> string(@NotNull String key) {
        return of(key, String.class);
    }

    public static @NotNull TemporaryStorageKey<Integer> integer(@NotNull String key) {
        return of(key, Integer.class);
    }

    public static @NotNull TemporaryStorageKey<Long> longKey(@NotNull String key) {
        return of(key, Long.class);
    }

    public static @NotNull TemporaryStorageKey<Double> doubleKey(@NotNull String key) {
        return of(key, Double.class);
    }

    public static @NotNull TemporaryStorageKey<Boolean> bool(@NotNull String key) {
        return of(key, Boolean.class);
    }
}