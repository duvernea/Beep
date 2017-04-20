package xyz.peast.beep;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Calendar;

import xyz.peast.beep.adapters.BeepRecyclerViewAdapter;
import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.CompressImageUpdateDbService;
import xyz.peast.beep.services.LoadDownsampledBitmapImageService;

public class BoardActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BoardActivity.class.getSimpleName();
    Context mContext;
    Activity mActivity;

    // Key from Intent (last activity)
    public static final String LAST_ACTIVITY_UNIQUE_ID = "Uniqid";
    // Values from Intent (last activity)
    public static final String FROM_SHARE_FRAGMENT = "From_ShareFragment";
    public static final String FROM_MAIN_ACTIVITY = "From_MainActivity";
    public static final String FROM_WIDGET = "From_Widget";
    public static final String FROM_CREATE_BOARD_ACTIVITY = "From_CreateBoardActivity";

    // Loader ids
    private static final int BEEPS_LOADER = 1;



    // Views
    private Button mRandomButton;
    private RecyclerView mBeepsRecyclerView;
    private BeepRecyclerViewAdapter mBeepsRecyclerViewAdapter = null;
    private TextView mBoardNameTextView;
    private FloatingActionButton mFab;
    private ImageView mBoardImage;

    private Uri mImageUri;
    private String mImagePath;
    private Handler mImageHandler;

    private int mBoardKey;
    private String mBeepMp3Path;

    // Permission Request Code
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL= 10;

    // Request Code for Photo Picker Intent
    private static final int SELECT_PHOTO = 1;
    // Request code, share beep intent
    private static final int SHARE_BEEP = 2;

    // Broadcast receiver for Board image save complete
    BroadcastReceiver mImageSavedBroadcastReceiver;

    // Audio
    private boolean mIsPlaying = false;
    private boolean mAudioState = false;

    private String mLastActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mContext = this;
        mActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        mBoardImage = (ImageView) findViewById(R.id.board_imageview);
        mImageSavedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Utility.updateWidgets(mActivity);

                String imageFileName = intent.getStringExtra(CompressImageUpdateDbService.IMAGE_SAVED_MESSAGE);
                Log.d(TAG, "imageFIleName:" +imageFileName);
                if (imageFileName == null || imageFileName.equals("")) {
                    // Do nothing, use the default imageview
                }
                else {
                    String imageDir = mContext.getFilesDir().getAbsolutePath();
                    String imagePath = "file:" + imageDir + "/" + imageFileName;
                    Glide.with(mContext).load(imagePath).into(mBoardImage);
                }
            }
        };

        Intent intent = getIntent();
        mLastActivity = intent.getExtras().getString(LAST_ACTIVITY_UNIQUE_ID);
        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);
        mBoardKey = intent.getIntExtra(MainActivity.BOARD_KEY_CLICKED, -1);

        String whereClause = BeepDbContract.BoardEntry._ID+"=?";
        String [] whereArgs = {mBoardKey+""};

        Cursor cursor = getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                Constants.BOARD_COLUMNS,
                whereClause,
                whereArgs,
                null);
        cursor.moveToFirst();

        String imageFileName = cursor.getString(Constants.BOARDS_COL_IMAGE);

        if (imageFileName == null) {
            // Do nothing, use the default imageview
        }
        else {
            String imageDir = mContext.getFilesDir().getAbsolutePath();
            String imagePath = "file:" + imageDir + "/" + imageFileName;
            Glide.with(mContext).load(imagePath).into(mBoardImage);
        }

        // Assign views
        mBoardNameTextView = (TextView) findViewById(R.id.board_name_textview);
        mRandomButton = (Button) findViewById(R.id.random_beep_button);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

