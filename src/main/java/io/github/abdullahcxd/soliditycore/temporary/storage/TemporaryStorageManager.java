package io.github.abdullahcxd.soliditycore.temporary.storage;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Centralized manager for player-specific temporary storage.
 * Thread-safe and optimized for concurrent access.
 */
public class TemporaryStorageManager {

    private static final Map<UUID, TemporaryPlayerStorage> PLAYER_STORAGES = new ConcurrentHashMap<>();

    /**
     * Gets or creates a player's storage.
     *
     * @param player the player
     * @return the player's storage
     */
    public static @NotNull TemporaryPlayerStorage getOrCreate(@NotNull Player player) {
        return getOrCreate(player.getUniqueId());
    }

    /**
     * Gets or creates storage by UUID.
     *
     * @param playerId the player UUID
     * @return the player's storage
     */
    public static @NotNull TemporaryPlayerStorage getOrCreate(@NotNull UUID playerId) {
        return PLAYER_STORAGES.computeIfAbsent(playerId, TemporaryPlayerStorage::new);
    }

    /**
     * Gets existing storage (returns null if doesn't exist).
     *
     * @param player the player
     * @return the storage or null
     */
    public static @Nullable TemporaryPlayerStorage get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets existing storage by UUID.
     *
     * @param playerId the player UUID
     * @return the storage or null
     */
    public static @Nullable TemporaryPlayerStorage get(@NotNull UUID playerId) {
        return PLAYER_STORAGES.get(playerId);
    }

    /**
     * Gets storage as Optional.
     *
     * @param player the player
     * @return Optional containing storage if present
     */
    public static @NotNull Optional<TemporaryPlayerStorage> getOptional(@NotNull Player player) {
        return Optional.ofNullable(get(player));
    }

    /**
     * Gets storage as Optional by UUID.
     *
     * @param playerId the player UUID
     * @return Optional containing storage if present
     */
    public static @NotNull Optional<TemporaryPlayerStorage> getOptional(@NotNull UUID playerId) {
        return Optional.ofNullable(get(playerId));
    }

    /**
     * Checks if storage exists for player.
     *
     * @param player the player
     * @return true if storage exists
     */
    public static boolean has(@NotNull Player player) {
        return has(player.getUniqueId());
    }

    /**
     * Checks if storage exists for UUID.
     *
     * @param playerId the player UUID
     * @return true if storage exists
     */
    public static boolean has(@NotNull UUID playerId) {
        return PLAYER_STORAGES.containsKey(playerId);
    }

    /**
     * Executes an action with the player's storage if it exists.
     *
     * @param player the player
     * @param action the action to perform
     */
    public static void ifPresent(@NotNull Player player, @NotNull Consumer<TemporaryPlayerStorage> action) {
        ifPresent(player.getUniqueId(), action);
    }

    /**
     * Executes an action with the storage if it exists.
     *
     * @param playerId the player UUID
     * @param action   the action to perform
     */
    public static void ifPresent(@NotNull UUID playerId, @NotNull Consumer<TemporaryPlayerStorage> action) {
        TemporaryPlayerStorage storage = PLAYER_STORAGES.get(playerId);
        if (storage != null) {
            action.accept(storage);
        }
    }

    /**
     * Removes player's storage and clears its data.
     *
     * @param player the player
     * @return true if storage was removed
     */
    public static boolean remove(@NotNull Player player) {
        return remove(player.getUniqueId());
    }

    /**
     * Removes storage by UUID and clears its data.
     *
     * @param playerId the player UUID
     * @return true if storage was removed
     */
    public static boolean remove(@NotNull UUID playerId) {
        TemporaryPlayerStorage storage = PLAYER_STORAGES.remove(playerId);
        if (storage != null) {
            storage.clear();
            return true;
        }
        return false;
    }

    /**
     * Clears all storages and removes them.
     */
    public static void clearAll() {
        PLAYER_STORAGES.values().forEach(TemporaryPlayerStorage::clear);
        PLAYER_STORAGES.clear();
    }

    /**
     * Clears data in all storages without removing them.
     */
    public static void clearAllData() {
        PLAYER_STORAGES.values().forEach(TemporaryPlayerStorage::clear);
    }

    /**
     * Gets the number of active storages.
     *
     * @return the count
     */
    public static int size() {
        return PLAYER_STORAGES.size();
    }

    /**
     * Checks if there are no active storages.
     *
     * @return true if empty
     */
    public static boolean isEmpty() {
        return PLAYER_STORAGES.isEmpty();
    }

