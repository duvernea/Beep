package xyz.peast.beep;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by duvernea on 7/30/16.
 */
public class SaveFragment extends Fragment {

    private static final String TAG = SaveFragment.class.getSimpleName();

    private Context mContext;

    private Spinner mBoardSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_save, container, false);

        mContext = getActivity();

        mBoardSpinner = (Spinner) rootView.findViewById(R.id.board_name_spinner);
        String[] spinnerItems = new String[] {"myBeeps", "Sweetie", "Mom & Dad", "Work Crewz"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, spinnerItems);
        mBoardSpinner.setAdapter(adapter);
        return rootView;
    }
}