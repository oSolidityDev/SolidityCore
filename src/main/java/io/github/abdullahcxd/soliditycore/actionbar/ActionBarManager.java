package io.github.abdullahcxd.soliditycore.actionbar;

import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages persistent action bar messages for players
 */
public class ActionBarManager {

    public interface ABFunction<F1, F2> {
        F2 apply(F1 f1);
    }

    private static final Map<UUID, ActionBarData> ACTIVE_BARS = new ConcurrentHashMap<>();
    private static BukkitTask updateTask;
    private static Plugin plugin;

    /**
     * Initializes the action bar manager
     */
    public static void initialize(@NotNull SolidityPlugin plugin) {
        ActionBarManager.plugin = plugin;
        
        // Start update task (runs every 2 ticks = 0.1 seconds)
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            ACTIVE_BARS.entrySet().removeIf(entry -> {
                Player player = Bukkit.getPlayer(entry.getKey());
                ActionBarData data = entry.getValue();
                
                // Remove if player is offline or expired
                if (player == null || !player.isOnline()) {
                    return true;
                }
                
                if (data.expireTime > 0 && currentTime >= data.expireTime) {
                    player.sendActionBar(Component.empty());
                    return true;
                }
                
                // Update dynamic action bars
                if (data.isDynamic()) {
                    Component message = data.dynamicMessage.apply(player);
                    player.sendActionBar(message);
                } else {
                    player.sendActionBar(data.message);
                }
                
                return false;
            });
        }, 0L, 2L);
    }

    /**
     * Shuts down the action bar manager
     */
    public static void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // Clear all action bars
        clearAll();
    }

    /**
     * Sends a persistent action bar message
     * @param player The player
     * @param message The message in MiniMessage format
     * @param durationSeconds How long to display (0 = until manually removed)
     */
    public static void send(@NotNull Player player, @NotNull String message, long durationSeconds) {
        Component component = MessageBuilder.fromMiniMessage(message).build();
        send(player, component, durationSeconds);
    }

    /**
     * Sends a persistent action bar component
     */
    public static void send(@NotNull Player player, @NotNull Component message, long durationSeconds) {
        long expireTime = durationSeconds > 0 ? 
            System.currentTimeMillis() + (durationSeconds * 1000) : 0;
        
        ACTIVE_BARS.put(player.getUniqueId(), new ActionBarData(message, expireTime));
    }

    /**
     * Sends a dynamic action bar that updates based on player state
     * @param player The player
     * @param messageFunction Function that generates the message
     * @param durationSeconds How long to display (0 = until manually removed)
     */
    public static void sendDynamic(@NotNull Player player, @NotNull ABFunction<Player, Component> messageFunction, long durationSeconds) {
        long expireTime = durationSeconds > 0 ? 
            System.currentTimeMillis() + (durationSeconds * 1000) : 0;
        
        ACTIVE_BARS.put(player.getUniqueId(), new ActionBarData(messageFunction, expireTime));
    }

    /**
     * Sends an action bar that stays until manually removed
     */
    public static void sendPermanent(@NotNull Player player, @NotNull String message) {
        send(player, message, 0);
    }

    /**
     * Sends a permanent action bar component
     */
    public static void sendPermanent(@NotNull Player player, @NotNull Component message) {
        send(player, message, 0);
    }

    /**
     * Sends a quick action bar (3 seconds)
     */
    public static void sendQuick(@NotNull Player player, @NotNull String message) {
        send(player, message, 3);
    }

    /**
     * Removes the action bar for a player
     */
    public static void remove(@NotNull Player player) {
        ACTIVE_BARS.remove(player.getUniqueId());
        player.sendActionBar(Component.empty());
    }

    /**
     * Checks if a player has an active action bar
     */
    public static boolean hasActionBar(@NotNull Player player) {
        return ACTIVE_BARS.containsKey(player.getUniqueId());
    }

    /**
     * Gets the current action bar message for a player
     */
    public static @Nullable Component getMessage(@NotNull Player player) {
        ActionBarData data = ACTIVE_BARS.get(player.getUniqueId());
        if (data == null) return null;
        
        if (data.isDynamic()) {
            return data.dynamicMessage.apply(player);
        }
        return data.message;
    }

    /**
     * Removes all action bars from all players
     */
    public static void clearAll() {
        ACTIVE_BARS.keySet().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendActionBar(Component.empty());
            }
        });
        ACTIVE_BARS.clear();
    }

    // === Quick Utility Methods ===

    /**
     * Sends a success action bar (green checkmark)
     */
    public static void success(@NotNull Player player, @NotNull String message) {
        send(player, "<green>✔</green> <gray>" + message + "</gray>", 3);
    }

    /**
     * Sends an error action bar (red X)
     */
    public static void error(@NotNull Player player, @NotNull String message) {
        send(player, "<red>✖</red> <gray>" + message + "</gray>", 3);
    }

    /**
     * Sends a warning action bar (yellow warning)
     */
    public static void warning(@NotNull Player player, @NotNull String message) {
        send(player, "<yellow>⚠</yellow> <gray>" + message + "</gray>", 3);
    }

    /**
     * Sends an info action bar (blue info)
     */
    public static void info(@NotNull Player player, @NotNull String message) {
        send(player, "<aqua>ℹ</aqua> <gray>" + message + "</gray>", 3);
    }

    /**
     * Sends a progress bar action bar
     */
    public static void progress(@NotNull Player player, 
                               @NotNull String label,
                               double current, 
                               double max,
                               long durationSeconds) {
        sendDynamic(player, p -> {
            int percentage = (int) ((current / max) * 100);
            int bars = (int) ((current / max) * 20);
            
            StringBuilder progressBar = new StringBuilder("<gray>[</gray>");
            for (int i = 0; i < 20; i++) {
                if (i < bars) {
                    progressBar.append("<green>|</green>");
                } else {
                    progressBar.append("<dark_gray>|</dark_gray>");
                }
            }
            progressBar.append("<gray>]</gray> <yellow>").append(percentage).append("%</yellow>");
            
            if (!label.isEmpty()) {
                progressBar.insert(0, "<white>" + label + "</white> ");
            }
            
            return MessageBuilder.fromMiniMessage(progressBar.toString()).build();
        }, durationSeconds);
    }

    /**
     * Sends a countdown action bar
     */
    public static void countdown(@NotNull Player player, int seconds) {
        final int[] remaining = {seconds};
        
        sendDynamic(player, p -> {
            if (remaining[0] <= 0) {
                remove(player);
                return Component.empty();
            }
            
            String color = remaining[0] <= 3 ? "<red>" : "<yellow>";
            String message = color + "⏱ " + remaining[0] + "s";
            remaining[0]--;
            
            return MessageBuilder.fromMiniMessage(message).build();
        }, seconds);
    }

    private static class ActionBarData {
        final Component message;
        final ABFunction<Player, Component> dynamicMessage;
        final long expireTime;

        ActionBarData(Component message, long expireTime) {
            this.message = message;
            this.dynamicMessage = null;
            this.expireTime = expireTime;
        }

        ActionBarData(ABFunction<Player, Component> dynamicMessage, long expireTime) {
            this.message = null;
            this.dynamicMessage = dynamicMessage;
            this.expireTime = expireTime;
        }

        boolean isDynamic() {
            return dynamicMessage != null;
        }
    }
}