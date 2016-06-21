package xyz.peast.beep.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by duverneay on 6/15/16.
 */
public class BeepProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();


    static final int BEEP = 100;
    static final int BOARD = 300;
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BEEP, BEEP);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BOARD, BOARD);

        return uriMatcher;
    }

}
