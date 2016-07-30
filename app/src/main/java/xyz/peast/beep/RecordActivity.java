package xyz.peast.beep;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.UUID;

import xyz.peast.beep.gles.RendererWrapper;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private static final String BUTTON_MENU_STATE = "button_menu_state";

    private AdView mAdView;
    private Button mRecordButton;
    private Button mPlayButton;

    private Button mNextButton;
    private Button mRedoButton;

    private boolean mIsRecording = false;

    private boolean mIsPlaying =false;

    private Context mContext;

    private GLSurfaceView mGlSurfaceView;
    private boolean mRendererSet = false;

    private String mRecordFilePath;

    // false = initial - start record state
    // true = after recording
    private boolean mMenuState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        boolean supportES2 = (info.reqGlEsVersion >= 0x20000);
        if (supportES2) {
            mContext = this;

            mRedoButton = (Button) findViewById(R.id.redo_record_button);
            mNextButton = (Button) findViewById(R.id.next_button);

            mAdView = (AdView) findViewById(R.id.adview);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("839737069995AAD5519D71B8B267924D")
                    .build();
            mAdView.loadAd(adRequest);

            // Superpowered Audio Setup
            final String path = getIntent().getStringExtra(MainActivity.TEMP_FILE_PATH);
            setupAudio();

            String uniqueID = UUID.randomUUID().toString();
            String recordDir = mContext.getFilesDir().getAbsolutePath();
            mRecordFilePath = recordDir + "/" + uniqueID + "TESTJAVA";
            Log.d(TAG, "Record Path: " + mRecordFilePath);
            setRecordPath(mRecordFilePath);
            //SuperAudio.setRecordPath(mRecordFilePath);

            //SurfaceView surfaceView = (SurfaceView) findViewById(R.id.waveform_surface);
            mGlSurfaceView = (GLSurfaceView) findViewById(R.id.glsurface_view);
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
            mRecordButton = (Button) findViewById(R.id.record_button);
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
            mPlayButton = (Button) findViewById(R.id.play_button);
            mPlayButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "mIsPlaying start: " + mIsPlaying);
                    Log.d(TAG, "mIsRecording start: " + mIsPlaying);
                    if (!mIsRecording) {
                        mIsPlaying = true;
                        //onPlayPause(mIsPlaying);
                        Log.d(TAG, "mIsPlaying play: " + mIsPlaying);
                        onFileChange(mRecordFilePath + ".wav", 0, 0);
                        onPlayPause(mRecordFilePath + ".wav", mIsPlaying, 0);
                    }
                }
            });
        } else {
            Log.e("OpenGLES 2", "Your device doesn't support ES2. )" + info.reqGlEsVersion + ")");
        }

        if (savedInstanceState != null) {
            mMenuState = savedInstanceState.getBoolean(BUTTON_MENU_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAudio();
        mIsRecording = false;
        mIsPlaying =false;
        setupAudio();
        resetMenuState(mMenuState);
        mGlSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
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
                toggleRecord(mIsRecording);
                mMenuState = true;
                setMenuState(mMenuState);
                //mRecordButton.setText("Start Recording");
                //mRecordButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recordButtonStartRecording));
            }
            if (mIsRecording) {
                //mIsRecording = !mIsRecording;
                toggleRecord(mIsRecording);
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
            toggleRecord(mIsRecording);
        }
    }
    private void handleNextButtonPress() {

    }

    private void playbackEndCallback() {
        Log.d(TAG, "Played file ended");
        mIsPlaying = false;
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUTTON_MENU_STATE, mMenuState);
    }

    private native void setupAudio();

    private native void SuperpoweredAudio(int samplerate, int buffersize);
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    private native void toggleRecord(boolean record);

    private native void setRecordPath(String path);

    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
