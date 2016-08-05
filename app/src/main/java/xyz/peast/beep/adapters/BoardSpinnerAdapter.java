package xyz.peast.beep.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by duverneay on 8/4/16.
 */
public class BoardSpinnerAdapter extends ArrayAdapter<Board> {

    public BoardSpinnerAdapter(Context context, int textViewResourceId, ArrayList<Board> items) {
        super(context, textViewResourceId, items);
    }

}
