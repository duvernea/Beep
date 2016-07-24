package xyz.peast.beep;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import xyz.peast.beep.adapters.BeepAdapter;
import xyz.peast.beep.adapters.BoardAdapter;
import xyz.peast.beep.adapters.BoardRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int TOP_BEEPS_LOADER = 0;
    public static final int BOARDS_LOADER = 1;

    boolean mIsPlaying = false;
    String mSamplerateString = null;
    String mBuffersizeString = null;
    boolean mSupportRecording;

    private Context mContext;
    private BeepAdapter mBeepAdapter;
    private GridView mTopBeepsGridView;

    private BoardAdapter mBoardAdapter;
    private ListView mBoardsListView;

    private RecyclerView mBoardsRecyclerView;
    private BoardRecyclerViewAdapter mBoardsRecyclerViewAdapter;

    private FrameLayout mOverlay;

    // false = normal activity. true = extra fab
    private FloatingActionButton mMainFab;
    private FloatingActionButton mAdditionalFab;
    private boolean mFabState = false;
    private TextView mMainFabTextView;
    private TextView mAdditionalFabTextView;

    // database projection for BEEPS
    private static final String[] BEEP_COLUMNS = {

            BeepDbContract.BeepEntry.TABLE_NAME + "." + BeepDbContract.BeepEntry._ID,
            BeepDbContract.BeepEntry.COLUMN_NAME,
            BeepDbContract.BeepEntry.COLUMN_IMAGE,
            BeepDbContract.BeepEntry.COLUMN_AUDIO,
            BeepDbContract.BeepEntry.COLUMN_COORD_LAT,
            BeepDbContract.BeepEntry.COLUMN_COORD_LONG,
            BeepDbContract.BeepEntry.COLUMN_PRIVACY,
            BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT,
            BeepDbContract.BeepEntry.COLUMN_DATE_CREATED,
            BeepDbContract.BeepEntry.COLUMN_BOARD_KEY
    };
    public static final int BEEPS_COL_BEEP_ID = 0;
    public static final int BEEPS_COL_NAME = 1;
    public static final int BEEPS_COL_IMAGE = 2;
    public static final int BEEPS_COL_AUDIO = 3;
    public static final int BEEPS_COL_COORD_LAT = 4;
    public static final int BEEPS_COL_COORD_LONG = 5;
    public static final int BEEPS_COL_PRIVACY = 6;
    public static final int BEEPS_COL_PLAY_COUNT = 7;
    public static final int BEEPS_COL_DATE_CREATED = 8;
    public static final int BEEPS_COL_BOARD_KEY = 9;

    // database projection for BOARDS
    private static final String[] BOARD_COLUMNS = {
            BeepDbContract.BoardEntry.TABLE_NAME + "." + BeepDbContract.BoardEntry._ID,
            BeepDbContract.BoardEntry.COLUMN_NAME,
            BeepDbContract.BoardEntry.COLUMN_IMAGE,
            BeepDbContract.BoardEntry.COLUMN_DATE_CREATED
    };
    public static final int BOARDS_BOARD_ID = 0;
    public static final int BOARDS_COL_NAME = 1;
    public static final int BOARDS_COL_IMAGE = 2;
    public static final int BOARD_COL_DATE = 3;

    public static final String TEMP_FILE_PATH = "file_path";
    private String mPath;


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

        mOverlay = (FrameLayout) findViewById(R.id.frame_overlay);

        mOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonObject(100, false);
                mFabState = false;

            }
        });

        mMainFab = (FloatingActionButton) findViewById(R.id.fab);
        mAdditionalFab = (FloatingActionButton) findViewById(R.id.fab2);
        mMainFabTextView = (TextView) findViewById(R.id.fab_textview_record_beep);
        mAdditionalFabTextView = (TextView) findViewById(R.id.fab_textview_create_board);

            mMainFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mFabState == false) {
                        mFabState = true;
                        mOverlay.setVisibility(View.VISIBLE);
                        mAdditionalFab.setVisibility(View.VISIBLE);
                        animateButtonObject(100, true);
                        return;
                    }
                    if (mFabState == true) {
                        Intent intent = new Intent(mContext, RecordActivity.class);
                        intent.putExtra(TEMP_FILE_PATH, mPath);
                        startActivity(intent);
                    }
                }
            });

        getLoaderManager().initLoader(TOP_BEEPS_LOADER, null, this);
        getLoaderManager().initLoader(BOARDS_LOADER, null, this);

        mBeepAdapter = new BeepAdapter(mContext, null, 0);
        mTopBeepsGridView = (GridView) findViewById(R.id.top_beeps_gridview);
        mTopBeepsGridView.setAdapter(mBeepAdapter);
        mTopBeepsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
                 Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                 String beepName = cursor.getString(MainActivity.BEEPS_COL_NAME);
                 String audiofileName = cursor.getString(MainActivity.BEEPS_COL_AUDIO);
