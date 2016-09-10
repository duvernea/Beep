package xyz.peast.beep;

import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by BrianDuv on 9/8/2016.
 */
public class Constants {

    public static final String NATIVE_LIBRARY_NAME = "SuperpoweredAudio";

     static final String SHARED_PREF_FILE = "xyz.peast.beep";
    static final String SHARED_PREF_FIRST_RUN = "pref_first_run";

    // database projection for BEEPS
    public static final String[] BEEP_COLUMNS = {

            BeepDbContract.BeepEntry.TABLE_NAME + "." + BeepDbContract.BeepEntry._ID,
            BeepDbContract.BeepEntry.COLUMN_NAME,
            BeepDbContract.BeepEntry.COLUMN_IMAGE,
            BeepDbContract.BeepEntry.COLUMN_AUDIO,
            BeepDbContract.BeepEntry.COLUMN_COORD_LAT,
            BeepDbContract.BeepEntry.COLUMN_COORD_LONG,
            BeepDbContract.BeepEntry.COLUMN_PRIVACY,
            BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT,
            BeepDbContract.BeepEntry.COLUMN_DATE_CREATED,
            BeepDbContract.BeepEntry.COLUMN_BOARD_KEY
    };
    public static final int BEEPS_COL_BEEP_ID = 0;
    public static final int BEEPS_COL_NAME = 1;
    public static final int BEEPS_COL_IMAGE = 2;
    public static final int BEEPS_COL_AUDIO = 3;
    public static final int BEEPS_COL_COORD_LAT = 4;
    public static final int BEEPS_COL_COORD_LONG = 5;
    public static final int BEEPS_COL_PRIVACY = 6;
    public static final int BEEPS_COL_PLAY_COUNT = 7;
    public static final int BEEPS_COL_DATE_CREATED = 8;
    public static final int BEEPS_COL_BOARD_KEY = 9;

    // database projection for BOARDS
    public static final String[] BOARD_COLUMNS = {
            BeepDbContract.BoardEntry.TABLE_NAME + "." + BeepDbContract.BoardEntry._ID,
            BeepDbContract.BoardEntry.COLUMN_NAME,
            BeepDbContract.BoardEntry.COLUMN_IMAGE,
            BeepDbContract.BoardEntry.COLUMN_DATE_CREATED
    };
    public static final int BOARDS_BOARD_ID = 0;
    public static final int BOARDS_COL_NAME = 1;
    public static final int BOARDS_COL_IMAGE = 2;
    public static final int BOARD_COL_DATE = 3;
}
