package io.github.abdullahcxd.soliditycore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.abdullahcxd.soliditycore.exception.DatabaseException;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced database manager with connection pooling, async operations, and schema management.
 * <p>
 * Deprecated and replaced with Storage providers, storage providers are better use instead of
 * the DatabaseManager, since they are powerful and doesn't need schema based databasing
 * which isn't good when trying to create tables for different databases, etc.
 * <p>
 * Use {@link io.github.abdullahcxd.soliditycore.storage.StorageManager} and {@link io.github.abdullahcxd.soliditycore.storage.StorageProvider}
 */
@Deprecated(since = "0.0.9")
public class DatabaseManager {

    @Getter
    private HikariDataSource dataSource;

    private final DatabaseConfig config;
    private final Logger logger;
    private final List<Schema> schemas;
    private final ConcurrentHashMap<String, Integer> schemaVersions;
    private final ExecutorService asyncExecutor;

    @Getter
    private boolean connected;

    public DatabaseManager(@NotNull DatabaseConfig config, @NotNull Logger logger) {
        this.config = config;
        this.logger = logger;
        this.schemas = new ArrayList<>();
        this.schemaVersions = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newFixedThreadPool(
                Math.max(2, config.getMaxPoolSize() / 2),
                r -> {
                    Thread thread = new Thread(r, "SolidityCore-DB-Async");
                    thread.setDaemon(true);
                    return thread;
                }
        );
        this.connected = false;
    }

    /**
     * Establishes database connection with optimized HikariCP settings.
     */
    public void connect() {
        if (connected) {
            logger.warning("Database is already connected!");
            return;
        }

        try {
            HikariConfig hikariConfig = buildHikariConfig();
            configureOptimizations(hikariConfig);

            this.dataSource = new HikariDataSource(hikariConfig);
            this.connected = true;

            // Test connection
            try (Connection conn = getConnection()) {
                if (!conn.isValid(5)) {
                    throw new SQLException("Connection validation failed");
                }
            }

            initializeSchemas();

        } catch (Exception e) {
            this.connected = false;
            logger.severe("Failed to establish database connection: " + e.getMessage());
            throw new DatabaseException("Failed to connect to database", e);
        }
    }

    /**
     * Builds HikariCP configuration based on database type.
     */
    private @NotNull HikariConfig buildHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();

        String jdbcUrl = buildJdbcUrl();
        hikariConfig.setJdbcUrl(jdbcUrl);

        // Set credentials for non-embedded databases
        if (requiresCredentials()) {
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
        }

        // Set driver class explicitly if needed
        switch (config.getType()) {
            case MYSQL -> hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            case MARIADB -> hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
            case POSTGRESQL -> hikariConfig.setDriverClassName("org.postgresql.Driver");
            case SQLITE -> hikariConfig.setDriverClassName("org.sqlite.JDBC");
            case H2 -> hikariConfig.setDriverClassName("org.h2.Driver");
        }

