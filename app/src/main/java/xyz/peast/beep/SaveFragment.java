package xyz.peast.beep;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import xyz.peast.beep.adapters.Board;
import xyz.peast.beep.adapters.BoardSpinnerAdapter;
import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.BeepService;

/**
 * Created by duvernea on 7/30/16.
 */
public class SaveFragment extends Fragment implements LocationListener {

    private static final String TAG = SaveFragment.class.getSimpleName();

    private static final String IMAGE_FILE_NAME = "image_file_name";
    private static final String IMAGE_FILE_URI = "image_file_uri";
    private static final String IMAGE_FILE_PATH = "image_file_path";

    public static final String COMPRESS_IMAGE_FILE_URI = "COMPRESS_IMAGE_FILE_URI";
    public static final String BEEP_URI = "beep_uri";

    private static final int SELECT_PHOTO = 1;

    private Context mContext;

    private AdView mAdView;

    private Spinner mBoardSpinner;
    private ImageView mBeepImage;

    private Button mSaveButton;
    private EditText mBeepNameEditText;

    private Button mReplayButton;

    private Uri mImageUri = null;
    private String mImagePath = null;

    private LocationManager mLocationManager;

    private String mRecordFileName;

    private Location mMostRecentLocation;

    private int mNumberOfBoards;

    private ArrayList<Board> mSpinnerItems;
    private BoardSpinnerAdapter mBoardSpinnerAdapter;

    private Bitmap mImageBitmap;
    private String mImageFileName;

    private boolean mIsPlaying;
    private Activity mActivity;

    private Intent mServiceIntent;

    public interface SaveCallback{
        public void onSaveNextButton(String beepName, String audioFile, Uri imageUri,
                                     String boardname, int boardkey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_save, container, false);

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME) + ".wav";
        Log.d(TAG, "Record File Name: " + mRecordFileName);