    /**
     * Gets all player UUIDs with active storage.
     *
     * @return unmodifiable set of UUIDs
     */
    public static @NotNull Set<UUID> getActivePlayers() {
        return Collections.unmodifiableSet(PLAYER_STORAGES.keySet());
    }

    /**
     * Gets all active storages.
     *
     * @return unmodifiable collection of storages
     */
    public static @NotNull Collection<TemporaryPlayerStorage> getAllStorages() {
        return Collections.unmodifiableCollection(PLAYER_STORAGES.values());
    }

    /**
     * Removes storages that are empty (have no data).
     *
     * @return number of storages removed
     */
    public static int cleanupEmpty() {
        int removed = 0;
        Iterator<Map.Entry<UUID, TemporaryPlayerStorage>> iterator = PLAYER_STORAGES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, TemporaryPlayerStorage> entry = iterator.next();
            if (entry.getValue().isEmpty()) {
                iterator.remove();
                removed++;
            }
        }

        return removed;
    }

    /**
     * Quick set - gets or creates storage and sets a value.
     *
     * @param player the player
     * @param key    the key
     * @param value  the value
     */
    public static void set(@NotNull Player player, @NotNull String key, @NotNull Object value) {
        getOrCreate(player).set(key, value);
    }

    /**
     * Quick set by UUID.
     *
     * @param playerId the player UUID
     * @param key      the key
     * @param value    the value
     */
    public static void set(@NotNull UUID playerId, @NotNull String key, @NotNull Object value) {
        getOrCreate(playerId).set(key, value);
    }

    /**
     * Quick get - returns null if storage or key doesn't exist.
     *
     * @param player the player
     * @param key    the key
     * @return the value or null
     */
    public static @Nullable Object getValue(@NotNull Player player, @NotNull String key) {
        TemporaryPlayerStorage storage = get(player);
        return storage != null ? storage.get(key) : null;
    }

    /**
     * Quick get by UUID.
     *
     * @param playerId the player UUID
     * @param key      the key
     * @return the value or null
     */
    public static @Nullable Object getValue(@NotNull UUID playerId, @NotNull String key) {
        TemporaryPlayerStorage storage = get(playerId);
        return storage != null ? storage.get(key) : null;
    }

    /**
     * Quick typed get.
     *
     * @param player the player
     * @param key    the key
     * @param type   the expected type
     * @param <T>    the type parameter
     * @return the value or null
     */
    public static <T> @Nullable T getTyped(@NotNull Player player, @NotNull String key, @NotNull Class<T> type) {
        TemporaryPlayerStorage storage = get(player);
        return storage != null ? storage.getTyped(key, type) : null;
    }

    /**
     * Quick typed get by UUID.
     *
     * @param playerId the player UUID
     * @param key      the key
     * @param type     the expected type
     * @param <T>      the type parameter
     * @return the value or null
     */
    public static <T> @Nullable T getTyped(@NotNull UUID playerId, @NotNull String key, @NotNull Class<T> type) {
        TemporaryPlayerStorage storage = get(playerId);
        return storage != null ? storage.getTyped(key, type) : null;
    }

    /**
     * Quick remove.
     *
     * @param player the player
     * @param key    the key
     * @return the removed value or null
     */
    public static @Nullable Object removeValue(@NotNull Player player, @NotNull String key) {
        TemporaryPlayerStorage storage = get(player);
        return storage != null ? storage.remove(key) : null;
    }

    /**
     * Quick remove by UUID.
     *
     * @param playerId the player UUID
     * @param key      the key
     * @return the removed value or null
     */
    public static @Nullable Object removeValue(@NotNull UUID playerId, @NotNull String key) {
        TemporaryPlayerStorage storage = get(playerId);
        return storage != null ? storage.remove(key) : null;
    }

    /**
     * Quick has checked.
     *
     * @param player the player
     * @param key    the key
     * @return true if the key exists in the player's storage
     */
    public static boolean hasValue(@NotNull Player player, @NotNull String key) {
        TemporaryPlayerStorage storage = get(player);
        return storage != null && storage.has(key);
    }

    /**
     * Quick has check by UUID.
     *
     * @param playerId the player UUID
     * @param key      the key
     * @return true if the key exists in the storage
     */
    public static boolean hasValue(@NotNull UUID playerId, @NotNull String key) {
        TemporaryPlayerStorage storage = get(playerId);
        return storage != null && storage.has(key);
    }
}