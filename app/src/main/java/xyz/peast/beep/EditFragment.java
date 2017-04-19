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
//    SeekBar mTrebleSeekBar;
//    SeekBar mBassSeekBar;
    ToggleButton mChipmunkButton;
    ToggleButton mFastFwdButton;
    ToggleButton mHeliumButton;
    ToggleButton mEvilButton;
    ToggleButton mSlomoButton;
    ToggleButton mDeepButton;
    ToggleButton mEchoButton;
    ToggleButton mChurchButton;
    ToggleButton mReverseButton;
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

    private static final int HELIUM_PITCH_SHIFT = 8;
    private static final int DEEP_PITCH_SHIFT = -8;
    private static final int NO_PITCH_SHIFT = 0;

    private static final double NORMAL_TEMPO = 1.0;
    private static final double CHIPMUNK_TEMPO = 1.6;
    private static final double EVIL_TEMPO = 0.7;
    private static final double FASTFWD_TEMPO = 2.0;

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
        mFastFwdButton = (ToggleButton) rootView.findViewById(R.id.fastfwd_button);
        mHeliumButton = (ToggleButton) rootView.findViewById(R.id.helium_button);
        mEvilButton = (ToggleButton) rootView.findViewById(R.id.evil_button);
        mSlomoButton = (ToggleButton) rootView.findViewById(R.id.slomo_button);
        mDeepButton = (ToggleButton) rootView.findViewById(R.id.deep_button);
        mEchoButton = (ToggleButton) rootView.findViewById(R.id.echo_button);
        mChurchButton = (ToggleButton) rootView.findViewById(R.id.church_button);
        mReverseButton = (ToggleButton) rootView.findViewById(R.id.reverse_button);
        mRobotButton = (ToggleButton) rootView.findViewById(R.id.robot_button);
//
//        mBassSeekBar = (SeekBar) rootView.findViewById(R.id.bass_seekBar);
//        mTrebleSeekBar = (SeekBar) rootView.findViewById(R.id.treble_seekBar);

        mNextButton = (Button) rootView.findViewById(R.id.next_button);

        mAdView = (AdView) rootView.findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("839737069995AAD5519D71B8B267924D")
                .build();
        mAdView.loadAd(adRequest);

        mBeepFx = new BeepFx(0);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mIsPlaying start: " + mIsPlaying);
                mIsPlaying = true;
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mChipmunkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mHeliumButton.isChecked());
                if (isChecked) {
                    mFastFwdButton.setChecked(false);
                    mHeliumButton.setChecked(false);
                    mEvilButton.setChecked(false);
                    mSlomoButton.setChecked(false);
                    mDeepButton.setChecked(false);
                    ((RecordActivity) mActivity).setPitchShift(HELIUM_PITCH_SHIFT);
                    ((RecordActivity) mActivity).setTempo(CHIPMUNK_TEMPO);
                    mBeepFx.setPitchShift(HELIUM_PITCH_SHIFT);
                    mBeepFx.setTempo(CHIPMUNK_TEMPO);
                }
                else {
                    ((RecordActivity) mActivity).setPitchShift(NO_PITCH_SHIFT);
                    ((RecordActivity) mActivity).setTempo(NORMAL_TEMPO);
                    mBeepFx.setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setTempo(NORMAL_TEMPO);
                }
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mFastFwdButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mHeliumButton.isChecked());
                if (isChecked) {
                    mChipmunkButton.setChecked(false);
                    mHeliumButton.setChecked(false);
                    mEvilButton.setChecked(false);
                    mSlomoButton.setChecked(false);
                    mDeepButton.setChecked(false);
                    ((RecordActivity) mActivity).setTempo(FASTFWD_TEMPO);
                    mBeepFx.setTempo(FASTFWD_TEMPO);
                }
                else {
                    ((RecordActivity) mActivity).setPitchShift(NO_PITCH_SHIFT);
                    ((RecordActivity) mActivity).setTempo(NORMAL_TEMPO);
                    mBeepFx.setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setTempo(NORMAL_TEMPO);
                }
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });

        mHeliumButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mHeliumButton.isChecked());
                if (isChecked) {
                    mChipmunkButton.setChecked(false);
                    mFastFwdButton.setChecked(false);
                    mEvilButton.setChecked(false);
                    mSlomoButton.setChecked(false);
                    mDeepButton.setChecked(false);
                    ((RecordActivity) mActivity).setPitchShift(HELIUM_PITCH_SHIFT);
                    mBeepFx.setPitchShift(HELIUM_PITCH_SHIFT);
                }
                else {
                    ((RecordActivity) mActivity).setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setTempo(NORMAL_TEMPO);
                }
                // TODO determine max and min shift
                ((RecordActivity) mActivity).onPlayPause(mRecordFilePath, mIsPlaying, 0);
            }
        });
        mDeepButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mHeliumButton.isChecked());
                // ((RecordActivity) mActivity).onFileChange(mRecordFilePath, 0, 0);
                if (isChecked) {
                    mChipmunkButton.setChecked(false);
                    mFastFwdButton.setChecked(false);
                    mHeliumButton.setChecked(false);
                    mEvilButton.setChecked(false);
                    mSlomoButton.setChecked(false);
                    ((RecordActivity) mActivity).setPitchShift(DEEP_PITCH_SHIFT);
                    mBeepFx.setPitchShift(DEEP_PITCH_SHIFT);
                }
                else {
                    ((RecordActivity) mActivity).setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setPitchShift(NO_PITCH_SHIFT);
                    mBeepFx.setTempo(NORMAL_TEMPO);
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
        mReverseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged listener");
                Log.d(TAG, "check state: " + mReverseButton.isChecked());
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
