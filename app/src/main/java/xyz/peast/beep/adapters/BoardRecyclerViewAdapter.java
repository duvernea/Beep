package xyz.peast.beep.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.peast.beep.MainActivity;
import xyz.peast.beep.R;

/**
 * Created by duvernea on 6/25/16.
 */

public class BoardRecyclerViewAdapter extends RecyclerView.Adapter<BoardRecyclerViewAdapter.BoardViewHolder> {

    private static final String TAG = BoardRecyclerViewAdapter.class.getSimpleName();

    private Cursor mCursor;
    private Context mContext;

    public BoardRecyclerViewAdapter(Context context, Cursor cursor, int flags) {
        mCursor = cursor;
        mContext = context;
    }


    @Override
    public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.board_list_item, parent, false);
        BoardViewHolder viewHolder = new BoardViewHolder(view);
        Log.d(TAG, "onCreateViewHolder called");

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BoardViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called");
        mCursor.moveToPosition(position);
        String boardName = mCursor.getString(MainActivity.BOARDS_COL_NAME);
        String boardImage = mCursor.getString(MainActivity.BOARDS_COL_IMAGE);
        holder.mBoardNameTextView.setText(boardName);

        // TODO - set image

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        public final TextView mBoardNameTextView;
        public final ImageView mBoardImageView;

        public BoardViewHolder(View view) {
            super(view);
            mBoardNameTextView = (TextView) view.findViewById(R.id.board_name_textview);
            mBoardImageView = (ImageView) view.findViewById(R.id.board_imageview);
        }
    }
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
    public Cursor getCursor() {
        return mCursor;
    }

}
