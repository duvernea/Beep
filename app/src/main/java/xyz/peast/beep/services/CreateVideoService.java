package xyz.peast.beep.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duvernea on 4/28/17.
 */

public class CreateVideoService extends IntentService {

    private static String TAG = CreateVideoService.class.getSimpleName();

    public static String VIDEO_CREATION_DONE = "android.intent.action.VIDEO_DONE";

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
        Log.d(TAG, "Getting bundle...");

        Bundle bundle = intent.getExtras();
        String beepName = bundle.getString(Constants.BEEP_NAME);
        Log.d(TAG, "beepName: " + beepName);
        String wavFileBase = bundle.getString(Constants.WAV_FILE_PATH);
        Log.d(TAG, "wav path: " + wavFileBase);
        boolean beepEdited = bundle.getBoolean(Constants.BEEP_EDITED);
        Log.d(TAG, "beep Edited: " + beepEdited);
        String fullWavPath = Utility.getFullWavPath(context, wavFileBase, beepEdited);
        Log.d(TAG, "full wav path: " + fullWavPath);
        long beepId = bundle.getLong(Constants.BEEP_ID);
        Log.d(TAG, "beepId: " + beepId);
        // Get image file path, once compression is finished
        // TODO - use a messenger/etc to check database rather than this infinite while loop?
        Cursor cursor;
        String imageFileName;
        // TODO - this code assumes that an image is stored in the database
        // TODO Currently, not a valid assumption, it can be null/empty
        while (true) {
            // query the database
            String whereClause = BeepDbContract.BeepEntry._ID+"=?";
            String [] whereArgs = {beepId+""};
            // Delete old image if exists
            cursor = context.getContentResolver().query(
                    BeepDbContract.BeepEntry.CONTENT_URI,
                    Constants.BEEP_COLUMNS,
                    whereClause,
                    whereArgs,
                    null);
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                imageFileName = cursor.getString(Constants.BEEPS_COL_IMAGE);
                if (imageFileName != null) {
                    break;
                }
            }
            cursor.close();
        }

        Log.d(TAG, "current imageFile name: " + imageFileName);
        String imageFilePath = Utility.getFullImagePath(context, imageFileName);
        Log.d(TAG, "current imageFile path: " + imageFilePath);

        cursor.close();

        File direct = new File(getFilesDir()+ File.separator + Constants.VIDEO_DIR);

        if(!direct.exists()) {
            if(direct.mkdir()); //directory is created;
        }

        // TODO - get watermark image here and pass it in? Right now is hardcoded
        String watermarkImagePath = createWatermarkedImage(imageFilePath, "");

        createVideo(watermarkImagePath, fullWavPath, beepName);
    }
    private void createVideo(String watermarkImagePath, String audioPath, String beepName) {

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


        // String audio = getFilesDir().getAbsolutePath() + File.separator + testAudio;
        String output = getFilesDir().getAbsolutePath() + File.separator +
                Constants.VIDEO_DIR + File.separator + beepName + Constants.MP4_FILE_SUFFIX;

        String cmd[] = {"-loop", "1",
                "-i", watermarkImagePath,
                "-i", audioPath,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-b:a", "320k",
                "-y",
                "-shortest", output
                };
        // "-vf", "\"zoompan=z='min(zoom+0.15,1.5)':d=125\"",
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

                    Intent intent = new Intent(VIDEO_CREATION_DONE);
                    sendBroadcast(intent);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
    }
    private String createWatermarkedImage(String imagePath, String watermarkPath) {
        String imageWatermarkedPath = getFilesDir().getAbsolutePath() + File.separator +
                Constants.VIDEO_DIR + File.separator + "watermarkedImage.jpg";


        Bitmap image = BitmapFactory.decodeFile(imagePath);
        Bitmap markedBitmap = Utility.addWaterMark(context, image);
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(imageWatermarkedPath);
            markedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
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
        return imageWatermarkedPath;
    }
}
