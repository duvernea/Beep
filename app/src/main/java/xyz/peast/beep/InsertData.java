package xyz.peast.beep;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duverneay on 6/24/16.
 */
public class InsertData {

    static void insertData(Context context) {
        context.getContentResolver().delete(BeepDbContract.BoardEntry.CONTENT_URI,null, null);
        context.getContentResolver().delete(BeepDbContract.BeepEntry.CONTENT_URI,null, null);


        ContentValues boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Sweetie <3");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");

        Uri boardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);

        boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Mom and Dad");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "1111111-792b-44eb-9715-cc96da9ce1c4");

        boardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);

        boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Work Crew");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "2222222-792b-44eb-9715-cc96da9ce1c4");

        boardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);

        ContentValues beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "Beep beep!");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 0);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 1);

        Uri beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "D-D-D!");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 50);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 1);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "oh hi huh!");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 200);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 2);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "geep!");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 100);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 2);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "ron swanson");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 5);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 2);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
    }
}
