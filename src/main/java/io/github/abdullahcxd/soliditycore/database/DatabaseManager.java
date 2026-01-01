package io.github.abdullahcxd.soliditycore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseManager {

    @Getter
    private HikariDataSource dataSource;
    private final DatabaseConfig config;
    private final Logger logger;
    private final List<Schema> schemas;

    public DatabaseManager(DatabaseConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
        this.schemas = new ArrayList<>();
    }

    public void connect() {
        try {
            HikariConfig hikariConfig = getHikariConfig();

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

            this.dataSource = new HikariDataSource(hikariConfig);
            logger.info("Database connection established successfully!");

            initializeSchemas();

        } catch (Exception e) {
            logger.severe("Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private @NotNull HikariConfig getHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();

        String jdbcUrl = switch (config.getType()) {
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&autoReconnect=true",
                    config.getHost(), config.getPort(), config.getDatabase(), config.isUseSSL());
            case MARIADB -> String.format("jdbc:mariadb://%s:%d/%s?useSSL=%s&autoReconnect=true",
                    config.getHost(), config.getPort(), config.getDatabase(), config.isUseSSL());
            case POSTGRESQL -> String.format("jdbc:postgresql://%s:%d/%s?ssl=%s",
                    config.getHost(), config.getPort(), config.getDatabase(), config.isUseSSL());
            case SQLITE -> String.format("jdbc:sqlite:%s", config.getDatabase());
            case H2 -> String.format("jdbc:h2:./%s", config.getDatabase());
        };

        hikariConfig.setJdbcUrl(jdbcUrl);

        if (config.getType() != DatabaseType.SQLITE && config.getType() != DatabaseType.H2) {
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
        }

        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdleConnections());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setPoolName("SolidityCore-Pool");
        return hikariConfig;
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection closed successfully!");
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or has been closed");
        }
        return dataSource.getConnection();
    }

    public void registerSchema(Schema schema) {
        schemas.add(schema);
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                schema.create(this);
                logger.info("Schema '" + schema.getName() + "' registered and initialized successfully!");
            } catch (SQLException e) {
                logger.severe("Failed to initialize schema '" + schema.getName() + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void initializeSchemas() {
        for (Schema schema : schemas) {
            try {
                schema.create(this);
                logger.info("Schema '" + schema.getName() + "' initialized successfully!");
            } catch (SQLException e) {
                logger.severe("Failed to initialize schema '" + schema.getName() + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void executeUpdate(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}