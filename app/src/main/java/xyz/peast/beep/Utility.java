package xyz.peast.beep;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import xyz.peast.beep.adapters.Board;
import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.BeepService;

/**
 * Created by duverneay on 7/24/16.
 */
public class Utility {
    private static final String TAG = Utility.class.getSimpleName();

    // KEYs for Service Intent extras
    public static final String ORIGINAL_IMAGE_FILE_URI = "ORIGINAL_IMAGE_FILE_URI";
    public static final String INSERTED_BEEP_URI = "beep_uri";

    public static float dpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float pxToDp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static Bitmap centerCropBitmap(Context context, Bitmap selectedImage) {
        Bitmap centerCropBmp;

            //final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            //final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            // Center crop
            if (selectedImage.getWidth() >= selectedImage.getHeight()) {

                centerCropBmp = Bitmap.createBitmap(
                        selectedImage,
                        selectedImage.getWidth() / 2 - selectedImage.getHeight() / 2,
                        0,
                        selectedImage.getHeight(),
                        selectedImage.getHeight()
                );

            } else {
                centerCropBmp = Bitmap.createBitmap(
                        selectedImage,
                        0,
                        selectedImage.getHeight() / 2 - selectedImage.getWidth() / 2,
                        selectedImage.getWidth(),
                        selectedImage.getWidth()
                );
            }

        return centerCropBmp;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public static Bitmap subsampleBitmap(Context context, String filepath, int reqwidth, int reqheight) {
        // Decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        int height = options.outHeight;
        int width = options.outWidth;
        String type = options.outMimeType;
        Log.d(TAG, "imageHeight: " + height);
        Log.d(TAG, "imageWidth: " + width);
        Log.d(TAG, "imageType: " + type);

        // Determine minimum inSampleSize (downsampling of Bitmap that is greater than required size)
        int inSampleSize = 1;
        if (height > reqheight || width > reqwidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqheight
                    && (halfWidth / inSampleSize) >= reqwidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "inSampleSize: " + inSampleSize);

        // Set inJustDecodeBounds to false to actually decode the file and return it
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }
    public static void insertNewBeep(Context context, String beepName, String audioFileName, Location location,
                                     int boardKey, Uri originalImageUri) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, beepName);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, audioFileName);
        if (location != null) {
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, location.getLatitude());
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, location.getLongitude());
        }
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 0);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis());
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, boardKey);

        Uri uri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, contentValues);
        Log.d(TAG, "Utility: Insert beep into ContentProvider: " + uri.toString());

        // Use service to save, compress, crop, etc the image
        if (originalImageUri != null) {
            Intent serviceIntent = new Intent(context, BeepService.class);
            Bundle bundle = new Bundle();
            bundle.putString(ORIGINAL_IMAGE_FILE_URI, originalImageUri.toString());
            bundle.putString(INSERTED_BEEP_URI, uri.toString());
            serviceIntent.putExtras(bundle);
            context.startService(serviceIntent);
        }
    }
}
