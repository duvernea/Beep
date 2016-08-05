package xyz.peast.beep.adapters;

/**
 * Created by duverneay on 8/4/16.
 */
public class Board {

    private int mKey;
    private String mName;
    private String mImage;
    private long mDate;

    public Board(int key, String name, String image, long date) {
        mKey = key;
        mName = name;
        mImage = image;
        mDate = date;
    }

    public int getmKey() {
        return mKey;
    }

    public void setmKey(int key) {
        mKey = key;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String name) {
        mName = name;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String image) {
        mImage = image;
    }

    public long getmDate() {
        return mDate;
    }

    public void setmDate(long date) {
        mDate = date;
    }
}
