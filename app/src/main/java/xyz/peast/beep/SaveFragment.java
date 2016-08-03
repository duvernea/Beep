package xyz.peast.beep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duvernea on 7/30/16.
 */
public class SaveFragment extends Fragment implements LocationListener {

    private static final String TAG = SaveFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 1;

    private Context mContext;

    private AdView mAdView;

    private Spinner mBoardSpinner;
    private ImageView mBeepImage;

    private Button mSaveButton;
    private EditText mBeepNameEditText;

    private Uri mImageUri = null;

    private LocationManager mLocationManager;

    private String mRecordFileName;

    private Location mMostRecentLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_save, container, false);

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME);
        Log.d(TAG, "Record File Name: " + mRecordFileName);

        mContext = getActivity();
        mLocationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);

        mBoardSpinner = (Spinner) rootView.findViewById(R.id.board_name_spinner);
        mBeepImage = (ImageView) rootView.findViewById(R.id.beep_image);
        mSaveButton = (Button) rootView.findViewById(R.id.save_button);
        mBeepNameEditText = (EditText) rootView.findViewById(R.id.beep_name_edittext);

        mAdView = (AdView) rootView.findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);
        mBoardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Spinner Item Selected");
                if (position == 4) {
                    Log.d(TAG, "create new board selected on spinner");
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Create new Board");
                    final EditText input = new EditText(mContext);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
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
                ContentValues contentValues = new ContentValues();
                // TODO name, board, time, audio file, image uri, etc
                insertContent();
            }
        });
        // Create new item should have a special icon, like a plus sign or something
        String[] spinnerItems = new String[]{"myBeeps", "Sweetie", "Mom & Dad", "Work Crewz", "Create New"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                mContext, R.layout.spinner_row, R.id.spinner_item_textview, spinnerItems);

        mBoardSpinner.setAdapter(adapter);

        mImageUri = Uri.parse("/temp/test/junk");
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult run");

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        mImageUri = data.getData();
                        final InputStream imageStream = mContext.getContentResolver().openInputStream(mImageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

//                        int nh = (int) ( mBeepImage.getHeight() * (512.0 / mBeepImage.getWidth()) );
//                        Bitmap scaled = Bitmap.createScaledBitmap(selectedImage, 512, nh, true);

                        // Center crop
                        Bitmap centerCropBmp;
                        if (selectedImage.getWidth() >= selectedImage.getHeight()){

                            centerCropBmp = Bitmap.createBitmap(
                                    selectedImage,
                                    selectedImage.getWidth()/2 - selectedImage.getHeight()/2,
                                    0,
                                    selectedImage.getHeight(),
                                    selectedImage.getHeight()
                            );

                        }else{

                            centerCropBmp = Bitmap.createBitmap(
                                    selectedImage,
                                    0,
                                    selectedImage.getHeight()/2 - selectedImage.getWidth()/2,
                                    selectedImage.getWidth(),
                                    selectedImage.getWidth()
                            );
                        }
                        
                        mBeepImage.setImageBitmap(centerCropBmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
    void insertContent() {
        //beepRowIds[0] = (int) ContentUris.parseId(beepUri);

        getLocation();
        ContentValues contentValues = new ContentValues();

        String beepName = mBeepNameEditText.getText().toString();
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_NAME, beepName);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_IMAGE, mImageUri.toString());
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, mRecordFileName);
        if (mMostRecentLocation != null) {
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LAT, mMostRecentLocation.getLatitude());
            contentValues.put(BeepDbContract.BeepEntry.COLUMN_COORD_LONG, mMostRecentLocation.getLongitude());
        }

        contentValues.put(BeepDbContract.BeepEntry.COLUMN_AUDIO, mRecordFileName);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_PRIVACY, 1);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_PLAY_COUNT, 0);
        contentValues.put(BeepDbContract.BeepEntry.COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis());

        String boardSelected = mBoardSpinner.getSelectedItem().toString();
        Log.d(TAG, "spinner getselecteditem to string " + boardSelected);

        long spinnerSelectedItemId =  mBoardSpinner.getSelectedItemId();
        Log.d(TAG, "spinner selected item ID " + spinnerSelectedItemId);

        int spinnerSelectedItemPosition  = mBoardSpinner.getSelectedItemPosition();
        Log.d(TAG, "spinner selected item position " + spinnerSelectedItemPosition);

        contentValues.put(BeepDbContract.BeepEntry.COLUMN_BOARD_KEY, boardSelected);

        Uri uri = mContext.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, contentValues);
        Log.d(TAG, "end of insert into ContentProvider");
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
            Log.d(TAG, "mostRecentLocation Lat: " + mMostRecentLocation.getLatitude());
            Log.d(TAG, "mostRecentLocation Long: " + mMostRecentLocation.getLongitude());

//            int timeWindow = 2 * 60 * 1000; // 2 minutes
//            if(mostRecentLocation.getTime() > Calendar.getInstance().getTimeInMillis() - timeWindow) {
//            }
//            else {
//                //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this)
//            }
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