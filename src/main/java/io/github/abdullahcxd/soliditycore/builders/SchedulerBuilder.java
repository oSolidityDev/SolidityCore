package io.github.abdullahcxd.soliditycore.builders;

import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Scheduler utilities for async and sync execution.
 * <p>
 * Replaced in favor of {@link io.github.abdullahcxd.soliditycore.task.TaskChain}
 */
@Deprecated(since = "1.0.0")
public final class SchedulerBuilder {

    private static final ExecutorService ASYNC_POOL =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "SolidityCore-Async");
                t.setDaemon(true);
                return t;
            });

    private SchedulerBuilder() {}

    /* ---------------- ASYNC ---------------- */

    public static void runAsync(@NotNull Runnable task) {
        ASYNC_POOL.execute(wrap(task));
    }

    public static CompletableFuture<Void> runAsyncFuture(@NotNull Runnable task) {
        return CompletableFuture.runAsync(wrap(task), ASYNC_POOL);
    }

    /* ---------------- SYNC ---------------- */

    public static void runSync(@NotNull Plugin plugin, @NotNull Runnable task) {
        Bukkit.getScheduler().runTask(plugin, wrap(task));
    }

    public static void runLater(@NotNull Plugin plugin,
                                @NotNull Runnable task,
                                long ticksDelay) {
        Bukkit.getScheduler().runTaskLater(plugin, wrap(task), ticksDelay);
    }

    public static void runRepeating(@NotNull Plugin plugin,
                                    @NotNull Runnable task,
                                    long delay,
                                    long period) {
        Bukkit.getScheduler().runTaskTimer(plugin, wrap(task), delay, period);
    }

    /* ---------------- INTERNAL ---------------- */

    private static Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                throw new SolidityException("Scheduled task failed", t);
            }
        };
    }
}
