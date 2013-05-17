android-data-utils
==================

When working with local sqlite databases in your Android applications, you can either work with the
standard API or use one of the open-source ORM solutions. The former reduces dependencies on
third-party tools, although it requires more boilerplate code; the latter can reduce code and give
a more Core Data or Hibernate-like experience but ties you to that application architecture. I have
inclined toward the standard API approach and, in doing so, have found myself reusing a number of
utility classes that make working with SQLite databases in Android apps a little easier. And here
they are.

This is currently packaged as a simple JAR - not an Android library - that you can build and add to
either your local Maven repository or your Android project's libs directory.

Creating and Updating Databases
-------------------------------

### AbstractSqliteOpenHelper

`AbstractSqliteOpenHelper` extends Android's `SQLiteOpenHelper` by adding structure around database
creation in particular. The `onCreate` method is broken down into three finer-grained template 
methods:

- `createTables`, in which, unsurprisingly, you create your SQLite tables and other objects
- `populateReferenceData`, in which you can insert any reference data into appropriate tables (a
`Context` is provided in order to access resource strings)
- `seedTransactionalTables`, in which you can insert any other data &mdash; this method will only
be called if the fourth constructor argument is true and can be used to insert test data during
app development.

The `onUpgrade` method has less structure currently, but the `upgradeIncludes` method helps
determine whether or not a specific version's changes apply to the current upgrade.

### Table

A common practice in Android is to define your database schema as a set of constants in a dedicated
class. For example, I usually define a class that looks like this:

    public final class Schema {
    
      static final String DATABASE_NAME = "myDb";
      static final int DATABASE_VERSION = 2;
    
      // All published versions in which schema changes occurred:
      static final int ADD_FEATURE_VERSION = 2;
    
      private Schema() {
      }
    
      private static String qualify(String tableName, String columnName) {
        return tableName + "." + columnName;
      }
    
      public static final class MyTable implements BaseColumns {
        private MyTable() {
        }
    
        public static final String TABLE_NAME = "myTable";
    
        public static final String Q_ID = qualify(TABLE_NAME, _ID);;
        public static final String TEXT = "my_text_value";
        public static final String Q_TEXT = qualify(TABLE_NAME, TEXT);
        public static final String VALUE = "my_numeric_value";
        public static final String Q_VALUE = qualify(TABLE_NAME, VALUE);

        static final String TEXT_INDEX_NAME = "myTable_idx1_text";
      }
      
      // And so on...
    }
    
Avoiding the use of string literals in DDL and queries is usually a good idea, but can lead to some
messy, error-prone string concatenation. The `Table` class provides a fluent interface for table
creation that mitigates this. To create the "myTable" table with an index on the "my_text_value"
column, do the following:

    Table table = new Table(MyTable.TABLE_NAME).withIdColumn(MyTable._ID)
        .withColumn(MyTable.TEXT, Table.COLUMN_TYPE_TEXT)
        .withColumn(MyTable.VALUE, Table.COLUMN_TYPE_INTEGER)
    db.execSQL(table.getSql());

    Index index = table.addIndex(MyTable.TEXT_INDEX_NAME, MyTable.TEXT);
    db.execSQL(index.getSql());
 

Querying Databases and Working with Cursors
-------------------------------------------

### Query

After struggling with some of the same string concatenation issues with Android's `SQLiteQueryBuilder`
&mdash; along with problems writing Robolectric-based unit tests of that functionality &mdash; I 
created the `Query` class as a fluent interface for SQL query building. While not supporting 
arbitrarily-complex SQL, it handles the common cases well enough to be useful and can be extended if
necessary. Consider the following example:

    Query query = new Query()
      .select(TransactionalTable.Q_ID, 
              TransactionalTable.Q_INTERESTING_DATA, 
              ReferenceDataTable.Q_LOOKUP, 
              OtherReferenceDataTable.Q_LOOKUP)
      .from(TransactionalTable.TABLE_NAME)
      .innerJoin(ReferenceDataTable.TABLE_NAME).on(TransactionalTable.Q_REF_DATA_ID, ReferenceDataTable.Q_ID)
      .leftOuterJoin(OtherReferenceDataTable.TABLE_NAME).on(TransactionalTable.Q_OTHER_REF_DATA_ID, OtherReferenceDataTable.Q_ID)
      .whereEqualTo(TransactionalTable.Q_FILTER_COLUMN, "?")
      .whereIn(ReferenceDataTable.Q_FILTER_COLUMN, "?, ?")
      .orderBy(TransactionalTable.Q_ID, true);

Note that all columns are referred to by their fully-qualified names, as defined in your Schema class
equivalent. I typically define these query objects as constants where possible. The resulting SQL is
obtained by calling the `getSql` method on the `Query` object.

### DataUtils

`DataUtils` is a collection of static methods that facilitate working with `Cursor` objects. Some of
the highlights:

- a set of `getXXX` methods for the usual Java types that encapsulate null handling and the column
index-based value lookup of column values
- integer constants for boolean values, so that boolean columns are handled consistently
- date columns are assumed to be INTEGER (Long) values and are retrieved and formatted appropriately

Testing
-------

When writing unit tests it is often useful to stub `Cursor` objects. `ContentValuesArrayBasedTestCursor`
does that by allowing you to define the contents of each row as a `ContentValues` object. It also
provides a constructor that lets you create an arbitrary number of rows consisting of `BaseColumns._ID`
values only.
