package xyz.peast.beep;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


public class ShareFragment extends Fragment {

    private static final String TAG = ShareFragment.class.getSimpleName();

    private Context mContext;
    private String mRecordFileName;

    private TextView mBeepNameTextView;
    private ImageView mBeepImageView;
    private Button mShareButton;

    private String mBoardName;
    private int mBoardKey;


    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);

        mBeepNameTextView = (TextView) rootView.findViewById(R.id.beep_name_textview);
        mBeepImageView = (ImageView) rootView.findViewById(R.id.beep_imageview);
        mShareButton = (Button) rootView.findViewById(R.id.share_button);

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME) + ".wav";
        String imagefile = bundle.getString(RecordActivity.IMAGE_FILE_UNIQUE_NAME);
        mBoardName = bundle.getString(RecordActivity.BOARD_NAME);
        mBoardKey = bundle.getInt(RecordActivity.BOARD_KEY);

        String beepname = bundle.getString(RecordActivity.BEEP_NAME);
        mBeepNameTextView.setText(beepname);
        Log.d(TAG, "Record File Name: " + mRecordFileName);
        Log.d(TAG, "Image File Name: " + imagefile);

        mContext = getActivity();
        String imageDir = mContext.getFilesDir().getAbsolutePath();
        String imagePath = imageDir + "/" + imagefile;
        Log.d(TAG, "BeepAdapter image file" + imagePath);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        mBeepImageView.setImageBitmap(bitmap);

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String audioPath = mContext.getFilesDir().getAbsolutePath();
                audioPath += "/" + mRecordFileName;
                Log.d(TAG, "audioPath: " + audioPath);
                Uri fileUri;
                //= Uri.parse(audioPath);
                File requestFile = new File(audioPath);
                try {
                    fileUri = FileProvider.getUriForFile(
                            mContext,
                            "xyz.peast.beep.fileprovider",
                            requestFile);
                } catch (IllegalArgumentException e) {
                    fileUri = null;
                    Log.e("File Selector",
                            "The selected file can't be shared: ");
                }
                if (fileUri != null) {
                    Log.d(TAG, "fileUri: " + fileUri);
                }
                Intent share = new Intent(Intent.ACTION_SEND);

                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivity(Intent.createChooser(share, "Share Sound File"));

//                Intent intent = new Intent(mContext, BoardActivity.class);
//                intent.putExtra(MainActivity.BOARD_KEY_CLICKED, mBoardKey);
//                intent.putExtra(MainActivity.BOARD_NAME_SELECTED, mBoardName);
//                startActivity(intent);
            }
        });

        return rootView;
    }


}
