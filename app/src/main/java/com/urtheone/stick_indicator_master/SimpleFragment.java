package com.urtheone.stick_indicator_master;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SimpleFragment extends Fragment {

    private static final String SECTION_COLOR = "section-color";

    public static SimpleFragment newInstance(int color) {
        SimpleFragment fragment = new SimpleFragment();
        Bundle args = new Bundle();
        args.putInt(SECTION_COLOR, color);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.setBackgroundColor(ContextCompat.getColor(getContext(), getArguments().getInt(SECTION_COLOR)));
        return rootView;
    }

}
