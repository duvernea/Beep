package xyz.peast.beep;

/**
 * Created by duvernea on 11/14/16.
 */
public class BeepFx {

    // Currently we are setting pitchshift + 8 for chipmunk and -8 for slomo

    private int mPitchShift = 0;

    public int getmPitchShift() {
        return mPitchShift;
    }

    public void setPitchShift(int pitchshift) {
        mPitchShift = pitchshift;
    }

    public BeepFx(int pitchshift) {

        mPitchShift = pitchshift;
    }
}
