package xyz.peast.beep.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by duvernea on 6/7/16.
 */

public class BeepDbHelper extends SQLiteOpenHelper {
    // If you change database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "beep.db";

    public BeepDbHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Beep Table
        final String SQL_CREATE_BEEP_TABLE = "CREATE TABLE " + BeepDbContract.BeepEntry.TABLE_NAME + " (" +
                BeepDbContract.BeepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BeepDbContract.BeepEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_IMAGE + " TEXT NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_AUDIO + " TEXT NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_COORD_LAT + " FLOAT NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_COORD_LONG + " FLOAT NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_PRIVACY + " BOOLEAN NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT + " INTEGER NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_DATE_CREATED + " INTEGER NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_BOARD_KEY + " INTEGER NOT NULL, " +
                // Set up foreign key
                " FOREIGN KEY (" + BeepDbContract.BeepEntry.COLUMN_BOARD_KEY + ") REFERENCES " +
                BeepDbContract.BoardEntry.TABLE_NAME + " (" + BeepDbContract.BoardEntry._ID + "));";

        db.execSQL(SQL_CREATE_BEEP_TABLE);

        // Board Table
        final String SQL_CREATE_BOARD_TABLE = "CREATE TABLE " + BeepDbContract.BoardEntry.TABLE_NAME + " (" +
                BeepDbContract.BoardEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BeepDbContract.BoardEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                BeepDbContract.BeepEntry.COLUMN_DATE_CREATED + " INTEGER NOT NULL, " +
                BeepDbContract.BoardEntry.COLUMN_IMAGE + " TEXT NOT NULL);";
        ;

        db.execSQL(SQL_CREATE_BOARD_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
