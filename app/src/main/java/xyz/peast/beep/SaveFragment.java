package xyz.peast.beep;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import xyz.peast.beep.data.BeepDbContract;

/**
 * Created by duvernea on 7/30/16.
 */
public class SaveFragment extends Fragment {

    private static final String TAG = SaveFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 1;

    private Context mContext;

    private AdView mAdView;

    private Spinner mBoardSpinner;
    private ImageView mBeepImage;

    private Button mSaveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_save, container, false);

        mContext = getActivity();

        mBoardSpinner = (Spinner) rootView.findViewById(R.id.board_name_spinner);
        mBeepImage = (ImageView) rootView.findViewById(R.id.beep_image);
        mSaveButton = (Button) rootView.findViewById(R.id.save_button);

        mAdView = (AdView) rootView.findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

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
                mContext.getContentResolver().insert(BeepDbContract.BeepEntry.CONTENT_URI, contentValues);
            }
        });
        // Create new item should have a special icon, like a plus sign or something
        String[] spinnerItems = new String[]{"myBeeps", "Sweetie", "Mom & Dad", "Work Crewz", "Create New"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                mContext, R.layout.spinner_row, R.id.spinner_item_textview, spinnerItems);

        mBoardSpinner.setAdapter(adapter);
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
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = mContext.getContentResolver().openInputStream(imageUri);
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
}