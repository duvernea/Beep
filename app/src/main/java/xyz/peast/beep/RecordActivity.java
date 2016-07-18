package xyz.peast.beep;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private AdView mAdView;
    private Button mRecordButton;
    private Button mPlayButton;

    private boolean mIsRecording = false;

    private boolean mIsPlaying =false;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mContext = this;

        mAdView = (AdView) findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        final String path = getIntent().getStringExtra(MainActivity.TEMP_FILE_PATH);
        setUp();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.waveform_surface);
        surfaceView.getHolder().addCallback(this);



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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
