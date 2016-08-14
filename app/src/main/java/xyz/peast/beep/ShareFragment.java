package xyz.peast.beep;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ShareFragment extends Fragment {

    private static final String TAG = ShareFragment.class.getSimpleName();

    private Context mContext;
    private String mRecordFileName;

    private TextView mBeepNameTextView;


    public ShareFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);

        mBeepNameTextView = (TextView) rootView.findViewById(R.id.beep_name_textview);

        Bundle bundle = this.getArguments();
        mRecordFileName = bundle.getString(RecordActivity.RECORD_FILE_UNIQUE_NAME) + ".wav";

        String beepname = bundle.getString(RecordActivity.BEEP_NAME);
        mBeepNameTextView.setText(beepname);
        Log.d(TAG, "Record File Name: " + mRecordFileName);

        mContext = getActivity();

        return rootView;
    }


}
