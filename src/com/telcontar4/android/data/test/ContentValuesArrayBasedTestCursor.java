/**
 * 
 */
package com.telcontar4.android.data.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;

/**
 * Provides stub data based on an array of ContentValues representing rows of database columns.
 */
public class ContentValuesArrayBasedTestCursor implements Cursor {

    private static final int INVALID_POSITION = -1;

    private final ContentValues[] rows;
    private final Map<String, Integer> columnIndicesByName = new HashMap<String, Integer>();
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, String> columnNamesByIndex = new HashMap<Integer, String>();

    private int position = INVALID_POSITION;
    private final List<ContentObserver> contentObservers = new ArrayList<ContentObserver>();
    private final List<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();
    private boolean closed;

    public ContentValuesArrayBasedTestCursor(int count) {
        this(createIdentityOnlyRows(count));
    }

    private static ContentValues[] createIdentityOnlyRows(int count) {
        ContentValues[] identityOnlyRows = new ContentValues[count];
        for (int i = 0; i < count; i++) {
            ContentValues row = new ContentValues();
            row.put(BaseColumns._ID, Long.valueOf(i + 1));
            identityOnlyRows[i] = row;
        }
        return identityOnlyRows;
    }

    public ContentValuesArrayBasedTestCursor(ContentValues... rows) {
        this.rows = rows;
        if (rows.length > 0) {
            mapColumnNamesToIndices(rows);
        }
    }

    private void mapColumnNamesToIndices(ContentValues[] rows) {
        ContentValues firstRow = rows[0];
        int columnIndex = -1;
        for (Entry<String, Object> rowEntry : firstRow.valueSet()) {
            columnIndicesByName.put(rowEntry.getKey(), ++columnIndex);
            columnNamesByIndex.put(columnIndex, rowEntry.getKey());
        }
    }

    @Override
    public int getCount() {
        return rows.length;
    }

    @Override
    public int getColumnIndex(String columnName) {
        Integer columnIndex = columnIndicesByName.get(columnName);
        if (columnIndex == null) {
            return -1;
        } else {
            return columnIndex;
        }
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        Integer columnIndex = columnIndicesByName.get(columnName);
        if (columnIndex == null) {
            throw new IllegalArgumentException(columnName + " is not a valid database column name");
        } else {
            return columnIndex;
        }
    }

    @Override
    public int getColumnCount() {
        return columnIndicesByName.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        String columnName = columnNamesByIndex.get(columnIndex);
        if (columnName == null) {
            throw new IllegalArgumentException(columnIndex + " is not a valid database column index");
        } else {
            return columnName;
        }
    }

    @Override
    public String[] getColumnNames() {
        String[] columnNames = new String[columnNamesByIndex.size()];
        columnNames = columnNamesByIndex.values().toArray(columnNames);
        return columnNames;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public double getDouble(int columnIndex) {
        return (Double) getCurrentRowColumnValue(columnIndex);
    }

    private Object getCurrentRowColumnValue(int columnIndex) {
        ContentValues row = getCurrentRow();
        return row.get(getColumnName(columnIndex));
    }

    private ContentValues getCurrentRow() {
        return rows[position];
    }

    @Override
    public float getFloat(int columnIndex) {
        return (Float) getCurrentRowColumnValue(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) {
        return (Integer) getCurrentRowColumnValue(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) {
        return (Long) getCurrentRowColumnValue(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) {
        return (Short) getCurrentRowColumnValue(columnIndex);
    }

    @Override
    public String getString(int columnIndex) {
        return getCurrentRowColumnValue(columnIndex).toString();
    }

    @Override
    public boolean isNull(int columnIndex) {
        return getCurrentRowColumnValue(columnIndex) == null;
    }

    @Override
    public boolean isBeforeFirst() {
        return position < 0;
    }

    @Override
    public boolean isFirst() {
        return position == 0;
    }

    @Override
    public boolean isLast() {
        return position == getCount();
    }

    @Override
    public boolean isAfterLast() {
        return position >= getCount();
    }

    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    @Override
    public boolean moveToPrevious() {
        return position > 0 ? moveToPosition(position - 1) : false;
    }

    @Override
    public boolean moveToNext() {
        return position < getCount() - 1 ? moveToPosition(position + 1) : false;
    }

    @Override
    public boolean moveToPosition(int position) {
        if (position >= INVALID_POSITION || position <= getCount()) {
            this.position = position;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean move(int offset) {
        if (moveToPosition(position + offset)) {
            return true;
        } else {
            position = (offset < 0) ? -1 : getCount();
            return false;
        }
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        contentObservers.add(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        contentObservers.remove(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        dataSetObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataSetObservers.remove(observer);
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void deactivate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getExtras() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean requery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle respond(Bundle extras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri getNotificationUri() {
        throw new UnsupportedOperationException();
    }

}
