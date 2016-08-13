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

    public int getKey() {
        return mKey;
    }

    public void setKey(int key) {
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }


}
