package xyz.peast.beep.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.peast.beep.R;

/**
 * Created by duverneay on 8/4/16.
 */
public class BoardSpinnerAdapter extends ArrayAdapter<Board> implements SpinnerAdapter {

    private Context context;
    //private Board[] values;
    private ArrayList<Board> values;
    int resource;
    int textViewResourceId;

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Board getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        super.getView(position, convertView, parent);
        TextView label = new TextView(context);
//        label.setTextColor(Color.RED);
//        label.setText(values.get(position).getName());
//
//        return label;

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resource, null);
        }
        TextView tv=(TextView) v.findViewById(textViewResourceId);
        tv.setText(this.getItem(position).getName());
        return v;
    }
    public BoardSpinnerAdapter(Context context, int resource, int textViewResourceId, ArrayList<Board> values) {
        super(context, resource, textViewResourceId, values);
        this.context = context;
        this.values = values;
        this.textViewResourceId = textViewResourceId;
        this.resource = resource;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resource, null);
        }
        TextView tv=(TextView) v.findViewById(textViewResourceId);
        tv.setText(this.getItem(position).getName());
        int numValues = values.size();
        if (position == numValues - 1) {
            tv.setTextColor(Color.BLUE);
            tv.setTypeface(null, Typeface.BOLD);
        }
//        switch (position) {
//            case numValues:
//                //set tv's color here...
//                tv.setTextColor(Color.BLUE);
//                break;
//            default:
//                //set default color or whatever...
//        }
        return v;
    }
}
