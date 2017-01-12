package xyz.peast.beep;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.Calendar;

import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.CompressImageUpdateDbService;
import xyz.peast.beep.widget.WidgetProvider;

/**
 * Created by duverneay on 7/24/16.
 */
public class Utility {
    private static final String TAG = Utility.class.getSimpleName();

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
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(filepath, options);
    }

    public static void insertNewBeep(Context context, String beepName, String audioFileName,
                                     boolean beepFx, Location location, int boardKey,
                                     Uri originalImageUri, String originalImageFilePath) {
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
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_FX, beepFx);

        Uri uri = context.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, contentValues);
        Log.d(TAG, "Utility: Insert beep into ContentProvider: " + uri.toString());

        // Use service to save, compress, crop, etc the image
        if (originalImageFilePath != null) {
            Intent serviceIntent = new Intent(context, CompressImageUpdateDbService.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ORIGINAL_IMAGE_FILE_PATH, originalImageFilePath);
            bundle.putString(Constants.ORIGINAL_IMAGE_FILE_URI, originalImageUri.toString());
            bundle.putString(Constants.INSERTED_RECORD_URI, uri.toString());
            serviceIntent.putExtra(Constants.DB_TABLE_ENUM, Constants.DbTable.BEEP);

            serviceIntent.putExtras(bundle);
            context.startService(serviceIntent);
        }
    }

    public static int insertNewBoard(Context context, String boardName, Uri originalImageUri) {

        int rowInsertKey;

        ContentValues contentValues = new ContentValues();
        contentValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, boardName);
        contentValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis());
        Uri uri = context.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, contentValues);
        Log.d(TAG, "Utility: Insert board into ContentProvider: " + uri.toString());
        rowInsertKey = (int) ContentUris.parseId(uri);
        // Use service to save, compress, crop, etc the image
        if (originalImageUri != null) {
            Intent serviceIntent = new Intent(context, CompressImageUpdateDbService.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ORIGINAL_IMAGE_FILE_URI, originalImageUri.toString());
            bundle.putString(Constants.INSERTED_RECORD_URI, uri.toString());
            serviceIntent.putExtra(Constants.DB_TABLE_ENUM, Constants.DbTable.BOARD);

            serviceIntent.putExtras(bundle);
            context.startService(serviceIntent);
        }
        return rowInsertKey;


    }

    public static boolean hasReadExternalPermission(Context context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "READ External permission: " + hasPermission);
        return hasPermission;
    }

    // update widgets
    public static void updateWidgets(Activity activity) {

        Intent updateWidgetIntent = new Intent(activity, WidgetProvider.class);
        updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(activity.getApplication()).
                getAppWidgetIds(new ComponentName(activity.getApplication(), WidgetProvider.class));
        updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        activity.sendBroadcast(updateWidgetIntent);
    }

    public static void incrementBeepPlayCount(Context context, int beepKey) {

        Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;
        String whereClause = BeepDbContract.BeepEntry._ID + "=?";
        String[] whereArgs = {beepKey + ""};
        Cursor playedBeepCursor = context.getContentResolver().query(
                uri,
                Constants.BEEP_COLUMNS,
                whereClause,
                whereArgs,
                null);
        playedBeepCursor.moveToFirst();
        int playCount = playedBeepCursor.getInt(Constants.BEEPS_COL_PLAY_COUNT);
        Log.d(TAG, "Play count: " + playCount);

        ContentValues values = new ContentValues();
        values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount + 1);

        context.getContentResolver().update(
                uri,
                values,
                whereClause,
                whereArgs);
    }

    public static String getFullWavPath(Context context, String audioFileName, boolean beepEdited) {

        String recordDir = context.getFilesDir().getAbsolutePath();
        String path = recordDir + File.separator + audioFileName;
        if (beepEdited) {
            path += Constants.EDITED_FILE_SUFFIX;
        }
        path += Constants.WAV_FILE_SUFFIX;
        return path;
    }
    public static String getFullImagePath(Context context, String imageFileName) {
        String imageDir = context.getFilesDir().getAbsolutePath();
        String imagePath = imageDir + File.separator + imageFileName;
        return imagePath;
    }

    public static final boolean[] deleteBeep(Context context, int beepKey, String audioFileBase, boolean edited,
                                           String imageFileBase) {
        boolean audioUneditedDeleted = false;
        boolean audioEditedDeleted = false;
        boolean imageDeleted = false;
        // continue with delete
        Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;

        String whereClause = BeepDbContract.BeepEntry._ID + "=?";
        String[] whereArgs = {beepKey + ""};

        int numRows = context.getContentResolver().delete(
                uri,
                whereClause,
                whereArgs);

        // Delete the no FX audio version
        String audioFilePathUnedited = getFullWavPath(context, audioFileBase, false);

        File audioFileUnedited = new File(audioFilePathUnedited);
        audioUneditedDeleted = audioFileUnedited.delete();

        if (edited) {
            String audioFilePathEdited = getFullWavPath(context, audioFileBase, true);

            File audioFileEdited = new File(audioFilePathEdited);
            audioEditedDeleted = audioFileEdited.delete();
        }
        if (imageFileBase != null) {
            String imageDir = context.getFilesDir().getAbsolutePath();
            File imageFile = new File(imageDir + File.separator + imageFileBase);
            imageDeleted = imageFile.delete();
        }

        boolean[] filesDeletedSuccess = {audioUneditedDeleted, audioEditedDeleted, imageDeleted};
        return filesDeletedSuccess;
    }
}
