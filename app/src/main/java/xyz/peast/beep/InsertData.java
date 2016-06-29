package xyz.peast.beep;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duverneay on 6/24/16.
 */
public class InsertData {

    public static final String TAG = InsertData.class.getSimpleName();

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

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "monkey brush");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 5000);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 1);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
    }
    static void insertSoundFile(Context context) {
        AssetFileDescriptor fd0 = context.getResources().openRawResourceFd(R.raw.king);
        int fileOffset = (int) fd0.getStartOffset();
        int fileLength = (int) fd0.getLength();
        try {
            fd0.getParcelFileDescriptor().close();
        }
        catch (IOException e) {
            Log.d(TAG, "File descriptor close error");
        }
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        String uniqueID = UUID.randomUUID().toString();
        uniqueID += ".mp3";
        Log.d(TAG, "random ID: " + uniqueID);

        String fileRaw = "king";
        InputStream in = context.getResources().openRawResource(R.raw.beep);
        OutputStream out = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int size = 0;
        byte[] buffer = new byte[1024];

        try {
            while ((size = in.read(buffer, 0, 1024)) >=0) {
                outputStream.write(buffer, 0, size);
            }
            in.close();
            buffer=outputStream.toByteArray();

            FileOutputStream fos = context.openFileOutput(uniqueID, Context.MODE_PRIVATE);
            fos.write(buffer);
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // update audio file path for beep in mock data
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, uniqueID);

        int count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, null,
                null);
//
//
//        int count = context.getContentResolver().update(
//                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "=?",
//                new String[] { String.valueOf(0)});
//
//        count = context.getContentResolver().update(
//                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "=?",
//                new String[] { Long.toString(1)});
//        count = context.getContentResolver().update(
//                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "=?",
//                new String[] { Long.toString(2)});
//        count = context.getContentResolver().update(
//                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "=?",
//                new String[] { Long.toString(3)});
//        count = context.getContentResolver().update(
//                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "=?",
//                new String[] { Long.toString(4)});
//        count = context.getContentResolver().update(
//                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + "=?",
//                new String[] { Long.toString(5)});


    }
}
