package com.polar.browser.homepage.sitelist.common;

import android.os.Bundle;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonTitleBar;

/**
 * Created by James on 2016/9/23.
 */

public abstract class SiteListActivity extends LemonBaseActivity {
    protected CommonTitleBar commonTitleBar;

    protected SiteListView siteListView;
    protected SiteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.CustomThemeActionBarOverlay);
        setContentView(R.layout.activity_site_list);
        commonTitleBar = ((CommonTitleBar) findViewById(R.id.title));
        siteListView = ((SiteListView) findViewById(R.id.site_list_view));
        adapter = new SiteAdapter(getApplicationContext());
        siteListView.setAdapter(adapter);
//        EventBus.getDefault().register(this);
    }

    public void setTitle(CharSequence title) {
        commonTitleBar.setTitle(title);
    }
}
