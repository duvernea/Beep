package xyz.peast.beep;

import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
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

    // extra arguments
    public static final String RECORD_FILE_UNIQUE_NAME = "record_file_name";
    public static final String IMAGE_FILE_UNIQUE_NAME = "image_file_name";
    public static final String IMAGE_FILE_URI_UNCOMPRESSED = "uncompressed_image_uri";
    public static final String BEEP_NAME = "beep_name";
    public static final String BOARD_NAME = "board_name";
    public static final String BOARD_KEY = "board_key";


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
    public void onSaveNextButton(String beepname, String audiofile, Uri imageUri,
                                 String boardName, int boardKey) {
        Log.d(TAG, "Save button pushed.");
        ShareFragment shareFragment = new ShareFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        bundle.putString(BEEP_NAME, beepname);
        bundle.putString(IMAGE_FILE_URI_UNCOMPRESSED, imageUri.toString());
        bundle.putString(BOARD_NAME, boardName);
        bundle.putInt(BOARD_KEY, boardKey);
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

        Log.d(TAG, "mRecordFileName: " + mRecordFileName);

        if (savedInstanceState == null) {
            mRecordFileName =UUID.randomUUID().toString();
            Log.d(TAG, "savedInstanceState is null, filename: " + mRecordFileName);
            Bundle bundle = new Bundle();
            bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);

            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setArguments(bundle);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.record_container, recordFragment, RECORD_FRAGMENT_TAG);

            transaction.addToBackStack(RECORD_FRAGMENT_TAG);

            transaction.commit();
            supportPostponeEnterTransition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!mAudioState) {
//            Log.d(TAG, "onResume !mAudioState)");
//            setupAudio();
//        }
        startupAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shutdownAudio();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
    }

    private void playbackEndCallback() {
        Log.d(TAG, "Played file ended");
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            // No fragments on backstack - do nothing
        }
        else {
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Log.d(TAG, "fragment tag: " +tag );
            if (tag == SAVE_FRAGMENT_TAG) {
                // TODO - do something when playback ends in save fragment
            }
            else if (tag == RECORD_FRAGMENT_TAG) {
                RecordFragment recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_TAG);
                recordFragment.onPlaybackEnd();
            }
        }


    }
    private void onBufferCallback(float rmsValue) {
        //Log.d(TAG, "onBufferCallback from process");
        RecordFragment recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_TAG);
        recordFragment.onBufferCallback(rmsValue);
    }


    public native void setupAudio();

    public native void SuperpoweredAudio(int samplerate, int buffersize);
    public native void onPlayPause(String filepath, boolean play, int size);
    public native void onFileChange(String apkPath, int fileOffset, int fileLength );
    public native void toggleRecord(boolean record);

    public native void setRecordPath(String path);
    public native void shutdownAudio();
    private native void startupAudio();

    public native void createWav();

    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
