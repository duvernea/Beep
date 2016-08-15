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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.UUID;

import xyz.peast.beep.gles.RendererWrapper;

/**
 * Created by duvernea on 7/30/16.
 */
public class RecordFragment extends Fragment {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private static final String BUTTON_MENU_STATE = "button_menu_state";

    private AdView mAdView;
    private Button mRecordButton;
    private Button mPlayButton;

    private Button mNextButton;
    private Button mRedoButton;

    private Button mCreateWavButton;

    private boolean mIsRecording = false;

    private boolean mIsPlaying =false;

    private Context mContext;
    private Activity mActivity;

    private GLSurfaceView mGlSurfaceView;
    private boolean mRendererSet = false;

    private String mRecordFilePath;

    // false = initial - start record state
    // true = after recording
    private boolean mMenuState = false;

    public interface RecordCallback{
        public void onRecordNextButton();
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
            mCreateWavButton = (Button) rootView.findViewById(R.id.createwavtest_button);

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
            Log.d(TAG, "Record File Name: " + uniqueID);


            //String uniqueID = UUID.randomUUID().toString();
            String recordDir = mContext.getFilesDir().getAbsolutePath();
            mRecordFilePath = recordDir + "/" + uniqueID;
            Log.d(TAG, "Record Path: " + mRecordFilePath);
            ((RecordActivity) mActivity).setRecordPath(mRecordFilePath);


            //SuperAudio.setRecordPath(mRecordFilePath);

            mCreateWavButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((RecordActivity) mActivity).createWav();

                }
            });

            //SurfaceView surfaceView = (SurfaceView) findViewById(R.id.waveform_surface);
            mGlSurfaceView = (GLSurfaceView) rootView.findViewById(R.id.glsurface_view);
            mGlSurfaceView.setEGLContextClientVersion(2);
            //mGlSurfaceView = new GLSurfaceView(this);
            mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

            final RendererWrapper rendererWrapper = new RendererWrapper(mContext);
            mGlSurfaceView.setRenderer(rendererWrapper);
            mGlSurfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event != null) {
                        // normalized back to openGL -1 to +1 scale
                        final float normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
                        final float normalizedY = (event.getY() / (float) v.getHeight()) * 2 - 1;

                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            mGlSurfaceView.queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    rendererWrapper.handleTouchPress(normalizedX, normalizedY);
                                }
                            });
                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            mGlSurfaceView.queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    rendererWrapper.handleTouchDrag(normalizedX, normalizedY);
                                }
                            });
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            mRendererSet = true;
            // Rendering mode is CONTINUOUS by default
            //mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

            // This only calls ondrawFrame 1 time
            mGlSurfaceView.requestRender();
            //surfaceView.getHolder().addCallback(this);
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
                    Log.d(TAG, "mIsPlaying start: " + mIsPlaying);
                    Log.d(TAG, "mIsRecording start: " + mIsPlaying);
                    if (!mIsRecording) {
                        mIsPlaying = true;
                        //onPlayPause(mIsPlaying);
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
        mGlSurfaceView.onResume();
    }

    private void setMenuState(boolean state) {
        if (state) {
            mRecordButton.setVisibility(View.GONE);
            mRedoButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
        }
        else {
            mRecordButton.setVisibility(View.VISIBLE);
            mRedoButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
        }
    }
    private void resetMenuState(boolean state) {
        if (state) {
            mRecordButton.setVisibility(View.GONE);
            mRedoButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
        }
        else {
        }
    }
    private void handleRecordButtonPress() {
        Log.d(TAG, "mIsPlaying: " + mIsPlaying);
        if (!mIsPlaying) {
            Log.d(TAG, "mIsRecording: " + mIsRecording);
            mIsRecording = !mIsRecording;
            if (!mIsRecording) {
                //mIsRecording = !mIsRecording;
                ((RecordActivity) mActivity).toggleRecord(mIsRecording);
                mMenuState = true;
                setMenuState(mMenuState);
                //mRecordButton.setText("Start Recording");
                //mRecordButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recordButtonStartRecording));
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
        Log.d(TAG, "Played file ended");
        mIsPlaying = false;
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
    }


}
