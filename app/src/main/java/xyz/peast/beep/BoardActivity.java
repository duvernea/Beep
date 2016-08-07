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

    private static final int BEEPS_LOADER = 1;

    Context mContext;

    private BeepAdapter mBeepAdapter;
    private GridView mBeepsGridView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;

        Intent intent = getIntent();

        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);

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
            loader = new CursorLoader(mContext,
                    uri,
                    null,
                    null,
                    null,
                    null);
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
