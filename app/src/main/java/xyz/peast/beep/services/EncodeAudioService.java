package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import xyz.peast.beep.AudioUtility;
import xyz.peast.beep.Constants;

/**
 * Created by duverneay on 9/11/16.
 */
public class EncodeAudioService extends IntentService {

    private static String TAG = EncodeAudioService.class.getSimpleName();

    public EncodeAudioService() {
        super("EncodeAudioService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long time1 = System.currentTimeMillis();
        // Log.d(TAG, "EncodeAudioService process starting at: " + time1);
        Bundle bundle = intent.getExtras();
        String wavPath = bundle.getString(Constants.WAV_FILE_PATH);
        String beepName = bundle.getString(Constants.BEEP_NAME);
        boolean beepEdited = bundle.getBoolean(Constants.BEEP_EDITED);
        Context context = getApplicationContext();
        AudioUtility.encodeMp3(context, wavPath, beepName);
        long time2 = System.currentTimeMillis();
        long elapsedTime = time2 - time1;
        // Log.d(TAG, "EncodeAudioService process stopping at: " + time2);
        String mp3Path = context.getFilesDir().getAbsolutePath() + File.separator + beepName + ".mp3";
        Log.d(TAG, "mp3 is ready for sharing at path: " + mp3Path );
        Log.d(TAG, "EncodeAudioService elapsed time: " + elapsedTime);
    }
}
