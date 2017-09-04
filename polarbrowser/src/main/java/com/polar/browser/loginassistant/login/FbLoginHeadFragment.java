package com.polar.browser.loginassistant.login;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.R;

/**
 * Created by FKQ on 2017/4/4.
 */

public class FbLoginHeadFragment extends Fragment{

//    public View onCreateView(
//            final LayoutInflater inflater,
//            final ViewGroup container,
//            final Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//        }
//
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        if (view == null) {
//            view = inflater.inflate(R.layout.fragment_login_head, container, false);
//            TextView viewById = (TextView) view.findViewById(R.id.title_view);
//            viewById.setText("vc browser");
//        }
//        return view;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_head, container, false);
        return view;
    }



    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

    }


}
