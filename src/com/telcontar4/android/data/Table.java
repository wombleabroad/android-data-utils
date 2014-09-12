package com.telcontar4.android.data;

/**
 * Fluent API for creating (and altering?) SQLite tables.
 */
public class Table {

    private static final String COLUMN_TYPE_ID = " INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String COLUMN_TYPE_INTEGER = " INTEGER";
    public static final String COLUMN_TYPE_TEXT = " TEXT";

    private final StringBuilder sql;
    private final String tableName;
    private boolean columnsStarted;

    public Table(String name) {
        tableName = name;

        sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName);
    }

    public Table withIdColumn(String columnName) {
        prefixColumn();
        sql.append(columnName).append(COLUMN_TYPE_ID);
        return this;
    }

    private void prefixColumn() {
        if (columnsStarted) {
            sql.append(", ");
        } else {
            sql.append(" (");
            columnsStarted = true;
        }
    }

    public Table withColumn(String columnName, String columnType) {
        prefixColumn();
        sql.append(columnName).append(columnType);
        return this;
    }

    public String getSql() {
        sql.append("); ");
        return sql.toString();
    }

    public Index addIndex(String indexName, String columnName) {
        return new Index(tableName, indexName, columnName);
    }

    public Index addIndex(String indexName, boolean unique, String... columnNames) {
        return new Index(tableName, indexName, unique, columnNames);
    }

    /**
     * Represents a table index.
     */
    public class Index {

        private final String sql;

        private Index(String tableName, String indexName, String columnName) {
            sql = "CREATE INDEX IF NOT EXISTS " + indexName + " ON " + tableName + " (" + columnName + "); ";
        }

        private Index(String tableName, String indexName, boolean unique, String... columnNames) {
            sql = "CREATE " + uniqueKeyword(unique) + "INDEX IF NOT EXISTS " + indexName + " ON " + tableName + " ("
                    + toCsv(columnNames) + "); ";
        }

        private String uniqueKeyword(boolean unique) {
            return unique ? "UNIQUE " : "";
        }

        private String toCsv(String[] columnNames) {
            StringBuilder columnNamesCsv = new StringBuilder();
            for (String columnName : columnNames) {
                if (columnNamesCsv.length() > 0) {
                    columnNamesCsv.append(", ");
                }
                columnNamesCsv.append(columnName);
            }
            return columnNamesCsv.toString();
        }

        public String getSql() {
            return sql;
        }

    }

}
