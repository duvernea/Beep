package xyz.peast.beep;

import android.content.Context;
import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.naman14.androidlame.WaveReader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by duverneay on 9/11/16.
 */
public class AudioUtility {

    private static final String TAG = AudioUtility.class.getSimpleName();

    public static boolean encodeMp3(Context context, String filename, String beepName) {
        Log.d(TAG, "filename: " + filename);
        BufferedOutputStream outputStream;
        final int OUTPUT_STREAM_BUFFER = 8192;

        // Android Lame Encoder testing
        String audioDir = context.getFilesDir().getAbsolutePath();
        String audioPath = audioDir + "/" + filename;
        File input = new File(audioPath);
        final File output = new File( audioDir + "/" + beepName + ".mp3");
        int CHUNK_SIZE = 8192;

        WaveReader waveReader = new WaveReader(input);
        try {
            waveReader.openWave();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AndroidLame androidLame = new LameBuilder()
                .setInSampleRate(waveReader.getSampleRate())
                .setOutChannels(waveReader.getChannels())
                .setOutBitrate(128)
                .setOutSampleRate(waveReader.getSampleRate())
                .setQuality(5)
                .build();

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(output), OUTPUT_STREAM_BUFFER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        int bytesRead = 0;

        short[] buffer_l = new short[CHUNK_SIZE];
        short[] buffer_r = new short[CHUNK_SIZE];
        byte[] mp3Buf = new byte[CHUNK_SIZE];

        int channels = waveReader.getChannels();

        while (true) {
            try {
                if (channels == 2) {

                    bytesRead = waveReader.read(buffer_l, buffer_r, CHUNK_SIZE);
                    Log.d(TAG, "bytes read=" + bytesRead);

                    if (bytesRead > 0) {

                        int bytesEncoded = 0;
                        bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);
                        Log.d(TAG, "bytes encoded=" + bytesEncoded);

                        if (bytesEncoded > 0) {
                            try {
                                Log.d(TAG, "writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else break;
                } else {

                    bytesRead = waveReader.read(buffer_l, CHUNK_SIZE);
                    Log.d(TAG,"bytes read=" + bytesRead);

                    if (bytesRead > 0) {
                        int bytesEncoded = 0;

                        bytesEncoded = androidLame.encode(buffer_l, buffer_l, bytesRead, mp3Buf);
                        Log.d(TAG,"bytes encoded=" + bytesEncoded);

                        if (bytesEncoded > 0) {
                            try {
                                Log.d(TAG, "writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "flushing final mp3buffer");
        int outputMp3buf = androidLame.flush(mp3Buf);
        Log.d(TAG, "flushed " + outputMp3buf + " bytes");

        if (outputMp3buf > 0) {
            try {
                Log.d(TAG,"writing final mp3buffer to outputstream");
                outputStream.write(mp3Buf, 0, outputMp3buf);
                Log.d(TAG,"closing output stream");
                outputStream.close();
                Log.d(TAG, "Output mp3 saved");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
