package xyz.peast.beep;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by duvernea on 7/30/16.
 */
public class RecordActivity extends AppCompatActivity
        implements RecordFragment.RecordCallback, SaveFragment.SaveCallback {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private Context mContext;

    // Fragment TAGs
    private static final String RECORD_FRAGMENT_TAG = "record_fragment_tag";
    private static final String EDIT_FRAGMENT_TAG = "edit_fragment_tag";
    private static final String SAVE_FRAGMENT_TAG = "save_fragment_tag";
    private static final String SHARE_FRAGMENT_TAG = "share_fragment_tag";

    // Intent extra arguments
    public static final String RECORD_FILE_UNIQUE_NAME = "record_file_name";
    public static final String IMAGE_FILE_UNIQUE_NAME = "image_file_name";
    public static final String IMAGE_FILE_URI_UNCOMPRESSED = "uncompressed_image_uri";
    public static final String BEEP_NAME = "beep_name";
    public static final String BOARD_NAME = "board_name";
    public static final String BOARD_KEY = "board_key";

    private static String mRecordFileName;



    // Audio
    private boolean mAudioState = false;

    @Override
    public void onRecordNextButton() {
        Log.d(TAG, "Next button pushed.");
        //SaveFragment saveFragment = new SaveFragment();
        EditFragment editFragment = new EditFragment();

        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        editFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.record_container, editFragment, SAVE_FRAGMENT_TAG);
        transaction.addToBackStack(EDIT_FRAGMENT_TAG);
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
        if (imageUri != null) {
            bundle.putString(IMAGE_FILE_URI_UNCOMPRESSED, imageUri.toString());
        }
        bundle.putString(BOARD_NAME, boardName);
        bundle.putInt(BOARD_KEY, boardKey);
        shareFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.record_container, shareFragment, SHARE_FRAGMENT_TAG);
        transaction.addToBackStack(SHARE_FRAGMENT_TAG);
        transaction.commit();
        Toast.makeText(mContext, "Beep Saved", Toast.LENGTH_SHORT ).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        if (savedInstanceState == null) {
            mRecordFileName = UUID.randomUUID().toString();
            Bundle bundle = new Bundle();
            bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);

            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setArguments(bundle);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.record_container, recordFragment, RECORD_FRAGMENT_TAG);

            //transaction.addToBackStack(RECORD_FRAGMENT_TAG);
            transaction.commit();
            supportPostponeEnterTransition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAudioState) {
            setupAudio();
            mAudioState = true;
        }
        startupAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shutdownAudio();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
    }

    // Calback from Native
    private void playbackEndCallback() {

        Log.d(TAG, "playbackEndCallback");
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            // No fragments on backstack - Only 1 fragment = Record Fragment
            RecordFragment recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_TAG);
            recordFragment.onPlaybackEnd();
        }
        else {
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            if (tag.equals(SAVE_FRAGMENT_TAG)) {
                // TODO - do something when playback ends in save fragment
            }
            else if (tag.equals(RECORD_FRAGMENT_TAG)) {

            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "backstack count: " + getSupportFragmentManager().getBackStackEntryCount());

        int backstackCount = getSupportFragmentManager().getBackStackEntryCount();
        // Record and Save Fragments
        if (backstackCount <= 1) {
            super.onBackPressed();
        }
        if (backstackCount == 2) {
            finish();
        }
//        String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
//        if (tag.equals(SHARE_FRAGMENT_TAG)) {
//            finish();
//        }
//        else {
//            super.onBackPressed();
//        }
    }

    private void onBufferCallback(float rmsValue) {
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
    public native void setPitchShift(int pitchShift);
    public native void setReverse(boolean reverse);

    static {
        System.loadLibrary(Constants.NATIVE_LIBRARY_NAME);
    }




}
