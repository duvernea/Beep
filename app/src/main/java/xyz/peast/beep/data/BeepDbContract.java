package xyz.peast.beep.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by duvernea on 6/7/16.
 */

public class BeepDbContract {

    // ContentProvider constants
    public static final String CONTENT_AUTHORITY = "xyz.peast.beep";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_BEEP = "beep";
    public static final String PATH_BOARD = "board";

    public static final class BeepEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BEEP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BEEP;

        public static final String TABLE_NAME = "beeps";

        // define columns
        // Name is stored as text representing the name of the board
        public static final String COLUMN_NAME = "name";
        // Image is stored as text representing the file name string, no extention
        public static final String COLUMN_IMAGE = "image";
        // Audio is stored as text representing the file name string, no extention
        public static final String COLUMN_AUDIO = "audio";
        // Latitude is stored as real number representing the location of beep creation
        public static final String COLUMN_COORD_LAT = "coord_lat";
        // Longitude is stored as real number representing the location of beep creation
        public static final String COLUMN_COORD_LONG = "coord_long";
        // Privacy is stored as a boolean - true for private, false for public
        public static final String COLUMN_PRIVACY = "privacy";
        //Play Count is stored as integer representing the number of times this beep has been played
        public static final String COLUMN_PLAY_COUNT = "play_count";
        //Date created in ms since epoch
        public static final String COLUMN_DATE_CREATED = "date_created";
        // Board is foreign key to the id of the board this beep belongs to.
        public static final String COLUMN_FX = "fx";
        public static final String COLUMN_BOARD_KEY = "board";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
    public static final class BoardEntry implements  BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOARD).build();
        public static final Uri CONTENT_URI_NUM_BEEPS = CONTENT_URI.buildUpon().appendPath("*").build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOARD;


        public static final String TABLE_NAME = "boards";

        // define columns
        // Name is stored as text representing the name of the board
        public static final String COLUMN_NAME = "name";
        // Image is stored as text representing the file name string, no extention
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_DATE_CREATED = "date_created";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
