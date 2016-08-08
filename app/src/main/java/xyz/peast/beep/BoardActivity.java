package xyz.peast.beep;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;

        Intent intent = getIntent();

        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);
        mBoardKey = intent.getIntExtra(MainActivity.BOARD_KEY_CLICKED, -1);
        Log.d(TAG, "mBoardKey" + mBoardKey);

        TextView textView = (TextView) findViewById(R.id.board_name_textview);

        textView.setText(boardName);

        getLoaderManager().initLoader(BEEPS_LOADER, null, this);
        mBeepAdapter = new BeepAdapter(mContext, null, 0);
        mBeepsGridView = (GridView) findViewById(R.id.beeps_gridview);
        mBeepsGridView.setAdapter(mBeepAdapter);

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
}
