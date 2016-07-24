package xyz.peast.beep;

import android.content.res.Resources;

/**
 * Created by duverneay on 7/24/16.
 */
public class Utility {
    public static float dpToPx(float dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float pxToDp(float px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
