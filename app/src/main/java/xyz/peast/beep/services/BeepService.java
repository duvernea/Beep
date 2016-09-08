package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import xyz.peast.beep.SaveFragment;
import xyz.peast.beep.Utility;
import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duverneay on 9/6/16.
 */
public class BeepService extends IntentService {
    private static String TAG = BeepService.class.getSimpleName();

    public BeepService() {
        super("BeepService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Bundle bundle = intent.getExtras();
        String imageUriString = bundle.getString(SaveFragment.COMPRESS_IMAGE_FILE_URI);
        String beepUriString = bundle.getString(SaveFragment.BEEP_URI);
        Uri beepUri = Uri.parse(beepUriString);
        Uri imageUri = Uri.parse(imageUriString);
        Log.d(TAG, "beepUri: " + beepUri);

        // New compressed file name and path
        String imageDir = getApplicationContext().getFilesDir().getAbsolutePath();
        String compressedImageFilename = UUID.randomUUID().toString() + ".jpg";

        FileOutputStream out = null;

        String compressedImageFilePath = imageDir + "/" + compressedImageFilename;

//        Bitmap bitmap = Utility.centerCropBitmap(this, imageUri);
//
//        try {
//            out = new FileOutputStream(compressedImageFilePath);
//            if (bitmap != null) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out); // bmp is your Bitmap instance
//                Log.d(TAG, "compressing bitmap, 50%" + compressedImageFilename);
//            }
//            // PNG is a lossless format, the compression factor (100) is ignored
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, compressedImageFilename);
        String whereClause = BeepDbContract.BeepEntry._ID+"=?";
        int key = (int) ContentUris.parseId(beepUri);
        String [] whereArgs = {key+""};

        int numRows = this.getContentResolver().
                update(BeepDbContract.BeepEntry.CONTENT_URI, contentValues, whereClause, whereArgs);

        Log.d(TAG, "num rows updated "  + numRows);
    }
}
