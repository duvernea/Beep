package xyz.peast.beep;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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

    private ImageView mBoardImage;
    private Button mCreateButton;
    private Button mCancelButton;
    private AdView mAdView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);

        mContext = this;
        mActivity = this;

        mBoardImage = (ImageView) findViewById(R.id.board_image);
        mCreateButton = (Button) findViewById(R.id.create_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);

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
}
