package xyz.peast.beep;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;

import xyz.peast.beep.adapters.BeepAdapter;
import xyz.peast.beep.data.BeepDbContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int TOP_BEEPS_LOADER = 0;

    boolean playing = false;
    String mSamplerateString = null;
    String mBuffersizeString = null;
    boolean mSupportRecording;

    private Context mContext;
    private BeepAdapter mBeepAdapter;
    private GridView mTopBeepsGridView;

    // database projection
    private static final String[] BEEP_COLUMNS = {

            BeepDbContract.BeepEntry.TABLE_NAME + "." + BeepDbContract.BeepEntry._ID,
            BeepDbContract.BeepEntry.COLUMN_NAME,
            BeepDbContract.BeepEntry.COLUMN_IMAGE,
            BeepDbContract.BeepEntry.COLUMN_AUDIO,
            BeepDbContract.BeepEntry.COLUMN_COORD_LAT,
            BeepDbContract.BeepEntry.COLUMN_COORD_LONG,
            BeepDbContract.BeepEntry.COLUMN_PRIVACY,
            BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT,
            BeepDbContract.BeepEntry.COLUMN_BOARD_KEY
    };
    public static final int COL_BEEP_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_IMAGE = 2;
    public static final int COL_AUDIO = 3;
    public static final int COL_COORD_LAT = 4;
    public static final int COL_COORD_LONG = 5;
    public static final int COL_PRIVACY = 6;
    public static final int COL_PLAY_COUNT = 7;
    public static final int COL_BOARD_KEY = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // Delete all old data. Insert mock data.
        InsertData.insertData(this);
        InsertData.insertSoundFile(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getLoaderManager().initLoader(TOP_BEEPS_LOADER, null, this);

        mBeepAdapter = new BeepAdapter(mContext, null, 0);
        mTopBeepsGridView = (GridView) findViewById(R.id.top_beeps_gridview);
        mTopBeepsGridView.setAdapter(mBeepAdapter);
        mTopBeepsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
                 Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                 String beepName = cursor.getString(MainActivity.COL_NAME);
                 String audiofileName = cursor.getString(MainActivity.COL_AUDIO);
                 Toast.makeText(getApplicationContext(),
                         "Item Clicked: " + beepName, Toast.LENGTH_SHORT).show();
                 AssetFileDescriptor fd0 = getResources().openRawResourceFd(R.raw.king);
                 int fileAoffset = (int)fd0.getStartOffset();
                 int fileAlength = (int)fd0.getLength();
                 try {
                     fd0.getParcelFileDescriptor().close();
                 } catch (IOException e) {
                     android.util.Log.d("", "Close error.");
                 }
                 onFileChange(getPackageResourcePath(), fileAlength, fileAoffset);
                 playing = !playing;
                 //onPlayPause(playing);
                onPlayPause(true);
             }
        });

        // Get the sample rate and buffer size, if possible from the device
        // Set to 44.1k samples/s and 512 frame buffer
        // A frame = sample size * channels (stereo with 16bit samples = 32 bits (4 bytes)/frame)
        // Get the device's sample rate and buffer size to enable low-latency Android audio output, if available.
        // get the min buffer size
        // check if recording is possible
        queryNativeAudioParameters();
        Log.d(TAG, "sampleRateString: " + mSamplerateString);
        Log.d(TAG, "buffersizeString: " + mBuffersizeString);

        // NOTE: This is temp code that will be deleted

        // Files under res/raw are not zipped, just copied into the APK. Get the offset and length to know where our files are located.
        AssetFileDescriptor fd0 = getResources().openRawResourceFd(R.raw.lycka), fd1 = getResources().openRawResourceFd(R.raw.nuyorica);
        int fileAoffset = (int)fd0.getStartOffset(), fileAlength = (int)fd0.getLength(), fileBoffset = (int)fd1.getStartOffset(), fileBlength = (int)fd1.getLength();
        try {
            fd0.getParcelFileDescriptor().close();
            fd1.getParcelFileDescriptor().close();
        } catch (IOException e) {
            android.util.Log.d("", "Close error.");
        }

        // Arguments: path to the APK file, offset and length of the two resource files, sample rate, audio buffer size.
        SuperpoweredExample(Integer.parseInt(mSamplerateString), Integer.parseInt(mBuffersizeString), getPackageResourcePath(), fileAoffset, fileAlength, fileBoffset, fileBlength);


    }
    public void SuperpoweredExample_PlayPause(View button) {  // Play/pause.
        playing = !playing;
        onPlayPause(playing);
        //Button b = (Button) findViewById(R.id.playPause);
        //if (b != null) b.setText(playing ? "Pause" : "Play");
    }
    private void queryNativeAudioParameters() {


        if (Build.VERSION.SDK_INT >= 17) {
            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            mSamplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            mBuffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }
        if (mSamplerateString == null) mSamplerateString = "44100";
        if (mBuffersizeString == null) mBuffersizeString = "512";

        int recBufSize = AudioRecord.getMinBufferSize(
                Integer.parseInt(mSamplerateString),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mSupportRecording = true;
        if (recBufSize == AudioRecord.ERROR ||
                recBufSize == AudioRecord.ERROR_BAD_VALUE) {
            mSupportRecording = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;
        // sort by top plays and only get the top 3
        return new android.content.CursorLoader(mContext, uri, null, null, null, BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT + " DESC LIMIT 3");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBeepAdapter.swapCursor(data);
        mBeepAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBeepAdapter.swapCursor(null);
    }
    private native void SuperpoweredExample(int samplerate, int buffersize, String apkPath, int fileAoffset, int fileAlength, int fileBoffset, int fileBlength);
    private native void onPlayPause(boolean play);
    private native void onCrossfader(int value);
    private native void onFxSelect(int value);
    private native void onFxOff();
    private native void onFxValue(int value);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    static {
        System.loadLibrary("SuperpoweredExample");
    }
}
