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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by duverneay on 9/11/16.
 */
public class AudioUtility {

    private static final String TAG = AudioUtility.class.getSimpleName();

    // Encode an mp3 to a wav. Save the file as the more friendly "Beep Name" .mp3
    public static boolean encodeMp3(Context context, String wavPath, String beepName) {
        long time1 = System.currentTimeMillis();
        Log.d(TAG, "Utility encodeMp3 process starting at: " + time1);

        BufferedOutputStream outputStream;
        BufferedOutputStream outputStream_test;
        final int OUTPUT_STREAM_BUFFER = 8192;
        final int OUTPUT_STREAM_BUFFER_test = 8192 * 2;

        String audioDir = context.getFilesDir().getAbsolutePath();

        Log.d(TAG, "wav path: " + wavPath);
        File inputWavFile = new File(wavPath);
        final File output = new File( audioDir + File.separator + beepName + Constants.MP3_FILE_SUFFIX);
        final File output_test = new File( audioDir + "/" + beepName + "test.wav");
        int CHUNK_SIZE = 8192;

        WaveReader waveReader = new WaveReader(inputWavFile);
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
            outputStream_test = new BufferedOutputStream(new FileOutputStream(output_test), OUTPUT_STREAM_BUFFER_test);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        int bytesRead = 0;

        short[] buffer_l = new short[CHUNK_SIZE];
        short[] buffer_r = new short[CHUNK_SIZE];
        short[] buffer_lr = new short[CHUNK_SIZE];
        byte[] mp3Buf = new byte[CHUNK_SIZE];

        int channels = waveReader.getChannels();

        while (true) {
            try {
                if (channels == 2) {

                    bytesRead = waveReader.read(buffer_l, buffer_r, CHUNK_SIZE);
                    //Log.d(TAG, "bytes read: " + bytesRead);
//                    /*** TROUBLESHOOTING ********/
//                    int bytesRead_temp = bytesRead * 2;
//                    ByteBuffer buffer = ByteBuffer.allocate(bytesRead_temp);
//                    buffer.order(ByteOrder.LITTLE_ENDIAN);
//                    buffer.asShortBuffer().put(buffer_l);
//                    byte[] bytes = buffer.array();
//                    int shortbufferlength = bytes.length;
//                    outputStream_test.write(bytes, 0, bytesRead_temp);
//                    /*** TROUBLESHOOTING ********/

                    if (bytesRead > 0) {
                        //Log.d(TAG, "bytes read from wav: " + bytesRead);

                        int bytesEncoded = 0;
                        bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);
                        //Log.d(TAG, "bytes encoded: " + bytesEncoded);
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
                outputStream_test.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG, "Utility encodeMp3 process stopping at: " + time2);
        long elapsedTime = time2 - time1;
        Log.d(TAG, "encodeMp3 process time: " + elapsedTime);

        return true;
    }

}
