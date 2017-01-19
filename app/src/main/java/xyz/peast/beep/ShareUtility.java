package xyz.peast.beep;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

import xyz.peast.beep.services.EncodeAudioService;

/**
 * Created by duverneay on 1/18/17.
 */
public class ShareUtility {

    private static final String TAG = ShareUtility.class.getSimpleName();

    public static void encodeMp3(Context context, String wavPath,
                                 String beepName, boolean beepEdited) {

        long time = System.currentTimeMillis();
        Log.d(TAG, "encodeMp3 process starting at: " + time);

        // Encode the wav to mp3 for sharing
        Bundle bundleEncodeAudio = new Bundle();
        bundleEncodeAudio.putString(Constants.WAV_FILE_PATH, wavPath);
        bundleEncodeAudio.putString(Constants.BEEP_NAME, beepName);
        bundleEncodeAudio.putBoolean(Constants.BEEP_EDITED, beepEdited);
        Intent encodeAudioIntent = new Intent(context, EncodeAudioService.class);
        encodeAudioIntent.putExtras(bundleEncodeAudio);

        context.startService(encodeAudioIntent);
        // String filename = bundle.getString(Constants.WAV_FILE_PATH);
        // String beepName = bundle.getString(Constants.BEEP_NAME);

        //Log.d(TAG, "mAudioWavPath: " + audioWavPath);

    }

    public static Uri encodeBeepGetUri(Context context, String recordFileName,
                                 String beepName, String beepPath, boolean beepEdited) {

        final String audioWavPath = Utility.getFullWavPath(context, recordFileName, beepEdited);

        // If encode on main thread takes < 1 second
        boolean encodeMp3Success = AudioUtility.encodeMp3(context, audioWavPath, beepName);

        File beepMp3 = new File(beepPath);

        Uri fileUri;
        //= Uri.parse(audioPath);
        try {
            fileUri = FileProvider.getUriForFile(
                    context,
                    "xyz.peast.beep.fileprovider",
                    beepMp3);
        } catch (IllegalArgumentException e) {
            fileUri = null;
            Log.e("File Selector",
                    "The selected file can't be shared: ");
        }
        if (fileUri != null) {
            Log.d(TAG, "fileUri: " + fileUri);
        }
        return fileUri;
    }
}
