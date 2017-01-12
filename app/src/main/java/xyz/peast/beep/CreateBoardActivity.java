package xyz.peast.beep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;

import xyz.peast.beep.data.BeepDbContract;
import xyz.peast.beep.services.LoadDownsampledBitmapImageService;

/**
 * Created by duvernea on 10/18/16.
 */
public class CreateBoardActivity extends AppCompatActivity {

    private static final String TAG = CreateBoardActivity.class.getSimpleName();

    // Permission Request Code
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL= 10;
    // Request Code for Photo Picker Intent
    private static final int SELECT_PHOTO = 1;

    private Activity mActivity;
    private Context mContext;

    // Selected Image in Picker - Uri and Path
    private Uri mImageUri = null;
    private String mImagePath = null;
    // Image loading
    Handler mImageHandler;

    private ImageView mBoardImage;
    private Button mCreateButton;
    private Button mCancelButton;
    private EditText mBoardNameEditText;
    private AdView mAdView;

    //onSaveInstanceState
    private static final String SELECTED_IMAGE_URI="selected_image_uri";
    private static final String SELECTED_IMAGE_PATH="selected_image_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);

        mContext = this;
        mActivity = this;

        mBoardImage = (ImageView) findViewById(R.id.board_image);
        mCreateButton = (Button) findViewById(R.id.create_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mBoardNameEditText = (EditText) findViewById(R.id.board_name_edittext);

        // EditText for setting Beep Name - onClick opens Dialog for entering text
        mBoardNameEditText.setClickable(true);
        mBoardNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBoardNameDialog();
            }
        });

        mAdView = (AdView) findViewById(R.id.adview);
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        mBoardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean permissionReadExternal = Utility.hasReadExternalPermission(mContext);
                Log.d(TAG, "hasRecordAudioPermission: " + permissionReadExternal);

                if (permissionReadExternal) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);

                } else {
                    requestReadExternalPermission();
                }
            }
        });

        mImageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "handler handleMessage");
                Bundle reply = msg.getData();
                Bitmap bitmap = reply.getParcelable(Constants.IMAGE_BITMAP_FROM_SERVICE);
                mBoardImage.setImageBitmap(bitmap);
            }
        };

        // Create Button onClick - run the Callback in RecordActivity
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String boardName = mBoardNameEditText.getText().toString().trim();

                if(boardName.isEmpty()) {
                    String toastMsg = getResources().getString(R.string.no_board_name_entered_msg);
                    Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                insertBoardContent();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState != null) {
            String mImageUriString = savedInstanceState.getString(SELECTED_IMAGE_URI);
            if (mImageUriString != null) {
                mImageUri = Uri.parse(mImageUriString);
                mImagePath = savedInstanceState.getString(SELECTED_IMAGE_PATH);
                displayDownsampledImage();
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
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                } else {
                    // Permission Denied
                    // TODO: What to do? Cannot use images
                    Toast.makeText(mContext, getResources().getString(R.string.need_read_permission), Toast.LENGTH_SHORT).show();
                }
            }
        }
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
                    displayDownsampledImage();
                }
        }
    }
    // Insert beep into database
    void insertBoardContent() {
        String boardName = mBoardNameEditText.getText().toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BeepDbContract.BoardEntry.COLUMN_NAME, boardName);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        contentValues.put(BeepDbContract.BoardEntry.COLUMN_DATE_CREATED, currentTime);

        int insertedRow = Utility.insertNewBoard(mContext, boardName, mImageUri);

        Intent intent = new Intent(mContext, BoardActivity.class);
        intent.putExtra(MainActivity.BOARD_KEY_CLICKED, insertedRow);
        intent.putExtra(MainActivity.BOARD_NAME_SELECTED, boardName);
        intent.putExtra(BoardActivity.LAST_ACTIVITY_UNIQUE_ID,BoardActivity.FROM_CREATE_BOARD_ACTIVITY);
        startActivity(intent);
    }

    private void createBoardNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String dialogTitle = getResources().getString(R.string.dialog_new_board_name);
        builder.setTitle(dialogTitle);
        final EditText input = new EditText(mContext);
        input.setMaxLines(1);
        input.setSingleLine();
        input.setText(mBoardNameEditText.getText());
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

        int maxLength = getResources().getInteger(R.integer.max_board_size);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(container);

        String positiveButtonText = getResources().getString(R.string.dialog_positive_button);
        String negativeButtonText = getResources().getString(R.string.dialog_negative_button);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().length() == 0) {
                    Toast.makeText(mContext, "test", Toast.LENGTH_SHORT).show();
                }
                String newBeepName = input.getText().toString();
                mBoardNameEditText.setText(newBeepName);
            }
        });
        builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog boardNameDialog = builder.create();

        boardNameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (input.getText().length() == 0) {
                            Toast.makeText(mContext,
                                    getResources().getString(R.string.dialog_no_text_entered_error),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String newBeepName = input.getText().toString();
                        mBoardNameEditText.setText(newBeepName);
                        boardNameDialog.dismiss();
                    }
                });
            }
        });

        boardNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        boardNameDialog.show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageUri != null) {
            outState.putString(SELECTED_IMAGE_PATH, mImagePath);
            outState.putString(SELECTED_IMAGE_URI, mImageUri.toString());
        }
    }

    private void displayDownsampledImage() {
        int imageSize = (int) mContext.getResources().getDimension(R.dimen.image_size_save_activity);
        Intent intent = new Intent(mContext, LoadDownsampledBitmapImageService.class);
        intent.putExtra(Constants.IMAGE_MESSENGER, new Messenger(mImageHandler));
        intent.putExtra(Constants.IMAGE_MIN_SIZE, imageSize);

        mContext.startService(intent);
    }

}
