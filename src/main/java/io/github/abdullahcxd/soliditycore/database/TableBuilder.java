package io.github.abdullahcxd.soliditycore.database;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fluent builder for creating SQL table definitions with cross-database support.
 *
 * <p>Example usage:
 * <pre>{@code
 * String sql = TableBuilder.create("users")
 *     .ifNotExists()
 *     .column(Column.create("id").bigInt().primaryKey().autoIncrement())
 *     .column(Column.create("username").varchar(32).notNull().unique())
 *     .column(Column.create("email").varchar(255).notNull())
 *     .column(Column.create("created_at").timestamp().defaultCurrentTimestamp())
 *     .index("idx_username", "username")
 *     .build(DatabaseType.MYSQL);
 * }</pre>
 */
@Deprecated(since = "0.0.9")
public class TableBuilder {

    private final String tableName;
    private final List<Column> columns;
    private final List<Index> indexes;
    private final List<ForeignKey> foreignKeys;
    private boolean ifNotExists;
    private String engine;
    private String charset;
    private String collation;
    private String comment;

    private TableBuilder(String tableName) {
        this.tableName = tableName;
        this.columns = new ArrayList<>();
        this.indexes = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.ifNotExists = false;
    }

    /**
     * Creates a new table builder.
     */
    public static @NotNull TableBuilder create(@NotNull String tableName) {
        return new TableBuilder(tableName);
    }

    /**
     * Gets the table name.
     */
    public @NotNull String getTableName() {
        return tableName;
    }

    /**
     * Adds IF NOT EXISTS clause.
     */
    public @NotNull TableBuilder ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    /**
     * Adds a column to the table.
     */
    public @NotNull TableBuilder column(@NotNull Column column) {
        this.columns.add(column);
        return this;
    }

    /**
     * Adds an index to the table.
     */
    public @NotNull TableBuilder index(@NotNull String name, @NotNull String... columns) {
        this.indexes.add(new Index(name, false, columns));
        return this;
    }

    /**
     * Adds a unique index to the table.
     */
    public @NotNull TableBuilder uniqueIndex(@NotNull String name, @NotNull String... columns) {
        this.indexes.add(new Index(name, true, columns));
        return this;
    }

    /**
     * Adds a foreign key constraint.
     */
    public @NotNull TableBuilder foreignKey(@NotNull String column, @NotNull String refTable, @NotNull String refColumn) {
        this.foreignKeys.add(new ForeignKey(column, refTable, refColumn, null, null));
        return this;
    }

    /**
     * Adds a foreign key constraint with actions.
     */
    public @NotNull TableBuilder foreignKey(@NotNull String column, @NotNull String refTable,
                                            @NotNull String refColumn, @Nullable String onDelete,
                                            @Nullable String onUpdate) {
        this.foreignKeys.add(new ForeignKey(column, refTable, refColumn, onDelete, onUpdate));
        return this;
    }

    /**
     * Sets the storage engine (MySQL/MariaDB only).
     */
    public @NotNull TableBuilder engine(@NotNull String engine) {
        this.engine = engine;
        return this;
    }

