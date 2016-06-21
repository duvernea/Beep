package xyz.peast.beep.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.data.BeepProvider;

/**
 * Created by duvernea on 6/20/16.
 */

public class TestUriMatcher extends AndroidTestCase {
    private static final Uri TEST_BEEP_DIR = BeepDbContract.BeepEntry.CONTENT_URI;
    private static final Uri TEST_BOARD_DIR = BeepDbContract.BoardEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = BeepProvider.buildUriMatcher();

        assertEquals("Error: The BEEP URI was matched incorrectly",
                testMatcher.match(TEST_BEEP_DIR), BeepProvider.BEEP);

        assertEquals("Error: The BOARD URI was matched incorrectly",
                testMatcher.match(TEST_BOARD_DIR), BeepProvider.BOARD);

    }

}
