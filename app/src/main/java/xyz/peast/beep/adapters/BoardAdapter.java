package xyz.peast.beep.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.peast.beep.MainActivity;
import xyz.peast.beep.R;


/**
 * Created by duvernea on 6/25/16.
 */

public class BoardAdapter extends CursorAdapter {
    private static final String TAG = BoardAdapter.class.getSimpleName();

    public BoardAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.board_list_item, parent, false);
        BoardAdapter.ViewHolder viewHolder = new BoardAdapter.ViewHolder(view);
        view.setTag(viewHolder);
        Log.d(TAG, "new view created");
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        BoardAdapter.ViewHolder viewHolder = (BoardAdapter.ViewHolder) view.getTag();

        // get and set data for this view
        String boardName = cursor.getString(MainActivity.BOARDS_COL_NAME);
        String boardImage = cursor.getString(MainActivity.BOARDS_COL_IMAGE);

        viewHolder.mBoardNameTextView.setText(boardName);

        // TODO - set image

    }
    public static class ViewHolder {
        public final TextView mBoardNameTextView;
        public final ImageView mBoardImageView;

        public ViewHolder(View view) {
            mBoardNameTextView = (TextView) view.findViewById(R.id.board_name_textview);
            mBoardImageView = (ImageView) view.findViewById(R.id.board_imageview);
        }
    }


}
