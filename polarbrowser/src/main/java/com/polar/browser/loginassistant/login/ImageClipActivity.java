package com.polar.browser.loginassistant.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.view.clipview.view.ClipViewLayout;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yd_lzk on 2017/4/20.
 */

public class ImageClipActivity extends LemonBaseActivity implements View.OnClickListener {

    private ClipViewLayout clipView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_clip);
        initView();
        initData();
    }

    private void initView(){
        clipView = (ClipViewLayout)findViewById(R.id.clipViewLayout);
        findViewById(R.id.clip_tv_cancel).setOnClickListener(this);
        findViewById(R.id.clip_tv_done).setOnClickListener(this);
    }

    private void initData(){
        if(getIntent()!= null && getIntent().getData() != null){
            //设置图片资源
            clipView.setImageSrc(getIntent().getData());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clip_tv_cancel:
                finish();
                break;
            case R.id.clip_tv_done:
                generateUriAndReturn();
                break;
        }
    }


    /**
     * 生成Uri并且通过setResult返回给打开的activity
     */
    private void generateUriAndReturn() {
        //调用返回剪切图
        Bitmap zoomedCropBitmap = clipView.clip();
        if (zoomedCropBitmap == null) {
            CustomToastUtils.getInstance().showTextToast(R.string.account_save_avatar_fail);
            return;
        }
        String tempAvatarName = ConfigManager.getInstance().getAvatarSaveName(true);
        FileUtils.saveBitmapToFile(zoomedCropBitmap,LogoutActivity.AVATAR_SAVE_DIR_TEMP, tempAvatarName);
        File saveFile = new File(LogoutActivity.AVATAR_SAVE_DIR_TEMP, tempAvatarName);
        if(saveFile.exists()){
            Intent intent = new Intent();
            intent.setData(Uri.fromFile(saveFile));
            setResult(RESULT_OK, intent);
            finish();
        }else{
            CustomToastUtils.getInstance().showTextToast(R.string.account_save_avatar_fail);
        }
    }
}
