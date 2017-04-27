package xyz.peast.beep.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import xyz.peast.beep.Constants;
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
    final private BeepAdapterOnLongClickHandler mLongClickHandler;

    public BeepRecyclerViewAdapter(Context context, BeepAdapterOnClickHandler clickHandler, BeepAdapterOnLongClickHandler longClickHandler, Cursor cursor, int flags) {
        mCursor = cursor;
        mContext = context;
        mClickHandler = clickHandler;
        mLongClickHandler = longClickHandler;
    }
    @Override
    public BeepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.beep_list_item, parent, false);
        BeepViewHolder viewHolder = new BeepViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(BeepViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String beepName = mCursor.getString(Constants.BEEPS_COL_NAME);
        String beepImage = mCursor.getString(Constants.BEEPS_COL_IMAGE);
        holder.mBeepNameTextView.setText(beepName);
        if (beepImage == null || beepImage.equals("")) {
            // Do nothing, use the default imageview
            holder.mBeepImageView.setImageResource(R.drawable.beep_item_temp);
        }
        else {
            String imageDir = mContext.getFilesDir().getAbsolutePath();
            String imagePath = "file:" + imageDir + "/" + beepImage;
            holder.mBeepImageView.setImageDrawable (null);
            Glide.with(mContext).load(imagePath).into(holder.mBeepImageView);
        }
    }
    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    public class BeepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener, View.OnCreateContextMenuListener {
        public final TextView mBeepNameTextView;
        public final ImageView mBeepImageView;

        public BeepViewHolder(View view) {
            super(view);
            mBeepNameTextView = (TextView) view.findViewById(R.id.beep_name_textview);
            mBeepImageView = (ImageView) view.findViewById(R.id.beep_imageview);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(this);
        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            boolean result = mLongClickHandler.onLongClick(this);
            v.showContextMenu();
            return result;
        }

        public int getBeepKey() {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int beepKey = mCursor.getInt(Constants.BEEPS_COL_BEEP_ID);
            return beepKey;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Log.d(TAG, "onCreateContextMenu");
                //menu.add(0, v.getId(), 0, "Something");
            //super.onCreateContextMenu(menu, v, menuInfo);

            MenuInflater menuInflater = ((Activity) mContext).getMenuInflater();
            menuInflater.inflate(R.menu.beep_context_menu, menu);

        }
    }
    public static interface BeepAdapterOnClickHandler {
        void onClick(BeepViewHolder vh);
    }
    public static interface BeepAdapterOnLongClickHandler {
        boolean onLongClick(BeepViewHolder vh);
    }
    public void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
    }
    public Cursor getCursor() {
        return mCursor;
    }




}
