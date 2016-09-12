package xyz.peast.beep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;

import xyz.peast.beep.services.BeepService;
import xyz.peast.beep.services.BitmapImageService;
import xyz.peast.beep.services.EncodeAudioService;


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

        // Encode the wav to mp3 for sharing
        Bundle bundleEncodeAudio = new Bundle();
        bundleEncodeAudio.putString(Constants.RECORD_FILE_NAME, mRecordFileName);
        bundleEncodeAudio.putString(Constants.BEEP_NAME, mBeepName);
        Intent encodeAudioIntent = new Intent(mContext, EncodeAudioService.class);
        encodeAudioIntent.putExtras(bundleEncodeAudio);

        mContext.startService(encodeAudioIntent);
        String filename = bundle.getString(Constants.RECORD_FILE_NAME);
        String beepName = bundle.getString(Constants.BEEP_NAME);

        boolean encodeMp3Success = AudioUtility.encodeMp3(mContext, mRecordFileName, mBeepName);

        Log.d(TAG, "Record File Name: " + mRecordFileName);
        Log.d(TAG, "Image File Name: " + imagefile);


//        if (imageUri != null) {
//            String imagePath = Utility.getRealPathFromURI(mContext, Uri.parse(imageUri));
//            int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);
//            Log.d(TAG, "image size dimen: " + imageSize);
//            // Downsample bitmap
//            Bitmap bitmap = Utility.subsampleBitmap(mContext, imagePath, imageSize, imageSize);
//            // Center crop bitmap
//            bitmap  = Utility.centerCropBitmap(mContext, bitmap);
//
//            mBeepImageView.setImageBitmap(bitmap);
//        }

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
                String beepMp3Path = audioPath + "/" + mBeepName + ".mp3";
                //audioPath += "/" + mRecordFileName;
                Log.d(TAG, "audioPath: " + beepMp3Path);
                File beepMp3File = new File(beepMp3Path);
                //File requestFile = new File(beepMp3Path);
                //mNewTempFilePath = mContext.getFilesDir().getAbsolutePath();
                //mNewTempFilePath += "/" + mBeepName + ".wav";
                //File renamedFile = new File(mNewTempFilePath);
//                try {
//                    FileInputStream inStream = new FileInputStream(requestFile);
//                    FileOutputStream outStream = new FileOutputStream(renamedFile);
//                    FileChannel inChannel = inStream.getChannel();
//                    FileChannel outChannel = outStream.getChannel();
//                    inChannel.transferTo(0, inChannel.size(), outChannel);
//                    inStream.close();
//                    outStream.close();
//                }
//                catch (IOException ioe) {
//                    Log.d(TAG, "IO Exception caught");
//                }

                Uri fileUri;
                //= Uri.parse(audioPath);
                try {
                    fileUri = FileProvider.getUriForFile(
                            mContext,
                            "xyz.peast.beep.fileprovider",
                            beepMp3File);
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
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "handler handleMessage");
                Bundle reply = msg.getData();
                Bitmap bitmap = reply.getParcelable("bitmap");
                // do whatever with the bundle here
                mBeepImageView.setImageBitmap(bitmap);
            }
        };
//        if (imageUri != null) {
//            String imagePath = Utility.getRealPathFromURI(mContext, Uri.parse(imageUri));
//            int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);
//            Log.d(TAG, "image size dimen: " + imageSize);
//            // Downsample bitmap
//            Bitmap bitmap = Utility.subsampleBitmap(mContext, imagePath, imageSize, imageSize);
//            // Center crop bitmap
//            bitmap  = Utility.centerCropBitmap(mContext, bitmap);
//
//            mBeepImageView.setImageBitmap(bitmap);
//        }


        Intent intent = new Intent(mContext, BitmapImageService.class);
        intent.putExtra("image_messenger", new Messenger(handler));
        intent.putExtra(Utility.ORIGINAL_IMAGE_FILE_URI, imageUri);

        mContext.startService(intent);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SHARE_BEEP) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                File file = new File(mNewTempFilePath);
                boolean deleted = file.delete();

                Intent intent = new Intent(mContext, BoardActivity.class);
                intent.putExtra(BoardActivity.LAST_ACTIVITY_UNIQUE_ID,
                        BoardActivity.FROM_SHARE_FRAGMENT);

                intent.putExtra(MainActivity.BOARD_KEY_CLICKED, mBoardKey);
                intent.putExtra(MainActivity.BOARD_NAME_SELECTED, mBoardName);

                startActivity(intent);
            }
        }
    }
}
