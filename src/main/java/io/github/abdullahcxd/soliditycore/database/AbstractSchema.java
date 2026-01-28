package io.github.abdullahcxd.soliditycore.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base implementation of Schema providing common functionality.
 *
 * <p>Extend this class to easily create database schemas with table builders.
 */
@Getter
@RequiredArgsConstructor
@Deprecated(since = "0.0.9")
public abstract class AbstractSchema implements Schema {

    private final String name;
    private final int version;
    private final String description;

    /**
     * Creates a schema with default version 1.
     */
    protected AbstractSchema(@NotNull String name) {
        this(name, 1, "");
    }

    /**
     * Creates a schema with specified version.
     */
    protected AbstractSchema(@NotNull String name, int version) {
        this(name, version, "");
    }

    /**
     * Builds the table definitions for this schema.
     * Override this method to define your tables using TableBuilder.
     *
     * @param dbType the database type
     * @return list of table builders
     */
    protected abstract @NotNull List<TableBuilder> buildTables(@NotNull DatabaseType dbType);

    /**
     * Returns the table names in the order they should be created.
     * Override this if you need to specify a custom order or table names.
     * By default, extracts names from table builders.
     *
     * @param dbType the database type
     * @return list of table names in creation order
     */
    protected @NotNull List<String> getTableNames(@NotNull DatabaseType dbType) {
        List<String> names = new ArrayList<>();
        for (TableBuilder builder : buildTables(dbType)) {
            names.add(builder.getTableName());
        }
        return names;
    }

    /**
     * Optional method to execute custom SQL after table creation.
     *
     * @param manager the database manager
     * @throws SQLException if execution fails
     */
    protected void afterCreate(@NotNull DatabaseManager manager) throws SQLException {
        // Default: no action
    }

    /**
     * Optional method to execute custom SQL before table creation.
     *
     * @param manager the database manager
     * @throws SQLException if execution fails
     */
    protected void beforeCreate(@NotNull DatabaseManager manager) throws SQLException {
        // Default: no action
    }

    /**
     * Optional method to execute custom SQL before dropping tables.
     *
     * @param manager the database manager
     * @throws SQLException if execution fails
     */
    protected void beforeDrop(@NotNull DatabaseManager manager) throws SQLException {
        // Default: no action
    }

    /**
     * Optional method to execute custom SQL after dropping tables.
     *
     * @param manager the database manager
     * @throws SQLException if execution fails
     */
    protected void afterDrop(@NotNull DatabaseManager manager) throws SQLException {
        // Default: no action
    }

    @Override
    public void create(@NotNull DatabaseManager manager) throws SQLException {
        DatabaseType dbType = manager.getDatabaseType();

        beforeCreate(manager);

        List<TableBuilder> tables = buildTables(dbType);

        if (tables.isEmpty()) {
            return;
        }

        try (Connection conn = manager.getConnection();
             Statement stmt = conn.createStatement()) {

            for (TableBuilder table : tables) {
                List<String> statements = table.buildAll(dbType, conn);

                for (String sql : statements) {
                    stmt.executeUpdate(sql);
                }
            }
        }

        afterCreate(manager);
    }

    @Override
    public void drop(@NotNull DatabaseManager manager) throws SQLException {
        DatabaseType dbType = manager.getDatabaseType();

        beforeDrop(manager);

        List<String> tableNames = getTableNames(dbType);

        if (tableNames.isEmpty()) {
            return;
        }

        // Reverse order for dropping (handles foreign key dependencies)
        List<String> reversedNames = new ArrayList<>(tableNames);
        Collections.reverse(reversedNames);

        try (Connection conn = manager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Disable foreign key checks for clean drops
            disableForeignKeyChecks(stmt, dbType);

            for (String tableName : reversedNames) {
                String dropSQL = buildDropStatement(tableName, dbType);
                stmt.executeUpdate(dropSQL);
            }

            // Re-enable foreign key checks
            enableForeignKeyChecks(stmt, dbType);
        }

        afterDrop(manager);
    }

    /**
     * Builds a DROP TABLE statement for the specified database type.
     */
    private @NotNull String buildDropStatement(@NotNull String tableName, @NotNull DatabaseType dbType) {
        return switch (dbType) {
            case MYSQL, MARIADB, SQLITE, H2 -> "DROP TABLE IF EXISTS " + tableName;
            case POSTGRESQL -> "DROP TABLE IF EXISTS " + tableName + " CASCADE";
        };
    }

    /**
     * Disables foreign key checks for the current session.
     */
    private void disableForeignKeyChecks(@NotNull Statement stmt, @NotNull DatabaseType dbType) throws SQLException {
        String sql = switch (dbType) {
            case MYSQL, MARIADB -> "SET FOREIGN_KEY_CHECKS=0";
            case POSTGRESQL -> "SET session_replication_role = 'replica'";
            case SQLITE -> "PRAGMA foreign_keys = OFF";
            case H2 -> "SET REFERENTIAL_INTEGRITY FALSE";
        };

        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Ignore if not supported
        }
    }

    /**
     * Re-enables foreign key checks for the current session.
     */
    private void enableForeignKeyChecks(@NotNull Statement stmt, @NotNull DatabaseType dbType) throws SQLException {
        String sql = switch (dbType) {
            case MYSQL, MARIADB -> "SET FOREIGN_KEY_CHECKS=1";
            case POSTGRESQL -> "SET session_replication_role = 'origin'";
            case SQLITE -> "PRAGMA foreign_keys = ON";
            case H2 -> "SET REFERENTIAL_INTEGRITY TRUE";
        };

        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Ignore if not supported
        }
    }

    /**
     * Creates a simple table builder.
     */
    protected @NotNull TableBuilder table(@NotNull String name) {
        return TableBuilder.create(name).ifNotExists();
    }
}