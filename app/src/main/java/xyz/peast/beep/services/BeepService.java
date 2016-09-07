package xyz.peast.beep.services;

import android.app.IntentService;
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
        Uri imageUri = Uri.parse(imageUriString);

        // New compressed file name and path
        String imageDir = getApplicationContext().getFilesDir().getAbsolutePath();
        String compressedImageFilename = UUID.randomUUID().toString() + ".jpg";

        FileOutputStream out = null;

        Bitmap bitmap = null;

        String compressedImageFilePath = imageDir + "/" + compressedImageFilename;

        try {
            final InputStream imageStream = getApplicationContext().getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

//                        int nh = (int) ( mBeepImage.getHeight() * (512.0 / mBeepImage.getWidth()) );
//                        Bitmap scaled = Bitmap.createScaledBitmap(selectedImage, 512, nh, true);

            // Center crop
            Bitmap centerCropBmp;
            if (selectedImage.getWidth() >= selectedImage.getHeight()){

                centerCropBmp = Bitmap.createBitmap(
                        selectedImage,
                        selectedImage.getWidth()/2 - selectedImage.getHeight()/2,
                        0,
                        selectedImage.getHeight(),
                        selectedImage.getHeight()
                );
            }
            else{

                centerCropBmp = Bitmap.createBitmap(
                        selectedImage,
                        0,
                        selectedImage.getHeight()/2 - selectedImage.getWidth()/2,
                        selectedImage.getWidth(),
                        selectedImage.getWidth()
                );
            }
            bitmap = centerCropBmp;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out = new FileOutputStream(compressedImageFilePath);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out); // bmp is your Bitmap instance
                Log.d(TAG, "compressing bitmap, 50%" + compressedImageFilename);

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
    }
}
