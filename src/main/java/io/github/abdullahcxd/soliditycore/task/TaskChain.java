package io.github.abdullahcxd.soliditycore.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A fluent API for chaining synchronous and asynchronous tasks
 */
public class TaskChain {

    private final Plugin plugin;
    private final List<ChainLink> links = new ArrayList<>();
    private Consumer<Exception> errorHandler;
    private Runnable finallyHandler;

    private TaskChain(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new TaskChain
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TaskChain create(@NotNull Plugin plugin) {
        return new TaskChain(plugin);
    }

    /**
     * Adds a synchronous task to the chain
     */
    public TaskChain sync(@NotNull Runnable task) {
        links.add(new ChainLink(task, false, 0));
        return this;
    }

    /**
     * Adds an asynchronous task to the chain
     */
    public TaskChain async(@NotNull Runnable task) {
        links.add(new ChainLink(task, true, 0));
        return this;
    }

    /**
     * Adds a delayed synchronous task (in ticks)
     */
    public TaskChain syncDelayed(@NotNull Runnable task, long delayTicks) {
        links.add(new ChainLink(task, false, delayTicks));
        return this;
    }

    /**
     * Adds a delayed asynchronous task (in ticks)
     */
    public TaskChain asyncDelayed(@NotNull Runnable task, long delayTicks) {
        links.add(new ChainLink(task, true, delayTicks));
        return this;
    }

    /**
     * Adds a delay to the chain (in ticks)
     */
    public TaskChain delay(long ticks) {
        links.add(new ChainLink(() -> {}, false, ticks));
        return this;
    }

    /**
     * Sets the error handler for the chain
     */
    public TaskChain onError(@NotNull Consumer<Exception> handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Sets a finally handler that runs after all tasks complete
     */
    public TaskChain onFinally(@NotNull Runnable handler) {
        this.finallyHandler = handler;
        return this;
    }

    /**
     * Executes the task chain
     */
    public void execute() {
        if (links.isEmpty()) {
            if (finallyHandler != null) {
                finallyHandler.run();
            }
            return;
        }

        executeNext(0);
    }

    private void executeNext(int index) {
        if (index >= links.size()) {
            if (finallyHandler != null) {
                Bukkit.getScheduler().runTask(plugin, finallyHandler);
            }
            return;
        }

        ChainLink link = links.get(index);

        Runnable taskWrapper = () -> {
            try {
                link.task.run();
                executeNext(index + 1);
            } catch (Exception e) {
                if (errorHandler != null) {
                    errorHandler.accept(e);
                } else {
                    e.printStackTrace();
                }
                
                if (finallyHandler != null) {
                    Bukkit.getScheduler().runTask(plugin, finallyHandler);
                }
            }
        };

        if (link.delay > 0) {
            if (link.async) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, taskWrapper, link.delay);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, taskWrapper, link.delay);
            }
        } else {
            if (link.async) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, taskWrapper);
            } else {
                Bukkit.getScheduler().runTask(plugin, taskWrapper);
            }
        }
    }

    private record ChainLink(Runnable task, boolean async, long delay) {}

    // === Utility Methods ===

    /**
     * Runs a task synchronously
     */
    public static BukkitTask runSync(@NotNull Plugin plugin, @NotNull Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Runs a task asynchronously
     */
    public static BukkitTask runAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Runs a task synchronously after a delay
     */
    public static BukkitTask runSyncLater(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /**
     * Runs a task asynchronously after a delay
     */
    public static BukkitTask runAsyncLater(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    /**
     * Runs a task synchronously at a fixed rate
     */
    public static BukkitTask runSyncTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    /**
     * Runs a task asynchronously at a fixed rate
     */
    public static BukkitTask runAsyncTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    /**
     * Converts seconds to ticks
     */
    public static long seconds(double seconds) {
        return (long) (seconds * 20);
    }

    /**
     * Converts minutes to ticks
     */
    public static long minutes(double minutes) {
        return seconds(minutes * 60);
    }
}