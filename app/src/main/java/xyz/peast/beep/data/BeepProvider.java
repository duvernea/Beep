package xyz.peast.beep.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import xyz.peast.beep.Constants;

/**
 * Created by duverneay on 6/15/16.
 */
public class BeepProvider extends ContentProvider {

    private static final String TAG = BeepProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    static final int BEEP = 100;
    static final int BOARD = 300;
    static final int BOARD_WITH_NUM_BEEPS = 301;

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
            case BOARD_WITH_NUM_BEEPS: {
                cursor = getBoardWithNumBeeps(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
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
            case BOARD_WITH_NUM_BEEPS:
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
            // TODO - this logic probably needs to be updated
            if (values.getAsInteger(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT) != null)
            {
                Log.d(TAG, "Play count updated");
            }
            else {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return numRows;
    }
    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BEEP, BEEP);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BOARD, BOARD);
        uriMatcher.addURI(BeepDbContract.CONTENT_AUTHORITY, BeepDbContract.PATH_BOARD + "/*", BOARD_WITH_NUM_BEEPS);

        return uriMatcher;
    }
    private Cursor getBoardWithNumBeeps(Uri uri, String[] projection, String selection,
                                        String[] selectionArgs, String sortOrder) {

        String boardTableName = BeepDbContract.BoardEntry.TABLE_NAME;
        String beepTableName = BeepDbContract.BeepEntry.TABLE_NAME;

        // Raw Query to get the boards + number of beeps associated with each board
        String queryString = "SELECT " + boardTableName + "." + "*," +
                " count(" + beepTableName + "."   + BeepDbContract.BoardEntry._ID +
                ") as " + Constants.COLUMN_NUMBER_OF_BEEPS + " from " + boardTableName +
                " left join " + beepTableName + " on (" +
                boardTableName + "." + BeepDbContract.BoardEntry._ID + " = "
                + beepTableName + "." + BeepDbContract.BeepEntry.COLUMN_BOARD_KEY + ") " +
                "group by " + boardTableName + "." + BeepDbContract.BoardEntry._ID;

        return mBeepDbHelper.getReadableDatabase().rawQuery(queryString, selectionArgs);
    }
}
