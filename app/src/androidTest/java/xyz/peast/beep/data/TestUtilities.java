package xyz.peast.beep.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import xyz.peast.beep.data.utils.PollingCheck;

/**
 * Created by duvernea on 6/9/16.
 */

public class TestUtilities extends AndroidTestCase {

    private static final String TAG = TestUtilities.class.getSimpleName();

    static ContentValues createBoardValues() {
        ContentValues boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Sweetie <3");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");

        return boardValues;
    }
    static ContentValues createBeepValues(long rowId) {
        ContentValues beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "Beep beep!");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 0);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, rowId);

        return beepValues;

    }

    static void validateLastRecord(String error, Cursor cursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        cursor.moveToLast();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = cursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            String cursorValue = cursor.getString(idx);

            assertEquals("Value '" + cursorValue +
                            "' did not match the expected value '" +
                            expectedValue + "'. " + error, expectedValue,
                    cursor.getType(idx)==Cursor.FIELD_TYPE_FLOAT
                            ? Float.toString(cursor.getFloat(idx)) : cursor.getString(idx));
        }
    }
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned, " + error, valueCursor.moveToFirst());
        validateLastRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }
    /*
    Note that this only tests that the onChange function is called; it does not test that the
    correct Uri is returned.
    */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
