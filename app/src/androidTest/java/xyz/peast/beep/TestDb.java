package xyz.peast.beep;

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
        assertTrue("Error: This means that the database has not been created correctlyl", c.moveToFirst());

        // Test that database tables were created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());
        assertTrue("Error: Your database was created without both the beep and board tables",
                tableNameHashSet.isEmpty());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
