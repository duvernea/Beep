package xyz.peast.beep;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by duvernea on 7/30/16.
 */
public class RecordActivity extends AppCompatActivity
        implements RecordFragment.RecordCallback, SaveFragment.SaveCallback,
                    EditFragment.NextCallback, FragmentManager.OnBackStackChangedListener {

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
    public static final String IMAGE_FILE_PATH_UNCOMPRESSED = "uncompressed_image_path";
    public static final String BEEP_NAME = "beep_name";
    public static final String BOARD_NAME = "board_name";
    public static final String BOARD_KEY = "board_key";
    public static final String BEEP_EDITED = "beep_edited";

    public static final String BEEP_FX_PARCELABLE = "beep_fx_parcelable";

    // Input intent extras
    public static final String BOARD_ORIGIN_KEY = "board_origin_key";
    private int mBoardOriginKey;

    // Audio
    private boolean mAudioState = false;
    private static String mRecordFileName;

    @Override
    public void onRecordNextButton() {
        EditFragment editFragment = new EditFragment();

        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        editFragment.setArguments(bundle);
        bundle.putInt(BOARD_ORIGIN_KEY, mBoardOriginKey);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.record_container, editFragment, EDIT_FRAGMENT_TAG);
        transaction.addToBackStack(EDIT_FRAGMENT_TAG);
        transaction.commit();
    }
    @Override
    public void onEditNextButton(BeepFx beepFx) {

        SaveFragment saveFragment = new SaveFragment();

        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        bundle.putInt(BOARD_ORIGIN_KEY, mBoardOriginKey);
        bundle.putParcelable(BEEP_FX_PARCELABLE, beepFx);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        saveFragment.setArguments(bundle);
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.record_container, saveFragment, SAVE_FRAGMENT_TAG);
        transaction.addToBackStack(SAVE_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onSaveNextButton(String beepname, String audiofile, Uri imageUri, String imageFilePath,
                                 String boardName, int boardKey, boolean beepEdited) {

        Log.d(TAG, "beepEdited: " + beepEdited);
        // Generate video
        String videoPath = Utility.getBeepVideoPath(mContext, beepname);
        // File videoFile = new File(videoPath);
        Log.d(TAG, "beep video path: " + videoPath);

//        String videoPath = context.getFilesDir().getAbsolutePath()
//                + File.separator + Constants.VIDEO_DIR
//                + File.separator + beepName + Constants.MP4_FILE_SUFFIX;
//        String videoPath2 = context.getFilesDir().getAbsolutePath();
//        try {
//            FFmpeg ffmpeg = new FFmpeg("");
//        } catch (IOException ioe) {
//            Log.d(TAG, "ffmpeg creation exception");
//            ioe.printStackTrace();
//        }
        FFmpeg ffmpeg = FFmpeg.getInstance(mContext);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            e.printStackTrace();
        }
        String cmd[] = new String[1];
        cmd[0] = "-version";
        String image = getFilesDir().getAbsolutePath() + File.separator + "img.jpg";
        String audio = getFilesDir().getAbsolutePath() + File.separator + "audio.wav";
        String output = getFilesDir().getAbsolutePath() + File.separator + "out.mp4";

        cmd[0] = "-loop 1 -i " + image + " -i " +  audio + "" +
                " -c:v libx264 -c:a aac -b:a 192k -shortest " + output;

        String cmd2[] = {"-loop", "1",
                "-i", image,
                "-i", audio,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-b:a", "192k",
                "-shortest", output};


        Log.d(TAG, "cmd: " + cmd2);
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd2, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onFailure message: " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess message: " + message);

                }
                @Override
                public void onFinish() {
                    Log.d(TAG, "onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }

        ShareFragment shareFragment = new ShareFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        bundle.putString(BEEP_NAME, beepname);
        if (imageUri != null) {
            bundle.putString(IMAGE_FILE_URI_UNCOMPRESSED, imageUri.toString());
        }
        if (imageFilePath != null) {
            bundle.putString(IMAGE_FILE_PATH_UNCOMPRESSED, imageFilePath);
        }
        bundle.putString(BOARD_NAME, boardName);
        bundle.putInt(BOARD_KEY, boardKey);
        bundle.putBoolean(BEEP_EDITED, beepEdited);
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
        Log.d(TAG, "onCreate run..");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        mContext = this;

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        // shouldDisplayHomeUp();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(BOARD_ORIGIN_KEY)) {
                mBoardOriginKey = extras.getInt(BOARD_ORIGIN_KEY);
            }
        }
        else {
            mBoardOriginKey = -1;
        }
        if (savedInstanceState != null) {
            mBoardOriginKey = savedInstanceState.getInt(BOARD_ORIGIN_KEY);
        }
        Log.d(TAG, "board origin key: " + mBoardOriginKey);

        if (savedInstanceState == null) {
            mRecordFileName = UUID.randomUUID().toString();
            Bundle bundle = new Bundle();
            bundle.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);

            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setArguments(bundle);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.record_container, recordFragment, RECORD_FRAGMENT_TAG);

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(RECORD_FILE_UNIQUE_NAME, mRecordFileName);
        outState.putInt(BOARD_ORIGIN_KEY, mBoardOriginKey);
    }

    // Callback from Native
    private void playbackEndCallback() {

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
        int backstackCount = getSupportFragmentManager().getBackStackEntryCount();
        Log.d(TAG, "onBackPressed backstackCount: " + backstackCount);
            if (backstackCount == 0) {
                displayDialog(backstackCount);
            }
            else if (backstackCount <= 2) {
                super.onBackPressed();
            }
            else if (backstackCount == 3) {
                displayDialog(backstackCount);
            }
    }

    @Override
    public void onBackStackChanged() {
        // shouldDisplayHomeUp();
    }
    public void shouldDisplayHomeUp(){
        //Enable Up button only if there are entries in the back stack
        // boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        // getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        int backstackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backstackCount == 0 || backstackCount == 3) {
            displayDialog(backstackCount);
        } else {
            getSupportFragmentManager().popBackStack();
        }
        //This method is called when the up button is pressed. Just the pop back stack.
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int backstackCount = getSupportFragmentManager().getBackStackEntryCount();
        return super.onOptionsItemSelected(item);
    }
    private void displayDialog(int backstackCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        if (backstackCount == 0) {
            builder.setMessage("Your work will be lost")
                    .setTitle("Cancel");
        } else if (backstackCount == 3) {
            builder.setMessage("Return to Main")
                    .setTitle("Cancel Sharing?");
        }
        // Add the buttons
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onBufferCallback(float rmsValue) {
        RecordFragment recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_TAG);
        recordFragment.onBufferCallback(rmsValue);
    }
    public native void setupAudio();
    public native void SuperpoweredAudio(int samplerate, int buffersize);
    public native void onPlayPause();
    public native void onFileChange(String apkPath, int fileOffset, int fileLength );
    public native void toggleRecord(boolean record);
    public native void setRecordPath(String path);
    public native void shutdownAudio();
    private native void startupAudio();
    public native void createWav(String filepath, BeepFx beepFx);
    public native void setPitchShift(int pitchShift);
    public native void setTempo(double tempo);
    public native void setReverse(boolean reverse);
    public native void setEcho(boolean echoSetting);
    public native void setTreble(float treble);
    public native void setBass(float bass);
    public native void setReverb(boolean reverb);
    public native void setRobot(boolean robot);
    public native void turnFxOff();



    static {
        System.loadLibrary(Constants.NATIVE_LIBRARY_NAME);
    }
}
