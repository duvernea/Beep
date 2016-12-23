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
        BufferedOutputStream outputStream;
        BufferedOutputStream outputStream_test;
        final int OUTPUT_STREAM_BUFFER = 8192;
        final int OUTPUT_STREAM_BUFFER_test = 8192 * 2;

        String audioDir = context.getFilesDir().getAbsolutePath();

        Log.d(TAG, "wav path: " + wavPath);
        File inputWavFile = new File(wavPath);
        final File output = new File(audioDir + "/" + beepName + ".mp3");
        final File output_test = new File(audioDir + "/" + beepName + "test.wav");
        int CHUNK_SIZE = 8192;

        WaveReader waveReader = new WaveReader(inputWavFile);
        try {
            waveReader.openWave();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int rate = waveReader.getSampleRate();
        Log.d(TAG, "rate: " + rate);

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
        try {
            byte[] header;
            header = getWavHeader();
            outputStream_test.write(header, 0, 44);
        } catch (IOException ioe) {
            Log.d(TAG, "Couldn't write header");
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
                    Log.d(TAG, "bytes read: " + bytesRead);
                    /*** TROUBLESHOOTING ********/
                    if (bytesRead == 8192) {
                        int bytesRead_temp = bytesRead * 2;
                        ByteBuffer buffer = ByteBuffer.allocate(bytesRead_temp);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        buffer.asShortBuffer().put(buffer_l);
                        byte[] bytes = buffer.array();
                        int shortbufferlength = bytes.length;
                        outputStream_test.write(bytes, 0, bytesRead_temp);
                        /*** TROUBLESHOOTING ********/
                    }
                    if (bytesRead > 0) {
                        Log.d(TAG, "bytes read from wav: " + bytesRead);

                        int bytesEncoded = 0;
                        bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);
                        Log.d(TAG, "bytes encoded: " + bytesEncoded);
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
        return true;
    }

    private static byte[] getWavHeader() {
        byte[] header = new byte[44];

        int channels = 1;
        int longSampleRate = 48000;
        int bitsPerSample = 16;
        int byteRate = longSampleRate * channels * bitsPerSample / 8;
        // subchunk 2 size
        // # of total samples * num channels * bits per sample / 8
        // number of bytes in the data
        int audioDuration = 5;  // in seconds
        int totalAudioLen = audioDuration * longSampleRate * channels * bitsPerSample / 8;
        // Chunk size = 36 + SubChunk2Size
        // 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
        // SubChunk1Size = 16 for PCM
        int totalDataLen = totalAudioLen + 36;

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8);  // block align
        header[33] = 0;
        header[34] = (byte) bitsPerSample;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        return header;
    }
}