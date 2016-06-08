package xyz.peast.beep.data;

import android.provider.BaseColumns;

/**
 * Created by duvernea on 6/7/16.
 */

public class dbContract {

    public static final class BeepEntry implements BaseColumns {
        public static final String TABLE_NAME = "beeps";
    }
    public static final class BoardEntry implements  BaseColumns {
        public static final String TABLE_NAME = "boards";
    }
}
