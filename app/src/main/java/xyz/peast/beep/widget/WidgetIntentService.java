package xyz.peast.beep.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import xyz.peast.beep.Constants;
import xyz.peast.beep.MainActivity;
import xyz.peast.beep.R;
import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duvernea on 10/17/16.
 */
public class WidgetIntentService extends IntentService {

    private static final String TAG = WidgetIntentService.class.getSimpleName();

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent, WidgetIntentService");



        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));

        Cursor boardsData = getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                null, null, null, null);
        boardsData.moveToFirst();
        Log.d(TAG, "board name test: " + boardsData.getString(Constants.BOARDS_COL_NAME));

        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch MainActivity
            Intent launchMainIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchMainIntent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.temp_textview, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
