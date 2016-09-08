package xyz.peast.beep;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
    public static Bitmap bitmapOptions(Context context, String filepath) {

        // say an imageview is 120 x 120 dp
        int reqHeight = 360;
        int reqWidth = 360;

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
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "inSampleSize: " + inSampleSize);

        // Set inJustDecodeBounds to false to actually decode the file and return it
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);



    }
}
