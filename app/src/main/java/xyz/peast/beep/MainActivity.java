package xyz.peast.beep;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
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

import xyz.peast.beep.adapters.BeepAdapter;
import xyz.peast.beep.adapters.BoardAdapter;
import xyz.peast.beep.adapters.BoardRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String FAB_MENU_STATE = "fab_menu_state";
    protected static final String BOARD_KEY_CLICKED = "board_selected";
    protected static final String BOARD_NAME_SELECTED = "board_name_selected";

    public static final int TOP_BEEPS_LOADER = 0;
    public static final int BOARDS_LOADER = 1;

    boolean mIsPlaying = false;
    String mSamplerateString = null;
    String mBuffersizeString = null;
    boolean mSupportRecording;

    private Context mContext;
    private Activity mActivity;
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
    private boolean mFabMenuState = false;
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

    private boolean mAudioState = false;

    public static final String FIRST_TIME_RUN = "first_run";
    public boolean firstTimeRun = true;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mActivity = this;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            firstTimeRun = getIntent().getExtras().getBoolean(FIRST_TIME_RUN);
        }

        // Delete all old data. Insert mock data.
        if (savedInstanceState == null) {
            // if returning from BoardActivity
            if (firstTimeRun) {
                InsertData.insertData(this);
                InsertData.insertSoundFile(this);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOverlay = (FrameLayout) findViewById(R.id.frame_overlay);
        mMainFab = (FloatingActionButton) findViewById(R.id.fab);
        mAdditionalFab = (FloatingActionButton) findViewById(R.id.fab2);
        mMainFabTextView = (TextView) findViewById(R.id.fab_textview_record_beep);
        mAdditionalFabTextView = (TextView) findViewById(R.id.fab_textview_create_board);


        mOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonObject(100, false);
                mFabMenuState = false;
            }
        });


        mMainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMenuState(mFabMenuState);
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
                int beepKey = cursor.getInt(MainActivity.BEEPS_COL_BEEP_ID);

                int playCount = cursor.getInt(MainActivity.BEEPS_COL_PLAY_COUNT);
                Log.d(TAG, "current play count: " + playCount);

                ContentValues values = new ContentValues();
                values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount + 1);

                Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;
                String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                String [] whereArgs = {beepKey+""};
                mContext.getContentResolver().update(
                        uri,
                        values,
                        whereClause,
                        whereArgs);


                String path = "/data/data/xyz.peast.beep/files/" + audiofileName;

                onFileChange(path, 0, 0);
                //Log.d(TAG, "getPackageResourcePath: " + getPackageResourcePath());
                mIsPlaying = !mIsPlaying;
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
                        //Toast.makeText(mContext, "recyclerview clicked " + vh.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, BoardActivity.class);
                        intent.putExtra(BOARD_KEY_CLICKED, vh.getBoardKey());
                        intent.putExtra(BOARD_NAME_SELECTED, vh.mBoardNameTextView.getText().toString());
                        mFabMenuState = false;
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    mActivity, vh.mBoardImageView, getResources().getString(R.string.board_image_trans)).toBundle();
                            startActivity(intent, bundle);
                        }
                        else {
                            startActivity(intent);
                        }
//
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

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate savedINstanceState = null");

        }
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate savedINstanceState != null");

            mFabMenuState = savedInstanceState.getBoolean(FAB_MENU_STATE);
        }
        if (savedInstanceState == null) {
            mAudioState = false;
        }
        else {
            mAudioState = true;
        }
        Log.d(TAG, "internal content uri: " + MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume run");
        super.onResume();
        resetMenuState(mFabMenuState);

        if (!mAudioState) {
            queryNativeAudioParameters();
            Log.d(TAG, "sampleRateString: " + mSamplerateString);
            Log.d(TAG, "buffersizeString: " + mBuffersizeString);
            SuperpoweredAudio(Integer.parseInt(mSamplerateString), Integer.parseInt(mBuffersizeString));
            setupAudio();
        }
        // Back button from other activity / etc - reset the menu state
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPlayerPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FAB_MENU_STATE, mFabMenuState);
        mFabMenuState = false;
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
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
    }

    private void resetMenuState(boolean fabstate) {
        if (!fabstate) {
            mOverlay.setVisibility(View.INVISIBLE);
            mAdditionalFab.setVisibility(View.INVISIBLE);
            mMainFab.setImageResource(R.drawable.ic_add_white_24dp);
        }
        else {
            mOverlay.setVisibility(View.VISIBLE);
            mAdditionalFab.setVisibility(View.VISIBLE);
            animateButtonObject(0, true);
        }
    }

    private void setMenuState(boolean fabMenuState) {
        if (!fabMenuState) {
            mFabMenuState = true;
            mOverlay.setVisibility(View.VISIBLE);
            mAdditionalFab.setVisibility(View.VISIBLE);
            animateButtonObject(50, true);
            return;
        }
        if (fabMenuState) {
            Intent intent = new Intent(mContext, RecordActivity.class);
            intent.putExtra(TEMP_FILE_PATH, mPath);
            mFabMenuState = false;
            startActivity(intent);
        }
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
        animFab2.setStartDelay(50);
        animFab2.setDuration(duration);
        ObjectAnimator animFab2TextView = ObjectAnimator.ofFloat(mAdditionalFabTextView,
                "translationY",
                translationPixels);
        animFab2TextView.setStartDelay(50);
        animFab2TextView.setDuration(duration);
        animFab2.start();
        animFab2TextView.start();
        //mJokePunchlineTextView.setVisibility(View.VISIBLE);
        animFab2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //mButton.setText(getResources().getString(R.string.button_joke_done));
                //mPunchlineRevealed = true;
                if (directionUp) {
                    mMainFab.setImageResource(R.drawable.ic_keyboard_voice_white_24dp);
                }
                else if (!directionUp) {
                    mAdditionalFab.setVisibility(View.INVISIBLE);
                    mOverlay.setVisibility(View.INVISIBLE);
                    mMainFab.setImageResource(R.drawable.ic_add_white_24dp);
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


    // Native Audio - Load library and Functions
    private native void setupAudio();
    private native void SuperpoweredAudio(int samplerate, int buffersize);
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onPlayerPause();
    private native void onFxSelect(int value);
    private native void onFxOff();
    private native void onFxValue(int value);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
