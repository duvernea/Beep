package xyz.peast.beep.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.ExecutionException;

import xyz.peast.beep.BoardActivity;
import xyz.peast.beep.Constants;
import xyz.peast.beep.MainActivity;
import xyz.peast.beep.R;
import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duvernea on 10/17/16.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private static final String TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {


        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                Log.d(TAG, "onCreate called..");


            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                        Constants.BOARD_COLUMNS, null, null, null);
            }


            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail);
                //TODO set empty view
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(TAG, "getViewAt called..position=" + position);

                if (position == AdapterView.INVALID_POSITION ||
                        data == null ||
                        !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.board_list_item_widget);
                data.moveToPosition(position);
                String boardName = data.getString(Constants.BOARDS_COL_NAME);
                String imageFileName = data.getString(Constants.BOARDS_COL_IMAGE);
                Log.d(TAG, "boardName widget: " + boardName);

                views.setTextViewText(R.id.board_name_textview, boardName);

                Bitmap boardImageBitmap = null;
                String imageDir = getFilesDir().getAbsolutePath();
                String imagePath = "file:" + imageDir + "/" + imageFileName;
                Log.d(TAG, "Board image file " + imagePath);
                try {
                    boardImageBitmap = Glide.with(DetailWidgetRemoteViewsService.this).
                            load(imagePath).asBitmap().
                            into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                }
                catch (InterruptedException | ExecutionException e) {
                    Log.d(TAG, "Error retrieving image from " + imagePath, e);
                }

                if (boardImageBitmap != null && imageFileName != null) {
                    views.setImageViewBitmap(R.id.board_imageview, boardImageBitmap);
                }

                  final Intent fillInIntent = new Intent();
                  fillInIntent.putExtra(MainActivity.BOARD_KEY_CLICKED, (int)getItemId(position));
                  fillInIntent.putExtra(MainActivity.BOARD_NAME_SELECTED, boardName);
                  fillInIntent.putExtra(BoardActivity.LAST_ACTIVITY_UNIQUE_ID,BoardActivity.FROM_WIDGET);
                views.setOnClickFillInIntent(R.id.board_list_item, fillInIntent);

                return views;

            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.board_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    long boardId = data.getLong(Constants.BOARDS_BOARD_ID);
                    Log.d(TAG, "boardId: " + boardId);
                    return boardId;
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };

    }
}
