package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bean.ApkInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.cache.ApkIconLoader;

import java.io.File;

/**
 * Created by saifei on 16/12/30.
 */

public class ApkItemView extends RelativeLayout{


    private ImageView ivApk;
    private TextView apkName;
    private TextView apkSize;
    private TextView apkInstallState;
    private CommonCheckBox1 checkBox;
    private ApkInfo mInfo;


    public ApkItemView(Context context) {
        this(context, null);

    }

    public ApkItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setListeners();
    }

    private void setListeners() {
        checkBox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                mInfo.isChecked = isChecked;
                SimpleLog.e("DownloadingItem", "isChecked == "+isChecked);

                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    private void init(Context context) {
        inflate(context, R.layout.apk_item_layout, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));
        checkBox = (CommonCheckBox1) findViewById(R.id.check_box);
        ivApk = (ImageView)findViewById(R.id.iv_apk);
        apkName = (TextView)findViewById(R.id.tv_apk_name);
        apkSize = (TextView)findViewById(R.id.tv_apk_size);
        apkInstallState =(TextView) findViewById(R.id.apk_install_state);
    }

    public void bind(ApkInfo info, boolean isScrollState) {
        SimpleLog.d("ApkInfo","info = "+info.toString()+" isScrollState = "+isScrollState);
        mInfo = info;
        ApkIconLoader.getInstance().loadLocalImage(info.getPath(),ivApk,R.drawable.file_icon_apk,isScrollState);
        apkName.setText(new File(info.getPath()).getName());
        apkSize.setText(FileUtils.formatFileSize(info.getSize()));
        apkInstallState.setText( info.isInstalled() ?
                getContext().getString(R.string.download_apk_installed) : getContext().getString(R.string.download_apk_not_installed));
        if (mInfo.isEditing()) {
            checkBox.setVisibility(View.VISIBLE);
            if (mInfo.isChecked) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        } else {
            checkBox.setVisibility(View.GONE);
        }
    }



}
