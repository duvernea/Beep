package xyz.peast.beep.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by duvernea on 6/20/16.
 */

public class TestProvider extends AndroidTestCase {
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                BeepProvider.class.getName());

        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: Provider registered with authority: " + providerInfo.authority +
                " instead of authority: " + BeepDbContract.CONTENT_AUTHORITY,
                    providerInfo.authority, BeepDbContract.CONTENT_AUTHORITY);
        }
        catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: Provider not registered at " + mContext.getPackageName(), false);
        }
    }
    public void testGetType() {
        String type = mContext.getContentResolver().getType(BeepDbContract.BeepEntry.CONTENT_URI);

        assertEquals("Error: the BeepEntry CONTENT_URI should return BeepEntry.CONTENT_TYPE",
                type, BeepDbContract.BeepEntry.CONTENT_TYPE);

        type = mContext.getContentResolver().getType(BeepDbContract.BoardEntry.CONTENT_URI);

        assertEquals("Error: the BoardEntry CONTENT_URI should return BoardEntry.CONTENT_TYPE",
                type, BeepDbContract.BoardEntry.CONTENT_TYPE);
    }
    public void testBasicQuery() {
        BeepDbHelper dbHelper = new BeepDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues boardValues = TestUtilities.createBoardValues();
        long boardRowId;
        boardRowId = db.insert(BeepDbContract.BoardEntry.TABLE_NAME, null, boardValues);
        assertTrue(boardRowId != -1);


        ContentValues beepValues = TestUtilities.createBeepValues(boardRowId);
        long beepRowId = db.insert(BeepDbContract.BeepEntry.TABLE_NAME, null, beepValues);

        assertTrue("Unable to insert BeepEntry into the database", beepRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor cursor = mContext.getContentResolver().query(
                BeepDbContract.BeepEntry.CONTENT_URI, null, null, null, null);
        TestUtilities.validateCursor("testBasicBeepQuery", cursor, beepValues);

        cursor = mContext.getContentResolver().query(
                BeepDbContract.BoardEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.validateCursor("testBasicBoardQuery", cursor, boardValues);
    }
    public void testInsertReadProvider() {

        // Register Content Observer for Boards
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(BeepDbContract.BoardEntry.CONTENT_URI, true, tco);

        // Insert data into content provider for Board Uri
        ContentValues boardValues = TestUtilities.createBoardValues();
        Uri boardUri = mContext.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues );

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
        long rowId = ContentUris.parseId(boardUri);

        // verify row inserted via content provider
        assertTrue(boardUri != null);
        assertTrue(rowId != -1);

        Cursor cursor = mContext.getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating BoardEntry.",
                cursor, boardValues);

        // Register Content Observer for Boards
        tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(BeepDbContract.BeepEntry.CONTENT_URI, true, tco);

        // Insert data into content provider for Beeps Uri
        ContentValues beepValues = TestUtilities.createBeepValues(rowId);
        Uri beepUri = mContext.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues );

        rowId = ContentUris.parseId(boardUri);
        // verify row inserted via content provider
        assertTrue(beepUri != null);
        assertTrue(rowId != -1);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
    }
    public void testUpdateProvider() {
        ContentValues values = TestUtilities.createBoardValues();

        Uri boardUri = mContext.getContentResolver().insert(
                BeepDbContract.BoardEntry.CONTENT_URI, values);
        long rowId = ContentUris.parseId(boardUri);

        // verify row inserted
        assertTrue(rowId != -1);

        // update the values
        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Sweetest");
        updatedValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c5");

        Cursor cursor = mContext.getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        cursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                BeepDbContract.BoardEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "= ?",
                new String[] { Long.toString(rowId)});

        // assert a single row updated
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        cursor.unregisterContentObserver(tco);
        cursor.close();

        // Query the database to verify change
        cursor = mContext.getContentResolver().query(
                BeepDbContract.BoardEntry.CONTENT_URI,
                null,   // projection
                BeepDbContract.BoardEntry._ID + " = " + rowId,
                null,   // Values for the "where" clause
                null    // sort order
        );
        TestUtilities.validateCursor("testUpdateBoard.  Error validating board entry update.",
                cursor, updatedValues);

        cursor.close();
    }
    public void testDeleteRecords() {
        testInsertReadProvider();
        // Register a content observer for our deletes.
        TestUtilities.TestContentObserver boardObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(BeepDbContract.BoardEntry.CONTENT_URI, true, boardObserver);

        TestUtilities.TestContentObserver beepObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(BeepDbContract.BoardEntry.CONTENT_URI, true, beepObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        boardObserver.waitForNotificationOrFail();
        beepObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(boardObserver);
        mContext.getContentResolver().unregisterContentObserver(beepObserver);

    }
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                BeepDbContract.BoardEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                BeepDbContract.BeepEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                BeepDbContract.BoardEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                BeepDbContract.BeepEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }
}