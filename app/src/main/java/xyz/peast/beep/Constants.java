package xyz.peast.beep;

import android.content.Intent;
import android.os.Messenger;

import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.LoadDownsampledBitmapImageService;

/**
 * Created by BrianDuv on 9/8/2016.
 */
public class Constants {

    // Parameters to send to createWAV function
    public static final int CHIPMUNK_PITCH = 1;
    public static final int SLOMO_PITCH = 2;
    public static final int NORMAL_PITCH = 3;

    public enum DbTable {
        BEEP, BOARD
    }

    // *********** SERVICES ************

    // LoadDownsampledBitmapImageService - In Keys
    public static final String IMAGE_MESSENGER = "image_messenger";
    public static final String IMAGE_MIN_SIZE = "image_size";
    public static final String ORIGINAL_IMAGE_FILE_PATH = "original_image_file_path";
    // LoadDownsampledBitmapImageService - Out Key for message
    public static final String IMAGE_BITMAP_FROM_SERVICE = "image_bitmap";

    // Utility, Image, etc constants
    // KEYs for Service Intent extras

    public static final String INSERTED_RECORD_URI = "beep_uri";

    public static final String EDITED_FILE_SUFFIX = "_edit";
    public static final String WAV_FILE_SUFFIX = ".wav";

    // key for the two table types - used a Extras on Intent
    public static String DB_TABLE_ENUM = "db_table_enum";

    // Bundle extras
    public static final String WAV_FILE_PATH = "record_file_name";
    public static final String BEEP_NAME = "beep_name";
    public static final String BEEP_EDITED = "beep_edited";

    // Audio Parameters Default - Sample Rate and Buffer Size
    public static final String SAMPLE_RATE_DEFAULT = "44100";
    public static final String BUFFER_SIZE_DEFAULT = "512";

    public static final String NATIVE_LIBRARY_NAME = "SuperpoweredAudio";

    // Shared Preferences constants
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
            BeepDbContract.BeepEntry.COLUMN_FX,
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
    public static final int BEEPS_COL_FX = 9;
    public static final int BEEPS_COL_BOARD_KEY = 10;

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
