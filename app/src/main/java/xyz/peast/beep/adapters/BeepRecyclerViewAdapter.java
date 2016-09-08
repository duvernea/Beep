package xyz.peast.beep.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
        String beepImage = mCursor.getString(MainActivity.BEEPS_COL_IMAGE);
        holder.mBeepNameTextView.setText(beepName);
        if (beepImage == null || beepImage.equals("")) {
            // Do nothing, use the default imageview
        }
        else {
            String imageDir = mContext.getFilesDir().getAbsolutePath();
            String imagePath = "file:" + imageDir + "/" + beepImage;
            Log.d(TAG, "BeepAdapter image file " + imagePath);
            //Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            //holder.mBeepImageView.setImageBitmap(bitmap);
            Picasso.with(mContext).load(imagePath).into(holder.mBeepImageView);

        }

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
        public int getBeepKey() {
            int adapterPosition = getAdapterPosition();
            Log.d(TAG, "getBeep getAdapterPosition = " + adapterPosition);
            mCursor.moveToPosition(adapterPosition);
            int beepKey = mCursor.getInt(MainActivity.BEEPS_COL_BEEP_ID);
            Log.d(TAG, "getBeepKey = " + beepKey);

            return beepKey;
        }
    }

    public static interface BeepAdapterOnClickHandler {
        void onClick(BeepViewHolder vh);
    }
    public void swapCursor(Cursor newCursor) {
        Log.d(TAG, "swapCursor called");
            mCursor = newCursor;
            notifyDataSetChanged();
        // mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
    public Cursor getCursor() {
        return mCursor;
    }
}
