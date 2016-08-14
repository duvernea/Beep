package xyz.peast.beep;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

/**
 * Created by duvernea on 7/30/16.
 */
public class RecordActivity extends AppCompatActivity
        implements RecordFragment.RecordCallback, SaveFragment.SaveCallback {
    private static final String TAG = RecordActivity.class.getSimpleName();

    private static final String RECORD_FRAGMENT_TAG = "record_fragment_tag";
    private static final String SAVE_FRAGMENT_TAG = "save_fragment_tag";
    private static final String SHARE_FRAGMENT_TAG = "share_fragment_tag";

    // no file extension
    public static final String RECORD_FILE_UNIQUE_NAME = "record_file_name";
    public static final String BEEP_NAME = "beep_name";

    private static String mRecordFileName;

    @Override
    public void onRecordNextButton() {
        Log.d(TAG, "Next button pushed.");
        SaveFragment saveFragment = new SaveFragment();

        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        saveFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.record_container, saveFragment, SAVE_FRAGMENT_TAG);

        transaction.addToBackStack(SAVE_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onSaveNextButton(String beepname) {
        Log.d(TAG, "Save button pushed.");
        ShareFragment shareFragment = new ShareFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        bundle.putString(BEEP_NAME, beepname);
        shareFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.record_container, shareFragment, SHARE_FRAGMENT_TAG);
        transaction.addToBackStack(SHARE_FRAGMENT_TAG);
        transaction.commit();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mRecordFileName =UUID.randomUUID().toString();
        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        Log.d(TAG, "mRecordFileName: " + mRecordFileName);

        RecordFragment recordFragment = new RecordFragment();
        recordFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.record_container, recordFragment, RECORD_FRAGMENT_TAG)
                .commit();

        supportPostponeEnterTransition();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shutdownAudio();
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
    public native void shutdownAudio();

    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
