package xyz.peast.beep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import java.io.File;

import xyz.peast.beep.services.LoadDownsampledBitmapImageService;
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
    private boolean mBeepEdited;

    private String mBeepMp3Path;

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
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME);
        String imagefile = bundle.getString(RecordActivity.IMAGE_FILE_UNIQUE_NAME);
        String imageUri = bundle.getString(RecordActivity.IMAGE_FILE_URI_UNCOMPRESSED);
        String imagePath = bundle.getString(RecordActivity.IMAGE_FILE_PATH_UNCOMPRESSED);
        mBoardName = bundle.getString(RecordActivity.BOARD_NAME);
        mBoardKey = bundle.getInt(RecordActivity.BOARD_KEY);
        mBeepEdited = bundle.getBoolean(RecordActivity.BEEP_EDITED);

        mBeepName = bundle.getString(RecordActivity.BEEP_NAME);
        mBeepNameTextView.setText(mBeepName);

        final String audioWavPath = Utility.getFullWavPath(mContext, mRecordFileName, mBeepEdited);

        // Encode the wav to mp3 for sharing
//        Bundle bundleEncodeAudio = new Bundle();
//        bundleEncodeAudio.putString(Constants.WAV_FILE_PATH, audioWavPath);
//        bundleEncodeAudio.putString(Constants.BEEP_NAME, mBeepName);
//        bundleEncodeAudio.putBoolean(Constants.BEEP_EDITED, mBeepEdited);
//        Intent encodeAudioIntent = new Intent(mContext, EncodeAudioService.class);
//        encodeAudioIntent.putExtras(bundleEncodeAudio);
//        mContext.startService(encodeAudioIntent);

        long time = System.currentTimeMillis();
        Log.d(TAG, "EncodeAudioService intent process starting at: " + time);

        //Log.d(TAG, "mAudioWavPath: " + audioWavPath);
        //boolean encodeMp3Success = AudioUtility.encodeMp3(mContext, audioWavPath, mBeepName);

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

                mBeepMp3Path = Utility.getBeepPath(mContext, mBeepName);
                Uri fileUri = ShareUtility.encodeBeepGetUri(mContext, mRecordFileName,
                        mBeepName, mBeepMp3Path, mBeepEdited);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivityForResult (share, SHARE_BEEP);
            }
        });

        Log.d(TAG, "imagePath: " + imagePath);

        if (imagePath != null) {
            int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle reply = msg.getData();
                    Bitmap bitmap = reply.getParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE);
                    mBeepImageView.setImageBitmap(bitmap);
                }
            };

            Intent intent = new Intent(mContext, LoadDownsampledBitmapImageService.class);
            intent.putExtra(Constants.IMAGE_MESSENGER, new Messenger(handler));
            intent.putExtra(Constants.ORIGINAL_IMAGE_FILE_PATH, imagePath);
            intent.putExtra(Constants.IMAGE_MIN_SIZE, imageSize);
            mContext.startService(intent);
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        if (requestCode == SHARE_BEEP) {

            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                File file = new File(mBeepMp3Path);
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
