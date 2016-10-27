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

import com.bumptech.glide.Glide;

import xyz.peast.beep.Constants;
import xyz.peast.beep.MainActivity;
import xyz.peast.beep.R;

/**
 * Created by duvernea on 6/25/16.
 */

public class BoardRecyclerViewAdapter extends RecyclerView.Adapter<BoardRecyclerViewAdapter.BoardViewHolder> {

    private static final String TAG = BoardRecyclerViewAdapter.class.getSimpleName();

    private Cursor mCursor;
    private Context mContext;

    final private BoardAdapterOnClickHandler mClickHandler;

    public BoardRecyclerViewAdapter(Context context, BoardAdapterOnClickHandler dh, Cursor cursor, int flags) {
        mCursor = cursor;
        mContext = context;
        mClickHandler = dh;
    }

    @Override
    public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.board_list_item, parent, false);
        BoardViewHolder viewHolder = new BoardViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BoardViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String boardName = mCursor.getString(Constants.BOARDS_COL_NAME);
        String boardImage = mCursor.getString(Constants.BOARDS_COL_IMAGE);
        holder.mBoardNameTextView.setText(boardName);

        if (boardImage == null || boardImage.equals("")) {
            // Do nothing, use the default imageview
        }
        else {
            String imageDir = mContext.getFilesDir().getAbsolutePath();
            String imagePath = "file:" + imageDir + "/" + boardImage;
            Glide.with(mContext).load(imagePath).into(holder.mBoardImageView);
        }
    }
    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    public class BoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mBoardNameTextView;
        public final ImageView mBoardImageView;

        public BoardViewHolder(View view) {
            super(view);
            mBoardNameTextView = (TextView) view.findViewById(R.id.board_name_textview);
            mBoardImageView = (ImageView) view.findViewById(R.id.board_imageview);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(this);
        }
        public int getBoardKey() {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int boardKey = mCursor.getInt(Constants.BOARDS_BOARD_ID);
            return boardKey;
        }
    }
    public static interface BoardAdapterOnClickHandler {
        void onClick(BoardViewHolder vh);
    }
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
    public Cursor getCursor() {
        return mCursor;
    }

}
