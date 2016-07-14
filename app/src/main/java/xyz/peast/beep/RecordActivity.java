package xyz.peast.beep;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private AdView mAdView;
    private Button mRecordButton;
    private Button mPlayButton;
    private boolean mIsRecording = false;

    private boolean mIsPlaying =false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mAdView = (AdView) findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        final String path = getIntent().getStringExtra(MainActivity.TEMP_FILE_PATH);

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
                    }
                    if (mIsRecording) {
                        //mIsRecording = !mIsRecording;
                        toggleRecord(mIsRecording);
                        mRecordButton.setText("Stop Recording");
                    }

                }
            }
        });

        mPlayButton = (Button) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsRecording) {
                    String path = "/data/data/xyz.peast.beep/files/temp.wav.wav";
                    mIsPlaying = !mIsPlaying;
                    //onPlayPause(mIsPlaying);
                    Log.d(TAG, "mIsPlaying java: " + mIsPlaying);
                    onFileChange(path, 0, 0);
                    onPlayPause(path, mIsPlaying, 0);
                }
            }
        });

    }

    private native void SuperpoweredExample(int samplerate, int buffersize, String apkPath, int fileAoffset, int fileAlength, int fileBoffset, int fileBlength);
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    private native void toggleRecord(boolean record);
    static {
        System.loadLibrary("SuperpoweredExample");
    }
}
