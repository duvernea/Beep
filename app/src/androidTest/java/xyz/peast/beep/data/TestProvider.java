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

}
