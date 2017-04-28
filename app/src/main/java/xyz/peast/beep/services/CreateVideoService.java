package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import xyz.peast.beep.AudioUtility;
import xyz.peast.beep.Constants;
import xyz.peast.beep.Utility;

/**
 * Created by duvernea on 4/28/17.
 */

public class CreateVideoService extends IntentService {

    private static String TAG = CreateVideoService.class.getSimpleName();

    private long startTime;
    private long stopTime;
    private Context context;

    public CreateVideoService() {
        super("CreateVideoService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        context = getApplicationContext();
        startTime = System.currentTimeMillis();
        Log.d(TAG, "Create video process starting at: " + startTime);

//        Bundle bundle = intent.getExtras();
//        String wavPath = bundle.getString(Constants.WAV_FILE_PATH);
//        String beepName = bundle.getString(Constants.BEEP_NAME);
//        boolean beepEdited = bundle.getBoolean(Constants.BEEP_EDITED);

        // TODO Create Video - utility method?
        // needs to have a messenger or something back to main thread
        createVideo();
    }
    private void createVideo() {

        /***************** FFMPEG **********************/

        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            e.printStackTrace();
        }
        File direct = new File(getFilesDir()+ File.separator + Constants.VIDEO_DIR);

        if(!direct.exists()) {
            if(direct.mkdir()); //directory is created;
        }

        String testAudio = "0cf0c46e-fd5b-4984-aa9e-981524790fb3_edit.wav";
        String testImage = "305d302a-516e-4560-a165-d6bb026dfd35.jpg";
        String outFile = "out";

        String image = getFilesDir().getAbsolutePath() + File.separator + testImage;

        Log.d(TAG, "image path: " + image);

        String imageWatermark = getFilesDir().getAbsolutePath() + File.separator + "temp.jpg";

        Bitmap temp = BitmapFactory.decodeFile(image);
        Bitmap temp2 = Utility.addWaterMark(context, temp);
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(imageWatermark);
            temp2.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
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

        String audio = getFilesDir().getAbsolutePath() + File.separator + testAudio;
        String output = getFilesDir().getAbsolutePath() + File.separator +
                Constants.VIDEO_DIR + File.separator + outFile + Constants.MP4_FILE_SUFFIX;

        String cmd[] = {"-loop", "1",
                "-i", imageWatermark,
                "-i", audio,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-b:a", "320k",
                "-shortest", output,
                "-y"};

        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "onProgress: " + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onFailure execute message: " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess execute message: " + message);
                }
                @Override
                public void onFinish() {
                    Log.d(TAG, "onFinish");
                    stopTime = System.currentTimeMillis();
                    long elapsedTime = stopTime - startTime;
                    Log.d(TAG, "EncodeAudioService process stopping at: " + stopTime);
                    Log.d(TAG, "EncodeAudioService elapsed time: " + elapsedTime);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
    }
}
