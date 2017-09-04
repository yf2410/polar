package com.polar.browser.download;

import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.BitmapUtils;
import com.polar.browser.utils.LoadingCircleDialog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.ToastDialog;

import java.io.IOException;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


public class SetWallpaperActivity extends LemonBaseActivity implements View.OnClickListener {

    private static final String TAG = SetWallpaperActivity.class.getSimpleName();

    private String image_path;
    private PhotoView wallpaper_image;
    private static final int MSG_FAILED = 0;
    private static final int MSG_SUCCESS = 1;
    private boolean isOperating = false;

    private ToastDialog toastDialog;

    private LoadingCircleDialog loadingDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FAILED:
                    dismissLoadingDialog();
                    showCustomToast(getResources().getString(R.string.menu_set_failed));
                    break;
                case MSG_SUCCESS:
                    dismissLoadingDialog();
                    showCustomToast(getResources().getString(R.string.menu_set_successful));
                    this.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },1500);
                    break;
            }
        }
    };

    private Bitmap wallPaperBm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set_wallpaper);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SysUtils.setFullScreen(this, true);
    }

    private void initViews() {
        findViewById(R.id.wallpaper_back).setOnClickListener(this);
        findViewById(R.id.wallpaper_set).setOnClickListener(this);
        final View titleBar = findViewById(R.id.wallpaper_title_bar);
        final View wallpaper_set = findViewById(R.id.wallpaper_set);
        wallpaper_image = (PhotoView) findViewById(R.id.wallpaper_image);
        wallpaper_image.setAdjustViewBounds(true);
        wallpaper_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        wallpaper_image.setZoomable(true);
        wallpaper_image.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                titleBar.setVisibility(titleBar.getVisibility()== View.VISIBLE?View.GONE:View.VISIBLE);
                wallpaper_set.setVisibility(wallpaper_set.getVisibility()== View.VISIBLE?View.GONE:View.VISIBLE);
            }
        });

        if(getIntent() != null){
            image_path = getIntent().getStringExtra("image_path");
        }
        if(image_path != null){
            wallPaperBm = BitmapUtils.decodeSampledBitmapFromFile(image_path, AppEnv.SCREEN_WIDTH, AppEnv.SCREEN_HEIGHT + getNavigationBarHeight(), true);
            wallpaper_image.setImageBitmap(wallPaperBm);
        }

    }


    private int getNavigationBarHeight() {
        int height = 0;
        try {
            Resources resources = getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
            height = resources.getDimensionPixelSize(resourceId);
        } catch (Exception e) {

        }
        return height;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wallpaper_back:
                finish();
//                Statistics.sendOnceStatistics(GoogleConfigDefine.FILE_MANAGER_STATISTICS,GoogleConfigDefine.PIC,
//                        GoogleConfigDefine.PIC_CAN_WALLPAPER);
                break;
            case R.id.wallpaper_set:
                setImageWallpaper();
//                Statistics.sendOnceStatistics(GoogleConfigDefine.FILE_MANAGER_STATISTICS,GoogleConfigDefine.PIC,
//                        GoogleConfigDefine.PIC_CONFIRM_WALLPAPER);
                break;
        }
    }

    @Override
    public void finish() {
        if(wallPaperBm!=null){
            wallPaperBm.recycle();
            wallPaperBm = null;
        }
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setImageWallpaper() {
        if(isOperating == true) return;
        showLoadingDialog();
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                isOperating = true;
//                WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
//                try {
//                    Bitmap visibleBM = wallpaper_image.getDrawingCache();
//                    manager.setBitmap(visibleBM);
//                    handler.sendEmptyMessage(MSG_SUCCESS);
//                } catch (IOException e) {
//                    handler.sendEmptyMessage(MSG_FAILED);
//                }finally {
//                    isOperating = false;
//                }
//            }
//        };
//        thread.start();
        ThreadManager.postTaskToIOHandler(new Task());
    }

    private  class Task implements Runnable{

        @Override
        public void run() {
            isOperating = true;
            WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
            try {
                Bitmap visibleBM = wallpaper_image.getDrawingCache();
                manager.setBitmap(visibleBM);
                handler.sendEmptyMessage(MSG_SUCCESS);
            } catch (IOException e) {
                handler.sendEmptyMessage(MSG_FAILED);
            }finally {
                isOperating = false;
            }
        }
    }


    private void showLoadingDialog(){
        if(loadingDialog == null){
            loadingDialog = new LoadingCircleDialog(this);
        }
        loadingDialog.show();
    }
    private void dismissLoadingDialog(){
        if(loadingDialog != null){
            loadingDialog.dismiss();
        }
    }

    private void showCustomToast(String message){
        if(toastDialog == null){
            toastDialog = new ToastDialog(this);
            toastDialog.setShowTime(ToastDialog.DEFAULT_SHOW_TIME_SHORT);
        }
        toastDialog.setMessage(message);
        if(toastDialog.isShowing()) return;
        toastDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
