package xyz.peast.beep;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import xyz.peast.beep.adapters.BeepRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;

public class BoardActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BoardActivity.class.getSimpleName();
    Context mContext;

    public static final String LAST_ACTIVITY_UNIQUE_ID = "Uniqid";
    public static final String FROM_SHARE_FRAGMENT = "From_ShareFragment";
    public static final String FROM_MAIN_ACTIVITY = "From_MainActivity";

    // Loader ids
    private static final int BEEPS_LOADER = 1;

    // Views
    private Button mRandomButton;
    private RecyclerView mBeepsRecyclerView;
    private BeepRecyclerViewAdapter mBeepsRecyclerViewAdapter = null;
    private TextView mBoardNameTextView;

    private int mBoardKey;

    // Audio
    private boolean mIsPlaying = false;
    private boolean mAudioState = false;

    private String mLastActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mLastActivity = intent.getExtras().getString(LAST_ACTIVITY_UNIQUE_ID);
        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);
        mBoardKey = intent.getIntExtra(MainActivity.BOARD_KEY_CLICKED, -1);
        Log.d(TAG, "mBoardKey" + mBoardKey);

        // Assign views
        mBoardNameTextView = (TextView) findViewById(R.id.board_name_textview);
        mRandomButton = (Button) findViewById(R.id.random_beep_button);

//        ObjectAnimator objAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.button_anim);
//        objAnim.setTarget(mRandomButton);
//        objAnim.start();

        mBoardNameTextView.setText(boardName);

        // Initialize loader for beeps
        getLoaderManager().initLoader(BEEPS_LOADER, null, this);

        // TODO - set emptyView for recyclerview
        // View rootView = getLayoutinflater.inflate(....)
        // findViewById...
        // View emptyView =

        mBeepsRecyclerViewAdapter = new BeepRecyclerViewAdapter(mContext,
                new BeepRecyclerViewAdapter.BeepAdapterOnClickHandler() {
                    @Override
                    public void onClick(BeepRecyclerViewAdapter.BeepViewHolder vh) {
                        Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                        cursor.moveToPosition(vh.getAdapterPosition());
                        String beepName = cursor.getString(Constants.BEEPS_COL_NAME);
                        String audiofileName = cursor.getString(Constants.BEEPS_COL_AUDIO);
                        int key = vh.getBeepKey();
                        Log.d(TAG, "beep key pressed: " + key);
                        int playCount = cursor.getInt(Constants.BEEPS_COL_PLAY_COUNT);
                        Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;

                        ContentValues values = new ContentValues();
                        values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount + 1);
                        //                Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;
                        String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                        String [] whereArgs = {key+""};
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
                        onPlayPause(path, mIsPlaying, 0);
                    }
                }, null, 0);

        mBeepsRecyclerView = (RecyclerView) findViewById(R.id.beeps_recyclerview);
        mBeepsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mBeepsRecyclerView.setAdapter(mBeepsRecyclerViewAdapter);

        if (savedInstanceState == null) {
            mAudioState = false;
        }
        else {
            mAudioState = true;
        }
        mRandomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                int numBeeps = cursor.getCount();

                int randBeep = (int)(Math.random() * ((numBeeps - 1) + 1));
                cursor.moveToPosition(randBeep);

                int key = cursor.getInt((Constants.BEEPS_COL_BEEP_ID));
                int playCount = cursor.getInt(Constants.BEEPS_COL_PLAY_COUNT);
                Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;

                ContentValues values = new ContentValues();
                values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount + 1);
                //                Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;
                String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                String [] whereArgs = {key+""};
                Log.d(TAG, "playCount: " + playCount);
                mContext.getContentResolver().update(
                        uri,
                        values,
                        whereClause,
                        whereArgs);

                String audiofileName = cursor.getString(Constants.BEEPS_COL_AUDIO);
                String path = "/data/data/xyz.peast.beep/files/" + audiofileName;

                onFileChange(path, 0, 0);
                //Log.d(TAG, "getPackageResourcePath: " + getPackageResourcePath());
                mIsPlaying = !mIsPlaying;
                Log.d(TAG, "mIsPlaying java: " + mIsPlaying);
                onPlayPause(path, mIsPlaying, 0);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        onPlayerPause();
        shutdownAudio();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAudioState) {
            Log.d(TAG, "onResume !mAudioState)");
            setupAudio();
        }
        startupAudio();
    }

    private void playbackEndCallback() {
        //Toast.makeText(mContext, "Callback", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Played file ended");
        mIsPlaying = false;
        //Log.d(TAG, "mIsPlaying: " + mIsPlaying);
    }
    @Override
    public void onBackPressed() {
        Log.d(TAG, "mLastActivity: " + mLastActivity);
        if (mLastActivity.equals(BoardActivity.FROM_MAIN_ACTIVITY)){
            supportFinishAfterTransition();
        }
        else if (mLastActivity.equals(BoardActivity.FROM_SHARE_FRAGMENT)) {
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader run");
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

            Log.d(TAG, "onLoadFinished getCount: " + data.getCount());
            //if (mBeepsRecyclerViewAdapter.getCursor() == null) {
                mBeepsRecyclerViewAdapter.swapCursor(data);
            //}
            //else if (mBeepsRecyclerViewAdapter.getCursor().getCount() !=
                    //data.getCount()) {
                //mBeepsRecyclerViewAdapter.notifyDataSetChanged();
            //}
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if (loader.getId() == BEEPS_LOADER) {
        Log.d(TAG, "onLoaderReset run");
        mBeepsRecyclerViewAdapter.swapCursor(null);
        //}
    }
    // Native Audio - Load library and Functions
    private native void setupAudio();
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    private native void onPlayerPause();
    private native void shutdownAudio();
    private native void startupAudio();


    static {
        System.loadLibrary("SuperpoweredAudio");
    }
}
