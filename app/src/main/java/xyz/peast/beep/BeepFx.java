package xyz.peast.beep;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duvernea on 11/14/16.
 */
public class BeepFx implements Parcelable {

    // Currently we are setting pitchshift + 8 for chipmunk and -8 for slomo

    private float mTreble = 1f;
    private float mBass = 1f;
    private int mPitchShift = 0;
    private double mTempo = 1.0;
    private boolean mEcho = false;
    private boolean mReverse = false;
    private boolean mReverb = false;
    private boolean mRobot = false;

    public int getmPitchShift() {
        return mPitchShift;
    }

    public void setPitchShift(int pitchshift) {
        mPitchShift = pitchshift;
    }

    public double getTempo() {
        return mTempo;
    }

    public void setTempo(double tempo) {
        mTempo = tempo;
    }

    public BeepFx(int pitchshift) {

        mPitchShift = pitchshift;
    }

    public boolean getEcho() {
        return mEcho;
    }

    public boolean getReverb() { return mReverb; }

    public boolean getRobot() { return mRobot; }

    public boolean getEditStatus() {
        if (mTreble == 1.0 && mBass == 1.0 && mPitchShift == 0 &&
                !mEcho && !mReverse && !mReverb && !mRobot) {
            return false;
        } else {
            return true;
        }
    }
    public void setBass(float bass) {
        mBass = bass;
    }
    public double getBass() {
        return mBass;
    }
    public void setTreble(float treble) {
        mTreble = treble;
    }
    public double getTreble() {
        return mTreble;
    }

    public void setEcho(boolean echo) {
        mEcho = echo;
    }

    public void setReverb(boolean reverb) {mReverb = reverb; }

    public void setRobot(boolean robot) { mRobot = robot; }

    public void setReverse(boolean reverse) {mReverse = reverse;}
    public boolean getReverse() {return mReverse;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mTreble);
        dest.writeFloat(mBass);
        dest.writeInt(mPitchShift);
        dest.writeByte((byte) (mEcho ? 1 : 0));
        dest.writeByte((byte) (mReverse ? 1 : 0));
        dest.writeByte((byte) (mReverb ? 1 : 0));
        dest.writeByte((byte) (mRobot ? 1 : 0));
    }
    private BeepFx(Parcel in) {
        mTreble = in.readFloat();
        mBass = in.readFloat();
        mPitchShift = in.readInt();
        mEcho = in.readByte() != 0;
        mReverse = in.readByte() != 0;
        mReverb = in.readByte() != 0;
        mRobot = in.readByte() != 0;
    }
    public static final Parcelable.Creator<BeepFx> CREATOR
            = new Parcelable.Creator<BeepFx>() {
        public BeepFx createFromParcel(Parcel in) {
            return new BeepFx(in);
        }
        public BeepFx[] newArray(int size) {
            return new BeepFx[size];
        }
    };
}
