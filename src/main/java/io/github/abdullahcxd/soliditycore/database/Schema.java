package io.github.abdullahcxd.soliditycore.database;

import java.sql.SQLException;

public interface Schema {

    /**
     * Get the name of this schema
     * @return schema name
     */
    String getName();

    /**
     * Create the schema (tables, indexes, etc.)
     * @param manager the database manager
     * @throws SQLException if creation fails
     */
    void create(DatabaseManager manager) throws SQLException;

    /**
     * Get the version of this schema
     * @return schema version
     */
    default int getVersion() {
        return 1;
    }
}