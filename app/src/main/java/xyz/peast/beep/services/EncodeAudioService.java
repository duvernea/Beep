package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
            Bundle bundle = intent.getExtras();
            String wavPath = bundle.getString(Constants.WAV_FILE_PATH);
            String beepName = bundle.getString(Constants.BEEP_NAME);
            boolean beepEdited = bundle.getBoolean(Constants.BEEP_EDITED);
        Context context = getApplicationContext();
        AudioUtility.encodeMp3(context, wavPath, beepName);
    }
}
