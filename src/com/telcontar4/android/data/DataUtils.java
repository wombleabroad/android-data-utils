/**
 * 
 */
package com.telcontar4.android.data;

import java.text.DateFormat;
import java.util.Date;

import android.database.Cursor;
import android.util.Log;

/**
 * Utility operations for data access components.
 */
public class DataUtils {

  public static final Integer SQLITE_BOOLEAN_INT_FALSE = Integer.valueOf(0);
  public static final Integer SQLITE_BOOLEAN_INT_TRUE = Integer.valueOf(1);

  private DataUtils() {
  }

  public static boolean getBoolean(Cursor cursor, String columnName) {
    return SQLITE_BOOLEAN_INT_TRUE.equals(getInt(cursor, columnName));
  }

  public static int getSqliteBoolean(boolean value) {
    return value ? SQLITE_BOOLEAN_INT_TRUE : SQLITE_BOOLEAN_INT_FALSE;
  }

  public static Integer getInt(Cursor cursor, String columnName) {
    return isAnyNull(cursor, columnName) ? null : cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
  }

  public static Long getLong(Cursor cursor, String columnName) {
    return isAnyNull(cursor, columnName) ? null : cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
  }

  public static String getString(Cursor cursor, String columnName) {
    return isAnyNull(cursor, columnName) ? null : cursor.getString(cursor.getColumnIndexOrThrow(columnName));
  }

  public static String getFormattedDate(Cursor cursor, String columnName) {
    if (isAnyNull(cursor, columnName)) {
      return null;
    } else {
      Date dateValue = new Date(getLong(cursor, columnName));
      return formatAsDate(dateValue);
    }
  }

  public static String formatAsDate(Date dateValue) {
    return DateFormat.getDateInstance().format(dateValue);
  }

  public static String getFormattedTime(Cursor cursor, String columnName) {
    if (isAnyNull(cursor, columnName)) {
      return null;
    } else {
      Date dateValue = new Date(getLong(cursor, columnName));
      return formatAsTime(dateValue);
    }
  }

  public static String formatAsTime(Date dateValue) {
    return DateFormat.getTimeInstance().format(dateValue);
  }

  public static String getFormattedDateTime(Cursor cursor, String columnName) {
    if (isAnyNull(cursor, columnName)) {
      return null;
    } else {
      Date dateValue = new Date(getLong(cursor, columnName));
      return formatAsDateTime(dateValue);
    }
  }

  public static String formatAsDateTime(Date dateValue) {
    return DateFormat.getDateTimeInstance().format(dateValue);
  }

  public static boolean isAnyNull(Cursor cursor, String... columnNames) {
    for (String columnName : columnNames) {
      if (cursor.isNull(cursor.getColumnIndexOrThrow(columnName))) {
        return true;
      }
    }
    return false;
  }

  public static void dumpCursorContents(String logTag, String title, Cursor cursor) {
    if (Log.isLoggable(logTag, Log.VERBOSE)) {
      while (cursor.moveToNext()) {
        Log.v(logTag, DataUtils.stringifyCursorAtCurrentPosition(title, cursor));
      }
      cursor.moveToPosition(-1);
    }
  }

  public static String stringifyCursorAtCurrentPosition(String title, Cursor cursor) {
    StringBuilder dump = new StringBuilder(title);
    dump.append(":").append(cursor.getPosition());
    for (int i = 0; i < cursor.getColumnCount(); i++) {
      dump.append("|").append(cursor.getColumnName(i)).append("=");
      try {
        if (cursor.isNull(i)) {
          dump.append("NULL");
        } else {
          dump.append(cursor.getString(i));
        }
      } catch (RuntimeException e) {
        dump.append("???");
      }
    }
    return dump.toString();
  }

}
