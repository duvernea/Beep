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
public class LoadDownsampledBitmapImageService extends IntentService {

    private static String TAG = EncodeAudioService.class.getSimpleName();

    public LoadDownsampledBitmapImageService() {
        super("LoadDownsampledBitmapImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundleIn = intent.getExtras();
        Messenger messenger = (Messenger) bundleIn.get(Constants.IMAGE_MESSENGER);
        int minImageSize = bundleIn.getInt(Constants.IMAGE_MIN_SIZE);

        String imageUriString = bundleIn.getString(Utility.ORIGINAL_IMAGE_FILE_URI);
        Uri imageUri = Uri.parse(imageUriString);

        String imagePath = Utility.getRealPathFromURI(getApplicationContext(), imageUri);
            // Downsample bitmap
            Bitmap bitmap = Utility.subsampleBitmap(getApplicationContext(), imagePath, minImageSize, minImageSize);
            // Center crop bitmap
            bitmap  = Utility.centerCropBitmap(getApplicationContext(), bitmap);

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
