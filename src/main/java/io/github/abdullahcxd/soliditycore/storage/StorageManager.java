package io.github.abdullahcxd.soliditycore.storage;

import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages storage providers for a Solidity plugin.
 * Handles initialization, lifecycle management, and provider retrieval.
 */
@Getter
public class StorageManager {

    private final SolidityPlugin plugin;
    private final StorageType storageType;
    private final Map<String, StorageProvider> providerMap;
    /**
     * -- GETTER --
     *  Checks if the storage manager has been initialized.
     *
     */
    private volatile boolean initialized = false;
    /**
     * -- GETTER --
     *  Checks if the storage manager has been started.
     *
     */
    private volatile boolean started = false;

    /**
     * Creates a new StorageManager instance.
     *
     * @param plugin      The plugin instance
     * @param storageType The type of storage to use
     * @throws IllegalArgumentException if plugin or storageType is null
     */
    public StorageManager(@NotNull SolidityPlugin plugin, @NotNull StorageType storageType) {
        this.plugin = plugin;
        this.storageType = storageType;
        this.providerMap = new ConcurrentHashMap<>();
    }

    /**
     * Registers a storage provider.
     *
     * @param provider The provider to register
     * @throws IllegalArgumentException if provider is null or provider name is null/empty
     * @throws IllegalStateException    if a provider with the same name is already registered
     */
    public void registerProvider(@NotNull StorageProvider provider) {
        if (provider.getProviderName() == null || provider.getProviderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }

        String providerName = provider.getProviderName();

        if (providerMap.containsKey(providerName)) {
            throw new IllegalStateException("Provider with name '" + providerName + "' is already registered");
        }

        providerMap.put(providerName, provider);
        plugin.getLogger().info("Registered storage provider: " + providerName);
    }

    /**
     * Unregisters a storage provider by name.
     *
     * @param providerName The name of the provider to unregister
     * @return true if the provider was removed, false if it wasn't found
     */
    public boolean unregisterProvider(@NotNull String providerName) {
        StorageProvider removed = providerMap.remove(providerName);
        if (removed != null) {
            plugin.getLogger().info("Unregistered storage provider: " + providerName);
            return true;
        }
        return false;
    }

    /**
     * Initializes all registered providers that match the configured storage type.
     * Should be called during plugin load phase.
     */
    public void loadProviders() {
        if (initialized) {
            plugin.getLogger().warning("Storage providers already initialized");
            return;
        }

        plugin.getLogger().info("Initializing storage providers for type: " + storageType);

        int initializedCount = 0;
        int failedCount = 0;

        for (StorageProvider provider : providerMap.values()) {
            if (!provider.getProviderType().equals(storageType)) {
                continue;
            }

            try {
                provider.initialize(plugin.getConfig());
                initializedCount++;
                plugin.getLogger().info("Initialized provider: " + provider.getProviderName());
            } catch (Exception e) {
                failedCount++;
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to initialize provider: " + provider.getProviderName(), e);
                SenderUtils.error(plugin.getConsoleCommandSender(),
                        "Failed to initialize storage provider: " + provider.getProviderName() +
                                " - " + e.getMessage());
            }
        }

        initialized = true;
        plugin.getLogger().info(String.format(
                "Storage initialization complete: %d succeeded, %d failed",
                initializedCount, failedCount));
    }

    /**
     * Starts all initialized providers that match the configured storage type.
     * Should be called during plugin enable phase.
     */
    public void enableProviders() {
        if (!initialized) {
            plugin.getLogger().warning("Cannot enable providers before initialization");
            return;
        }

        if (started) {
            plugin.getLogger().warning("Storage providers already started");
            return;
        }

        plugin.getLogger().info("Starting storage providers");

        int startedCount = 0;
        int failedCount = 0;

        for (StorageProvider provider : providerMap.values()) {
            if (!provider.getProviderType().equals(storageType)) {
                continue;
            }

            try {
                provider.start();
                startedCount++;
                plugin.getLogger().info("Started provider: " + provider.getProviderName());
            } catch (Exception e) {
                failedCount++;
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to start provider: " + provider.getProviderName(), e);
                SenderUtils.error(plugin.getConsoleCommandSender(),
                        "Failed to start storage provider: " + provider.getProviderName() +
                                " - " + e.getMessage());
            }
        }

        started = true;
        plugin.getLogger().info(String.format(
                "Storage startup complete: %d succeeded, %d failed",
                startedCount, failedCount));
    }

    /**
     * Closes all active providers that match the configured storage type.
     * Should be called during plugin disable phase.
     */
    public void closeProviders() {
        plugin.getLogger().info("Closing storage providers");

        int closedCount = 0;
        int failedCount = 0;

        for (StorageProvider provider : providerMap.values()) {
            if (!provider.getProviderType().equals(storageType)) {
                continue;
            }

            try {
                provider.close();
                closedCount++;
                plugin.getLogger().info("Closed provider: " + provider.getProviderName());
            } catch (Exception e) {
                failedCount++;
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to close provider: " + provider.getProviderName(), e);
            }
        }

        started = false;
        initialized = false;

        plugin.getLogger().info(String.format(
                "Storage shutdown complete: %d succeeded, %d failed",
                closedCount, failedCount));
    }

    /**
     * Retrieves a provider by its class type.
     *
     * @param providerClass The class of the provider to retrieve
     * @param <T>           The provider type
     * @return The provider instance, or null if not found
     */
    @Nullable
    public <T extends StorageProvider> T getProviderByType(@NotNull Class<T> providerClass) {

        return providerMap.values().stream()
                .filter(providerClass::isInstance)
                .map(providerClass::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a provider by its name.
     *
     * @param providerName The name of the provider
     * @return The provider instance, or null if not found
     */
    @Nullable
    public StorageProvider getProviderByName(@NotNull String providerName) {
        if (providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }
        return providerMap.get(providerName);
    }

    /**
     * Gets an unmodifiable collection of all registered providers.
     *
     * @return Collection of all providers
     */
    @NotNull
    public Collection<StorageProvider> getAllProviders() {
        return Collections.unmodifiableCollection(providerMap.values());
    }

    /**
     * Gets an unmodifiable collection of providers matching the configured storage type.
     *
     * @return Collection of active providers
     */
    @NotNull
    public Collection<StorageProvider> getActiveProviders() {
        return providerMap.values().stream()
                .filter(provider -> provider.getProviderType().equals(storageType))
                .toList();
    }

    /**
     * Checks if any providers are registered.
     *
     * @return true if at least one provider is registered
     */
    public boolean hasProviders() {
        return !providerMap.isEmpty();
    }

    /**
     * Checks if any providers matching the storage type are registered.
     *
     * @return true if at least one matching provider is registered
     */
    public boolean hasActiveProviders() {
        return providerMap.values().stream()
                .anyMatch(provider -> provider.getProviderType().equals(storageType));
    }

}