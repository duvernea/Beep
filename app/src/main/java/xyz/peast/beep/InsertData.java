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
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, 1467857522);

        Uri sweetieBoardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);
        int sweetieKey = (int) ContentUris.parseId(sweetieBoardUri);

        Log.d(TAG, "Sweetie Uri: " + sweetieBoardUri);
        boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Mom and Dad");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "1111111-792b-44eb-9715-cc96da9ce1c4");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, 1467857522);

        Uri momDadboardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);
        int momDadKey = (int) ContentUris.parseId(momDadboardUri);

        Log.d(TAG, "Mom and Dad Uri: " + momDadboardUri);
        boardValues = new ContentValues();
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, "Work Crew");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, "2222222-792b-44eb-9715-cc96da9ce1c4");
        boardValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, 1467857522);

        int[] rowIds = new int[6];

        Uri workCrewBoardUri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, boardValues);
        int workCrewKey = (int) ContentUris.parseId(workCrewBoardUri);

        Log.d(TAG, "Work Crew: " + workCrewBoardUri);

        ContentValues beepValues = new ContentValues();
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, "bad");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, "5f9247bf-792b-44eb-9715-cc96da9ce1c4");
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, 178.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, 92.1234);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 10000);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, 1467857522);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, sweetieKey);

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
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, 1467857522);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, sweetieKey);

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
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, 1467857522);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, workCrewKey);

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
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, 1467857522);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, workCrewKey);

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
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, 1467857522);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, momDadKey);

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
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, 1467857522);
        beepValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, workCrewKey);

        beepUri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, beepValues);
        beepRowIds[5] = (int) ContentUris.parseId(beepUri);

    }
    static void insertSoundFile(Context context) {

        String fileName;
        fileName = createFileFromRaw(context, R.raw.bad);
        Log.d(TAG, "Bad sound file: " + fileName);
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



    // NOTE: This is temp code that will be deleted

    // Files under res/raw are not zipped, just copied into the APK. Get the offset and length to know where our files are located.
//
//    AssetFileDescriptor fd0 = getResources().openRawResourceFd(R.raw.lycka), fd1 = getResources().openRawResourceFd(R.raw.king);
//
//    int fileAoffset = (int)fd0.getStartOffset(), fileAlength = (int)fd0.getLength(), fileBoffset = (int)fd1.getStartOffset(), fileBlength = (int)fd1.getLength();
//try {
//        fd0.getParcelFileDescriptor().close();
//        fd1.getParcelFileDescriptor().close();
//        } catch (IOException e) {
//        android.util.Log.d("", "Close error.");
//        }
//        String uniqueID = UUID.randomUUID().toString();
//        uniqueID += ".mp3";
//        Log.d(TAG, "getresourcepath: " + getPackageResourcePath());
//        InputStream in = mContext.getResources().openRawResource(R.raw.beep);
//        OutputStream out = null;
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        int size = 0;
//        byte[] buffer = new byte[1024];
//
//        try {
//        while ((size = in.read(buffer, 0, 1024)) >=0) {
//        outputStream.write(buffer, 0, size);
//        }
//        in.close();
//        buffer=outputStream.toByteArray();
//
//        FileOutputStream fos = mContext.openFileOutput(uniqueID, Context.MODE_PRIVATE);
//        fos.write(buffer);
//        fos.close();
//        }
//        catch (Exception e) {
//        e.printStackTrace();
//        }
//        String path = mContext.getFilesDir().getPath() + uniqueID;
//String path = "/data/data/xyz.peast.beep/files/" + uniqueID;
// Arguments: path to the APK file, offset and length of the two resource files, sample rate, audio buffer size.
