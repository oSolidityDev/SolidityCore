package io.github.abdullahcxd.soliditycore.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Thread-safe typed storage implementation with type-checking and convenience methods.
 *
 * @param <K> the key type
 */
public class TypedStorage<K> implements Storage<K, Object> {

    private final Map<K, Object> data;

    public TypedStorage() {
        this(new ConcurrentHashMap<>());
    }

    public TypedStorage(@NotNull Map<K, Object> backingMap) {
        this.data = backingMap;
    }

    @Override
    public @NotNull Storage<K, Object> set(@NotNull K key, @Nullable Object value) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
        return this;
    }

    @Override
    public @Nullable Object get(@NotNull K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return data.get(key);
    }

    @Override
    public @NotNull Object getOrDefault(@NotNull K key, @NotNull Object defaultValue) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(defaultValue, "Default value cannot be null");
        return data.getOrDefault(key, defaultValue);
    }

    @Override
    public @NotNull Object computeIfAbsent(@NotNull K key, @NotNull Supplier<Object> supplier) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(supplier, "Supplier cannot be null");
        return data.computeIfAbsent(key, k -> supplier.get());
    }

    @Override
    public @NotNull Object computeIfAbsent(@NotNull K key, @NotNull Function<K, Object> mappingFunction) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(mappingFunction, "Mapping function cannot be null");
        return data.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public @NotNull Optional<Object> getOptional(@NotNull K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public boolean has(@NotNull K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return data.containsKey(key);
    }

    @Override
    public @Nullable Object remove(@NotNull K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return data.remove(key);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public @NotNull Set<K> keys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    @Override
    public @NotNull Map<K, Object> asMap() {
        return Collections.unmodifiableMap(new HashMap<>(data));
    }

    @Override
    public @NotNull Storage<K, Object> putAll(@NotNull Map<K, Object> newData) {
        Objects.requireNonNull(newData, "Data cannot be null");
        data.putAll(newData);
        return this;
    }

    /**
     * Gets a typed value from storage.
     *
     * @param key  the key
     * @param type the expected type
     * @param <T>  the type parameter
     * @return the value if it exists and matches the type, null otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getTyped(@NotNull K key, @NotNull Class<T> type) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        
        Object value = data.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Gets a typed value with a default fallback.
     *
     * @param key          the key
     * @param type         the expected type
     * @param defaultValue the default value
     * @param <T>          the type parameter
     * @return the value or default
     */
    public <T> @NotNull T getTyped(@NotNull K key, @NotNull Class<T> type, @NotNull T defaultValue) {
        T value = getTyped(key, type);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a typed value as Optional.
     *
     * @param key  the key
     * @param type the expected type
     * @param <T>  the type parameter
     * @return Optional containing the value if present and type matches
     */
    public <T> @NotNull Optional<T> getTypedOptional(@NotNull K key, @NotNull Class<T> type) {
        return Optional.ofNullable(getTyped(key, type));
    }

    public @Nullable String getString(@NotNull K key) {
        return getTyped(key, String.class);
    }

    public @NotNull String getString(@NotNull K key, @NotNull String defaultValue) {
        return getTyped(key, String.class, defaultValue);
    }

    public @Nullable Integer getInt(@NotNull K key) {
        return getTyped(key, Integer.class);
    }

    public int getInt(@NotNull K key, int defaultValue) {
        Integer value = getTyped(key, Integer.class);
        return value != null ? value : defaultValue;
    }

    public @Nullable Long getLong(@NotNull K key) {
        return getTyped(key, Long.class);
    }

    public long getLong(@NotNull K key, long defaultValue) {
        Long value = getTyped(key, Long.class);
        return value != null ? value : defaultValue;
    }

    public @Nullable Double getDouble(@NotNull K key) {
        return getTyped(key, Double.class);
    }

    public double getDouble(@NotNull K key, double defaultValue) {
        Double value = getTyped(key, Double.class);
        return value != null ? value : defaultValue;
    }

    public @Nullable Float getFloat(@NotNull K key) {
        return getTyped(key, Float.class);
    }

    public float getFloat(@NotNull K key, float defaultValue) {
        Float value = getTyped(key, Float.class);
        return value != null ? value : defaultValue;
    }

    public @Nullable Boolean getBoolean(@NotNull K key) {
        return getTyped(key, Boolean.class);
    }

    public boolean getBoolean(@NotNull K key, boolean defaultValue) {
        Boolean value = getTyped(key, Boolean.class);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable List<T> getList(@NotNull K key) {
        return getTyped(key, List.class);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull List<T> getList(@NotNull K key, @NotNull List<T> defaultValue) {
        List<T> value = getTyped(key, List.class);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable Set<T> getSet(@NotNull K key) {
        return getTyped(key, Set.class);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Set<T> getSet(@NotNull K key, @NotNull Set<T> defaultValue) {
        Set<T> value = getTyped(key, Set.class);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <K2, V> @Nullable Map<K2, V> getMap(@NotNull K key) {
        return getTyped(key, Map.class);
    }

    @SuppressWarnings("unchecked")
    public <K2, V> @NotNull Map<K2, V> getMap(@NotNull K key, @NotNull Map<K2, V> defaultValue) {
        Map<K2, V> value = getTyped(key, Map.class);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a number and converts it to the target type.
     * Useful when you stored an int but need a long, etc.
     */
    public @Nullable Number getNumber(@NotNull K key) {
        Object value = get(key);
        return value instanceof Number ? (Number) value : null;
    }

    public int getIntFromNumber(@NotNull K key, int defaultValue) {
        Number num = getNumber(key);
        return num != null ? num.intValue() : defaultValue;
    }

    public long getLongFromNumber(@NotNull K key, long defaultValue) {
        Number num = getNumber(key);
        return num != null ? num.longValue() : defaultValue;
    }

    public double getDoubleFromNumber(@NotNull K key, double defaultValue) {
        Number num = getNumber(key);
        return num != null ? num.doubleValue() : defaultValue;
    }

    public float getFloatFromNumber(@NotNull K key, float defaultValue) {
        Number num = getNumber(key);
        return num != null ? num.floatValue() : defaultValue;
    }
}