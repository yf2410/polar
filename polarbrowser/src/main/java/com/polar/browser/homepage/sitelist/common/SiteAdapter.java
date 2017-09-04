package com.polar.browser.homepage.sitelist.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.homepage.sitelist.recommand.RecommendItem;
import com.polar.browser.vclibrary.bean.Site;

public class SiteAdapter extends JZBaseAdapter<Site> {

    public SiteAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, Site data, ViewGroup parent, int type) {
        RecommendItem item = new RecommendItem(context);
        return item;
    }

    @Override
    public void bindView(View view, int position, Site data) {
        RecommendItem item = (RecommendItem) view;
        item.bind(data,position);
//        EventBus.getDefault().register(this);
    }
}