//                 Log.d(TAG, audiofileName);
//                 Toast.makeText(getApplicationContext(),
//                         "Item Clicked: " + beepName, Toast.LENGTH_SHORT).show();

                 String path = "/data/data/xyz.peast.beep/files/" + audiofileName;

                 //String path = mContext.getFilesDir().getpath + audiofileName;

                 //Log.d(TAG, "file path: " + path);
                 //File file = new File(path);
                 //int size = (int) file.length();
                 //byte[] bytes = new byte[size];

//                 try {
//                     BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
//                     buf.read(bytes, 0, bytes.length);
//                     buf.close();
//                 } catch (FileNotFoundException e) {
//                     // TODO Auto-generated catch block
//                     e.printStackTrace();
//                 } catch (IOException e) {
//                     // TODO Auto-generated catch block
//                     e.printStackTrace();
//                 }
                 //onFileChange(getPackageResourcePath(), fileAlength, fileAoffset);
                 //Log.d(TAG, "Size: " + size);
                 //int offset = 16384;
                 onFileChange(path, 0, 0);
                 //Log.d(TAG, "getPackageResourcePath: " + getPackageResourcePath());
                 mIsPlaying = !mIsPlaying;
                 //onPlayPause(mIsPlaying);
                 Log.d(TAG, "mIsPlaying java: " + mIsPlaying);
                mPath = path;
                 onPlayPause(path, mIsPlaying, 0);
             }
        });

        mBoardAdapter = new BoardAdapter(mContext, null, 0);

        // TODO - set emptyView for recyclerview
        // View rootView = getLayoutinflater.inflate(....)
        // findViewById...
        // View emptyView =

        mBoardsRecyclerViewAdapter = new BoardRecyclerViewAdapter(mContext,
                new BoardRecyclerViewAdapter.BoardAdapterOnClickHandler() {
            @Override
            public void onClick(BoardRecyclerViewAdapter.BoardViewHolder vh) {
                Toast.makeText(mContext, "recyclerview clicked " + vh.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        }, null, 0);

        mBoardsRecyclerView = (RecyclerView) findViewById(R.id.boards_recyclerview);
        mBoardsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBoardsRecyclerView.setAdapter(mBoardsRecyclerViewAdapter);
        mBoardsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));


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

        AssetFileDescriptor fd0 = getResources().openRawResourceFd(R.raw.lycka), fd1 = getResources().openRawResourceFd(R.raw.king);

        int fileAoffset = (int)fd0.getStartOffset(), fileAlength = (int)fd0.getLength(), fileBoffset = (int)fd1.getStartOffset(), fileBlength = (int)fd1.getLength();
        try {
            fd0.getParcelFileDescriptor().close();
            fd1.getParcelFileDescriptor().close();
        } catch (IOException e) {
            android.util.Log.d("", "Close error.");
        }
        String uniqueID = UUID.randomUUID().toString();
        uniqueID += ".mp3";
        Log.d(TAG, "getresourcepath: " + getPackageResourcePath());
        InputStream in = mContext.getResources().openRawResource(R.raw.beep);
        OutputStream out = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int size = 0;
        byte[] buffer = new byte[1024];

        try {
            while ((size = in.read(buffer, 0, 1024)) >=0) {
                outputStream.write(buffer, 0, size);
            }
            in.close();
            buffer=outputStream.toByteArray();

            FileOutputStream fos = mContext.openFileOutput(uniqueID, Context.MODE_PRIVATE);
            fos.write(buffer);
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String path = mContext.getFilesDir().getPath() + uniqueID;
        //String path = "/data/data/xyz.peast.beep/files/" + uniqueID;
        // Arguments: path to the APK file, offset and length of the two resource files, sample rate, audio buffer size.
        SuperpoweredAudio(Integer.parseInt(mSamplerateString), Integer.parseInt(mBuffersizeString), getPackageResourcePath()+"xx");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUp();
    }

    public void SuperpoweredExample_PlayPause(View button) {  // Play/pause.

        mIsPlaying = !mIsPlaying;
        Log.d(TAG, "mIsPlaying java: " + mIsPlaying);
        //onPlayPause(mIsPlaying);

        //Button b = (Button) findViewById(R.id.playPause);
        //if (b != null) b.setText(mIsPlaying ? "Pause" : "Play");
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
        CursorLoader loader;
        Uri uri;
        if (id == TOP_BEEPS_LOADER) {
            uri = BeepDbContract.BeepEntry.CONTENT_URI;
            loader = new CursorLoader(mContext,
                    uri,
                    null,
                    null,
                    null,
                    BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT + " DESC LIMIT 3"
                    );
        }
        else if (id == BOARDS_LOADER) {
            uri = BeepDbContract.BoardEntry.CONTENT_URI;
            loader = new CursorLoader(mContext,
                    uri,
                    null,
                    null,
                    null,
                    null
            );
        }
        else {
            loader = null;
        }
        // sort by top plays and only get the top 3
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == TOP_BEEPS_LOADER) {
            mBeepAdapter.swapCursor(data);
            mBeepAdapter.notifyDataSetChanged();
        }
        if (loader.getId() == BOARDS_LOADER) {
            mBoardAdapter.swapCursor(data);
            Log.d(TAG, "# of boards in cursor: " + data.getCount());
            mBoardAdapter.notifyDataSetChanged();

            mBoardsRecyclerViewAdapter.swapCursor(data);
            mBoardsRecyclerViewAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == TOP_BEEPS_LOADER) {
            mBeepAdapter.swapCursor(null);
        }
        if (loader.getId() == BOARDS_LOADER) {
            mBoardAdapter.swapCursor(null);
            mBoardsRecyclerViewAdapter.swapCursor(null);
        }
    }

    private void playbackEndCallback() {
        //Toast.makeText(mContext, "Callback", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Played file ended");
        mIsPlaying = false;
        Log.d(TAG, "mIsPlaying: " + mIsPlaying);
    }

    private native void setUp();

    private native void SuperpoweredAudio(int samplerate, int buffersize, String apkPath);
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFxSelect(int value);
    private native void onFxOff();
    private native void onFxValue(int value);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    static {
        System.loadLibrary("SuperpoweredAudio");
    }
    public void animateButtonObject(int duration, final boolean directionUp) {
        // fab = 56dp, fab margin = 16dp
        float translationPx = getResources().getDimension(R.dimen.fab_size) + getResources().getDimension(R.dimen.fab_margin);
        //int translationDp = 56 + 16 * 2;
        //.d(TAG, "translationDp: " + translationDp);
        // translationPx = Utility.dpToPx(translationDp);
        //Log.d(TAG, "translationPx: " + translationPx);


        float translationPixels;
        if (directionUp) {
            translationPixels = -translationPx;
        }
        else {
            translationPixels = translationPx;
        }
        mAdditionalFab.clearAnimation();
        ObjectAnimator animFab2 = ObjectAnimator.ofFloat(mAdditionalFab, "translationY", translationPixels);
        animFab2.setStartDelay(100);
        animFab2.setDuration(duration);
        ObjectAnimator animFab2TextView = ObjectAnimator.ofFloat(mAdditionalFabTextView,
                "translationY",
                translationPixels);
        animFab2TextView.setStartDelay(100);
        animFab2TextView.setDuration(duration);
        animFab2.start();
        animFab2TextView.start();
        //mJokePunchlineTextView.setVisibility(View.VISIBLE);
        animFab2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //mButton.setText(getResources().getString(R.string.button_joke_done));
                //mPunchlineRevealed = true;
                if (!directionUp) {
                    mAdditionalFab.setVisibility(View.INVISIBLE);
                    mOverlay.setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }
}
