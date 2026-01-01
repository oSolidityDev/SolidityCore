package io.github.abdullahcxd.soliditycore.database;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DatabaseConfig {

    @Builder.Default
    private DatabaseType type = DatabaseType.SQLITE;

    @Builder.Default
    private String host = "localhost";

    @Builder.Default
    private int port = 3306;

    @Builder.Default
    private String database = "soliditycore";

    @Builder.Default
    private String username = "root";

    @Builder.Default
    private String password = "";

    @Builder.Default
    private boolean useSSL = false;

    @Builder.Default
    private int maxPoolSize = 10;

    @Builder.Default
    private int minIdleConnections = 2;

    @Builder.Default
    private long connectionTimeout = 30000;

    @Builder.Default
    private long idleTimeout = 600000;

    @Builder.Default
    private long maxLifetime = 1800000;
}