package xyz.peast.beep;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.UUID;

import xyz.peast.beep.gles.RendererWrapper;

/**
 * Created by duvernea on 7/30/16.
 */
public class RecordFragment extends Fragment {

    private static final String TAG = RecordActivity.class.getSimpleName();

    // onSaveInstance KEY
    private static final String BUTTON_MENU_STATE = "button_menu_state";

    private Context mContext;
    private Activity mActivity;

    // Views
    private AdView mAdView;
    private Button mRecordButton;
    private Button mPlayButton;
    private Button mNextButton;
    private Button mRedoButton;
    private TextView mRecordBeepMsg;

    // Menu State
    // false = initial state, true = first recording complete
    private boolean mMenuState = false;

    // Audio
    private boolean mIsRecording = false;
    private boolean mIsPlaying =false;

    private String mRecordFilePath;

    public interface RecordCallback{
        void onRecordNextButton();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        mContext = getActivity();

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        boolean supportES2 = (info.reqGlEsVersion >= 0x20000);
        if (supportES2) {

            mRedoButton = (Button) rootView.findViewById(R.id.redo_record_button);
            mNextButton = (Button) rootView.findViewById(R.id.next_button);
            mRecordBeepMsg = (TextView) rootView.findViewById(R.id.record_beep_msg);

            mAdView = (AdView) rootView.findViewById(R.id.adview);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("839737069995AAD5519D71B8B267924D")
                    .build();
            mAdView.loadAd(adRequest);

            // Superpowered Audio Setup
            mActivity = getActivity();
            if (!(mActivity instanceof RecordActivity)) {
                Log.d(TAG, "Error: Incorrect activity from getActivity. Fragment calling Activity methods");

            }
            ((RecordActivity) mActivity).setupAudio();

            Bundle bundle = this.getArguments();
            String uniqueID = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME);

            String recordDir = mContext.getFilesDir().getAbsolutePath();
            mRecordFilePath = recordDir + "/" + uniqueID;
            ((RecordActivity) mActivity).setRecordPath(mRecordFilePath);


            mRecordButton = (Button) rootView.findViewById(R.id.record_button);
            mRecordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleRecordButtonPress();
                }
            });
            mRedoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleRedoButtonPress();
                }
            });
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleNextButtonPress();
                }
            });
            mPlayButton = (Button) rootView.findViewById(R.id.play_button);
            mPlayButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!mIsRecording) {
                        mIsPlaying = true;
                        Log.d(TAG, "mIsPlaying play: " + mIsPlaying);
                        ((RecordActivity) mActivity).onFileChange(mRecordFilePath + ".wav", 0, 0);
                        ((RecordActivity) mActivity).onPlayPause(mRecordFilePath + ".wav", mIsPlaying, 0);
                    }
                }
            });
        } else {
            Log.e("OpenGLES 2", "Your device doesn't support ES2. )" + info.reqGlEsVersion + ")");
        }

        if (savedInstanceState != null) {
            mMenuState = savedInstanceState.getBoolean(BUTTON_MENU_STATE);
        }
        return rootView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUTTON_MENU_STATE, mMenuState);
    }
    @Override
    public void onResume() {
        super.onResume();
        mIsRecording = false;
        mIsPlaying =false;
        ((RecordActivity) mActivity).setupAudio();
        resetMenuState(mMenuState);
    }

    private void setMenuState(boolean state) {
        if (state) {
            mRecordButton.setVisibility(View.GONE);
            mRedoButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            mRecordBeepMsg.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
        else {
            mRecordButton.setVisibility(View.VISIBLE);
            mRedoButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
            mRecordBeepMsg.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.GONE);
        }
    }
    private void resetMenuState(boolean state) {
        if (state) {
            mRecordButton.setVisibility(View.GONE);
            mRedoButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            mRecordBeepMsg.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
        else {
        }
    }
    private void handleRecordButtonPress() {
        if (!mIsPlaying) {
            mIsRecording = !mIsRecording;
            if (!mIsRecording) {
                ((RecordActivity) mActivity).toggleRecord(mIsRecording);
                mMenuState = true;
                setMenuState(mMenuState);
            }
            if (mIsRecording) {
                //mIsRecording = !mIsRecording;
                ((RecordActivity) mActivity).toggleRecord(mIsRecording);
                mRecordButton.setText(getResources().getString(R.string.stop_recording));
                mRecordButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recordButtonStopRecording));
            }
        }
    }
    private void handleRedoButtonPress() {

        if (!mIsPlaying) {
            mMenuState = false;
            mIsRecording = true;
            mRecordButton.setText(getResources().getString(R.string.stop_recording));
            mRecordButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recordButtonStopRecording));
            setMenuState(mMenuState);
            ((RecordActivity) mActivity).toggleRecord(mIsRecording);
        }
    }
    private void handleNextButtonPress() {
        ((RecordCallback) getActivity()).onRecordNextButton();
    }

    public void onPlaybackEnd() {
        mIsPlaying = false;
    }
    public void onBufferCallback(float rmsValue) {
        // TODO?
    }
}
