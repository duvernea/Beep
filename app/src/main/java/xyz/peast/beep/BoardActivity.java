package xyz.peast.beep;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import xyz.peast.beep.adapters.BeepRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.CompressImageUpdateDbService;

public class BoardActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BoardActivity.class.getSimpleName();
    Context mContext;
    Activity mActivity;

    // Key from Intent (last activity)
    public static final String LAST_ACTIVITY_UNIQUE_ID = "Uniqid";
    // Values from Intent (last activity)
    public static final String FROM_SHARE_FRAGMENT = "From_ShareFragment";
    public static final String FROM_MAIN_ACTIVITY = "From_MainActivity";
    public static final String FROM_WIDGET = "From_Widget";
    public static final String FROM_CREATE_BOARD_ACTIVITY = "From_CreateBoardActivity";

    // Loader ids
    private static final int BEEPS_LOADER = 1;

    // Views
    private Button mRandomButton;
    private RecyclerView mBeepsRecyclerView;
    private BeepRecyclerViewAdapter mBeepsRecyclerViewAdapter = null;
    private TextView mBoardNameTextView;
    private FloatingActionButton mFab;
    private ImageView mBoardImage;

    private int mBoardKey;

    // Broadcast receiver for Board image save complete
    BroadcastReceiver mImageSavedBroadcastReceiver;

    // Audio
    private boolean mIsPlaying = false;
    private boolean mAudioState = false;

    private String mLastActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;
        mActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBoardImage = (ImageView) findViewById(R.id.board_imageview);
        mImageSavedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Utility.updateWidgets(mActivity);

                String imageFileName = intent.getStringExtra(CompressImageUpdateDbService.IMAGE_SAVED_MESSAGE);
                Log.d(TAG, "imageFIleName:" +imageFileName);
                if (imageFileName == null || imageFileName.equals("")) {
                    // Do nothing, use the default imageview
                }
                else {
                    String imageDir = mContext.getFilesDir().getAbsolutePath();
                    String imagePath = "file:" + imageDir + "/" + imageFileName;
                    Glide.with(mContext).load(imagePath).into(mBoardImage);
                }
            }
        };

        Intent intent = getIntent();
        mLastActivity = intent.getExtras().getString(LAST_ACTIVITY_UNIQUE_ID);
        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);
        mBoardKey = intent.getIntExtra(MainActivity.BOARD_KEY_CLICKED, -1);

        String whereClause = BeepDbContract.BoardEntry._ID+"=?";
        String [] whereArgs = {mBoardKey+""};

        Cursor cursor = getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                Constants.BOARD_COLUMNS,
                whereClause,
                whereArgs,
                null);
        cursor.moveToFirst();

        String imageFileName = cursor.getString(Constants.BOARDS_COL_IMAGE);

        if (imageFileName == null) {
            // Do nothing, use the default imageview
        }
        else {
            String imageDir = mContext.getFilesDir().getAbsolutePath();
            String imagePath = "file:" + imageDir + "/" + imageFileName;
            Glide.with(mContext).load(imagePath).into(mBoardImage);
        }

        // Assign views
        mBoardNameTextView = (TextView) findViewById(R.id.board_name_textview);
        mRandomButton = (Button) findViewById(R.id.random_beep_button);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

