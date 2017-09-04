package com.polar.browser.homepage.sitelist.recommand.bookmark;

import android.os.Bundle;

import com.polar.browser.R;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.homepage.sitelist.common.SiteListActivity;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.common.CommonCallback;

import java.util.List;

public class SiteFromBookmarkActivity extends SiteListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.favorite);
        updateHistory(new CommonCallback<List<Site>>() {
            @Override
            public void onSuccess(List<Site> sites) throws Exception {
                adapter.updateData(sites);
            }

            @Override
            public void onError(Exception e) {
//                do nothing
            }
        });
    }

    private void updateHistory(final CommonCallback<List<Site>> callback) {
        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Site> sites = BookmarkManager.getInstance().queryBookmarkToSite();
                    callback.onSuccess(sites);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
}
