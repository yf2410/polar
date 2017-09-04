package com.polar.browser.setting;

import android.content.Context;
import android.content.Intent;

/**
 * Created by FKQ on 2016/11/23.
 */

public class JavascriptInterfaceFeedBack {

    private Context mContext;

    public JavascriptInterfaceFeedBack(Context context) {
        this.mContext = context;
    }

    @android.webkit.JavascriptInterface
    public void onFeedBack() {
        Intent intent = new Intent(mContext, FeedBackActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
