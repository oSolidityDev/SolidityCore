package io.github.abdullahcxd.soliditycore.temporary.storage;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Player-specific storage that wraps TypedStorage for player data.
 * Provides player-contextual methods and uses String keys.
 */
@Getter
public class TemporaryPlayerStorage extends TemporaryTypedStorage<String> {

    private final UUID playerId;

    public TemporaryPlayerStorage(@NotNull UUID playerId) {
        super();
        this.playerId = playerId;
    }

    public TemporaryPlayerStorage(@NotNull Player player) {
        this(player.getUniqueId());
    }

    /**
     * Creates a namespaced key for plugin isolation.
     * Example: "myplugin:balance" instead of just "balance"
     *
     * @param plugin the plugin name/prefix
     * @param key    the key
     * @return namespaced key
     */
    public static @NotNull String namespaced(@NotNull String plugin, @NotNull String key) {
        return plugin + ":" + key;
    }

    /**
     * Checks if this storage belongs to the given player.
     */
    public boolean belongsTo(@NotNull Player player) {
        return this.playerId.equals(player.getUniqueId());
    }

    /**
     * Checks if this storage belongs to the given UUID.
     */
    public boolean belongsTo(@NotNull UUID uuid) {
        return this.playerId.equals(uuid);
    }
}