        // Pool configuration
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdleConnections());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setPoolName("SolidityCore-Pool");

        // Leak detection (helpful for debugging)
        hikariConfig.setLeakDetectionThreshold(60000); // 60 seconds

        return hikariConfig;
    }

    /**
     * Builds JDBC URL based on database type.
     */
    private @NotNull String buildJdbcUrl() {
        return switch (config.getType()) {
            case MYSQL -> String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=%s&autoReconnect=true&useUnicode=true&characterEncoding=utf8",
                    config.getHost(), config.getPort(), config.getDatabase(), config.isUseSSL()
            );
            case MARIADB -> String.format(
                    "jdbc:mariadb://%s:%d/%s?useSSL=%s&autoReconnect=true",
                    config.getHost(), config.getPort(), config.getDatabase(), config.isUseSSL()
            );
            case POSTGRESQL -> String.format(
                    "jdbc:postgresql://%s:%d/%s?ssl=%s&ApplicationName=SolidityCore",
                    config.getHost(), config.getPort(), config.getDatabase(), config.isUseSSL()
            );
            case SQLITE -> String.format("jdbc:sqlite:%s.db", config.getDatabase());
            case H2 -> String.format("jdbc:h2:./%s;AUTO_SERVER=TRUE", config.getDatabase());
        };
    }

    /**
     * Configures database-specific optimizations.
     */
    private void configureOptimizations(@NotNull HikariConfig hikariConfig) {
        switch (config.getType()) {
            case MYSQL, MARIADB -> {
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
                hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
                hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
                hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
                hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

                if (config.getType() == DatabaseType.MARIADB) {
                    hikariConfig.addDataSourceProperty("allowMultiQueries", "true"); // improves batch inserts
                    hikariConfig.addDataSourceProperty("useSSL", String.valueOf(config.isUseSSL()));
                }
            }
            case POSTGRESQL -> {
                hikariConfig.addDataSourceProperty("prepareThreshold", "3");
                hikariConfig.addDataSourceProperty("preparedStatementCacheQueries", "256");
                hikariConfig.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
            }
            case SQLITE -> {
                hikariConfig.addDataSourceProperty("journal_mode", "WAL");
                hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
                hikariConfig.addDataSourceProperty("cache_size", "10000");
            }
            case H2 -> {
                hikariConfig.addDataSourceProperty("CACHE_SIZE", "65536");   // default is small, increase for large datasets
                hikariConfig.addDataSourceProperty("LOCK_MODE", "3");       // table-level locking for concurrency
                hikariConfig.addDataSourceProperty("AUTO_SERVER", "TRUE");  // allow multiple processes to access same file DB
                hikariConfig.addDataSourceProperty("MV_STORE", "TRUE");     // use modern MVCC storage engine
                hikariConfig.addDataSourceProperty("MVCC", "TRUE");         // enable multi-version concurrency
            }
        }
    }

    /**
     * Checks if database type requires credentials.
     */
    private boolean requiresCredentials() {
        return config.getType() != DatabaseType.SQLITE && config.getType() != DatabaseType.H2;
    }

    /**
     * Disconnects and closes the database connection pool.
     */
    public void disconnect() {
        if (!connected) {
            logger.warning("Database is not connected!");
            return;
        }

        try {
            // Shutdown async executor
            asyncExecutor.shutdown();

            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } catch (Exception e) {
            logger.severe("Error while closing database connection: " + e.getMessage());
            throw new SolidityException(e);
        } finally {
            connected = false;
        }
    }

    /**
     * Gets a connection from the pool.
     */
    public @NotNull Connection getConnection() throws SQLException {
        if (!connected || dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or has been closed");
        }
        return dataSource.getConnection();
    }

    /**
     * Executes a query with a connection consumer.
     */
    public void execute(@NotNull Consumer<Connection> consumer) {
        try (Connection conn = getConnection()) {
            consumer.accept(conn);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing database operation", e);
            throw new DatabaseException("Failed to execute database operation", e);
        }
    }

    /**
     * Executes a query with a connection function and returns a result.
     */
    public <T> @Nullable T executeWithResult(@NotNull Function<Connection, T> function) {
        try (Connection conn = getConnection()) {
            return function.apply(conn);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing database query", e);
            throw new DatabaseException("Failed to execute database query", e);
        }
    }

    /**
     * Executes an async operation.
     */
    public @NotNull CompletableFuture<Void> executeAsync(@NotNull Consumer<Connection> consumer) {
        return CompletableFuture.runAsync(() -> execute(consumer), asyncExecutor);
    }

    /**
     * Executes an async query and returns a result.
     */
    public <T> @NotNull CompletableFuture<T> executeAsyncWithResult(@NotNull Function<Connection, T> function) {
        return CompletableFuture.supplyAsync(() -> executeWithResult(function), asyncExecutor);
    }

    /**
     * Executes a transaction with automatic rollback on failure.
     */
    public void executeTransaction(@NotNull Consumer<Connection> transaction) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            transaction.accept(conn);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    logger.severe("Failed to rollback transaction: " + rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Transaction failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Failed to close connection after transaction", e);
                }
            }
        }
    }

    /**
     * Executes an update statement.
     */
    public int executeUpdate(@NotNull String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }

    /**
     * Executes a batch update.
     */
    public int[] executeBatch(@NotNull String sql, @NotNull List<Object[]> paramsList) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] params : paramsList) {
                setParameters(stmt, params);
                stmt.addBatch();
            }

            return stmt.executeBatch();
        }
    }

    /**
     * Registers a schema for initialization.
     */
    public void registerSchema(@NotNull Schema schema) {
        if (schemas.stream().anyMatch(s -> s.getName().equals(schema.getName()))) {
            logger.warning("Schema '" + schema.getName() + "' is already registered!");
            return;
        }

        schemas.add(schema);

        if (connected) {
            try {
                initializeSchema(schema);
            } catch (Exception e) {
                logger.severe("Failed to initialize schema '" + schema.getName() + "': " + e.getMessage());
                throw new SolidityException(e);
            }
        }
    }

    /**
     * Unregisters a schema.
     */
    public void unregisterSchema(@NotNull String schemaName) {
        schemas.removeIf(s -> s.getName().equals(schemaName));
        schemaVersions.remove(schemaName);
    }

    /**
     * Initializes all registered schemas.
     */
    private void initializeSchemas() {
        if (schemas.isEmpty()) {
            return;
        }

        for (Schema schema : schemas) {
            try {
                initializeSchema(schema);
            } catch (Exception e) {
                logger.severe("Failed to initialize schema '" + schema.getName() + "': " + e.getMessage());
                throw new SolidityException(e);
            }
        }
    }

    /**
     * Initializes a single schema with version tracking.
     */
    private void initializeSchema(@NotNull Schema schema) throws SQLException {
        String schemaName = schema.getName();
        int currentVersion = schemaVersions.getOrDefault(schemaName, 0);
        int newVersion = schema.getVersion();

        if (currentVersion == newVersion) {
            return;
        }

        schema.create(this);
        schemaVersions.put(schemaName, newVersion);
    }

    /**
     * Checks if a table exists in the database.
     */
    public boolean tableExists(@NotNull String tableName) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to check if table exists: " + tableName, e);
            return false;
        }
    }

    /**
     * Gets the database type.
     */
    public @NotNull DatabaseType getDatabaseType() {
        return config.getType();
    }

    /**
     * Gets schema version.
     */
    public int getSchemaVersion(@NotNull String schemaName) {
        return schemaVersions.getOrDefault(schemaName, 0);
    }

    /**
     * Helper method to set prepared statement parameters.
     */
    private void setParameters(@NotNull PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * Gets connection pool statistics.
     */
    public @NotNull String getPoolStats() {
        if (dataSource == null) {
            return "DataSource not initialized";
        }

        return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
}