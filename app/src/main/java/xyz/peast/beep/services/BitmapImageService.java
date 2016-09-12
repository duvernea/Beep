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
        Bundle bundle = intent.getExtras();
        Messenger messenger = (Messenger) bundle.get("image_messenger");

        String imageUriString = bundle.getString(Utility.ORIGINAL_IMAGE_FILE_URI);
        Uri imageUri = Uri.parse(imageUriString);

        // New compressed file name and path
        String imageDir = getApplicationContext().getFilesDir().getAbsolutePath();
        String compressedImageFilename = UUID.randomUUID().toString() + ".jpg";

        FileOutputStream out = null;

        String compressedImageFilePath = imageDir + "/" + compressedImageFilename;


        // Downsample bitmap
        Bitmap bitmap = Utility.subsampleBitmap(getApplicationContext(),
                Utility.getRealPathFromURI(getApplicationContext(), imageUri), 360, 360);
        // Center crop bitmap
        bitmap = Utility.centerCropBitmap(getApplicationContext(), bitmap);

        Message message = new Message();
        Bundle bitmapBundle = new Bundle();
        bundle.putParcelable("bitmap", bitmap);
        message.setData(bitmapBundle);

        try {
            messenger.send(message);
        }
        catch (RemoteException re) {
            // TODO handle
        }
    }
}
