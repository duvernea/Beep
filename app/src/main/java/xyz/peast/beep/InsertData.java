package xyz.peast.beep;

import android.content.ContentUris;
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

    static private int[] beepRowIds = new int[6];

    public static final String TAG = InsertData.class.getSimpleName();

    static void insertData(Context context) {
        context.getContentResolver().delete(BeepDbContract.BoardEntry.CONTENT_URI,null, null);
        context.getContentResolver().delete(BeepDbContract.BeepEntry.CONTENT_URI,null, null);

        ContentValues boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Sweetie <3");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");

        Uri boardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);

        Log.d(TAG, "Sweetie Uri: " + boardUri);
        boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Mom and Dad");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "1111111-792b-44eb-9715-cc96da9ce1c4");

        boardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);
        Log.d(TAG, "Mom and Dad Uri: " + boardUri);
        boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Work Crew");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "2222222-792b-44eb-9715-cc96da9ce1c4");

        int[] rowIds = new int[6];

        boardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);
        Log.d(TAG, "Work Crew: " + boardUri);

        ContentValues beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "bad");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 10000);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 1);

        Uri beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[0] = (int) ContentUris.parseId(beepUri);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "beep");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 50);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 1);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[1] = (int) ContentUris.parseId(beepUri);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "cuckoo");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 200);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 2);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[2] = (int) ContentUris.parseId(beepUri);

        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "kevin");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 100);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 2);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[3] = (int) ContentUris.parseId(beepUri);


        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "meat");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 5);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 2);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[4] = (int) ContentUris.parseId(beepUri);


        beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "nah");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 5000);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, 1);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[4] = (int) ContentUris.parseId(beepUri);

    }
    static void insertSoundFile(Context context) {

        String fileName;
        fileName = createFileFromRaw(context, R.raw.bad);
        // update audio file path for beep in mock data
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, fileName);

        int count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + " =? ",
                new String[] { Integer.toString(beepRowIds[0])});

        fileName = createFileFromRaw(context, R.raw.beep);
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, fileName);
        count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + " =? ",
                new String[] { Integer.toString(beepRowIds[1])});

        fileName = createFileFromRaw(context, R.raw.cuckoo);
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, fileName);

        count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + " =? ",
                new String[] { Integer.toString(beepRowIds[2])});
        fileName = createFileFromRaw(context, R.raw.kevin);
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, fileName);

        count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + " =? ",
                new String[] { Integer.toString(beepRowIds[3])});
        fileName = createFileFromRaw(context, R.raw.meat);
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, fileName);
        count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + " =? ",
                new String[] { Integer.toString(beepRowIds[4])});
        fileName = createFileFromRaw(context, R.raw.nah);
        updatedValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, fileName);
        count = context.getContentResolver().update(
                BeepDbContract.BeepEntry.CONTENT_URI, updatedValues, BeepDbContract.BeepEntry._ID + " =? ",
                new String[] { Integer.toString(beepRowIds[5])});
    }
    static private String createFileFromRaw(Context context, int rawfile) {

        String uniqueID = UUID.randomUUID().toString();
        uniqueID += ".wav";
        Log.d(TAG, "random ID: " + uniqueID);

        InputStream in = context.getResources().openRawResource(rawfile);
        OutputStream out = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int size = 0;
        byte[] buffer = new byte[1024];

        try {
            while ((size = in.read(buffer, 0, 1024)) >= 0) {
                outputStream.write(buffer, 0, size);
            }
            in.close();
            buffer = outputStream.toByteArray();

            FileOutputStream fos = context.openFileOutput(uniqueID, Context.MODE_PRIVATE);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueID;
    }
}
