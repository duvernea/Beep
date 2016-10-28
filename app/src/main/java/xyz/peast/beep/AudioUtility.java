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

    // Encode an mp3 to a wav. Save the file as the more friendly "Beep Name" .mp3
    public static boolean encodeMp3(Context context, String filename, String beepName) {
        BufferedOutputStream outputStream;
        final int OUTPUT_STREAM_BUFFER = 8192;

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
                    if (bytesRead > 0) {

                        int bytesEncoded = 0;
                        bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);
                        if (bytesEncoded > 0) {
                            try {
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else break;
                } else {

                    bytesRead = waveReader.read(buffer_l, CHUNK_SIZE);

                    if (bytesRead > 0) {
                        int bytesEncoded = 0;

                        bytesEncoded = androidLame.encode(buffer_l, buffer_l, bytesRead, mp3Buf);

                        if (bytesEncoded > 0) {
                            try {
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
        int outputMp3buf = androidLame.flush(mp3Buf);

        if (outputMp3buf > 0) {
            try {
                outputStream.write(mp3Buf, 0, outputMp3buf);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
