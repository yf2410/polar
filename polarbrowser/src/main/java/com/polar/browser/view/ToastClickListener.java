package com.polar.browser.view;

import android.content.Intent;
import android.view.View;

import com.polar.browser.JuziApp;
import com.polar.browser.download.DecompresstionFolderActivity;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.download_refactor.Constants;

/**
 * Created by lzk-pc on 2017/3/28.
 */

public class ToastClickListener implements View.OnClickListener {

    public static final int EVENT_CLICK_OPEN_FILE = 1; //
    public static final int EVENT_CLICK_DOWNLOAD = 2; //
    public static final int EVENT_CLICK_OPEN_IMAGE = 3; //

    private int event;
    private String filePath;
    public ToastClickListener(int event) {
        this.event = event;
    }
    public ToastClickListener(int event, String filePath) {
        this.event = event;
        this.filePath = filePath;
    }
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (event){
            case EVENT_CLICK_OPEN_FILE:
                intent = new Intent(JuziApp.getAppContext(), DecompresstionFolderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                JuziApp.getAppContext().startActivity(intent);
                break;
            case EVENT_CLICK_DOWNLOAD:
                intent = new Intent(JuziApp.getAppContext(), DownloadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                JuziApp.getAppContext().startActivity(intent);
                break;
            case EVENT_CLICK_OPEN_IMAGE:
                intent = new Intent(JuziApp.getAppContext(), FileClassifyDetailActivity.class);
                intent.putExtra(FileClassifyDetailActivity.TYPE, Constants.TYPE_IMAGE);
                intent.putExtra(FileClassifyDetailActivity.FILE_PATH, filePath);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                JuziApp.getAppContext().startActivity(intent);
                break;
            default:
                break;
        }
    }
}
