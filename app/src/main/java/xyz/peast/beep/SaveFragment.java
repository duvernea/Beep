package xyz.peast.beep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import xyz.peast.beep.adapters.Board;
import xyz.peast.beep.adapters.BoardSpinnerAdapter;
import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.LoadDownsampledBitmapImageService;

/**
 * Created by duvernea on 7/30/16.
 */
public class SaveFragment extends Fragment implements LocationListener {

    private static final String TAG = SaveFragment.class.getSimpleName();
    private Context mContext;
    private Activity mActivity;

    // Permission Request Code
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL= 10;

    // KEYs for onSaveInstanceState
    private static final String IMAGE_FILE_NAME = "image_file_name";
    private static final String IMAGE_FILE_URI = "image_file_uri";
    private static final String IMAGE_FILE_PATH = "image_file_path";
    private static final String DELETE_TEMP_PIC = "delete_temp_pic";

    // Request Code for Photo Picker Intent
    private static final int SELECT_PHOTO = 1;

    // Views
    private Spinner mBoardSpinner;
    private ImageView mBeepImage;
    private Button mSaveButton;
    private Button mDeleteButton;
    private EditText mBeepNameEditText;
    private Button mReplayButton;
    private AdView mAdView;
    // Spinner for Board selection and creation
    private ArrayList<Board> mSpinnerItems;
    private BoardSpinnerAdapter mBoardSpinnerAdapter;

    // Selected Image in Picker - Uri and Path
    private Uri mImageUri = null;
    private String mImagePath = null;

    // Camera image intent variables
    private String mCameraTempImagePath;
    private Uri mCameraIntentOutputFileUri;
    private boolean mDeleteTempPic = false;

    // Location variables
    private LocationManager mLocationManager;
    private Location mMostRecentLocation;

    // Image loading
    Handler mImageHandler;

    // Beep Fx class
    BeepFx mBeepFx;

    // Audio variables
    private boolean mIsPlaying;

    private String mRecordFileName;
    private int mNumberOfBoards;

    private int mBoardOriginKey;

    // SaveCallback interface - implemented in RecordActivity
    public interface SaveCallback{
        public void onSaveNextButton(String beepName, String audioFile, Uri imageUri, String imageFilePath,
                                     String boardname, int boardkey, boolean beepEdited);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContext = getActivity();
        mActivity = getActivity();

        View rootView = inflater.inflate(R.layout.fragment_save, container, false);

        // Record File Unique Name is generated in RecordActivity and passed into the Fragment
        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME);
        mBoardOriginKey = bundle.getInt(RecordActivity.BOARD_ORIGIN_KEY);
        mBeepFx = (BeepFx) bundle.getParcelable(RecordActivity.BEEP_FX_PARCELABLE);
        Log.d(TAG, "beep Fx Echo: " + mBeepFx.getEcho());
        Log.d(TAG, "beep Fx Pitch shift: " + mBeepFx.getmPitchShift());
        Log.d(TAG, "mBoardOriginKey: " + mBoardOriginKey);

