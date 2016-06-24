package xyz.peast.beep.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by duverneay on 6/15/16.
 */
public class BeepProvider extends ContentProvider {

    private static final String TAG = BeepProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    static final int BEEP = 100;
    static final int BOARD = 300;

    private BeepDbHelper mBeepDbHelper;

    @Override
    public boolean onCreate() {
        mBeepDbHelper = new BeepDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch(sUriMatcher.match(uri)) {
            case BEEP:
                cursor = mBeepDbHelper.getReadableDatabase().query(
                        BeepDbContract.BeepEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOARD:
                cursor = mBeepDbHelper.getReadableDatabase().query(
                        BeepDbContract.BoardEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case BEEP:
                return BeepDbContract.BeepEntry.CONTENT_TYPE;
            case BOARD:
                return BeepDbContract.BoardEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        long _id;
        switch (sUriMatcher.match(uri)) {

            case BEEP:
                _id = mBeepDbHelper.getWritableDatabase().insert(BeepDbContract.BeepEntry.TABLE_NAME,
                        null, values);
                if (_id > 0) {
                    returnUri = BeepDbContract.BeepEntry.buildUri(_id);
                    Log.d(TAG, "Insert return uri: " + returnUri.toString());
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case BOARD:
                _id = mBeepDbHelper.getWritableDatabase().insert(BeepDbContract.BoardEntry.TABLE_NAME,
                        null, values);
                if (_id >0) {
                    returnUri = BeepDbContract.BoardEntry.buildUri(_id);
                    Log.d(TAG, "Insert return uri: " + returnUri.toString());
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRows;

        switch (sUriMatcher.match(uri)) {
            case BEEP:
                numRows = mBeepDbHelper.getWritableDatabase().delete(BeepDbContract.BeepEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOARD:
                numRows = mBeepDbHelper.getWritableDatabase().delete(BeepDbContract.BoardEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRows;
        switch (sUriMatcher.match(uri)) {
            case BEEP:
                numRows = mBeepDbHelper.getWritableDatabase().update(BeepDbContract.BeepEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BOARD:
                numRows = mBeepDbHelper.getWritableDatabase().update(BeepDbContract.BoardEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }
    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BEEP, BEEP);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BOARD, BOARD);

        return uriMatcher;
    }

}
