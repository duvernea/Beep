package xyz.peast.beep.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by duvernea on 6/7/16.
 */

public class DbHelper extends SQLiteOpenHelper {
    // If you change database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "beep.db";

    public DbHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Beep Table
        final String SQL_CREATE_BEEP_TABLE = "CREATE TABLE " + DbContract.BeepEntry.TABLE_NAME + " (" +
                DbContract.BeepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbContract.BeepEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DbContract.BeepEntry.COLUMN_IMAGE + " TEXT NOT NULL, " +
                DbContract.BeepEntry.COLUMN_AUDIO + " TEXT NOT NULL, " +
                DbContract.BeepEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                DbContract.BeepEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                DbContract.BeepEntry.COLUMN_PRIVACY + " BOOLEAN NOT NULL, " +
                DbContract.BeepEntry.COLUMN_PLAY_COUNT + " INTEGER NOT NULL, " +
                DbContract.BeepEntry.COLUMN_BOARD_KEY + " INTEGER NOT NULL, " +
                // Set up foreign key
                " FOREIGN KEY (" + DbContract.BeepEntry.COLUMN_BOARD_KEY + ") REFERENCES " +
                DbContract.BoardEntry.TABLE_NAME + " (" + DbContract.BoardEntry._ID + ");";

        db.execSQL(SQL_CREATE_BEEP_TABLE);






    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
