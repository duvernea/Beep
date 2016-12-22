package xyz.peast.beep;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duvernea on 11/14/16.
 */
public class BeepFx implements Parcelable {

    // Currently we are setting pitchshift + 8 for chipmunk and -8 for slomo

    private int mPitchShift = 0;
    private boolean mEcho = false;

    public int getmPitchShift() {
        return mPitchShift;
    }

    public void setPitchShift(int pitchshift) {
        mPitchShift = pitchshift;
    }

    public BeepFx(int pitchshift) {

        mPitchShift = pitchshift;
    }

    public boolean getEcho() {
        return mEcho;
    }

    public boolean getEditStatus() {
        if (mPitchShift == 0 && !mEcho) {
            return false;
        } else {
            return true;
        }
    }

    public void setEcho(boolean echo) {
        mEcho = echo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPitchShift);
        dest.writeByte((byte) (mEcho ? 1 : 0));
    }
    private BeepFx(Parcel in) {
        mPitchShift = in.readInt();
        mEcho = in.readByte() != 0;
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
