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

import com.facebook.FacebookSdk;

import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;

import xyz.peast.beep.services.LoadDownsampledBitmapImageService;


public class ShareFragment extends Fragment {

    private static final String TAG = ShareFragment.class.getSimpleName();

    // Request code, share intent
    private static final int SHARE_BEEP = 1;

    private Context mContext;
    private Activity mActivity;

    private AdView mAdView;

    // Audio variables
    private boolean mIsPlaying;

    // Views
    private TextView mBeepNameTextView;
    private TextView mBoardNameTextView;
    private ImageView mBeepImageView;
    private Button mShareButton;
    private Button mDontShareButton;
    private Button mReplayButton;
    private Button mFacebookShareButton;

    private String mBoardName;
    private int mBoardKey;
    private String mBeepName;
    private String mNewTempFilePath;
    private String mRecordFileName;
    private boolean mBeepEdited;

    private String mBeepMp3Path;

    private Bitmap mImageViewBitmap;

    String mImageUri;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
        mContext = getActivity();
        mActivity = getActivity();

        mBeepNameTextView = (TextView) rootView.findViewById(R.id.beep_name_textview);
        mBoardNameTextView = (TextView) rootView.findViewById(R.id.board_name_textview);
        mBeepImageView = (ImageView) rootView.findViewById(R.id.beep_imageview);
        mReplayButton = (Button) rootView.findViewById(R.id.replay_button);
        mShareButton = (Button) rootView.findViewById(R.id.email_share_button);
        mDontShareButton = (Button) rootView.findViewById(R.id.no_button);
        mFacebookShareButton = (Button) rootView.findViewById(R.id.facebook_button);

        // Initilize Ads
        mAdView = (AdView) rootView.findViewById(R.id.adview);
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME);
        String imagefile = bundle.getString(RecordActivity.IMAGE_FILE_UNIQUE_NAME);
        mImageUri = bundle.getString(RecordActivity.IMAGE_FILE_URI_UNCOMPRESSED);
        String imagePath = bundle.getString(RecordActivity.IMAGE_FILE_PATH_UNCOMPRESSED);
        mBoardName = bundle.getString(RecordActivity.BOARD_NAME);
        mBoardKey = bundle.getInt(RecordActivity.BOARD_KEY);
        mBeepEdited = bundle.getBoolean(RecordActivity.BEEP_EDITED);

        mBeepName = bundle.getString(RecordActivity.BEEP_NAME);
        mBeepNameTextView.setText(mBeepName);
        mBoardNameTextView.setText(mBoardName);

        // Audio File Name path
        final String filePath = Utility.getFullWavPath(mContext, mRecordFileName, false);

        // Replay audio button
        mReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlaying = true;
                ((RecordActivity) mActivity).onFileChange(filePath, 0, 0);
                ((RecordActivity) mActivity).onPlayPause();
            }
        });

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
                ((RecordActivity) mActivity).setPitchShift(0);
                ((RecordActivity) mActivity).setTempo(1.0);
                ((RecordActivity) mActivity).turnFxOff();
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
                ((RecordActivity) mActivity).setPitchShift(0);
                ((RecordActivity) mActivity).setTempo(1.0);
                ((RecordActivity) mActivity).turnFxOff();

                ((RecordActivity) mActivity).onFileChange("", 0, 0);

                mBeepMp3Path = Utility.getBeepPath(mContext, mBeepName);
                Log.d(TAG, "mRecordFileName: " + mRecordFileName);
                Uri fileUri = ShareUtility.encodeBeepGetUri(mContext, mRecordFileName,
                        mBeepName, mBeepMp3Path, mBeepEdited);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                String subject = "I created this on beep app!";
                share.putExtra(Intent.EXTRA_SUBJECT, subject);
                String body = "I call it \"" + mBeepName + "\"";
                share.putExtra(Intent.EXTRA_TEXT, body);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivityForResult (share, SHARE_BEEP);
            }
        });
        mFacebookShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mRecordFileName: " + mRecordFileName);
                Log.d(TAG, "mImageUri: " + mImageUri);

                SharePhoto sharePhoto1 = new SharePhoto.Builder()
                        .setBitmap(mImageViewBitmap).build();

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("http://k003.kiwi6.com/hotlink/rlucf3los9/TOAD_Free_MP3_Download_.mp3")).build();

//                ShareContent shareContent = new ShareMediaContent.Builder()
//                        .addMedium(sharePhoto1)
//                        .build();

                ShareDialog.show(mActivity, content);
            }
        });

        Log.d(TAG, "imagePath: " + imagePath);

        if (imagePath != null) {
            int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle reply = msg.getData();
                    mImageViewBitmap = reply.getParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE);
                    mBeepImageView.setImageBitmap(mImageViewBitmap);
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
                // TODO - deleting file here seems to happen before the message is sent
                // TODO - need to find a way to delete after send complete
                //File file = new File(mBeepMp3Path);
                // boolean deleted = file.delete();

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
