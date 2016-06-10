package xyz.peast.beep;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import xyz.peast.beep.data.BeepDbContract;

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

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            if (valueCursor.getType(idx) == Cursor.FIELD_TYPE_FLOAT) {
                Log.d(TAG, "we found a float");
                Log.d(TAG, "Float value: " + valueCursor.getFloat(idx));
            }
            Log.d(TAG, "getString(idx): " + valueCursor.getString(idx));
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue,
                    valueCursor.getType(idx)==Cursor.FIELD_TYPE_FLOAT
                            ? Float.toString(valueCursor.getFloat(idx)) : valueCursor.getString(idx));
        }
    }

}
