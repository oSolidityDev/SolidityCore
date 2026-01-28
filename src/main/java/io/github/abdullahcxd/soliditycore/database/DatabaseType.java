package io.github.abdullahcxd.soliditycore.database;

import lombok.Getter;

@Deprecated(since = "0.0.9")
public enum DatabaseType {
    MYSQL("mysql"),
    MARIADB("mariadb"),
    POSTGRESQL("postgresql"),
    SQLITE("sqlite"),
    H2("h2");

    @Getter
    private final String id;

    DatabaseType(String id) {
        this.id = id;
    }
}