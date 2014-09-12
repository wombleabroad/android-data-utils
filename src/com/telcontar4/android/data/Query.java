/**
 * 
 */
package com.telcontar4.android.data;

/**
 * Robolectric-friendly replacement for Android's SQLiteQueryBuilder. Provides a fluent interface for generating custom
 * SQL that easily incorporates table and column name constants defined in your Schema class.
 */
public class Query {

  private final StringBuilder sql;
  private boolean selectStarted;
  private boolean whereStarted;
  private boolean orStarted;
  private boolean orderByStarted;

  public Query() {
    sql = new StringBuilder("SELECT ");
  }

  public Query select(String... columns) {
    for (String column : columns) {
      if (selectStarted) {
        sql.append(", ");
      }
      sql.append(column);
      selectStarted = true;
    }
    return this;
  }

  public Query selectDistinct(String... columns) {
    sql.append(" DISTINCT ");
    return select(columns);
  }

  public Query count(String alias) {
    if (selectStarted) {
      sql.append(", ");
    }
    sql.append("COUNT(*) ").append(alias);
    selectStarted = true;
    return this;
  }

  public Query from(String table) {
    sql.append(" FROM ").append(table);
    return this;
  }

  public Query innerJoin(String table) {
    sql.append(" INNER JOIN ").append(table);
    return this;
  }

  public Query innerJoin(Query query, String alias) {
    sql.append(" INNER JOIN (").append(query.getSql()).append(") ").append(alias);
    return this;
  }

  public Query leftOuterJoin(String table) {
    sql.append(" LEFT OUTER JOIN ").append(table);
    return this;
  }

  public Query on(String fromColumn, String toColumn) {
    sql.append(" ON ").append(fromColumn).append(" = ").append(toColumn);
    return this;
  }

  public Query whereEqualTo(String column, String value) {
    prefixWhereSubclause();
    sql.append(column).append(" = ").append(value);
    return this;
  }

  private void prefixWhereSubclause() {
    if (orStarted) {
    } else if (whereStarted) {
      sql.append(" AND ");
    } else {
      sql.append(" WHERE ");
      whereStarted = true;
    }
  }

  public Query whereEqualToText(String column, String value) {
    prefixWhereSubclause();
    sql.append(column).append(" = '").append(value).append("'");
    return this;
  }

  public Query whereNotEqualToText(String column, String value) {
    prefixWhereSubclause();
    sql.append(column).append(" <> '").append(value).append("'");
    return this;
  }

  public Query whereIn(String column, String valueList) {
    prefixWhereSubclause();
    sql.append(column).append(" IN (").append(valueList).append(")");
    return this;
  }

  public Query whereNotIn(String column, String valueList) {
    prefixWhereSubclause();
    sql.append(column).append(" NOT IN (").append(valueList).append(")");
    return this;
  }

  public Query whereStartsWith(String column, String prefix) {
    prefixWhereSubclause();
    sql.append(column).append(" LIKE '").append(prefix).append("%'");
    return this;
  }

  public Query whereNull(String column) {
    prefixWhereSubclause();
    sql.append(column).append(" IS NULL");
    return this;
  }

  public Query andEither() {
    orStarted = true;
    sql.append(" AND (");
    return this;
  }

  public Query or() {
    sql.append(" OR ");
    return this;
  }

  public Query endOr() {
    sql.append(") ");
    return this;
  }

  public Query groupBy(String column) {
    sql.append(" GROUP BY ").append(column);
    return this;
  }

  public Query orderBy(String column, boolean ascending) {
    prefixOrderBySubclause();
    sql.append(column).append(ascending ? " ASC" : " DESC");
    return this;
  }

  private void prefixOrderBySubclause() {
    if (orderByStarted) {
      sql.append(", ");
    } else {
      sql.append(" ORDER BY ");
      orderByStarted = true;
    }
  }

  public String getSql() {
    return sql.toString();
  }

}
