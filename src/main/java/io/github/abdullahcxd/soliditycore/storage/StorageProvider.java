package io.github.abdullahcxd.soliditycore.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for storage providers in the Solidity framework.
 * Implementations should handle specific database types (MySQL, SQLite, etc.)
 * and manage their own connection lifecycle.
 */
public interface StorageProvider {

    /**
     * Gets the unique name of this storage provider.
     * This name is used for registration and retrieval.
     *
     * @return The provider name (must not be null or empty)
     */
    @NotNull
    String getProviderName();

    /**
     * Gets the storage type this provider handles.
     *
     * @return The storage type (must not be null)
     */
    @NotNull
    StorageType getProviderType();

    /**
     * Initializes the storage provider with configuration.
     * This method is called during the plugin load phase and should:
     * - Load configuration values
     * - Validate settings
     * - Prepare resources (but not start connections)
     *
     * @param pluginConfiguration The plugin's configuration file
     * @throws StorageException if initialization fails
     */
    void initialize(@NotNull FileConfiguration pluginConfiguration) throws StorageException;

    /**
     * Starts the storage provider and establishes connections.
     * This method is called during the plugin enable phase and should:
     * - Establish database connections
     * - Create/verify tables and schemas
     * - Start any background tasks
     *
     * @throws StorageException if startup fails
     */
    void start() throws StorageException;

    /**
     * Closes the storage provider and releases all resources.
     * This method is called during the plugin disable phase and should:
     * - Close all database connections gracefully
     * - Stop any background tasks
     * - Release allocated resources
     * - Save any pending data
     * <p>
     * This method should not throw exceptions and should handle errors internally.
     */
    void close();

    /**
     * Checks if the provider is currently initialized.
     *
     * @return true if the provider has been initialized
     */
    default boolean isInitialized() {
        return false;
    }

    /**
     * Checks if the provider is currently active and ready to use.
     *
     * @return true if the provider is started and operational
     */
    default boolean isActive() {
        return false;
    }

    /**
     * Tests the connection to verify the provider is working correctly.
     * Implementations should perform a simple operation to verify connectivity.
     *
     * @return true if the connection test succeeds
     */
    default boolean testConnection() {
        return isActive();
    }
}