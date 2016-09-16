package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        String imageUriString = bundle.getString(Utility.ORIGINAL_IMAGE_FILE_URI);
        String beepUriString = bundle.getString(Utility.INSERTED_BEEP_URI);
        Uri beepUri = Uri.parse(beepUriString);
        Uri imageUri = Uri.parse(imageUriString);
        Log.d(TAG, "beepUri: " + beepUri);

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
