package xyz.peast.beep;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import xyz.peast.beep.gles.RendererWrapper;

public class RecordActivity extends AppCompatActivity  {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private AdView mAdView;
    private Button mRecordButton;
    private Button mPlayButton;

    private boolean mIsRecording = false;

    private boolean mIsPlaying =false;

    private Context mContext;

    private GLSurfaceView mGlSurfaceView;
    private boolean mRendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        boolean supportES2 = (info.reqGlEsVersion >= 0x20000);
        if (supportES2) {
            mContext = this;

            mAdView = (AdView) findViewById(R.id.adview);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("839737069995AAD5519D71B8B267924D")
                    .build();
            mAdView.loadAd(adRequest);

            final String path = getIntent().getStringExtra(MainActivity.TEMP_FILE_PATH);
            setUp();

            //SurfaceView surfaceView = (SurfaceView) findViewById(R.id.waveform_surface);
            mGlSurfaceView = (GLSurfaceView) findViewById(R.id.glsurface_view);
            mGlSurfaceView.setEGLContextClientVersion(2);
            //mGlSurfaceView = new GLSurfaceView(this);
            mGlSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);

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
                        }
                        else if (event.getAction() ==MotionEvent.ACTION_MOVE){
                            mGlSurfaceView.queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    rendererWrapper.handleTouchDrag(normalizedX, normalizedY);
                                }
                            });
                        }
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });
            mRendererSet = true;


            //surfaceView.getHolder().addCallback(this);



            mRecordButton = (Button) findViewById(R.id.record_button);
            mRecordButton.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    Log.d(TAG, "mIsPlaying: " + mIsPlaying);
                    if (!mIsPlaying) {
                        Log.d(TAG, "mIsRecording: " + mIsRecording);
                        mIsRecording = !mIsRecording;
                        if (!mIsRecording) {
                            //mIsRecording = !mIsRecording;
                            toggleRecord(mIsRecording);
                            mRecordButton.setText("Start Recording");
                            mRecordButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recordButtonStartRecording));
                        }
                        if (mIsRecording) {
                            //mIsRecording = !mIsRecording;
                            toggleRecord(mIsRecording);
                            mRecordButton.setText("Stop Recording");
                            mRecordButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recordButtonStopRecording));
                        }

                    }
                }
            });

            mPlayButton = (Button) findViewById(R.id.play_button);
            mPlayButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "mIsPlaying start: " + mIsPlaying);
                    Log.d(TAG, "mIsRecording start: " + mIsPlaying);
                    if (!mIsRecording) {
                        String path = "/data/data/xyz.peast.beep/files/temp.wav.wav";
                        mIsPlaying = true;
                        //onPlayPause(mIsPlaying);
                        Log.d(TAG, "mIsPlaying play: " + mIsPlaying);
                        onFileChange(path, 0, 0);
                        onPlayPause(path, mIsPlaying, 0);
                    }
                }
            });
        }
        else {
            Log.e("OpenGLES 2", "Your device doesn't support ES2. )" + info.reqGlEsVersion + ")");
        }



    }

    private void playbackEndCallback() {
        //Toast.makeText(mContext, "Callback", Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Played file ended");
        mIsPlaying = false;
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
    }

    private native void setUp();

    private native void SuperpoweredAudio(int samplerate, int buffersize, String apkPath);
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    private native void toggleRecord(boolean record);



    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
