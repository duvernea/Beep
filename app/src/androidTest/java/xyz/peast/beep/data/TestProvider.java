package xyz.peast.beep.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.test.AndroidTestCase;

/**
 * Created by duvernea on 6/20/16.
 */

public class TestProvider extends AndroidTestCase {
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                BeepProvider.class.getName());

        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: Provider registered with authority: " + providerInfo.authority +
                " instead of authority: " + BeepDbContract.CONTENT_AUTHORITY,
                    providerInfo.authority, BeepDbContract.CONTENT_AUTHORITY);
        }
        catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: Provider not registered at " + mContext.getPackageName(), false);
        }
    }
    public void testGetType() {
        String type = mContext.getContentResolver().getType(BeepDbContract.BeepEntry.CONTENT_URI);

        assertEquals("Error: the BeepEntry CONTENT_URI should return BeepEntry.CONTENT_TYPE",
                type, BeepDbContract.BeepEntry.CONTENT_TYPE);

        type = mContext.getContentResolver().getType(BeepDbContract.BoardEntry.CONTENT_URI);

        assertEquals("Error: the BoardEntry CONTENT_URI should return BoardEntry.CONTENT_TYPE",
                type, BeepDbContract.BoardEntry.CONTENT_TYPE);
    }

}
