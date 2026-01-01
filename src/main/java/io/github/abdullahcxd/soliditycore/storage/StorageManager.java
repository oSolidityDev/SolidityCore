
package io.github.abdullahcxd.soliditycore.storage;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private static final Map<UUID, PlayerTemporaryStorage> playerStorages = new ConcurrentHashMap<>();

    /**
     * Get or create a player's temporary storage
     */
    public static PlayerTemporaryStorage getOrCreate(Player player) {
        return getOrCreate(player.getUniqueId());
    }

    /**
     * Get or create storage by UUID
     */
    public static PlayerTemporaryStorage getOrCreate(UUID playerId) {
        return playerStorages.computeIfAbsent(playerId, PlayerTemporaryStorage::new);
    }

    /**
     * Get existing storage (returns null if doesn't exist)
     */
    public static PlayerTemporaryStorage get(UUID playerId) {
        return playerStorages.get(playerId);
    }

    /**
     * Check if storage exists for player
     */
    public static boolean has(UUID playerId) {
        return playerStorages.containsKey(playerId);
    }

    /**
     * Remove player's storage
     */
    public static void remove(UUID playerId) {
        PlayerTemporaryStorage storage = playerStorages.remove(playerId);
        if (storage != null) {
            storage.clear();
        }
    }

    /**
     * Remove player's storage
     */
    public static void remove(Player player) {
        remove(player.getUniqueId());
    }

    /**
     * Clear all storages
     */
    public static void clearAll() {
        playerStorages.values().forEach(PlayerTemporaryStorage::clear);
        playerStorages.clear();
    }

    /**
     * Get the number of active storages
     */
    public static int size() {
        return playerStorages.size();
    }
}