        // Get Location manager, so that GPS coordinates can be saved
        mLocationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);

        // Initialize views
        mBoardSpinner = (Spinner) rootView.findViewById(R.id.board_name_spinner);
        mBeepImage = (ImageView) rootView.findViewById(R.id.beep_image);
        mSaveButton = (Button) rootView.findViewById(R.id.save_button);
        mBeepNameEditText = (EditText) rootView.findViewById(R.id.beep_name_edittext);
        mReplayButton = (Button) rootView.findViewById(R.id.replay_button);
        mDeleteButton = (Button) rootView.findViewById(R.id.delete_button);

        // Initilize Ads
        mAdView = (AdView) rootView.findViewById(R.id.adview);
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        // EditText for setting Beep Name - onClick opens Dialog for entering text
        mBeepNameEditText.setClickable(true);
        mBeepNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBeepNameDialog();
            }
        });
        // Spinner for setting Board Name - onItemSelected
        mBoardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Spinner Item Selected");
                if (position == mNumberOfBoards) {
                    createBoardNameDialog();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // BeepImage selected, launch the photo picker
        mBeepImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean permissionReadExternal = Utility.hasReadExternalPermission(mContext);
                Log.d(TAG, "hasRecordAudioPermission: " + permissionReadExternal);

                if (permissionReadExternal) {
                    openImageIntent();
                } else {
                    requestReadExternalPermission();
                }
            }
        });

        // Save Button onClick - run the Callback in RecordActivity
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String beepName = mBeepNameEditText.getText().toString().trim();

                if(beepName.isEmpty()) {
                    String toastMsg = getResources().getString(R.string.no_beep_name_entered_msg);
                    Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                insertContent();
                int spinnerSelectedItemPosition  = mBoardSpinner.getSelectedItemPosition();
                Board selected = mSpinnerItems.get(spinnerSelectedItemPosition);
                int selectedKey = selected.getKey();
                String boardname = selected.getName();
                ((RecordActivity) mActivity).setPitchShift(0);

                Log.d(TAG, "mbeepfx edit status: " + mBeepFx.getEditStatus());

                ((SaveCallback) getActivity()).onSaveNextButton(beepName,
                        mRecordFileName,
                        mImageUri,
                        mImagePath,
                        boardname,
                        selectedKey,
                        mBeepFx.getEditStatus()
                );
            }
        });
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
                Intent intent = new Intent(mActivity, RecordActivity.class);
                startActivity(intent);
            }
        });
        // Populate spinner with board data from database
        getAndPopulateBoardData();

        // Audio File Name path
        final String filePath = Utility.getFullWavPath(mContext, mRecordFileName, false);

        // Replay audio button
        mReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlaying = true;
                ((RecordActivity) mActivity).onFileChange(filePath, 0, 0);
                ((RecordActivity) mActivity).onPlayPause(filePath, mIsPlaying, 0);
            }
        });
        mImageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                Bitmap bitmap = reply.getParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE);
                mBeepImage.setImageBitmap(bitmap);
            }
        };
        // Set the Image Uri, Path, and restore bitmap if previous state saved
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(IMAGE_FILE_URI)) {
                mImageUri = Uri.parse(savedInstanceState.getString(IMAGE_FILE_URI));
            }
            if (savedInstanceState.containsKey(IMAGE_FILE_PATH)) {
                mImagePath = savedInstanceState.getString(IMAGE_FILE_PATH);
            }
            if (savedInstanceState.containsKey(DELETE_TEMP_PIC)) {
                mDeleteTempPic = savedInstanceState.getBoolean(DELETE_TEMP_PIC);
            }
        }
        if (mImagePath != null) {
            // Downsample and display bitmap
            loadImageView();
        }

        return rootView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_PHOTO:
                    // Camera - new image taken
                    // data is returned as null, but file saved at specified location
                    if (data == null) {

                        Log.d(TAG, "onActivityResult: data = null");
                        mImageUri = mCameraIntentOutputFileUri;
                        Log.d(TAG, "mImageUri: " + mImageUri);
                        mImagePath = mCameraTempImagePath;
                        Log.d(TAG, "mImagePath: " + mImagePath);
                        mDeleteTempPic = true;
                    }
                    // Image selected from existing photos
                    else {
                        Log.d(TAG, "onActivityResult: data != null");
                        Log.d(TAG, "data.getAction(): " + data.getAction());
                        mImageUri = data.getData();
                        Log.d(TAG, "mImageUri: " + mImageUri);
                        mImagePath = Utility.getRealPathFromURI(mContext, mImageUri);
                        Log.d(TAG, "mImagePath: " + mImagePath);
                        mCameraIntentOutputFileUri = null;
                        mCameraTempImagePath = null;
                        mDeleteTempPic = false;
                    }
                    loadImageView();
            }
        }
    }
    // Insert beep into database
    void insertContent() {
        String beepName = mBeepNameEditText.getText().toString();
        int spinnerSelectedItemPosition  = mBoardSpinner.getSelectedItemPosition();
        Board selected = mSpinnerItems.get(spinnerSelectedItemPosition);
        int selectedKey = selected.getKey();
        // TODO - beep edited should not be set hard true
        boolean beepEdited = mBeepFx.getEditStatus();
        if (beepEdited) {
            // Note here the path does not contain ".wav"
            String recordDir = mContext.getFilesDir().getAbsolutePath();
            final String filePath = recordDir + "/" + mRecordFileName;
            Log.d(TAG, "mBeepFx value in Java at createWav" + mBeepFx.getmPitchShift());
            ((RecordActivity) mActivity).createWav(filePath, mBeepFx);
        }
        Log.d(TAG, "save button, mImageuri: " + mImageUri);
        Log.d(TAG, "save button, mImagePath: " + mImagePath);
        Utility.insertNewBeep(mContext, beepName, mRecordFileName, beepEdited,
                mMostRecentLocation, selectedKey, mImagePath, mDeleteTempPic);

    }
    // Save Image items
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(IMAGE_FILE_URI, mImageUri.toString());
        }
        if (mImagePath != null) {
            outState.putString(IMAGE_FILE_PATH, mImagePath);
        }
        outState.putBoolean(DELETE_TEMP_PIC, mDeleteTempPic);
    }
    private void getAndPopulateBoardData() {
        // Get the data to populate the Board Spinner
        Cursor cursor = mContext.getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                Constants.BOARD_COLUMNS,
                null,
                null,
                null);

        mNumberOfBoards = cursor.getCount();
        Log.d(TAG, "Number of boards for spinner: " + mNumberOfBoards);
        cursor.moveToFirst();
        // Populate the Board Spinner
        mSpinnerItems = new ArrayList<Board>();
        int position = -1;

        for(int i = 0; i < cursor.getCount(); i++){
            String name = cursor.getString(Constants.BOARDS_COL_NAME);
            String key = cursor.getString(Constants.BOARDS_BOARD_ID);
            if (Integer.parseInt(key) == mBoardOriginKey) {
                position = i;
            }
            String image = cursor.getString(Constants.BOARDS_COL_IMAGE);
            long date = cursor.getLong(Constants.BOARD_COL_DATE);
            Log.d(TAG, "BOARD");
            Log.d(TAG, "name: " + name);
            Log.d(TAG, "key: " + key);
            Log.d(TAG, "image: " + image);
            Log.d(TAG, "date: " + date);
            Board board = new Board(Integer.parseInt(key), name, image, date);
            mSpinnerItems.add(board);
            cursor.moveToNext();
        }
        // Add item for creating new cursor
        // leave the date and image set to null for this - it doesn't actually
        String createNewBoard = getResources().getString(R.string.create_new_board_spinner_item);
        Board createNew = new Board(-1, createNewBoard, null, -1);
        mSpinnerItems.add(createNew);

        mBoardSpinnerAdapter = new BoardSpinnerAdapter(mContext,
                R.layout.spinner_row, R.id.spinner_item_textview, mSpinnerItems);

        mBoardSpinner.setAdapter(mBoardSpinnerAdapter);
        if (position != -1) {
            mBoardSpinner.setSelection(position);
        }
    }
    private void createBeepNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String dialogTitle = getResources().getString(R.string.dialog_new_beep_name);
        builder.setTitle(dialogTitle);
        final EditText input = new EditText(mContext);
        input.setMaxLines(1);
        input.setSingleLine();
        input.setText(mBeepNameEditText.getText());
        input.setSelectAllOnFocus(true);

        FrameLayout container = new FrameLayout(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float marginDpLeft = 16;
        float marginDpRight = 64;
        float pxLeft = marginDpLeft * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        float pxRight = marginDpRight * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        params.leftMargin = (int) pxLeft;
        params.rightMargin = (int) pxRight;
        input.setLayoutParams(params);
        container.addView(input);

        int maxLength = getResources().getInteger(R.integer.max_beep_size);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(container);

        builder.setPositiveButton(getResources().getString(R.string.beep_name_dialog_positive),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newBeepName = input.getText().toString();
                mBeepNameEditText.setText(newBeepName);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.beep_name_dialog_negative),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }
    private void createBoardNameDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        String dialogTitle = getResources().getString(R.string.dialog_new_board_name);
        builder.setTitle(dialogTitle);
        final EditText input = new EditText(mContext);
        input.setMaxLines(1);
        input.setSingleLine();

        FrameLayout container = new FrameLayout(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float marginDpLeft = 16;
        float marginDpRight = 64;
        float pxLeft = marginDpLeft * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        float pxRight = marginDpRight * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        params.leftMargin = (int) pxLeft;
        params.rightMargin = (int) pxRight;
        input.setLayoutParams(params);
        container.addView(input);

        int maxLength = getResources().getInteger(R.integer.max_board_size);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(container);

        String positiveButtonText = getResources().getString(R.string.dialog_positive_button);
        String negativeButtonText = getResources().getString(R.string.dialog_negative_button);

        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newBoardName = input.getText().toString();
                ContentValues contentValues = new ContentValues();
                contentValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, newBoardName);
                long currentTime = Calendar.getInstance().getTimeInMillis();
                contentValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, currentTime);
                Uri uri = mContext.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, contentValues);
                int insertedRow = (int) ContentUris.parseId(uri);
                Board newBoardz = new Board(insertedRow, newBoardName, null, currentTime);
                mSpinnerItems.add(mSpinnerItems.size() - 1, newBoardz);
                mBoardSpinnerAdapter.notifyDataSetChanged();
                mNumberOfBoards += 1;
                Utility.updateWidgets(mActivity);
            }
        });
        builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    // Get location - currently just uses last known location to save battery and avoid network/GPS issues
    private void getLocation() {
        // Get GPS coordinates
        try {
            Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationGPS == null || locationNetwork == null) {
                if (locationGPS == null) {
                    if (locationNetwork == null) {
                        mMostRecentLocation = null;
                    }
                    else {
                        mMostRecentLocation = locationNetwork;
                    }
                }
                else {
                    mMostRecentLocation = locationGPS;
                }
            }
            else if (locationGPS.getTime() > locationNetwork.getTime()) {
                mMostRecentLocation = locationGPS;
            }
            else {
                mMostRecentLocation = locationNetwork;
            }
            if (mMostRecentLocation != null) {
                Log.d(TAG, "mostRecentLocation Lat: " + mMostRecentLocation.getLatitude());
                Log.d(TAG, "mostRecentLocation Long: " + mMostRecentLocation.getLongitude());
            }
            else {
                Log.d(TAG, "mostRecentLocation is NULL");
            }
        }
        catch (SecurityException e) {
            // Handle if GPS not enabled
            mMostRecentLocation = null;
        }
    }
    // Remove updates - don't care
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            try {
                mLocationManager.removeUpdates(this);
                // update the contentvalues and insert
            }
            catch (SecurityException e) {
                // Handle if GPS not enabled
            }
        }
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted, create intent");
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);

                } else {
                    // Permission Denied
                    Toast.makeText(mContext,
                            getResources().getString(R.string.need_external_permission), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }
    private void loadImageView() {
        int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);

        Intent intent = new Intent(mContext, LoadDownsampledBitmapImageService.class);
        intent.putExtra(Constants.IMAGE_MESSENGER, new Messenger(mImageHandler));
        intent.putExtra(Constants.ORIGINAL_IMAGE_FILE_PATH, mImagePath);

        intent.putExtra(Constants.IMAGE_MIN_SIZE, imageSize);
        mContext.startService(intent);
    }
    private void openImageIntent() {

        String tempImageName = getResources().getString(R.string.camera_temp_photo_filename_prefix);
        String tempImagePath = mContext.getFilesDir().getAbsolutePath() + File.separator + tempImageName;
        File storageDir = mContext.getFilesDir();
        File image = null;
        try {
            image = File.createTempFile(
                    tempImageName,
                    Constants.JPG_EXTENSION,
                    storageDir);
            mCameraTempImagePath =image.getAbsolutePath();
            Log.d(TAG, "Camera temp image path: " + mCameraTempImagePath);

        } catch(IOException ioe) {
            Log.d(TAG, "Temp image path not created");
        }
        if (image != null) {
            mCameraIntentOutputFileUri = FileProvider.getUriForFile(mContext,
                    "xyz.peast.beep.fileprovider",
                    image);
            Log.d(TAG, "photoURI: " + mCameraIntentOutputFileUri);
        }
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            Log.d(TAG, "Resolve info packageName: " + packageName);
            Log.d(TAG, "Resolve info Name: " + res.activityInfo.name);

            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            // Uri extra is temp file name for camera image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraIntentOutputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        String intentChooserText = getResources().getString(R.string.beep_image_intent_chooser_text);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, intentChooserText);

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, SELECT_PHOTO);
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}