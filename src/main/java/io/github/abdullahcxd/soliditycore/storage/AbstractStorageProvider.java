package io.github.abdullahcxd.soliditycore.storage;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * Abstract base class for storage providers.
 * Provides common functionality and state management for implementations.
 */
@Getter
public abstract class AbstractStorageProvider implements StorageProvider {

    protected final String providerName;
    protected final StorageType providerType;
    protected final Logger logger;
    
    protected volatile boolean initialized = false;
    protected volatile boolean active = false;
    
    protected FileConfiguration configuration;

    /**
     * Creates a new AbstractStorageProvider.
     *
     * @param providerName The unique name for this provider
     * @param providerType The storage type this provider handles
     * @param logger       Logger instance for this provider
     */
    protected AbstractStorageProvider(@NotNull String providerName, 
                                     @NotNull StorageType providerType,
                                     @NotNull Logger logger) {
        if (providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }

        this.providerName = providerName;
        this.providerType = providerType;
        this.logger = logger;
    }

    @Override
    @NotNull
    public String getProviderName() {
        return providerName;
    }

    @Override
    @NotNull
    public StorageType getProviderType() {
        return providerType;
    }

    @Override
    public void initialize(@NotNull FileConfiguration pluginConfiguration) throws StorageException {
        if (initialized) {
            logger.warning("Provider " + providerName + " is already initialized");
            return;
        }

        this.configuration = pluginConfiguration;
        
        try {
            doInitialize(pluginConfiguration);
            initialized = true;
            logger.info("Provider " + providerName + " initialized successfully");
        } catch (Exception e) {
            throw new StorageException("Failed to initialize provider " + providerName, e);
        }
    }

    @Override
    public void start() throws StorageException {
        if (!initialized) {
            throw new StorageException("Provider " + providerName + " must be initialized before starting");
        }

        if (active) {
            logger.warning("Provider " + providerName + " is already active");
            return;
        }

        try {
            doStart();
            active = true;
            logger.info("Provider " + providerName + " started successfully");
        } catch (Exception e) {
            throw new StorageException("Failed to start provider " + providerName, e);
        }
    }

    @Override
    public void close() {
        if (!active && !initialized) {
            return;
        }

        try {
            doClose();
            logger.info("Provider " + providerName + " closed successfully");
        } catch (Exception e) {
            logger.severe("Error closing provider " + providerName + ": " + e.getMessage());
        } finally {
            active = false;
            initialized = false;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Performs provider-specific initialization.
     * Called by {@link #initialize(FileConfiguration)} after validation.
     *
     * @param configuration The plugin configuration
     * @throws Exception if initialization fails
     */
    protected abstract void doInitialize(FileConfiguration configuration) throws Exception;

    /**
     * Performs provider-specific startup.
     * Called by {@link #start()} after validation.
     *
     * @throws Exception if startup fails
     */
    protected abstract void doStart() throws Exception;

    /**
     * Performs provider-specific cleanup.
     * Called by {@link #close()}. Should not throw exceptions.
     */
    protected abstract void doClose();

    /**
     * Gets a configuration value with a default.
     *
     * @param path         The configuration path
     * @param defaultValue The default value
     * @param <T>          The value type
     * @return The configuration value or default
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConfigValue(String path, T defaultValue) {
        if (configuration == null) {
            return defaultValue;
        }

        Object value = configuration.get(path);
        if (value == null) {
            return defaultValue;
        }

        try {
            return (T) value;
        } catch (ClassCastException e) {
            logger.warning("Invalid type for config path '" + path + "', using default");
            return defaultValue;
        }
    }

    /**
     * Validates that required configuration paths exist.
     *
     * @param requiredPaths The paths that must exist
     * @throws StorageException if any required path is missing
     */
    protected void validateConfiguration(String... requiredPaths) throws StorageException {
        if (configuration == null) {
            throw new StorageException("Configuration is null");
        }

        for (String path : requiredPaths) {
            if (!configuration.contains(path)) {
                throw new StorageException("Missing required configuration: " + path);
            }
        }
    }
}