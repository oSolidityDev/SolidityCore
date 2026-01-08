package io.github.abdullahcxd.soliditycore.storage;

import org.jetbrains.annotations.NotNull;

/**
 * Type-safe storage key wrapper for better type inference and compile-time safety.
 *
 * @param <T> the value type associated with this key
 */
public class StorageKey<T> {

    private final String key;
    private final Class<T> type;

    private StorageKey(@NotNull String key, @NotNull Class<T> type) {
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
    public static <T> @NotNull StorageKey<T> of(@NotNull String key, @NotNull Class<T> type) {
        return new StorageKey<>(key, type);
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
    public static <T> @NotNull StorageKey<T> namespaced(@NotNull String namespace, @NotNull String key, @NotNull Class<T> type) {
        return new StorageKey<>(namespace + ":" + key, type);
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
    public T get(@NotNull TypedStorage<String> storage) {
        return storage.getTyped(key, type);
    }

    /**
     * Gets a value with a default using this typed key.
     */
    public T get(@NotNull TypedStorage<String> storage, @NotNull T defaultValue) {
        return storage.getTyped(key, type, defaultValue);
    }

    /**
     * Sets a value in storage using this typed key.
     */
    public void set(@NotNull TypedStorage<String> storage, @NotNull T value) {
        storage.set(key, value);
    }

    /**
     * Removes a value from storage using this typed key.
     */
    public T remove(@NotNull TypedStorage<String> storage) {
        return storage.getTyped(key, type);
    }

    /**
     * Checks if this key exists in storage.
     */
    public boolean exists(@NotNull TypedStorage<String> storage) {
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
        StorageKey<?> that = (StorageKey<?>) o;
        return key.equals(that.key) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }


    public static @NotNull StorageKey<String> string(@NotNull String key) {
        return of(key, String.class);
    }

    public static @NotNull StorageKey<Integer> integer(@NotNull String key) {
        return of(key, Integer.class);
    }

    public static @NotNull StorageKey<Long> longKey(@NotNull String key) {
        return of(key, Long.class);
    }

    public static @NotNull StorageKey<Double> doubleKey(@NotNull String key) {
        return of(key, Double.class);
    }

    public static @NotNull StorageKey<Boolean> bool(@NotNull String key) {
        return of(key, Boolean.class);
    }
}