package com.polar.browser.push.api;

import com.polar.browser.JuziApp;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SystemNewsApi;

import java.sql.SQLException;

/**
 * Created by Administrator on 2016/7/14.
 */

public class SystemNewsAsyncApi {

    private static SystemNewsAsyncApi instance;

    private SystemNewsAsyncApi() {
    }

    public static SystemNewsAsyncApi getInstance() {
        if (instance == null) {
            synchronized (SystemNewsAsyncApi.class) {
                if (instance == null) {
                    instance = new SystemNewsAsyncApi();
                }
            }
        }
        return instance;
    }

    /**
     * 异步查询未读消息数
     *
     * @return
     * @throws SQLException
     */
    public void getUnreadSystemNewsCountAsync(final ISystemNewsNumberCallback callback){

        Runnable r = new Runnable() {

            @Override
            public void run() {
				long unreadSystemNewsCount = 0;
				try {
					unreadSystemNewsCount = SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).queryUnreadSystemNewsCount();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (callback != null) {
                        callback.notifyQueryResult(unreadSystemNewsCount);
                    }

            }
        };
        ThreadManager.postTaskToIOHandler(r);
    }
}
