package xyz.peast.beep;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.naman14.androidlame.WaveReader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class ShareFragment extends Fragment {

    private static final String TAG = ShareFragment.class.getSimpleName();

    // Request code, share intent
    private static final int SHARE_BEEP = 1;

    private Context mContext;

    // Views
    private TextView mBeepNameTextView;
    private ImageView mBeepImageView;
    private Button mShareButton;
    private Button mDontShareButton;

    private String mBoardName;
    private int mBoardKey;
    private String mBeepName;
    private String mNewTempFilePath;
    private String mRecordFileName;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
        mContext = getActivity();

        mBeepNameTextView = (TextView) rootView.findViewById(R.id.beep_name_textview);
        mBeepImageView = (ImageView) rootView.findViewById(R.id.beep_imageview);
        mShareButton = (Button) rootView.findViewById(R.id.share_button);
        mDontShareButton = (Button) rootView.findViewById(R.id.no_button);

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME) + ".wav";
        String imagefile = bundle.getString(RecordActivity.IMAGE_FILE_UNIQUE_NAME);
        String imageUri = bundle.getString(RecordActivity.IMAGE_FILE_URI_UNCOMPRESSED);
        mBoardName = bundle.getString(RecordActivity.BOARD_NAME);
        mBoardKey = bundle.getInt(RecordActivity.BOARD_KEY);

        mBeepName = bundle.getString(RecordActivity.BEEP_NAME);
        mBeepNameTextView.setText(mBeepName);

        boolean encodeMp3Success = encodeMp3();

        Log.d(TAG, "Record File Name: " + mRecordFileName);
        Log.d(TAG, "Image File Name: " + imagefile);


        if (imageUri != null) {
            String imagePath = Utility.getRealPathFromURI(mContext, Uri.parse(imageUri));
            int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);
            Log.d(TAG, "image size dimen: " + imageSize);
            // Downsample bitmap
            Bitmap bitmap = Utility.subsampleBitmap(mContext, imagePath, imageSize, imageSize);
            // Center crop bitmap
            bitmap  = Utility.centerCropBitmap(mContext, bitmap);

            mBeepImageView.setImageBitmap(bitmap);
        }

        mDontShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BoardActivity.class);
                intent.putExtra(BoardActivity.LAST_ACTIVITY_UNIQUE_ID,
                        BoardActivity.FROM_SHARE_FRAGMENT);

                intent.putExtra(MainActivity.BOARD_KEY_CLICKED, mBoardKey);
                intent.putExtra(MainActivity.BOARD_NAME_SELECTED, mBoardName);
                startActivity(intent);
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String audioPath = mContext.getFilesDir().getAbsolutePath();
                audioPath += "/" + mRecordFileName;
                Log.d(TAG, "audioPath: " + audioPath);
                File requestFile = new File(audioPath);
                mNewTempFilePath = mContext.getFilesDir().getAbsolutePath();
                mNewTempFilePath += "/" + mBeepName + ".wav";
                File renamedFile = new File(mNewTempFilePath);
                try {
                    FileInputStream inStream = new FileInputStream(requestFile);
                    FileOutputStream outStream = new FileOutputStream(renamedFile);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inStream.close();
                    outStream.close();
                }
                catch (IOException ioe) {
                    Log.d(TAG, "IO Exception caught");
                }

                Uri fileUri;
                //= Uri.parse(audioPath);
                try {
                    fileUri = FileProvider.getUriForFile(
                            mContext,
                            "xyz.peast.beep.fileprovider",
                            renamedFile);
                } catch (IllegalArgumentException e) {
                    fileUri = null;
                    Log.e("File Selector",
                            "The selected file can't be shared: ");
                }
                if (fileUri != null) {
                    Log.d(TAG, "fileUri: " + fileUri);
                }
                Intent share = new Intent(Intent.ACTION_SEND);

                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                String shareChooserTitle = getResources().getString(R.string.share_chooser_title);
                startActivity(Intent.createChooser(share, shareChooserTitle));

                startActivityForResult (share, SHARE_BEEP);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file = new File(mNewTempFilePath);
        boolean deleted = file.delete();

        Intent intent = new Intent(mContext, BoardActivity.class);
        intent.putExtra(BoardActivity.LAST_ACTIVITY_UNIQUE_ID,
                BoardActivity.FROM_SHARE_FRAGMENT);

        intent.putExtra(MainActivity.BOARD_KEY_CLICKED, mBoardKey);
        intent.putExtra(MainActivity.BOARD_NAME_SELECTED, mBoardName);

        startActivity(intent);
    }
    private boolean encodeMp3() {
        BufferedOutputStream outputStream;
        final int OUTPUT_STREAM_BUFFER = 8192;

        // Android Lame Encoder testing
        String audioDir = mContext.getFilesDir().getAbsolutePath();
        String audioPath = audioDir + "/" + mRecordFileName;
        File input = new File(audioPath);
        final File output = new File( audioDir + "/testencode.mp3");
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
