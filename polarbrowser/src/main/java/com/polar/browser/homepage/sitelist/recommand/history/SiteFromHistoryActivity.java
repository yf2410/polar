package com.polar.browser.homepage.sitelist.recommand.history;

import android.os.Bundle;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.homepage.sitelist.common.SiteListActivity;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.common.CommonCallback;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2016/9/23.
 */

public class SiteFromHistoryActivity extends SiteListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.history);
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
                    //List<Site> sites = HistoryManager.getInstance().queryHistoryToSite(100, true);
                    List<Site> sites = new ArrayList<>();

                    List<HistoryRecord> historyRecords = HistoryRecordApi.getInstance(CustomOpenHelper
                            .getInstance(SiteFromHistoryActivity.this)).queryAllHistoryRecord(100);
                    if (!ListUtils.isEmpty(historyRecords)) {
                        for (int i = 0; i < historyRecords.size(); i++) {
                            HistoryRecord historyRecord = historyRecords.get(i);
                            String iconPath = String.format("%s/%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
                                    CommonData.ICON_DIR_NAME, UrlUtils.getHost(historyRecord.getHistoryAddr()));
                            sites.add(new Site(historyRecord.getHistoryTitle(),historyRecord.getHistoryAddr(),iconPath));
                        }
                    }
                    callback.onSuccess(sites);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
}