        mContext = getActivity();
        mLocationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);

        mBoardSpinner = (Spinner) rootView.findViewById(R.id.board_name_spinner);
        mBeepImage = (ImageView) rootView.findViewById(R.id.beep_image);
        mSaveButton = (Button) rootView.findViewById(R.id.save_button);
        mBeepNameEditText = (EditText) rootView.findViewById(R.id.beep_name_edittext);
        mReplayButton = (Button) rootView.findViewById(R.id.replay_button);

        mAdView = (AdView) rootView.findViewById(R.id.adview);
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);
        mBeepNameEditText.setClickable(true);
        mBeepNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Beep name");
                    final EditText input = new EditText(mContext);
                    input.setMaxLines(1);
                    input.setSingleLine();
                    input.setText(mBeepNameEditText.getText());
                    input.setSelectAllOnFocus(true);

                    FrameLayout container = new FrameLayout(mContext);
                    FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    Resources resources = mContext.getResources();
                    DisplayMetrics metrics = resources.getDisplayMetrics();
                    float marginDpLeft = 16;
                    float marginDpRight = 64;
                    float pxLeft = marginDpLeft * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                    float pxRight = marginDpRight * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

                    params.leftMargin = (int) pxLeft;
                    params.rightMargin = (int) pxRight;
                    input.setLayoutParams(params);
                    container.addView(input);

                    int maxLength = getResources().getInteger(R.integer.max_beep_size);
                    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(container);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newBeepName = input.getText().toString();
                            mBeepNameEditText.setText(newBeepName);
                            //ContentValues contentValues = new ContentValues();
                            //contentValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, newBoardName);
                            //long currentTime = Calendar.getInstance().getTimeInMillis();
                            //contentValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, currentTime);
                            //String tempImageUri = "";
                            //contentValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, tempImageUri);
                            //Uri uri = mContext.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, contentValues);
                            //int insertedRow = (int) ContentUris.parseId(uri);
                            //Log.d(TAG, "inserted Row into Board db: " + insertedRow);
                            //mSpinnerItems.add(mSpinnerItems.size()-1, newBoard);
                            //Board newBoardz = new Board(insertedRow, newBoardName, tempImageUri, currentTime);
                            //mSpinnerItems.add(mSpinnerItems.size()-1, newBoardz);
                            //mBoardSpinnerAdapter.notifyDataSetChanged();
                            //mNumberOfBoards +=1;
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                    dialog.show();
            }
        });
        mBoardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Spinner Item Selected");
                if (position == mNumberOfBoards) {
                    Log.d(TAG, "create new board selected on spinner");
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Create new Board");
                    final EditText input = new EditText(mContext);
                    input.setMaxLines(1);
                    input.setSingleLine();

                    FrameLayout container = new FrameLayout(mContext);
                    FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    Resources resources = mContext.getResources();
                    DisplayMetrics metrics = resources.getDisplayMetrics();
                    float marginDpLeft = 16;
                    float marginDpRight = 64;
                    float pxLeft = marginDpLeft * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                    float pxRight = marginDpRight * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                    
                    params.leftMargin = (int) pxLeft;
                    params.rightMargin = (int) pxRight;
                    input.setLayoutParams(params);
                    container.addView(input);

                    int maxLength = getResources().getInteger(R.integer.max_board_size);
                    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(container);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newBoardName = input.getText().toString();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, newBoardName);
                            long currentTime = Calendar.getInstance().getTimeInMillis();
                            contentValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, currentTime);
                            // TODO - need default image resources to use
                            String tempImageUri = "";
                            contentValues.put(BeepDbContract.BoardEntry.COLUMN_IMAGE, tempImageUri);
                            Uri uri = mContext.getContentResolver().insert(BeepDbContract.BoardEntry.CONTENT_URI, contentValues);
                            int insertedRow = (int) ContentUris.parseId(uri);
                            Log.d(TAG, "inserted Row into Board db: " + insertedRow);
                            //mSpinnerItems.add(mSpinnerItems.size()-1, newBoard);
                            Board newBoardz = new Board(insertedRow, newBoardName, tempImageUri, currentTime);
                            mSpinnerItems.add(mSpinnerItems.size()-1, newBoardz);
                            mBoardSpinnerAdapter.notifyDataSetChanged();
                            mNumberOfBoards +=1;
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                    dialog.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBeepImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO name, board, time, audio file, image uri, etc
                insertContent();
                int spinnerSelectedItemPosition  = mBoardSpinner.getSelectedItemPosition();
                Log.d(TAG, "spinner selected item position " + spinnerSelectedItemPosition);
                Board selected = mSpinnerItems.get(spinnerSelectedItemPosition);
                int selectedKey = selected.getKey();
                String boardname = selected.getName();

                ((SaveCallback) getActivity()).onSaveNextButton(mBeepNameEditText.getText().toString(),
                        mRecordFileName,
                        mImageUri,
                        boardname,
                        selectedKey
                );
            }
        });
        String[] mProjection =
                {
                        BeepDbContract.BoardEntry._ID,
                        BeepDbContract.BoardEntry.COLUMN_NAME
                };
        Cursor cursor = mContext.getContentResolver().query(BeepDbContract.BoardEntry.CONTENT_URI,
                mProjection,
                null,
                null,
                null);

        Log.d(TAG, "Cursor count, #boards returned" + cursor.getCount());

        mNumberOfBoards = cursor.getCount();
        //mSpinnerItems = new ArrayList<String>();
        cursor.moveToFirst();
        mSpinnerItems = new ArrayList<Board>();

        for(int i = 0; i < cursor.getCount(); i++){
            String row = cursor.getString(
                    cursor.getColumnIndex(BeepDbContract.BoardEntry.COLUMN_NAME));
            String key = cursor.getString(
                    cursor.getColumnIndex(BeepDbContract.BoardEntry._ID));
            //mSpinnerItems.add(row);
            //String key = cursor.getInt(cursor.getColumnIndex())
            Board temp = new Board(Integer.parseInt(key), row, "temp", 242);
            mSpinnerItems.add(temp);
            cursor.moveToNext();
        }
        // Add item for creating new cursor
        Board createNew = new Board(-1, "Create New", "N/A", 0);
        //mSpinnerItems.add("Create New");
        mSpinnerItems.add(createNew);

        //String[] spinnerItemsTest = {"test", "test2"};
        //mSpinnerItems = new ArrayList<String>();

        mBoardSpinnerAdapter = new BoardSpinnerAdapter(mContext,
                R.layout.spinner_row, R.id.spinner_item_textview, mSpinnerItems);

        //mSpinnerAdapter = new ArrayAdapter<String>(
                //mContext, R.layout.spinner_row, R.id.spinner_item_textview, mSpinnerItems);

        mBoardSpinner.setAdapter(mBoardSpinnerAdapter);

        mActivity = getActivity();

        String recordDir = mContext.getFilesDir().getAbsolutePath();
        final String filePath = recordDir + "/" + mRecordFileName;
        Log.d(TAG, "filePath: " + filePath);

        mReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPlaying = true;
                Log.d(TAG, "mIsPlaying play: " + mIsPlaying);
                ((RecordActivity) mActivity).onFileChange(filePath, 0, 0);
                ((RecordActivity) mActivity).onPlayPause(filePath, mIsPlaying, 0);
            }
        });

        if (savedInstanceState != null) {
            mImageUri = Uri.parse(savedInstanceState.getString(IMAGE_FILE_URI));
            mImagePath = savedInstanceState.getString(IMAGE_FILE_PATH);
        }

        if (mImagePath != null) {

            // Downsample bitmap
            Bitmap bitmap = Utility.subsampleBitmap(mContext, mImagePath, 360, 360);
            // Center crop bitmap
            mImageBitmap = Utility.centerCropBitmap(mContext, bitmap);

            mBeepImage.setImageBitmap(mImageBitmap);
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult run");

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {

                    mImageUri = data.getData();
                    mImagePath = Utility.getRealPathFromURI(mContext, mImageUri);
                    // Downsample bitmap
                    Bitmap bitmap = Utility.subsampleBitmap(mContext, mImagePath, 360, 360);
                    // Center crop bitmap
                    mImageBitmap = Utility.centerCropBitmap(mContext, bitmap);

                    mBeepImage.setImageBitmap(mImageBitmap);
                }
        }
    }
    void insertContent() {
        //beepRowIds[0] = (int) ContentUris.parseId(beepUri);
        ContentValues contentValues = new ContentValues();


        if (mImageBitmap != null) {
            String imageDir = mContext.getFilesDir().getAbsolutePath();

            mImageFileName = UUID.randomUUID().toString() + ".jpg";
            //String tempFileName = "temp.jpg";

            //saveBitmap(imageDir + "/" + mImageFileName);

            //contentValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, mImageFileName);
        }

        getLocation();

        String beepName = mBeepNameEditText.getText().toString();
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, beepName);
        //contentValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, mImageUri.toString());

        contentValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, mRecordFileName);
        if (mMostRecentLocation != null) {
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, mMostRecentLocation.getLatitude());
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, mMostRecentLocation.getLongitude());
        }
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 0);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis());

