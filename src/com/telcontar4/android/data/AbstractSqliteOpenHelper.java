package com.telcontar4.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Base class for creating and, potentially, upgrading the application's SQLite database.
 */
public abstract class AbstractSqliteOpenHelper extends SQLiteOpenHelper {

  private static final String LOG_TAG = AbstractSqliteOpenHelper.class.getSimpleName();

  private final Context context;
  private final boolean seedingData;

  public AbstractSqliteOpenHelper(Context context, String name, int version, boolean seedingData) {
    super(context, name, null, version);
    this.context = context;
    this.seedingData = seedingData;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    if (db.inTransaction()) {
      initialize(db);
    } else {
      db.beginTransaction();
      try {
        initialize(db);
        db.setTransactionSuccessful();
      } catch (Exception e) {
        Log.e(LOG_TAG, "Unable to create SQLite database", e);
      } finally {
        db.endTransaction();
      }
    }
  }

  private void initialize(SQLiteDatabase db) {
    Log.i(LOG_TAG, "Creating database tables");
    createTables(db);
    Log.i(LOG_TAG, "Populating reference data");
    populateReferenceData(db, context);
    if (seedingData) {
      Log.i(LOG_TAG, "Seeding transactional tables with data");
      seedTransactionalTables(db, context);
    }
  }

  protected abstract void createTables(SQLiteDatabase db);

  protected abstract void populateReferenceData(SQLiteDatabase db, Context context);

  protected abstract void seedTransactionalTables(SQLiteDatabase db, Context context);

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.beginTransaction();
    try {
      doUpgrade(db, oldVersion, newVersion);
      db.setTransactionSuccessful();
    } catch (Exception e) {
      Log.e(LOG_TAG, "Unable to upgrade SQLite database from " + oldVersion + " to " + newVersion, e);
    } finally {
      db.endTransaction();
    }
  }

  protected abstract void doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

  protected boolean upgradeIncludes(int schemaChangeVersion, int oldVersion, int newVersion) {
    return schemaChangeVersion > oldVersion && schemaChangeVersion <= newVersion;
  }

}
