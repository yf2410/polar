package com.polar.browser.homepage.sitelist.classify;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.base.JZBaseAdapter;

public class ClassifyAdapter extends JZBaseAdapter<ClassifyItemInfo> {

    public ClassifyAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, ClassifyItemInfo data, ViewGroup parent, int type) {
        return new ClassifyItem(context);
    }

    @Override
    public void bindView(View view, int position, ClassifyItemInfo data) {
        ClassifyItem item = (ClassifyItem) view;
        item.bind(data);
    }
}