//        String boardSelected = mBoardSpinner.getSelectedItem().toString();
//        Log.d(TAG, "spinner getselecteditem to string " + boardSelected);
//
//        long spinnerSelectedItemId =  mBoardSpinner.getSelectedItemId();
//        Log.d(TAG, "spinner selected item ID " + spinnerSelectedItemId);
//
        int spinnerSelectedItemPosition  = mBoardSpinner.getSelectedItemPosition();
        Log.d(TAG, "spinner selected item position " + spinnerSelectedItemPosition);
        Board selected = mSpinnerItems.get(spinnerSelectedItemPosition);
        int selectedKey = selected.getKey();
        String boardSelectedString = mBoardSpinnerAdapter.getItem(spinnerSelectedItemPosition).getName();
        Log.d(TAG, "spinner selected item string " + boardSelectedString);


        contentValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, selectedKey);

        Uri uri = mContext.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, contentValues);
        Log.d(TAG, "end of insert beep into ContentProvider uri = " + uri.toString());
        mServiceIntent = new Intent(getActivity(), BeepService.class);
        //Utility.subsampleBitmap(mContext, mImageUri);
        //Convert to byte array
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //mImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //byte[] byteArray = stream.toByteArray();

        Bundle bundle = new Bundle();
        bundle.putString(COMPRESS_IMAGE_FILE_URI, mImageUri.toString());
        bundle.putString(BEEP_URI, uri.toString());
        mServiceIntent.putExtras(bundle);

        getActivity().startService(mServiceIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(IMAGE_FILE_NAME, mImageFileName);
        outState.putString(IMAGE_FILE_PATH, mImagePath);
        if (mImageUri != null) {
            outState.putString(IMAGE_FILE_URI, mImageUri.toString());
        }

        Log.d(TAG, "onSaveInstanceState");
    }

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

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}