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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;


import xyz.peast.beep.adapters.BeepAdapter;
import xyz.peast.beep.adapters.BeepRecyclerViewAdapter;
import xyz.peast.beep.adapters.BoardAdapter;
import xyz.peast.beep.adapters.BoardRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;

public class BoardActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BoardActivity.class.getSimpleName();

    private static final int BEEPS_LOADER = 1;

    Context mContext;

    private GridView mBeepsGridView;
    private Button mRandomButton;

    private int mBoardKey;

    private String mPath;
    private boolean mIsPlaying = false;
    private boolean mAudioState = false;

    private String mLastActivity;

    private BeepAdapter mBeepAdapter;

    private RecyclerView mBeepsRecyclerView;
    private BeepRecyclerViewAdapter mBeepsRecyclerViewAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mLastActivity = intent.getExtras().getString("Uniqid");


        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);
        mBoardKey = intent.getIntExtra(MainActivity.BOARD_KEY_CLICKED, -1);
        Log.d(TAG, "mBoardKey" + mBoardKey);

        TextView textView = (TextView) findViewById(R.id.board_name_textview);
        mRandomButton = (Button) findViewById(R.id.random_beep_button);
//        ObjectAnimator objAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.button_anim);
//        objAnim.setTarget(mRandomButton);
//        objAnim.start();

        textView.setText(boardName);

        getLoaderManager().initLoader(BEEPS_LOADER, null, this);

        mBeepAdapter = new BeepAdapter(mContext, null, 0);

        // TODO - set emptyView for recyclerview
        // View rootView = getLayoutinflater.inflate(....)
        // findViewById...
        // View emptyView =

        mBeepsRecyclerViewAdapter = new BeepRecyclerViewAdapter(mContext,
                new BeepRecyclerViewAdapter.BeepAdapterOnClickHandler() {
                    @Override
                    public void onClick(BeepRecyclerViewAdapter.BeepViewHolder vh) {
                        Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                        cursor.moveToPosition(vh.getPosition());
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
                        mPath = path;
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
                mPath = path;
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
//        Bundle bundle = new Bundle();
//        bundle.putBoolean(MainActivity.FIRST_TIME_RUN, false);
//        intent.putExtras(bundle);
//        startActivity(intent);
        Log.d(TAG, "mLastActivity: " + mLastActivity);
        if (mLastActivity.equals("From_MainActivity")){
            supportFinishAfterTransition();
        }
        else if (mLastActivity.equals("From_ShareFragment")) {
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
//
//        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
//                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
//                + Contacts.DISPLAY_NAME + " != '' ))";
//        return new CursorLoader(getActivity(), baseUri,
//                CONTACTS_SUMMARY_PROJECTION, select, null,
//                Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
//    }
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