//        ObjectAnimator objAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.button_anim);
//        objAnim.setTarget(mRandomButton);
//        objAnim.start();

        mBoardNameTextView.setText(boardName);

        // Initialize loader for beeps
        getLoaderManager().initLoader(BEEPS_LOADER, null, this);

        mBeepsRecyclerViewAdapter = new BeepRecyclerViewAdapter(mContext,
                new BeepRecyclerViewAdapter.BeepAdapterOnClickHandler() {
                    @Override
                    public void onClick(BeepRecyclerViewAdapter.BeepViewHolder vh) {
                        Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                        cursor.moveToPosition(vh.getAdapterPosition());

                        String audioFileName = cursor.getString(Constants.BEEPS_COL_AUDIO);
                        boolean beepEdited = cursor.getInt(Constants.BEEPS_COL_FX) > 0;

                        int key = vh.getBeepKey();

                        // Increase play count by 1
                        Utility.incrementBeepPlayCount(mContext, key);
                        String path = Utility.getFullWavPath(mContext, audioFileName, beepEdited);

                        onFileChange(path, 0, 0);
                        mIsPlaying = !mIsPlaying;
                        onPlayPause(path, mIsPlaying, 0);
                    }
                }, new BeepRecyclerViewAdapter.BeepAdapterOnLongClickHandler() {
            @Override
            public boolean onLongClick(BeepRecyclerViewAdapter.BeepViewHolder vh) {
                Log.d(TAG, "onLongClick");
                return true;
            }
        }, null, 0);

        mBeepsRecyclerView = (RecyclerView) findViewById(R.id.beeps_recyclerview);
        mBeepsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mBeepsRecyclerView.setAdapter(mBeepsRecyclerViewAdapter);
        registerForContextMenu(mBeepsRecyclerView);


        mRandomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
                int numBeeps = cursor.getCount();
                if (numBeeps == 0) {
                    Toast.makeText(mContext, getResources().getString(R.string.no_beeps_yet_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                int randBeep = (int)(Math.random() * ((numBeeps - 1) + 1));
                cursor.moveToPosition(randBeep);

                int key = cursor.getInt((Constants.BEEPS_COL_BEEP_ID));

                int playCount = cursor.getInt(Constants.BEEPS_COL_PLAY_COUNT);
                Uri uri = BeepDbContract.BeepEntry.CONTENT_URI;

                ContentValues values = new ContentValues();
                playCount = playCount +1;
                values.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, playCount);

                String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                String [] whereArgs = {key+""};
                int numRows = mContext.getContentResolver().update(
                        uri,
                        values,
                        whereClause,
                        whereArgs);
                getLoaderManager().restartLoader(BEEPS_LOADER, null, BoardActivity.this );
                
                boolean edited = cursor.getInt(Constants.BEEPS_COL_FX) > 0;
                String audioFileName = cursor.getString(Constants.BEEPS_COL_AUDIO);
                String path = Utility.getFullWavPath(mContext, audioFileName, edited);

                onFileChange(path, 0, 0);
                mIsPlaying = !mIsPlaying;
                onPlayPause(path, mIsPlaying, 0);
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, RecordActivity.class);
                intent.putExtra(RecordActivity.BOARD_ORIGIN_KEY, mBoardKey);
                startActivity(intent);            }
        });

        mBoardImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "long clicked on the beep board image");
                boolean permissionReadExternal = Utility.hasReadExternalPermission(mContext);

                if (permissionReadExternal) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);

                } else {
                    requestReadExternalPermission();
                }
                return true;
            }
        });
        mImageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                Bitmap bitmap = reply.getParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE);
                mBoardImage.setImageBitmap(bitmap);
            }
        };
    }
    // Callback after image selected in photo picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {

                    mImageUri = data.getData();
                    mImagePath = Utility.getRealPathFromURI(mContext, mImageUri);
                    loadImageView();


                    String whereClause = BeepDbContract.BeepEntry._ID+"=?";
                    String [] whereArgs = {mBoardKey+""};
                    // Delete old image if exists
                    Cursor cursor = mContext.getContentResolver().query(
                            BeepDbContract.BoardEntry.CONTENT_URI,
                            Constants.BOARD_COLUMNS,
                            whereClause,
                            whereArgs,
                            null);
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        String imageFileName = cursor.getString(Constants.BOARDS_COL_IMAGE);
                        Log.d(TAG, "current imageFile name: " + imageFileName);
                        if (imageFileName != null) {
                            // Delete the image file associated with this beep
                            String path = Utility.getFullImagePath(mContext, imageFileName);
                            File imageFile = new File(path);
                            mBoardImage.setImageResource(R.drawable.beep_item_temp);
                            boolean deleted = imageFile.delete();
                            Log.d(TAG, "image file deleted? - " + deleted);

                        }
                        Uri uri = BeepDbContract.BoardEntry.CONTENT_URI;
                        Log.d(TAG, "base uri: " + uri);
                        int key =  cursor.getInt(Constants.BOARDS_BOARD_ID);
                        uri = Uri.withAppendedPath(uri, Integer.toString(key));
                        Log.d(TAG, "build upon uri: " + uri);


                        Log.d(TAG, "Utility: Insert board into ContentProvider: " + uri.toString());
                        Intent serviceIntent = new Intent(mContext, CompressImageUpdateDbService.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.INSERTED_RECORD_URI, uri.toString());

                        bundle.putString(Constants.ORIGINAL_IMAGE_FILE_PATH, mImagePath);
                        // bundle.putBoolean(Constants.DELETE_TEMP_PIC, deleteTempPic);


                        serviceIntent.putExtra(Constants.DB_TABLE_ENUM, Constants.DbTable.BOARD);

                        serviceIntent.putExtras(bundle);
                        mContext.startService(serviceIntent);
                    }
                }
                break;
            case SHARE_BEEP:
                // Make sure the request was successful
                Log.d(TAG, "onActivityResult SHARE_BEEP");
                if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                    File file = new File(mBeepMp3Path);
                    boolean deleted = file.delete();
                }
                break;
        }
    }
    private void loadImageView() {
        int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);

        Intent intent = new Intent(mContext, LoadDownsampledBitmapImageService.class);
        intent.putExtra(Constants.IMAGE_MESSENGER, new Messenger(mImageHandler));
        intent.putExtra(Constants.IMAGE_MIN_SIZE, imageSize);
        intent.putExtra(Constants.ORIGINAL_IMAGE_FILE_PATH, mImagePath);
        mContext.startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mImageSavedBroadcastReceiver),
        new IntentFilter(CompressImageUpdateDbService.IMAGE_SAVED_MESSAGE));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mImageSavedBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPlayerPause();
        shutdownAudio();
        mAudioState = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAudioState) {
            setupAudio();
        }
        startupAudio();
    }

    // Callback from Native
    private void playbackEndCallback() {
        mIsPlaying = false;
    }
    @Override
    public void onBackPressed() {
        if (mLastActivity.equals(BoardActivity.FROM_MAIN_ACTIVITY)){
            supportFinishAfterTransition();
        }
        else if (mLastActivity.equals(BoardActivity.FROM_SHARE_FRAGMENT)
                || mLastActivity.equals(BoardActivity.FROM_WIDGET)
                || mLastActivity.equals(BoardActivity.FROM_CREATE_BOARD_ACTIVITY)) {
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        Uri uri;
        if (id == BEEPS_LOADER) {
            uri = BeepDbContract.BeepEntry.CONTENT_URI;
            String whereClause = BeepDbContract.BeepEntry.COLUMN_BOARD_KEY+"=?";
            String [] whereArgs = {mBoardKey+""};
            loader = new CursorLoader(mContext,
                    uri,
                    null,  // projection
                    whereClause,  // where clause
                    whereArgs,  // where clause value
                    null);  // sort order
        }
    else {
            loader = null;
        }
        // sort by top plays and only get the top 3
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == BEEPS_LOADER) {
             mBeepsRecyclerViewAdapter.swapCursor(data);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mBeepsRecyclerViewAdapter.getCursor();
        final int beepKey = cursor.getInt(Constants.BEEPS_COL_BEEP_ID);
        final String beepName = cursor.getString(Constants.BEEPS_COL_NAME);
        final boolean edited = cursor.getInt(Constants.BEEPS_COL_FX) > 0;
        final String audioFileBase = cursor.getString(Constants.BEEPS_COL_AUDIO);
        final String imageFileBase = cursor.getString(Constants.BEEPS_COL_IMAGE);
        Log.d(TAG, "key: " + beepKey + " name: " + beepName);

        Log.d(TAG, "onContextItemSelected");
        switch (item.getItemId()) {
            case R.id.delete_beep:
                Log.d(TAG, "Delete Beep");
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Beep")
                        .setMessage("Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // {audioUneditedDeleted, audioEditedDeleted, imageDeleted}
                                boolean[] filesDeletedSuccess = new boolean[3];
                                filesDeletedSuccess = Utility.deleteBeep(mContext, beepKey, audioFileBase, edited, imageFileBase);
                                Log.d(TAG, "Audio Unedited Deleted: " + filesDeletedSuccess[0]);
                                Log.d(TAG, "Audio Edited Deleted: " + filesDeletedSuccess[1]);
                                Log.d(TAG, "Image Deleted: " + filesDeletedSuccess[2]);

                                getLoaderManager().restartLoader(BEEPS_LOADER, null, BoardActivity.this );
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

                break;
            case R.id.edit_beep:
                Toast.makeText(mContext, "Edit beep not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_beep:
                mBeepMp3Path = Utility.getBeepPath(mContext, beepName);
                Uri fileUri = ShareUtility.encodeBeepGetUri(mContext, audioFileBase,
                        beepName, mBeepMp3Path, edited);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivityForResult (share, SHARE_BEEP);
                break;
        }

        return super.onContextItemSelected(item);
    }
    private void requestReadExternalPermission(){

        // The dangerous READ External permission is NOT already granted.
        // Check if the user has been asked about this permission already and denied
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.d(TAG, "permission has previously been denied.  Explain why need");
                // TODO Show UI to explain to the user why we need to read external
            }
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBeepsRecyclerViewAdapter.swapCursor(null);
    }
    // Native Audio - Load library and Functions
    private native void setupAudio();
    private native void onPlayPause(String filepath, boolean play, int size);
    private native void onFileChange(String apkPath, int fileOffset, int fileLength );
    private native void onPlayerPause();
    private native void shutdownAudio();
    private native void startupAudio();

    static {
        System.loadLibrary(Constants.NATIVE_LIBRARY_NAME);
    }
}
