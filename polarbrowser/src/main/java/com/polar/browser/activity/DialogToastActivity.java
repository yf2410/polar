package com.polar.browser.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.utils.CustomToastUtils;

/**
 * Created by Administrator on 2017/2/28.
 */

public class DialogToastActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialogtoast);
        CustomToastUtils.getInstance().showClickToast(DialogToastActivity.this , R.string.download_start, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogToastActivity.this, DownloadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                JuziApp.getInstance().startActivity(intent);
            }
        });
    }
}
