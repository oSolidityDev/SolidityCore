package io.github.abdullahcxd.soliditycore.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Represents a database schema with versioning support.
 * 
 * <p>Schemas are used to organize and manage database table structures.
 * They support versioning for migrations and updates.
 */
public interface Schema {

    /**
     * Get the unique name of this schema.
     * This is used for schema identification and version tracking.
     * 
     * @return schema name (should be unique)
     */
    @NotNull String getName();

    /**
     * Create or update the schema structure.
     * This method is called when the schema is registered or when
     * the database manager detects a version change.
     * 
     * @param manager the database manager instance
     * @throws SQLException if creation/update fails
     */
    void create(@NotNull DatabaseManager manager) throws SQLException;

    /**
     * Get the current version of this schema.
     * Increment this when making structural changes to trigger updates.
     * 
     * @return schema version (default: 1)
     */
    default int getVersion() {
        return 1;
    }

    /**
     * Optional migration method for upgrading from old versions.
     * Called when the registered version differs from stored version.
     * 
     * @param manager the database manager instance
     * @param oldVersion the previous schema version
     * @param newVersion the new schema version
     * @throws SQLException if migration fails
     */
    default void migrate(@NotNull DatabaseManager manager, int oldVersion, int newVersion) throws SQLException {
        // Default: recreate schema (can be overridden for safer migrations)
        create(manager);
    }

    /**
     * Optional cleanup method called before schema removal.
     * Use this to drop tables or clean up resources.
     * 
     * @param manager the database manager instance
     * @throws SQLException if cleanup fails
     */
    default void drop(@NotNull DatabaseManager manager) throws SQLException {
        // Default: no cleanup
    }

    /**
     * Optional validation method to check schema integrity.
     * 
     * @param manager the database manager instance
     * @return true if schema is valid, false otherwise
     */
    default boolean validate(@NotNull DatabaseManager manager) {
        // Default: assume valid
        return true;
    }

    /**
     * Get a description of this schema.
     * 
     * @return schema description
     */
    default @NotNull String getDescription() {
        return "No description provided";
    }
}