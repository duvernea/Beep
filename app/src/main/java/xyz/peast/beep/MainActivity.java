package xyz.peast.beep;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import xyz.peast.beep.adapters.BeepAdapter;
import xyz.peast.beep.adapters.BoardRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.views.RecyclerViewEmptySupport;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;
    private Activity mActivity;

    // KEYs for onSaveInstanceState
    private static final String FAB_MENU_STATE = "fab_menu_state";
    // KEYs for Intent extras
    public static final String BOARD_KEY_CLICKED = "board_selected";
    public static final String BOARD_NAME_SELECTED = "board_name_selected";

    // Loader ids
    public static final int TOP_BEEPS_LOADER = 0;
    public static final int BOARDS_LOADER = 1;

    // Permission Request Code
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 5;

    // Views
    private BeepAdapter mBeepAdapter;
    private GridView mTopBeepsGridView;
    private RecyclerViewEmptySupport mBoardsRecyclerView;
    private BoardRecyclerViewAdapter mBoardsRecyclerViewAdapter;
    private FrameLayout mOverlay;
    private TextView mMainFabTextView;
    private TextView mAdditionalFabTextView;
    private FloatingActionButton mMainFab;
    private FloatingActionButton mAdditionalFab;
    // false = normal activity state. true = extra fab and overlay
    private boolean mFabMenuState = false;

    // Audio
    private boolean mAudioState = false;
    boolean mIsPlaying = false;
    String mSamplerateString = null;
    String mBuffersizeString = null;
    boolean mSupportRecording;

    // Shared Preferences
    SharedPreferences mSharedPrefs = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start tracking app with Google Analytics
        ((MyApplication) getApplication()).startTracking();

        mContext = this;
        mActivity = this;

        mSharedPrefs = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        Log.d(TAG, "msharedPrefs"+mSharedPrefs.getBoolean(Constants.SHARED_PREF_FIRST_RUN, true));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        // if first time run, add My Boards
        if (mSharedPrefs.getBoolean(Constants.SHARED_PREF_FIRST_RUN, true)) {
            InsertData.insertMyBeepsBoard(this);
        }

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set views
        mOverlay = (FrameLayout) findViewById(R.id.frame_overlay);
        mMainFab = (FloatingActionButton) findViewById(R.id.fab);
        mAdditionalFab = (FloatingActionButton) findViewById(R.id.fab2);
        mMainFabTextView = (TextView) findViewById(R.id.fab_textview_record_beep);
        mAdditionalFabTextView = (TextView) findViewById(R.id.fab_textview_create_board);

        // Set listeners for overlay and Fabs
        mOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonObject(100, false);
                mFabMenuState = false;
            }
        });
        View.OnClickListener mMainFabListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMenuState(mFabMenuState);
            }
        };
        mMainFab.setOnClickListener(mMainFabListener);
        mMainFabTextView.setOnClickListener(mMainFabListener);
        View.OnClickListener mAdditionalFabListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CreateBoardActivity.class);
                startActivity(intent);
            }
        };
        mAdditionalFab.setOnClickListener(mAdditionalFabListener);
        mAdditionalFabTextView.setOnClickListener(mAdditionalFabListener);

        // Initialize loaders for top beeps and boards
        getLoaderManager().initLoader(TOP_BEEPS_LOADER, null, this);
        getLoaderManager().initLoader(BOARDS_LOADER, null, this);

        // Top Beeps
        mBeepAdapter = new BeepAdapter(mContext, null, 0);
        mTopBeepsGridView = (GridView) findViewById(R.id.top_beeps_gridview);
        if (mTopBeepsGridView != null) {
            mTopBeepsGridView.setEmptyView(findViewById(R.id.empty_grid_view));
            mTopBeepsGridView.setAdapter(mBeepAdapter);
            mTopBeepsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    playBeep((Cursor) parent.getItemAtPosition(position));
                }
            });
        }

        mBoardsRecyclerViewAdapter = new BoardRecyclerViewAdapter(mContext,
            boardOnClickHandler, null, 0);

        mBoardsRecyclerView = (RecyclerViewEmptySupport) findViewById(R.id.boards_recyclerview);
        mBoardsRecyclerView.setEmptyView(findViewById(R.id.empty_board_recyclerview));
        mBoardsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBoardsRecyclerView.setAdapter(mBoardsRecyclerViewAdapter);
        mBoardsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        if (savedInstanceState != null) {
            mFabMenuState = savedInstanceState.getBoolean(FAB_MENU_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(TOP_BEEPS_LOADER, null, this);
        if (mSharedPrefs.getBoolean(Constants.SHARED_PREF_FIRST_RUN, true)) {
            // If first time run, set to false
            mSharedPrefs.edit().putBoolean(Constants.SHARED_PREF_FIRST_RUN, false).apply();
        }
        resetMenuState(mFabMenuState);

        // Setup audio if not set
        if (!mAudioState) {
            // Android M requires this for dangerous permissions (microphone)
            boolean permissionAudio = hasRecordAudioPermission();
            if (permissionAudio) {
                queryNativeAudioParameters();
                SuperpoweredAudio(Integer.parseInt(mSamplerateString), Integer.parseInt(mBuffersizeString));
                setupAudio();
                mAudioState = true;
            } else {
                requestRecordAudioPermission();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAudioState) {
            onPlayerPause();
            shutdownAudio();
            mAudioState = false;
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FAB_MENU_STATE, mFabMenuState);
        mFabMenuState = false;
    }
    private void queryNativeAudioParameters() {

        // Get/set the sample rate and buffer size, if possible from the device
        // Set to 44.1k samples/s and 512 frame buffer, if cannot get from device
        // A frame = sample size * channels (stereo with 16bit samples = 32 bits (4 bytes)/frame)
        // Get the device's sample rate and buffer size to enable low-latency Android audio output, if available.
        if (Build.VERSION.SDK_INT >= 17) {
            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            mSamplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            mBuffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }
        if (mSamplerateString == null) mSamplerateString = Constants.SAMPLE_RATE_DEFAULT;
        if (mBuffersizeString == null) mBuffersizeString = Constants.BUFFER_SIZE_DEFAULT;

        Log.d(TAG, "sampleRateString: " + mSamplerateString);
        Log.d(TAG, "buffersizeString: " + mBuffersizeString);
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
                    Constants.BEEP_COLUMNS,
                    null,
                    null,
                    BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT + " DESC LIMIT 3"
                    );
        }
        else if (id == BOARDS_LOADER) {
            uri = BeepDbContract.BoardEntry.CONTENT_URI_NUM_BEEPS;
            loader = new CursorLoader(mContext,
                    uri,
                    Constants.BOARD_COLUMNS_NUM_BEEPS,
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
            mBoardsRecyclerViewAdapter.swapCursor(null);
        }
    }
    // Callback from Native
    private void playbackEndCallback() {
        mIsPlaying = false;
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
        }
        else  {
            Intent intent = new Intent(mContext, RecordActivity.class);
            mFabMenuState = false;
            startActivity(intent);
        }
    }
    BoardRecyclerViewAdapter.BoardAdapterOnClickHandler boardOnClickHandler = new BoardRecyclerViewAdapter.BoardAdapterOnClickHandler() {
        @Override
        public void onClick(BoardRecyclerViewAdapter.BoardViewHolder vh) {
            Intent intent = new Intent(mContext, BoardActivity.class);
            intent.putExtra(BOARD_KEY_CLICKED, vh.getBoardKey());
            intent.putExtra(BOARD_NAME_SELECTED, vh.mBoardNameTextView.getText().toString());
            intent.putExtra(BoardActivity.LAST_ACTIVITY_UNIQUE_ID,BoardActivity.FROM_MAIN_ACTIVITY);
            mFabMenuState = false;
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mActivity, vh.mBoardImageView, getResources().getString(R.string.board_image_trans)).toBundle();
                startActivity(intent, bundle);
            }
            else {
                startActivity(intent);
            }
        }
    };

    public void animateButtonObject(int duration, final boolean directionUp) {
        float translationPx = getResources().getDimension(R.dimen.fab_size) + getResources().getDimension(R.dimen.fab_margin);

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
                else {
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
    private void playBeep(Cursor cursor) {

        String audioFileNameBase = cursor.getString(Constants.BEEPS_COL_AUDIO);
        int beepKey = cursor.getInt(Constants.BEEPS_COL_BEEP_ID);

        boolean beepEdited = cursor.getInt(Constants.BEEPS_COL_FX) > 0;
        Log.d(TAG, "Beep edited? " + beepEdited);
        // Increment play count by 1
        Utility.incrementBeepPlayCount(mContext, beepKey);

        String path = Utility.getFullWavPath(mContext, audioFileNameBase, beepEdited);

        onFileChange(path, 0, 0);
        mIsPlaying = !mIsPlaying;
        onPlayPause(path, mIsPlaying, 0);
    }

    private boolean hasRecordAudioPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "RECORD_AUDIO permission: " + hasPermission);
        return hasPermission;
    }
    private void requestRecordAudioPermission(){

        // The dangerous RECORD_AUDIO permission is NOT already granted.
        // Check if the user has been asked about this permission already and denied
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.RECORD_AUDIO)) {
                Log.d(TAG, "permission has previously been denied.  Explain why need");
                // TODO Show UI to explain to the user why we need to record audio
            }
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!mAudioState) {
                        queryNativeAudioParameters();
                        SuperpoweredAudio(Integer.parseInt(mSamplerateString), Integer.parseInt(mBuffersizeString));
                        setupAudio();
                        mAudioState = true;
                    }
                } else {
                    // Permission Denied
                    // TODO: What to do? Cannot use app
                    Toast.makeText(mContext, getResources().getString(R.string.need_audio_permission), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    // Native Audio - Load library and Functions
    private native void setupAudio();
    private native void shutdownAudio();
    private native void SuperpoweredAudio(int samplerate, int buffersize);
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onPlayerPause();
    private native void onFxSelect(int value);
    private native void onFxOff();
    private native void onFxValue(int value);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    static {
        System.loadLibrary(Constants.NATIVE_LIBRARY_NAME);
    }
}
