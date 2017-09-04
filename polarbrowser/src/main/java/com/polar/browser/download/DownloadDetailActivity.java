package com.polar.browser.download;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.utils.FileUtils;

/**
 * Created by duan on 16/9/28.
 */
public class DownloadDetailActivity extends LemonBaseActivity{

    private DownloadItemInfo mInfo;

    private TextView mTvFileName;
    private TextView mTvFileSize;
    private TextView mTvSupportPoint;
    private TextView mTvFilePath;
    private TextView mTvFileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_detail);
        initView();
        initData();
    }

    private void initView() {
        mTvFileName = (TextView) findViewById(R.id.tv_file_name);
        mTvFileSize = (TextView) findViewById(R.id.tv_file_size);
        mTvSupportPoint = (TextView) findViewById(R.id.tv_support_point);
        mTvFilePath = (TextView) findViewById(R.id.tv_file_path);
        mTvFileUrl = (TextView) findViewById(R.id.tv_file_url);
    }

    private void initData() {
        if (getIntent() != null && getIntent().hasExtra("DownloadItemInfo")) {
            mInfo = (DownloadItemInfo)(getIntent().getSerializableExtra("DownloadItemInfo"));

            mTvFileName.setText(mInfo.getFilename());
            mTvFileSize.setText(FileUtils.formatFileSize(mInfo.mTotalBytes));
            if (mInfo.isContuningDownloadSupported()) {
                mTvSupportPoint.setText(R.string.support);
            } else {
                mTvSupportPoint.setText(R.string.not_support);
            }
            mTvFilePath.setText(getSavePath());
            mTvFileUrl.setText(mInfo.mUrl);

        }
    }

    private String getSavePath() {
        String savePath = mInfo.mFilePath;
        if (!TextUtils.isEmpty(savePath) && (savePath.lastIndexOf("/") != -1)) {
            savePath = savePath.substring(0, savePath.lastIndexOf("/"));
        }
        return savePath;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
    }

}
