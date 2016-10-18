package xyz.peast.beep;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by duvernea on 10/18/16.
 */
public class CreateBoardActivity extends AppCompatActivity {

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

    }
}
