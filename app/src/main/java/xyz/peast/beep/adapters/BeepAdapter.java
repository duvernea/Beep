package xyz.peast.beep.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 * Created by duverneay on 6/24/16.
 */
public class BeepAdapter extends CursorAdapter {

    private static final String TAG = BeepAdapter.class.getSimpleName();

    //private Context mContext;
    private TextView mBeepTextView;

    public BeepAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.beep_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // get and set data for this view
        String beepName = cursor.getString(MainActivity.BEEPS_COL_NAME);
        String beepImage = cursor.getString(MainActivity.BEEPS_COL_IMAGE);
        Log.d(TAG, "beepImage: " + beepImage);

        viewHolder.mBeepNameTextView.setText(beepName);
        if (beepImage == null || beepImage.equals("")) {
            // Do nothing, use the default imageview
        }
        else {
            String imageDir = context.getFilesDir().getAbsolutePath();
            String imagePath = imageDir + "/" + beepImage;
            Log.d(TAG, "BeepAdapter image file" + imagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            viewHolder.mBeepImageView.setImageBitmap(bitmap);
        }

        // TODO - set image

    }
    public static class ViewHolder {
        public final TextView mBeepNameTextView;
        public final ImageView mBeepImageView;

        public ViewHolder(View view) {
            mBeepNameTextView = (TextView) view.findViewById(R.id.beep_name_textview);
            mBeepImageView = (ImageView) view.findViewById(R.id.beep_imageview);
        }
    }
}
