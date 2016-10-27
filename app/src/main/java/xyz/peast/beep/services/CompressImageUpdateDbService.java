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
        Bundle bundle = intent.getExtras();
        Constants.DbTable dbTableType = (Constants.DbTable) intent.getSerializableExtra(Constants.DB_TABLE_ENUM);
        String imageUriString = bundle.getString(Utility.ORIGINAL_IMAGE_FILE_URI);
        String rowString = bundle.getString(Utility.INSERTED_RECORD_URI);
        Uri rowUri = Uri.parse(rowString);
        Uri imageUri = Uri.parse(imageUriString);

        // New compressed file name and path
        String imageDir = getApplicationContext().getFilesDir().getAbsolutePath();
        String compressedImageFileName = UUID.randomUUID().toString() + ".jpg";
        String compressedImageFilePath = imageDir + "/" + compressedImageFileName;
        String originalFilePath = Utility.getRealPathFromURI(getApplicationContext(), imageUri);

        File originalImageFile = new File(originalFilePath);
        long length = originalImageFile.length();

        // Downsample bitmap
            Bitmap downsampledBitmap = Utility.subsampleBitmap(getApplicationContext(),
                    Utility.getRealPathFromURI(getApplicationContext(), imageUri), 360, 360);
        // Center crop bitmap
        Bitmap centerCropBitmap = Utility.centerCropBitmap(getApplicationContext(), downsampledBitmap);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(compressedImageFilePath);
            if (centerCropBitmap != null) {
                centerCropBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); // bmp is your Bitmap instance
                Log.d(TAG, "compressing bitmap, 80%" + compressedImageFileName);
            }
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
        long lengthCompressedFile = compressedImageFile.length();

        // Update database to reflect the new file name
        ContentValues contentValues = new ContentValues();

        // If Image belongs to a "beep"
        if (dbTableType.equals(Constants.DbTable.BEEP)) {
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, compressedImageFileName);
            String whereClause = BeepDbContract.BeepEntry._ID + "=?";
            int key = (int) ContentUris.parseId(rowUri);
            String[] whereArgs = {key + ""};

            int numRows = this.getContentResolver().
                    update(BeepDbContract.BeepEntry.CONTENT_URI, contentValues, whereClause, whereArgs);
        }
        // If Image belongs to a "board"
        if (dbTableType.equals(Constants.DbTable.BOARD)) {
            contentValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, compressedImageFileName);
            String whereClause = BeepDbContract.BoardEntry._ID + "=?";
            int key = (int) ContentUris.parseId(rowUri);
            String[] whereArgs = {key + ""};

            int numRows = this.getContentResolver().
                    update(BeepDbContract.BoardEntry.CONTENT_URI, contentValues, whereClause, whereArgs);
            if (numRows > 0) {
                //update UI
                Intent imageSavedIntent = new Intent(IMAGE_SAVED_MESSAGE);
                imageSavedIntent.putExtra(IMAGE_SAVED_MESSAGE, compressedImageFileName);
                broadcaster.sendBroadcast(imageSavedIntent);
            }
        }
    }
}
