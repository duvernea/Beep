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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