    /**
     * Sets the charset (MySQL/MariaDB only).
     */
    public @NotNull TableBuilder charset(@NotNull String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Sets the collation (MySQL/MariaDB only).
     */
    public @NotNull TableBuilder collation(@NotNull String collation) {
        this.collation = collation;
        return this;
    }

    /**
     * Sets a table comment.
     */
    public @NotNull TableBuilder comment(@NotNull String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Builds the CREATE TABLE SQL statement for the specified database type.
     */
    public @NotNull String build(@NotNull DatabaseType dbType) {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Table must have at least one column");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");

        if (ifNotExists) {
            sql.append("IF NOT EXISTS ");
        }

        sql.append(tableName).append(" (");

        // Add columns
        sql.append(columns.stream()
                .map(col -> col.toSQL(dbType))
                .collect(Collectors.joining(", ")));

        // Add foreign keys
        if (!foreignKeys.isEmpty()) {
            sql.append(", ");
            sql.append(foreignKeys.stream()
                    .map(fk -> fk.toSQL(dbType))
                    .collect(Collectors.joining(", ")));
        }

        sql.append(")");

        // Add database-specific options
        appendDatabaseSpecificOptions(sql, dbType);

        return sql.toString();
    }

    public @NotNull List<String> buildAll(@NotNull DatabaseType dbType, @NotNull java.sql.Connection conn) {
        List<String> statements = new ArrayList<>();

        // Main table creation
        statements.add(build(dbType));

        // Index creation (safely)
        for (Index index : indexes) {
            if (!indexExists(conn, dbType, tableName, index.name)) {
                statements.add(index.toCreateStatement(tableName, dbType));
            }
        }

        return statements;
    }

    /**
     * Checks if an index exists in a cross-database manner.
     */
    @SneakyThrows
    private boolean indexExists(@NotNull java.sql.Connection conn,
                                @NotNull DatabaseType dbType,
                                @NotNull String tableName,
                                @NotNull String indexName) {
        switch (dbType) {
            case MYSQL, MARIADB -> {
                try (var rs = conn.getMetaData().getIndexInfo(conn.getCatalog(), null, tableName, false, false)) {
                    while (rs.next()) {
                        String existingIndex = rs.getString("INDEX_NAME");
                        if (indexName.equalsIgnoreCase(existingIndex)) return true;
                    }
                }
                return false;
            }
            case POSTGRESQL -> {
                String sql = "SELECT indexname FROM pg_indexes WHERE tablename = ? AND indexname = ?";
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, tableName.toLowerCase());
                    ps.setString(2, indexName.toLowerCase());
                    try (var rs = ps.executeQuery()) {
                        return rs.next();
                    }
                }
            }
            case SQLITE -> {
                String sql = "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name=? AND name=?";
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, tableName);
                    ps.setString(2, indexName);
                    try (var rs = ps.executeQuery()) {
                        return rs.next();
                    }
                }
            }
            case H2 -> {
                String sql = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = ? AND INDEX_NAME = ?";
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, tableName.toUpperCase());
                    ps.setString(2, indexName.toUpperCase());
                    try (var rs = ps.executeQuery()) {
                        return rs.next();
                    }
                }
            }
            default -> throw new IllegalStateException("Unsupported DB type: " + dbType);
        }
    }

    private void appendDatabaseSpecificOptions(@NotNull StringBuilder sql, @NotNull DatabaseType dbType) {
        switch (dbType) {
            case MYSQL, MARIADB -> {
                if (engine != null) {
                    sql.append(" ENGINE=").append(engine);
                }
                if (charset != null) {
                    sql.append(" DEFAULT CHARSET=").append(charset);
                }
                if (collation != null) {
                    sql.append(" COLLATE=").append(collation);
                }
                if (comment != null) {
                    sql.append(" COMMENT='").append(comment.replace("'", "''")).append("'");
                }
            }
            case POSTGRESQL -> {
                // PostgreSQL uses different syntax for comments
                // Comments should be added as separate ALTER TABLE statements
            }
            case SQLITE, H2 -> {
                // These databases don't support table-level options in CREATE TABLE
            }
        }
    }

    /**
     * Represents a table column definition.
     */
    @Getter
    public static class Column {
        private final String name;
        private String type;
        private Integer length;
        private Integer precision;
        private Integer scale;
        private boolean notNull;
        private boolean primaryKey;
        private boolean autoIncrement;
        private boolean unique;
        private String defaultValue;
        private String comment;
        private String checkConstraint;

        private Column(String name) {
            this.name = name;
            this.notNull = false;
            this.primaryKey = false;
            this.autoIncrement = false;
            this.unique = false;
        }

        public static @NotNull Column create(@NotNull String name) {
            return new Column(name);
        }

        // Type methods
        public @NotNull Column tinyInt() {
            this.type = "TINYINT";
            return this;
        }

        public @NotNull Column smallInt() {
            this.type = "SMALLINT";
            return this;
        }

        public @NotNull Column integer() {
            this.type = "INTEGER";
            return this;
        }

        public @NotNull Column bigInt() {
            this.type = "BIGINT";
            return this;
        }

        public @NotNull Column decimal(int precision, int scale) {
            this.type = "DECIMAL";
            this.precision = precision;
            this.scale = scale;
            return this;
        }

        public @NotNull Column varchar(int length) {
            this.type = "VARCHAR";
            this.length = length;
            return this;
        }

        public @NotNull Column text() {
            this.type = "TEXT";
            return this;
        }

        public @NotNull Column longText() {
            this.type = "LONGTEXT";
            return this;
        }

        public @NotNull Column bool() {
            this.type = "BOOLEAN";
            return this;
        }

        public @NotNull Column date() {
            this.type = "DATE";
            return this;
        }

        public @NotNull Column time() {
            this.type = "TIME";
            return this;
        }

        public @NotNull Column timestamp() {
            this.type = "TIMESTAMP";
            return this;
        }

        public @NotNull Column datetime() {
            this.type = "DATETIME";
            return this;
        }

        public @NotNull Column json() {
            this.type = "JSON";
            return this;
        }

        public @NotNull Column blob() {
            this.type = "BLOB";
            return this;
        }

        // Constraint methods
        public @NotNull Column notNull() {
            this.notNull = true;
            return this;
        }

        public @NotNull Column primaryKey() {
            this.primaryKey = true;
            this.notNull = true;
            return this;
        }

        public @NotNull Column autoIncrement() {
            this.autoIncrement = true;
            return this;
        }

        public @NotNull Column unique() {
            this.unique = true;
            return this;
        }

        public @NotNull Column defaultValue(@NotNull String value) {
            this.defaultValue = value;
            return this;
        }

        public @NotNull Column defaultNull() {
            this.defaultValue = "NULL";
            return this;
        }

        public @NotNull Column defaultCurrentTimestamp() {
            this.defaultValue = "CURRENT_TIMESTAMP";
            return this;
        }

        public @NotNull Column comment(@NotNull String comment) {
            this.comment = comment;
            return this;
        }

        public @NotNull Column check(@NotNull String constraint) {
            this.checkConstraint = constraint;
            return this;
        }

        String toSQL(@NotNull DatabaseType dbType) {
            StringBuilder sql = new StringBuilder(name).append(" ");

            // Type conversion based on database
            sql.append(convertType(dbType));

            // NOT NULL
            if (notNull) {
                sql.append(" NOT NULL");
            }

            // Auto increment
            if (autoIncrement) {
                sql.append(getAutoIncrementSyntax(dbType));
            }

            // Default value
            if (defaultValue != null) {
                sql.append(" DEFAULT ").append(defaultValue);
            }

            // Unique constraint
            if (unique) {
                sql.append(" UNIQUE");
            }

            // Primary key
            if (primaryKey) {
                sql.append(" PRIMARY KEY");
            }

            // Check constraint
            if (checkConstraint != null) {
                sql.append(" CHECK (").append(checkConstraint).append(")");
            }

            // Comment (MySQL/MariaDB)
            if (comment != null && (dbType == DatabaseType.MYSQL || dbType == DatabaseType.MARIADB)) {
                sql.append(" COMMENT '").append(comment.replace("'", "''")).append("'");
            }

            return sql.toString();
        }

        private @NotNull String convertType(@NotNull DatabaseType dbType) {
            if (type == null) {
                throw new IllegalStateException("Column type not specified for: " + name);
            }

            String result = switch (type) {
                case "BOOLEAN" -> dbType == DatabaseType.POSTGRESQL ? "BOOLEAN" : "TINYINT(1)";
                case "LONGTEXT" -> dbType == DatabaseType.POSTGRESQL ? "TEXT" : "LONGTEXT";
                case "JSON" -> {
                    if (dbType == DatabaseType.SQLITE) yield "TEXT";
                    if (dbType == DatabaseType.POSTGRESQL) yield "JSONB";
                    yield "JSON";
                }
                case "DATETIME" -> dbType == DatabaseType.POSTGRESQL ? "TIMESTAMP" : "DATETIME";
                default -> type;
            };

            // Add length/precision
            if (length != null) {
                result += "(" + length + ")";
            } else if (precision != null && scale != null) {
                result += "(" + precision + "," + scale + ")";
            }

            return result;
        }

        private @NotNull String getAutoIncrementSyntax(@NotNull DatabaseType dbType) {
            return switch (dbType) {
                case MYSQL, MARIADB -> " AUTO_INCREMENT";
                case POSTGRESQL -> ""; // PostgreSQL uses SERIAL type instead
                case SQLITE -> " AUTOINCREMENT";
                case H2 -> " AUTO_INCREMENT";
            };
        }
    }

    /**
     * Represents an index definition.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Index {
        private final String name;
        private final boolean unique;
        private final String[] columns;

        String toSQL(@NotNull DatabaseType dbType) {
            String type = unique ? "UNIQUE KEY" : "KEY";
            String cols = String.join(", ", columns);
            return type + " " + name + " (" + cols + ")";
        }

        String toCreateStatement(@NotNull String tableName, @NotNull DatabaseType dbType) {
            String type = unique ? "UNIQUE INDEX" : "INDEX";
            String cols = String.join(", ", columns);

            return switch (dbType) {
                case MYSQL, MARIADB -> String.format("CREATE %s %s ON %s (%s)", type, name, tableName, cols);
                case POSTGRESQL, SQLITE, H2 -> String.format("CREATE %s %s ON %s (%s)", type, name, tableName, cols);
            };
        }
    }

    /**
     * Represents a foreign key constraint.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ForeignKey {
        private final String column;
        private final String refTable;
        private final String refColumn;
        private final String onDelete;
        private final String onUpdate;

        @NotNull String toSQL(@NotNull DatabaseType dbType) {
            StringBuilder sql = new StringBuilder("FOREIGN KEY (")
                    .append(column)
                    .append(") REFERENCES ")
                    .append(refTable)
                    .append("(")
                    .append(refColumn)
                    .append(")");

            if (onDelete != null) {
                sql.append(" ON DELETE ").append(onDelete);
            }
            if (onUpdate != null) {
                sql.append(" ON UPDATE ").append(onUpdate);
            }

            return sql.toString();
        }
    }
}