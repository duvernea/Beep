package xyz.peast.beep;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by duvernea on 7/30/16.
 */
public class RecordActivity extends AppCompatActivity implements RecordFragment.RecordCallback {
    private static final String TAG = RecordActivity.class.getSimpleName();

    private static final String RECORD_FRAGMENT_TAG = "record_fragment_tag";

    @Override
    public void onRecordNextButton() {
        Log.d(TAG, "Next button pusehd.");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        RecordFragment recordFragment = new RecordFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.record_container, recordFragment, RECORD_FRAGMENT_TAG)
                .commit();

        supportPostponeEnterTransition();
    }
    private void playbackEndCallback() {
        Log.d(TAG, "Played file ended");
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
        RecordFragment recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_TAG);
        recordFragment.onPlaybackEnd();
    }


    public native void setupAudio();

    public native void SuperpoweredAudio(int samplerate, int buffersize);
    public native void onPlayPause(String filepath, boolean play, int size);
    public native void onFileChange(String apkPath, int fileOffset, int fileLength );
    public native void toggleRecord(boolean record);

    public native void setRecordPath(String path);

    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
