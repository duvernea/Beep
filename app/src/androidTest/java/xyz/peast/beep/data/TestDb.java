package xyz.peast.beep.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.HashSet;

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
        tableNameHashSet.add(BeepDbContract.BoardEntry.TABLE_NAME);
        tableNameHashSet.add(BeepDbContract.BeepEntry.TABLE_NAME);

        // Test that database was created
        SQLiteDatabase db = new BeepDbHelper(this.mContext).getWritableDatabase();
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
        c = db.rawQuery("PRAGMA table_info(" + BeepDbContract.BeepEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Unable to query the database for table information", c.moveToFirst());

        //Build HashSet of all of the columns to look for - Beep Table
        final HashSet<String> beepColumnHashSet = new HashSet<String>();
        beepColumnHashSet.add(BeepDbContract.BeepEntry._ID);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_NAME);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_IMAGE);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_AUDIO);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_COORD_LAT);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_COORD_LONG);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_PRIVACY);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED);
        beepColumnHashSet.add(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            Log.d(TAG, "Column was created: " + columnName);
            beepColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required Beep table columns",
                beepColumnHashSet.isEmpty());

        // Test that Board table contain the correct columns
        c = db.rawQuery("PRAGMA table_info(" + BeepDbContract.BoardEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Unable to query the database for table information", c.moveToFirst());
        //Build HashSet of all of the columns to look for - Board Table
        final HashSet<String> boardColumnHashSet = new HashSet<String>();
        boardColumnHashSet.add(BeepDbContract.BoardEntry._ID);
        boardColumnHashSet.add(BeepDbContract.BoardEntry.COLUMN_NAME);
        boardColumnHashSet.add(BeepDbContract.BoardEntry.COLUMN_IMAGE);
        boardColumnHashSet.add(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED);



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
    public void testInsertQueryTables() {
        // Insert data into database, query database, validate result
        BeepDbHelper dbHelper = new BeepDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues boardValues = TestUtilities.createBoardValues();

        // Insert row into table
        long boardRowId;
        boardRowId = db.insert(BeepDbContract.BoardEntry.TABLE_NAME, null, boardValues);
        assertTrue(boardRowId != -1);

        Cursor boardCursor = db.query(BeepDbContract.BoardEntry.TABLE_NAME,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No records returned from Board query", boardCursor.moveToFirst());

        TestUtilities.validateLastRecord("Error: testInsertReadDb Board Entry failed to validate",
                boardCursor, boardValues);

        assertFalse("Error: More than one record returned from Board query",
                boardCursor.moveToNext());

        ContentValues beepValues = TestUtilities.createBeepValues(boardRowId);
        long beepRowId;
        beepRowId = db.insert(BeepDbContract.BeepEntry.TABLE_NAME, null, beepValues);
        assertTrue(beepRowId != -1);

        Cursor beepCursor = db.query(BeepDbContract.BeepEntry.TABLE_NAME,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );
        assertTrue("Error: No records returned from Beep query", beepCursor.moveToFirst());

        TestUtilities.validateLastRecord("Error: testInsertReadDb Beep Entry failed to validate",
                beepCursor, beepValues);

        assertFalse("Error: More than one record returned from Beep query",
                beepCursor.moveToNext());

        boardCursor.close();
        dbHelper.close();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