//        ObjectAnimator objAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.button_anim);
//        objAnim.setTarget(mRandomButton);
//        objAnim.start();

        mBoardNameTextView.setText(boardName);

        // Initialize loader for beeps
        getLoaderManager().initLoader(BEEPS_LOADER, null, this);

        mBeepsRecyclerViewAdapter = new BeepRecyclerViewAdapter(mContext,
                new BeepRecyclerViewAdapter.BeepAdapterOnClickHandler() {
                    @Override
                    public void onClick(BeepRecyclerViewAdapter.BeepViewHolder vh) {
                        Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                        cursor.moveToPosition(vh.getAdapterPosition());

                        String audioFileName = cursor.getString(Constants.BEEPS_COL_AUDIO);
                        int key = vh.getBeepKey();
                        int playCount = cursor.getInt(Constants.BEEPS_COL_PLAY_COUNT);
                        Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;

                        // increase playcount by 1
                        ContentValues values = new ContentValues();
                        values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount + 1);
                        String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                        String [] whereArgs = {key+""};
                        mContext.getContentResolver().update(
                                uri,
                                values,
                                whereClause,
                                whereArgs);
                        String recordDir = mContext.getFilesDir().getAbsolutePath();
                        String path = recordDir + "/" + audioFileName;

                        onFileChange(path, 0, 0);
                        mIsPlaying = !mIsPlaying;
                        onPlayPause(path, mIsPlaying, 0);
                    }
                }, null, 0);

        mBeepsRecyclerView = (RecyclerView) findViewById(R.id.beeps_recyclerview);
        mBeepsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mBeepsRecyclerView.setAdapter(mBeepsRecyclerViewAdapter);

        mRandomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                int numBeeps = cursor.getCount();
                if (numBeeps == 0) {
                    Toast.makeText(mContext, getResources().getString(R.string.no_beeps_yet_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                int randBeep = (int)(Math.random() * ((numBeeps - 1) + 1));
                cursor.moveToPosition(randBeep);

                int key = cursor.getInt((Constants.BEEPS_COL_BEEP_ID));
                int playCount = cursor.getInt(Constants.BEEPS_COL_PLAY_COUNT);
                Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;

                ContentValues values = new ContentValues();
                values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount + 1);
                String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                String [] whereArgs = {key+""};
                mContext.getContentResolver().update(
                        uri,
                        values,
                        whereClause,
                        whereArgs);

                String audioFileName = cursor.getString(Constants.BEEPS_COL_AUDIO);
                String recordDir = mContext.getFilesDir().getAbsolutePath();
                String path = recordDir + "/" + audioFileName;

                onFileChange(path, 0, 0);
                mIsPlaying = !mIsPlaying;
                onPlayPause(path, mIsPlaying, 0);
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, RecordActivity.class);
                startActivity(intent);            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mImageSavedBroadcastReceiver),
        new IntentFilter(CompressImageUpdateDbService.IMAGE_SAVED_MESSAGE));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mImageSavedBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPlayerPause();
        shutdownAudio();
        mAudioState = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAudioState) {
            setupAudio();
        }
        startupAudio();
    }

    // Callback from Native
    private void playbackEndCallback() {
        mIsPlaying = false;
    }
    @Override
    public void onBackPressed() {
        if (mLastActivity.equals(BoardActivity.FROM_MAIN_ACTIVITY)){
            supportFinishAfterTransition();
        }
        else if (mLastActivity.equals(BoardActivity.FROM_SHARE_FRAGMENT)
                || mLastActivity.equals(BoardActivity.FROM_WIDGET)
                || mLastActivity.equals(BoardActivity.FROM_CREATE_BOARD_ACTIVITY)) {
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        Uri uri;
        if (id == BEEPS_LOADER) {
            uri = BeepDbContract.BeepEntry.CONTENT_URI;
            String whereClause = BeepDbContract.BeepEntry.COLUMN_BOARD_KEY+"=?";
            String [] whereArgs = {mBoardKey+""};
            loader = new CursorLoader(mContext,
                    uri,
                    null,  // projection
                    whereClause,  // where clause
                    whereArgs,  // where clause value
                    null);  // sort order
        }
    else {
            loader = null;
        }
        // sort by top plays and only get the top 3
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == BEEPS_LOADER) {
             mBeepsRecyclerViewAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBeepsRecyclerViewAdapter.swapCursor(null);
    }
    // Native Audio - Load library and Functions
    private native void setupAudio();
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    private native void onPlayerPause();
    private native void shutdownAudio();
    private native void startupAudio();

    static {
        System.loadLibrary(Constants.NATIVE_LIBRARY_NAME);
    }
}
