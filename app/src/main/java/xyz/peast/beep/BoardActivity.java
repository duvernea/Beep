package xyz.peast.beep;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import xyz.peast.beep.adapters.BeepAdapter;
import xyz.peast.beep.data.BeepDbContract;

public class BoardActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BoardActivity.class.getSimpleName();

    private static final int BEEPS_LOADER = 1;

    Context mContext;

    private BeepAdapter mBeepAdapter;
    private GridView mBeepsGridView;

    private int mBoardKey;

    private String mPath;
    private boolean mIsPlaying = false;
    private boolean mAudioState = false;

    private String mLastActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;

        Intent intent = getIntent();
        mLastActivity = intent.getExtras().getString("Uniqid");


        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);
        mBoardKey = intent.getIntExtra(MainActivity.BOARD_KEY_CLICKED, -1);
        Log.d(TAG, "mBoardKey" + mBoardKey);

        TextView textView = (TextView) findViewById(R.id.board_name_textview);

        textView.setText(boardName);

        getLoaderManager().initLoader(BEEPS_LOADER, null, this);
        mBeepAdapter = new BeepAdapter(mContext, null, 0);
        mBeepsGridView = (GridView) findViewById(R.id.beeps_gridview);
        mBeepsGridView.setAdapter(mBeepAdapter);
        mBeepsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        if (savedInstanceState == null) {
            mAudioState = false;
        }
        else {
            mAudioState = true;
        }

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
    public void onBackPressed()
    {
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
            mBeepAdapter.swapCursor(data);
            mBeepAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == BEEPS_LOADER) {
            mBeepAdapter.swapCursor(null);
        }
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
