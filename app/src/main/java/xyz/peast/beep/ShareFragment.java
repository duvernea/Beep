package xyz.peast.beep;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.SendButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xyz.peast.beep.services.CompressImageUpdateDbService;
import xyz.peast.beep.services.CreateVideoService;
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
    private Button mFacebookMessengerButton;

    private String mBoardName;
    private int mBoardKey;
    private String mBeepName;
    private String mNewTempFilePath;
    private String mRecordFileName;
    private boolean mBeepEdited;

    private boolean mVideoCreationComplete = false;

    private String mBeepMp3Path;

    private Bitmap mImageViewBitmap;

    private BroadcastReceiver mVideoBroadcastReceiver;

    private ProgressDialog mProgressDialog;
    private ProgressDialog mProgressDialog2;


    CallbackManager callbackManager;

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

        // Facebook callback manager
        callbackManager = CallbackManager.Factory.create();

        mBeepNameTextView = (TextView) rootView.findViewById(R.id.beep_name_textview);
        mBoardNameTextView = (TextView) rootView.findViewById(R.id.board_name_textview);
        mBeepImageView = (ImageView) rootView.findViewById(R.id.beep_imageview);
        mReplayButton = (Button) rootView.findViewById(R.id.replay_button);
        mShareButton = (Button) rootView.findViewById(R.id.email_share_button);
        mDontShareButton = (Button) rootView.findViewById(R.id.no_button);
        mFacebookShareButton = (Button) rootView.findViewById(R.id.facebook_button);
        mFacebookMessengerButton = (Button) rootView.findViewById(R.id.facebook_messenger_button);

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

                // >= Lollipop gmail only email client used
                ((RecordActivity) mActivity).setPitchShift(0);
                ((RecordActivity) mActivity).setTempo(1.0);
                ((RecordActivity) mActivity).turnFxOff();

                ((RecordActivity) mActivity).onFileChange("", 0, 0);

                mBeepMp3Path = Utility.getBeepPath(mContext, mBeepName);
                Log.d(TAG, "mRecordFileName: " + mRecordFileName);
                Uri fileUri = ShareUtility.encodeBeepGetUri(mContext, mRecordFileName,
                        mBeepName, mBeepMp3Path, mBeepEdited);

                PackageManager pm = mActivity.getPackageManager();
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                String body = "I call it \"" + mBeepName + "\"";
                String subject = "I created this on beep app!";

                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.setType("message/rfc822");

//                Intent openInChooser = Intent.createChooser(emailIntent, "Test string");

                List<ResolveInfo> resInfo = pm.queryIntentActivities(emailIntent, 0);
                List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

                emailIntent.setType("audio/*");
                emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                Log.d(TAG, "resInfo.size(): " + resInfo.size());

                for (int i = 0; i < resInfo.size(); i++) {
                    ResolveInfo ri = resInfo.get(i);
                    String packageName = ri.activityInfo.packageName;
                    Log.d(TAG, "packageName " + i + " " + packageName);
                    if (packageName.contains("android.gm")) {
                        emailIntent.setPackage(packageName);
                    }
//                    } else if (packageName.contains("android.gm")) {
//                        Log.d(TAG, "package contains android.gm");
//                        Intent intent = new Intent();
//                        intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
//                        intent.setAction(Intent.ACTION_SEND);
//                        intent.setType("text/plain");
//
//                        intent.putExtra(Intent.EXTRA_TEXT, body);
//
//                        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//                        intent.setType("message/rfc822");
//                        intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
//
//                    }
                }
                // convert intentList to array
//                LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
//                Log.d(TAG, "ExtraIntents count: " + extraIntents.length);

//                openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                // startActivity(openInChooser);
                startActivityForResult(emailIntent, SHARE_BEEP);

                // startActivityForResult (share, SHARE_BEEP);
            }
        });
        mFacebookShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String videoPath = Utility.getBeepVideoPath(mContext, mBeepName);
                File videoFile = new File(videoPath);
                Uri videoFileUri = Uri.fromFile(videoFile);

                ShareVideo shareVideo = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();

                final ShareVideoContent content = new ShareVideoContent.Builder()
                        .setVideo(shareVideo)
                        .build();

                mProgressDialog2 = new ProgressDialog(mContext);
                mProgressDialog2.setTitle("Creating content");
                // mProgressDialog.setMessage("message...");
                mProgressDialog2.setCancelable(false); // disable dismiss by tapping outside of the dialog
                if (mVideoCreationComplete) {
                    MessageDialog.show(mActivity, content);
                } else {
                    mProgressDialog2.show();
                    mProgressDialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ShareDialog.show(mActivity, content);
                        }
                    });
                }
            }
        });
        mFacebookMessengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String videoPath = Utility.getBeepVideoPath(mContext, mBeepName);
                File videoFile = new File(videoPath);
                Uri videoFileUri = Uri.fromFile(videoFile);

                ShareVideo shareVideo = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();

                final ShareVideoContent content = new ShareVideoContent.Builder()
                        .setVideo(shareVideo)
                        .build();

                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setTitle("Creating content");
                // mProgressDialog.setMessage("message...");
                mProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
                if (mVideoCreationComplete) {
                    MessageDialog.show(mActivity, content);
                } else {
                    mProgressDialog.show();
                    mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            MessageDialog.show(mActivity, content);
                        }
                    });
                }

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
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(CreateVideoService.VIDEO_CREATION_DONE);

        mVideoBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive Broadcast Listener");
                mVideoCreationComplete = true;
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                if (mProgressDialog2 != null && mProgressDialog2.isShowing()) {
                    mProgressDialog2.dismiss();
                }

            }
        };
            getActivity().registerReceiver(mVideoBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mVideoBroadcastReceiver);
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
