package xyz.peast.beep;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by duverneay on 9/21/16.
 */
public class MyApplication extends Application {

    public Tracker mTracker;

    // Start analytics tracking
    public void startTracking() {

        if (mTracker != null) {
            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
            mTracker = ga.newTracker(R.xml.track_app);
            ga.enableAutoActivityReports(this);


        }
    }
    public Tracker getTracker() {

        startTracking();
        return mTracker;
    }

}
