package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.UUID;

import xyz.peast.beep.Constants;
import xyz.peast.beep.Utility;

/**
 * Created by duverneay on 9/11/16.
 */
public class BitmapImageService extends IntentService {


    private static String TAG = EncodeAudioService.class.getSimpleName();

    public BitmapImageService() {
        super("BitmapImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent BitmapImageService");
        Bundle bundleIn = intent.getExtras();
        Messenger messenger = (Messenger) bundleIn.get(Constants.IMAGE_MESSENGER);
        int minImageSize = bundleIn.getInt(Constants.IMAGE_MIN_SIZE);

        String imageUriString = bundleIn.getString(Utility.ORIGINAL_IMAGE_FILE_URI);
        Uri imageUri = Uri.parse(imageUriString);

        String imagePath = Utility.getRealPathFromURI(getApplicationContext(), imageUri);
        Log.d(TAG, "imagePath: " + imagePath);
            //int imageSize = 460;
            Log.d(TAG, "image size dimen: " + minImageSize);
            // Downsample bitmap
            Bitmap bitmap = Utility.subsampleBitmap(getApplicationContext(), imagePath, minImageSize, minImageSize);
            // Center crop bitmap
            bitmap  = Utility.centerCropBitmap(getApplicationContext(), bitmap);

        // New compressed file name and path
//        String imageDir = getApplicationContext().getFilesDir().getAbsolutePath();
//        String compressedImageFilename = UUID.randomUUID().toString() + ".jpg";
//
//        FileOutputStream out = null;
//
//        String compressedImageFilePath = imageDir + "/" + compressedImageFilename;
//
//
//        // Downsample bitmap
//        Bitmap bitmap = Utility.subsampleBitmap(getApplicationContext(),
//                Utility.getRealPathFromURI(getApplicationContext(), imageUri), 360, 360);
//        // Center crop bitmap
//        bitmap = Utility.centerCropBitmap(getApplicationContext(), bitmap);

        Message message = new Message();
        Bundle bitmapBundle = new Bundle();
        bitmapBundle.putParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE, bitmap);
        message.setData(bitmapBundle);

        try {
            messenger.send(message);
        }
        catch (RemoteException re) {
            // TODO handle
        }
    }
}
