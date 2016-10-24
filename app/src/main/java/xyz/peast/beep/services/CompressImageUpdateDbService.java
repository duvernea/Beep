package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import xyz.peast.beep.Constants;
import xyz.peast.beep.Utility;
import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duverneay on 9/6/16.
 */
public class CompressImageUpdateDbService extends IntentService {
    private static String TAG = CompressImageUpdateDbService.class.getSimpleName();

    static final public String IMAGE_SAVED_MESSAGE = "xyz.peast.beep.services.IMAGED_SAVED_MSG";



    public CompressImageUpdateDbService() {
        super("CompressImageUpdateDbService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        Log.d(TAG, "onHandleIntent");
        Bundle bundle = intent.getExtras();
        Constants.DbTable dbTableType = (Constants.DbTable) intent.getSerializableExtra(Constants.DB_TABLE_ENUM);
        Log.d(TAG, dbTableType.toString());
        String imageUriString = bundle.getString(Utility.ORIGINAL_IMAGE_FILE_URI);
        String rowString = bundle.getString(Utility.INSERTED_RECORD_URI);
        Log.d(TAG, "Insert Row Uri: " + rowString);
        Uri rowUri = Uri.parse(rowString);
        Uri imageUri = Uri.parse(imageUriString);
        Log.d(TAG, "beep or board record Uri: " + rowUri);

        // New compressed file name and path
        String imageDir = getApplicationContext().getFilesDir().getAbsolutePath();
        String compressedImageFilename = UUID.randomUUID().toString() + ".jpg";

        FileOutputStream out = null;

        String compressedImageFilePath = imageDir + "/" + compressedImageFilename;

        String originalFilePath = Utility.getRealPathFromURI(getApplicationContext(), imageUri);
        Log.d(TAG, "originalFilePath: " + originalFilePath);

        Bitmap bitmapOriginal = BitmapFactory.decodeFile(originalFilePath);
        Log.d(TAG, "bitmap width original: " + bitmapOriginal.getWidth());
        Log.d(TAG, "bitmap height original: " + bitmapOriginal.getHeight());
        Log.d(TAG, "bitmap size: " + bitmapOriginal.getByteCount());
        File originalImageFile = new File(originalFilePath);
        Log.d(TAG, "file exists? " + originalImageFile.exists());
        long length = originalImageFile.length();
        Log.d(TAG, "bitmap length in bytes: " + length);


        // Downsample bitmap
            Bitmap downsampledBitmap = Utility.subsampleBitmap(getApplicationContext(),
                    Utility.getRealPathFromURI(getApplicationContext(), imageUri), 360, 360);
        Log.d(TAG, "bitmap width subsample: " + downsampledBitmap.getWidth());
        Log.d(TAG, "bitmap height subsample: " + downsampledBitmap.getHeight());
        Log.d(TAG, "bitmap size subsample: " + downsampledBitmap.getByteCount());

            // Center crop bitmap
        Bitmap centerCropBitmap = Utility.centerCropBitmap(getApplicationContext(), downsampledBitmap);
        Log.d(TAG, "bitmap width centercrop: " + centerCropBitmap.getWidth());
        Log.d(TAG, "bitmap height centercrop: " + centerCropBitmap.getHeight());
        Log.d(TAG, "bitmap size centercrop: " + centerCropBitmap.getByteCount());

        try {
            out = new FileOutputStream(compressedImageFilePath);
            if (centerCropBitmap != null) {
                centerCropBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); // bmp is your Bitmap instance
                Log.d(TAG, "compressing bitmap, 80%" + compressedImageFilename);
            }
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File compressedImageFile = new File(compressedImageFilePath);
        Log.d(TAG, "file exists? " + compressedImageFile.exists());
        long lengthCompressedFile = compressedImageFile.length();
        Log.d(TAG, "compressed bitmap length in bytes: " + lengthCompressedFile);


        // Update database to reflect the new file name
        ContentValues contentValues = new ContentValues();
        // If Image belongs to a "beep"
        if (dbTableType.equals(Constants.DbTable.BEEP)) {
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, compressedImageFilename);
            String whereClause = BeepDbContract.BeepEntry._ID + "=?";
            int key = (int) ContentUris.parseId(rowUri);
            String[] whereArgs = {key + ""};

            int numRows = this.getContentResolver().
                    update(BeepDbContract.BeepEntry.CONTENT_URI, contentValues, whereClause, whereArgs);

            Log.d(TAG, "num rows updated " + numRows);
        }
        // If Image belongs to a "board"

        if (dbTableType.equals(Constants.DbTable.BOARD)) {
            contentValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, compressedImageFilename);
            Log.d(TAG, "compressed image file: " + compressedImageFilename);
            String whereClause = BeepDbContract.BoardEntry._ID + "=?";
            int key = (int) ContentUris.parseId(rowUri);
            String[] whereArgs = {key + ""};

            int numRows = this.getContentResolver().
                    update(BeepDbContract.BoardEntry.CONTENT_URI, contentValues, whereClause, whereArgs);
            if (numRows > 0) {
                //update UI
                Intent imageSavedIntent = new Intent(IMAGE_SAVED_MESSAGE);
                imageSavedIntent.putExtra(IMAGE_SAVED_MESSAGE, compressedImageFilename);
                broadcaster.sendBroadcast(imageSavedIntent);
            }
            Log.d(TAG, "num rows updated " + numRows);
        }
    }
}
