package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import xyz.peast.beep.Constants;
import xyz.peast.beep.Utility;

/**
 * Created by duverneay on 9/11/16.
 */
public class LoadDownsampledBitmapImageService extends IntentService {

    private static String TAG = LoadDownsampledBitmapImageService.class.getSimpleName();

    public LoadDownsampledBitmapImageService() {
        super("LoadDownsampledBitmapImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundleIn = intent.getExtras();
        Messenger messenger = (Messenger) bundleIn.get(Constants.IMAGE_MESSENGER);
        int minImageSize = bundleIn.getInt(Constants.IMAGE_MIN_SIZE);
        String imagePath = bundleIn.getString(Constants.ORIGINAL_IMAGE_FILE_PATH);

        // Downsample bitmap
        Bitmap bitmap = Utility.subsampleBitmap(getApplicationContext(),
                imagePath, minImageSize, minImageSize);
        // Center crop bitmap
        bitmap  = Utility.centerCropBitmap(getApplicationContext(), bitmap);

        // Create and Send bitmap message
        Message message = new Message();
        Bundle bitmapBundle = new Bundle();
        bitmapBundle.putParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE, bitmap);
        message.setData(bitmapBundle);
        try {
            messenger.send(message);
        }
        catch (NullPointerException | RemoteException e) {
            e.printStackTrace();
        }
    }
}