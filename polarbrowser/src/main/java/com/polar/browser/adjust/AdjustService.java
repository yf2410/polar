package com.polar.browser.adjust;

import android.app.IntentService;
import android.content.Intent;

import com.adjust.sdk.Adjust;

/**
 * Created by FKQ on 2016/10/18.
 */

public class AdjustService extends IntentService{

    public AdjustService () {
        super("AdjustService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Adjust.onResume();
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AdjustService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Adjust.onPause();
    }
}
