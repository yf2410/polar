package com.polar.browser.homepage.sitelist.classify.details;

import android.os.Bundle;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.homepage.sitelist.common.SiteAdapter;
import com.polar.browser.homepage.sitelist.common.SiteListActivity;
import com.polar.browser.homepage.sitelist.common.SiteListView;
import com.polar.browser.vclibrary.bean.events.SyncSiteItemHideEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by James on 2016/9/21.
 */

public class ClassifyActivity extends SiteListActivity {
    public static final String SITE_TYPE = "siteType";
    private CommonTitleBar title;
    private SiteListView siteListView;
    private int[] titleResources = new int[]{R.string.navigate_title_video,
            R.string.navigate_title_social,
            R.string.navigate_title_news,
            R.string.navigate_title_games,
            R.string.navigate_title_sports,
            R.string.navigate_title_music,
            R.string.navigate_title_shop,
            R.string.navigate_title_life};
    private int siteType;

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int siteType = getIntent().getIntExtra(SITE_TYPE, -1);
        if (siteType - 2 >= titleResources.length || siteType < 0) {
            return;
        }
        this.siteType = siteType;
        setTitle(titleResources[siteType - 2]);
        siteListView = ((SiteListView) findViewById(R.id.site_list_view));
        siteListView.setAdapter(new SiteAdapter(getApplicationContext()));
        siteListView.refreshData(siteType);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncSiteItemHide(SyncSiteItemHideEvent syncSiteItemHideEvent) {
        int position = syncSiteItemHideEvent.getPosition();
//        mAdapter.remove(position);
        siteListView.refreshSiteItemHide(position);
     }
}
