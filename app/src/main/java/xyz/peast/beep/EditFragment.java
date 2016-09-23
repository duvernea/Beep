package xyz.peast.beep;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by duverneay on 9/22/16.
 */
public class EditFragment extends Fragment {

    private static final String TAG = EditFragment.class.getSimpleName();

    Context mContext;
    Activity mActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        mContext = getActivity();

        return rootView;
    }
}
