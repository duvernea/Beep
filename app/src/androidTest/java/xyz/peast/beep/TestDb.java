package xyz.peast.beep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.HashSet;

import xyz.peast.beep.data.DbContract;
import xyz.peast.beep.data.DbHelper;

/**
 * Created by duvernea on 6/8/16.
 */

public class TestDb extends AndroidTestCase {
    public static final String TAG = TestDb.class.getSimpleName();

    private static final String TEST_FILE_PREFIX = "test_";

    private RenamingDelegatingContext mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContext = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
    }
    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DbContract.BoardEntry.TABLE_NAME);
        tableNameHashSet.add(DbContract.BeepEntry.TABLE_NAME);

        // Test that database was created
        SQLiteDatabase db = new DbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", c.moveToFirst());

        // Test that database tables were created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());
        assertTrue("Error: The database created does not contain all tables",
                tableNameHashSet.isEmpty());

        // Test that Beep table contain the correct columns
        c = db.rawQuery("PRAGMA table_info(" + DbContract.BeepEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Unable to query the database for table information", c.moveToFirst());

        //Build HashSet of all of the columns to look for - Beep Table
        final HashSet<String> beepColumnHashSet = new HashSet<String>();
        beepColumnHashSet.add(DbContract.BeepEntry._ID);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_NAME);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_IMAGE);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_AUDIO);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_COORD_LAT);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_COORD_LONG);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_PRIVACY);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_PLAY_COUNT);
        beepColumnHashSet.add(DbContract.BeepEntry.COLUMN_BOARD_KEY);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            Log.d(TAG, "Column was created: " + columnName);
            beepColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required Beep table columns",
                beepColumnHashSet.isEmpty());

        // Test that Board table contain the correct columns
        c = db.rawQuery("PRAGMA table_info(" + DbContract.BoardEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Unable to query the database for table information", c.moveToFirst());
        //Build HashSet of all of the columns to look for - Board Table
        final HashSet<String> boardColumnHashSet = new HashSet<String>();
        boardColumnHashSet.add(DbContract.BoardEntry._ID);
        boardColumnHashSet.add(DbContract.BoardEntry.COLUMN_NAME);
        boardColumnHashSet.add(DbContract.BoardEntry.COLUMN_IMAGE);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            Log.d(TAG, "Column was created: " + columnName);

            boardColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required Board table columns",
                boardColumnHashSet.isEmpty());

        db.close();
    }
    public void testBoardTable() {
        // Insert data into database, query database, validate result
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues boardValues = TestUtilities.createBoardValues();

        long rowId = db.insert(DbContract.BoardEntry.TABLE_NAME, null, boardValues);
        assertTrue(rowId != -1);

        Cursor boardCursor = db.query(DbContract.BoardEntry.TABLE_NAME,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No records returned from board query", boardCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: testInsertReadDb Board Entry failed to validate",
                boardCursor, boardValues);

        assertFalse("Error: More than one record returned from query",
                boardCursor.moveToNext());

        boardCursor.close();
        dbHelper.close();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
