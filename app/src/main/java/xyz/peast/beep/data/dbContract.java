package xyz.peast.beep.data;

import android.provider.BaseColumns;

/**
 * Created by duvernea on 6/7/16.
 */

public class DbContract {

    public static final class BeepEntry implements BaseColumns {
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
        // Board is foreign key to the id of the board this beep belongs to.
        public static final String COLUMN_BOARD = "board";

    }
    public static final class BoardEntry implements  BaseColumns {

        public static final String TABLE_NAME = "boards";

        // define columns
        // Name is stored as text representing the name of the board
        public static final String COLUMN_NAME = "name";
        // Image is stored as text representing the file name string, no extention
        public static final String COLUMN_IMAGE = "image";
    }
}
