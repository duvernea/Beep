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
 * Created by duverneay on 9/2/16.
 */
public class BeepRecyclerViewAdapter extends RecyclerView.Adapter<BeepRecyclerViewAdapter.BeepViewHolder> {


    private static final String TAG = BeepRecyclerViewAdapter.class.getSimpleName();

    private Cursor mCursor;
    private Context mContext;

    final private BeepAdapterOnClickHandler mClickHandler;
    // final private View mEmptyView;

    public BeepRecyclerViewAdapter(Context context, BeepAdapterOnClickHandler dh, Cursor cursor, int flags) {
        mCursor = cursor;
        mContext = context;
        // mEmptyView = emptyview;
        mClickHandler = dh;
    }



    @Override
    public BeepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.beep_list_item, parent, false);
        BeepViewHolder viewHolder = new BeepViewHolder(view);
        Log.d(TAG, "onCreateViewHolder called");

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BeepViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called");
        mCursor.moveToPosition(position);
        String beepName = mCursor.getString(MainActivity.BEEPS_COL_NAME);
        String boardImage = mCursor.getString(MainActivity.BEEPS_COL_IMAGE);
        holder.mBeepNameTextView.setText(beepName);

        // TODO - set image

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    public class BeepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mBeepNameTextView;
        public final ImageView mBeepImageView;

        public BeepViewHolder(View view) {
            super(view);
            mBeepNameTextView = (TextView) view.findViewById(R.id.beep_name_textview);
            mBeepImageView = (ImageView) view.findViewById(R.id.beep_imageview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Log.d(TAG, "onClick getAdapterPosition = " + adapterPosition);
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(this);
        }
//        public int getBoardKey() {
//            int adapterPosition = getAdapterPosition();
//            Log.d(TAG, "getBoardKey getAdapterPosition = " + adapterPosition);
//            mCursor.moveToPosition(adapterPosition);
//            int boardKey = mCursor.getInt(MainActivity.BOARDS_BOARD_ID);
//            Log.d(TAG, "getBoardKey = " + boardKey);
//
//            return boardKey;
//        }
    }


    public static interface BeepAdapterOnClickHandler {
        void onClick(BeepViewHolder vh);
    }
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        // mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
    public Cursor getCursor() {
        return mCursor;
    }


}
