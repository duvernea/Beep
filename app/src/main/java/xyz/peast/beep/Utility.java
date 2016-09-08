package xyz.peast.beep;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by duverneay on 7/24/16.
 */
public class Utility {
    public static float dpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float pxToDp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    public static Bitmap centerCropBitmap(Context context, Uri imageUri) {
        Bitmap centerCropBmp;
        try {
            final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            // Center crop
            if (selectedImage.getWidth() >= selectedImage.getHeight()){

                centerCropBmp = Bitmap.createBitmap(
                        selectedImage,
                        selectedImage.getWidth()/2 - selectedImage.getHeight()/2,
                        0,
                        selectedImage.getHeight(),
                        selectedImage.getHeight()
                );

            }
            else {
                centerCropBmp = Bitmap.createBitmap(
                        selectedImage,
                        0,
                        selectedImage.getHeight()/2 - selectedImage.getWidth()/2,
                        selectedImage.getWidth(),
                        selectedImage.getWidth()
                );
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            centerCropBmp = null;
        }
        return centerCropBmp;
    }
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
