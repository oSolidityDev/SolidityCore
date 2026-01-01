package io.github.abdullahcxd.soliditycore.database;

import lombok.Getter;

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