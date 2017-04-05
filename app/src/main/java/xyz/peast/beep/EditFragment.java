package xyz.peast.beep;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by duverneay on 9/22/16.
 */
public class EditFragment extends Fragment {

    private static final String TAG = EditFragment.class.getSimpleName();

    Context mContext;
    Activity mActivity;

    Button mPlayButton;
    SeekBar mTrebleSeekBar;
    SeekBar mBassSeekBar;
    ToggleButton mChipmunkButton;
    ToggleButton mSlomoButton;
    ToggleButton mCreepyButton;
    ToggleButton mEchoButton;
    ToggleButton mChurchButton;
    ToggleButton mRobotButton;
    Button mNextButton;

    private AdView mAdView;


    String mRecordFileName;
    String mRecordFilePath;
    Boolean mIsPlaying = false;

    private BeepFx mBeepFx;

    int mPitchShift =1;
    boolean mReverse=false;

    public interface NextCallback{
        void onEditNextButton(BeepFx beepFx);
    }

    private static final int CHIPMUNK_PITCH_SHIFT = 8;
    private static final int SLOMO_PITCH_SHIFT = -8;
    private static final int NO_PITCH_SHIFT = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        mContext = getActivity();
        mActivity = getActivity();

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME);
        mRecordFilePath = Utility.getFullWavPath(mContext, mRecordFileName, false);

        ((RecordActivity) mActivity).onFileChange(mRecordFilePath, 0, 0);

        mPlayButton = (Button) rootView.findViewById(R.id.play_button);
        mChipmunkButton = (ToggleButton) rootView.findViewById(R.id.chipmunk_button);
        mSlomoButton = (ToggleButton) rootView.findViewById(R.id.slomo_button);
        mCreepyButton = (ToggleButton) rootView.findViewById(R.id.creepy_button);
        mEchoButton = (ToggleButton) rootView.findViewById(R.id.echo_button);
        mChurchButton = (ToggleButton) rootView.findViewById(R.id.church_button);
        mRobotButton = (ToggleButton) rootView.findViewById(R.id.robot_button);

        mBassSeekBar = (SeekBar) rootView.findViewById(R.id.bass_seekBar);
        mTrebleSeekBar = (SeekBar) rootView.findViewById(R.id.treble_seekBar);

        mNextButton = (Button) rootView.findViewById(R.id.next_button);

        mAdView = (AdView) rootView.findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        mBeepFx = new BeepFx(0);

        mTrebleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "Progress: " + progress);
                // raw progress is 0 - 24
                // center around 0
                double dB = progress - 12;
                Log.d(TAG, "Progress dB scale: " + dB);
                mBeepFx.setTreble((float) Math.pow(10, dB/20));
                Log.d(TAG, "Progress linear scale; " + mBeepFx.getTreble());

                ((RecordActivity) mActivity).setTreble((float) mBeepFx.getTreble());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);

            }
        });
        mBassSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "Progress: " + progress);
                // raw progress is 0 - 24
                // center around 0
                float dB = progress - 12;
                Log.d(TAG, "Progress dB scale: " + dB);
                mBeepFx.setBass((float) Math.pow(10, dB/20));
                Log.d(TAG, "Progress linear scale; " + mBeepFx.getBass());

                ((RecordActivity) mActivity).setBass((float) mBeepFx.getBass());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mIsPlaying start: " + mIsPlaying);
                mIsPlaying = true;
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mChipmunkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick listener");
            }
        });
        mChipmunkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mChipmunkButton.isChecked());
                // ((RecordActivity) mActivity).onFileChange(mRecordFilePath, 0, 0);
                if (isChecked) {
                    mSlomoButton.setChecked(false);
                    ((RecordActivity) mActivity).setPitchShift(CHIPMUNK_PITCH_SHIFT);
                    mBeepFx.setPitchShift(CHIPMUNK_PITCH_SHIFT);
                }
                else {
                    ((RecordActivity) mActivity).setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setPitchShift(NO_PITCH_SHIFT);
                }
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mSlomoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick listener");
            }
        });
        mSlomoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mChipmunkButton.isChecked());
                // ((RecordActivity) mActivity).onFileChange(mRecordFilePath, 0, 0);
                if (isChecked) {
                    mChipmunkButton.setChecked(false);
                    ((RecordActivity) mActivity).setPitchShift(SLOMO_PITCH_SHIFT);
                    mBeepFx.setPitchShift(SLOMO_PITCH_SHIFT);
                }
                else {
                    ((RecordActivity) mActivity).setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setPitchShift(NO_PITCH_SHIFT);
                }
                //mReverse = !mReverse;
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mCreepyButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mCreepyButton.isChecked());
                // ((RecordActivity) mActivity).onFileChange(mRecordFilePath, 0, 0);
                if (isChecked) {
                    ((RecordActivity) mActivity).setReverse(true);
                    mBeepFx.setReverse(true);
                }
                else {
                    ((RecordActivity) mActivity).setReverse(false);
                    mBeepFx.setReverse(false);
                }
                //mReverse = !mReverse;
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mEchoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick listener");

            }
        });
        mEchoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mEchoButton.isChecked());
                // ((RecordActivity) mActivity).onFileChange(mRecordFilePath, 0, 0);
                if (isChecked) {
                    ((RecordActivity) mActivity).setEcho(true);
                    mBeepFx.setEcho(true);
                }
                else {
                    ((RecordActivity) mActivity).setEcho(false);
                    mBeepFx.setEcho(false);
                }
                //mReverse = !mReverse;
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mEchoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick listener");

            }
        });
        mChurchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mChurchButton.isChecked());
                if (isChecked) {
                    ((RecordActivity) mActivity).setReverb(true);
                    mBeepFx.setReverb(true);
                }
                else {
                    ((RecordActivity) mActivity).setReverb(false);
                    mBeepFx.setReverb(false);
                }
                //mReverse = !mReverse;
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mRobotButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mRobotButton.isChecked());
                if (isChecked) {
                    ((RecordActivity) mActivity).setRobot(true);
                    mBeepFx.setRobot(true);
                }
                else {
                    ((RecordActivity) mActivity).setRobot(false);
                    mBeepFx.setRobot(false);
                }
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNextButtonPress();
            }
        });
        return rootView;
    }


    private void handleNextButtonPress() {
        ((EditFragment.NextCallback) getActivity()).onEditNextButton(mBeepFx);
    }
}
