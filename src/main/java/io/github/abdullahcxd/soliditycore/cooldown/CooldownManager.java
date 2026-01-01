package io.github.abdullahcxd.soliditycore.cooldown;

import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cooldowns for player actions
 */
public class CooldownManager {

    private static final Map<String, Map<UUID, Long>> COOLDOWNS = new ConcurrentHashMap<>();

    /**
     * Sets a cooldown for a player
     * @param player The player
     * @param key The cooldown key (e.g., "teleport", "command.home")
     * @param seconds The cooldown duration in seconds
     */
    public static void setCooldown(@NotNull Player player, @NotNull String key, long seconds) {
        setCooldown(player.getUniqueId(), key, seconds);
    }

    /**
     * Sets a cooldown for a UUID
     */
    public static void setCooldown(@NotNull UUID uuid, @NotNull String key, long seconds) {
        Map<UUID, Long> cooldownMap = COOLDOWNS.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        cooldownMap.put(uuid, System.currentTimeMillis() + (seconds * 1000));
    }

    /**
     * Checks if a player has an active cooldown
     * @return true if the player is on cooldown
     */
    public static boolean hasCooldown(@NotNull Player player, @NotNull String key) {
        return hasCooldown(player.getUniqueId(), key);
    }

    /**
     * Checks if a UUID has an active cooldown
     */
    public static boolean hasCooldown(@NotNull UUID uuid, @NotNull String key) {
        Map<UUID, Long> cooldownMap = COOLDOWNS.get(key);
        if (cooldownMap == null) return false;

        Long expireTime = cooldownMap.get(uuid);
        if (expireTime == null) return false;

        if (System.currentTimeMillis() >= expireTime) {
            cooldownMap.remove(uuid);
            return false;
        }

        return true;
    }

    /**
     * Gets the remaining cooldown time in seconds
     * @return The remaining time, or 0 if no cooldown exists
     */
    public static long getRemainingTime(@NotNull Player player, @NotNull String key) {
        return getRemainingTime(player.getUniqueId(), key);
    }

    /**
     * Gets the remaining cooldown time in seconds for a UUID
     */
    public static long getRemainingTime(@NotNull UUID uuid, @NotNull String key) {
        Map<UUID, Long> cooldownMap = COOLDOWNS.get(key);
        if (cooldownMap == null) return 0;

        Long expireTime = cooldownMap.get(uuid);
        if (expireTime == null) return 0;

        long remaining = (expireTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Gets the remaining time formatted as a human-readable string
     */
    public static @NotNull String getRemainingTimeFormatted(@NotNull Player player, @NotNull String key) {
        return formatTime(getRemainingTime(player, key));
    }

    /**
     * Removes a cooldown for a player
     */
    public static void removeCooldown(@NotNull Player player, @NotNull String key) {
        removeCooldown(player.getUniqueId(), key);
    }

    /**
     * Removes a cooldown for a UUID
     */
    public static void removeCooldown(@NotNull UUID uuid, @NotNull String key) {
        Map<UUID, Long> cooldownMap = COOLDOWNS.get(key);
        if (cooldownMap != null) {
            cooldownMap.remove(uuid);
        }
    }

    /**
     * Removes all cooldowns for a player
     */
    public static void removeAllCooldowns(@NotNull Player player) {
        removeAllCooldowns(player.getUniqueId());
    }

    /**
     * Removes all cooldowns for a UUID
     */
    public static void removeAllCooldowns(@NotNull UUID uuid) {
        COOLDOWNS.values().forEach(map -> map.remove(uuid));
    }

    /**
     * Clears all cooldowns from all players
     */
    public static void clearAll() {
        COOLDOWNS.clear();
    }

    /**
     * Clears all cooldowns for a specific key
     */
    public static void clearKey(@NotNull String key) {
        COOLDOWNS.remove(key);
    }

    /**
     * Checks cooldown and sends a message if player is on cooldown
     * @return true if the action can proceed (no cooldown), false if on cooldown
     */
    public static boolean checkAndNotify(@NotNull Player player, @NotNull String key) {
        if (hasCooldown(player, key)) {
            String timeLeft = getRemainingTimeFormatted(player, key);
            SenderUtils.error(player, "You must wait <yellow>" + timeLeft + "</yellow> before doing this again.");
            return false;
        }
        return true;
    }

    /**
     * Checks cooldown, sets it if not active, and notifies if on cooldown
     * @return true if the action can proceed, false if on cooldown
     */
    public static boolean checkSetAndNotify(@NotNull Player player, @NotNull String key, long seconds) {
        if (hasCooldown(player, key)) {
            String timeLeft = getRemainingTimeFormatted(player, key);
            SenderUtils.error(player, "You must wait <yellow>" + timeLeft + "</yellow> before doing this again.");
            return false;
        }
        setCooldown(player, key, seconds);
        return true;
    }

    /**
     * Formats seconds into a human-readable time string
     */
    public static @NotNull String formatTime(long seconds) {
        if (seconds <= 0) return "0s";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder.append(days).append("d ");
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("m ");
        if (secs > 0 || builder.isEmpty()) builder.append(secs).append("s");

        return builder.toString().trim();
    }

    /**
     * Gets all active cooldowns for a player
     */
    public static @NotNull Map<String, Long> getActiveCooldowns(@NotNull Player player) {
        return getActiveCooldowns(player.getUniqueId());
    }

    /**
     * Gets all active cooldowns for a UUID
     */
    public static @NotNull Map<String, Long> getActiveCooldowns(@NotNull UUID uuid) {
        Map<String, Long> active = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        COOLDOWNS.forEach((key, cooldownMap) -> {
            Long expireTime = cooldownMap.get(uuid);
            if (expireTime != null && expireTime > currentTime) {
                active.put(key, (expireTime - currentTime) / 1000);
            }
        });

        return active;
    }

    /**
     * Cleanup expired cooldowns (should be called periodically)
     */
    public static void cleanupExpired() {
        long currentTime = System.currentTimeMillis();
        COOLDOWNS.values().forEach(map -> 
            map.entrySet().removeIf(entry -> entry.getValue() <= currentTime)
        );
    }
}