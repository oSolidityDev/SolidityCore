package io.github.abdullahcxd.soliditycore.storage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enum representing supported storage types in the Solidity framework.
 * Each type includes metadata about the database system.
 */
@Getter
public enum StorageType {

    /**
     * H2 embedded database - file-based, no external server required
     */
    H2("H2", "org.h2.Driver", "jdbc:h2:", true, false),

    /**
     * SQLite embedded database - lightweight, file-based
     */
    SQLITE("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:", true, false),

    /**
     * MySQL database - requires external server
     */
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql:", false, true),

    /**
     * MariaDB database - MySQL-compatible, requires external server
     */
    MARIADB("MariaDB", "org.mariadb.jdbc.Driver", "jdbc:mariadb:", false, true),

    /**
     * PostgreSQL database - requires external server
     */
    POSTGRESQL("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql:", false, true),

    /**
     * JSON file storage - file-based using org.json library
     */
    JSON("JSON", null, null, true, false),

    /**
     * YAML file storage - file-based using Bukkit's configuration API
     */
    YAML("YAML", null, null, true, false);

    private final String displayName;
    private final String driverClass;
    private final String jdbcPrefix;
    private final boolean embedded;
    private final boolean requiresServer;

    /**
     * Creates a new StorageType with the specified properties.
     *
     * @param displayName    Human-readable name
     * @param driverClass    JDBC driver class name
     * @param jdbcPrefix     JDBC URL prefix
     * @param embedded       Whether this is an embedded database
     * @param requiresServer Whether this requires an external server
     */
    StorageType(String displayName, String driverClass, String jdbcPrefix,
                boolean embedded, boolean requiresServer) {
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.jdbcPrefix = jdbcPrefix;
        this.embedded = embedded;
        this.requiresServer = requiresServer;
    }

    /**
     * Checks if the JDBC driver for this storage type is available.
     * For file-based storage types (JSON, YAML), this always returns true.
     *
     * @return true if the driver class can be loaded or if this is a file-based type
     */
    public boolean isDriverAvailable() {
        // File-based types don't need drivers
        if (driverClass == null) {
            return true;
        }

        try {
            Class.forName(driverClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets a StorageType by its name (case-insensitive).
     *
     * @param name The name to search for
     * @return The matching StorageType, or null if not found
     */
    @Nullable
    public static StorageType fromString(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String normalized = name.trim().toUpperCase().replace("-", "").replace("_", "");

        for (StorageType type : values()) {
            String typeName = type.name().replace("_", "");
            if (typeName.equals(normalized)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Gets a StorageType by its name with a default fallback.
     *
     * @param name         The name to search for
     * @param defaultValue The default value if not found
     * @return The matching StorageType, or the default value
     */
    @NotNull
    public static StorageType fromStringOrDefault(@Nullable String name, @NotNull StorageType defaultValue) {
        StorageType type = fromString(name);
        return type != null ? type : defaultValue;
    }

    /**
     * Checks if this storage type is suitable for small-scale deployments.
     *
     * @return true if this is an embedded database
     */
    public boolean isSuitableForSmallScale() {
        return embedded;
    }

    /**
     * Checks if this storage type is suitable for large-scale deployments.
     *
     * @return true if this requires an external server
     */
    public boolean isSuitableForLargeScale() {
        return requiresServer;
    }

    /**
     * Checks if this is a file-based storage type (JSON, YAML).
     *
     * @return true if this is a file-based storage
     */
    public boolean isFileStorage() {
        return this == JSON || this == YAML;
    }

    /**
     * Checks if this is a database storage type (SQL-based).
     *
     * @return true if this is a database storage
     */
    public boolean isDatabaseStorage() {
        return !isFileStorage();
    }

    /**
     * Gets a user-friendly description of this storage type.
     *
     * @return Description string
     */
    public String getDescription() {
        if (embedded) {
            return displayName + " (embedded, file-based)";
        } else {
            return displayName + " (external server required)";
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}