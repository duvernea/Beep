package xyz.peast.beep;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

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

        SQLiteDatabase db = new DbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());


    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
