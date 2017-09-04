package com.polar.browser.loginassistant.login;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.common.ui.LoadingDialog;
import com.polar.browser.i.IDownloadCallBack;
import com.polar.browser.i.IUploadCallBack;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.LogOutEvent;
import com.polar.browser.sync.SettingSyncManager;
import com.polar.browser.sync.UserHomeSiteManager;
import com.polar.browser.utils.BitmapUtils;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.GlideUtils;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.view.SelectPictureDialog;
import com.polar.browser.view.clipview.view.CircleImageView;
import com.polar.browser.view.switchbutton.SwitchButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;


/**
 * Created by lzk-pc on 2017/4/1.
 * 退出登录界面
 */

public class LogoutActivity extends LemonBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = LogoutActivity.class.getSimpleName();

    //private Button mLogoutBtn;  //退出
    private Button mSaveAvatarBtn; //保存头像
    private CircleImageView mUserAvatarIv;  //用户头像
    private TextView mUsernameTv; //用户名

    //编辑头像
    private static final int ALBUM_SELECT = 1;
    private static final int CAMERA_SELECT = 2;
    private static final int CROP_SELECT = 3;
    private static final int REQUEST_CODE = 4;

    //头像保存路径
    public static final String AVATAR_SAVE_DIR = VCStoragerManager.getInstance().getImageDirPath() + "avatar/";
    public static final String AVATAR_SAVE_DIR_TEMP = AVATAR_SAVE_DIR + "temp/";  //临时头像，使用完毕及时删除
//    private static final String AVATAR_IMAGE_NAME = "avatar_temp.png";
    private String tempAvatarName;  //头像临时名称
    private String tempAvatarPath; //头像临时路径
    private boolean isEditAvatar;
    private Bitmap curAvatarBm;  //当前编辑头像、保存的Bitmap
    public static final int DEFAULT_AVATAR = R.drawable.menu_default_head;  //默认头像
    private RelativeLayout mLogBookmark;
    private RelativeLayout mHomepageIcon;
    private RelativeLayout mBrowserSet;
    private RelativeLayout mSyncInWifi;
    private RelativeLayout mTag;
    private ImageView mIvLogSet;
    private SwitchButton mBookSwithBtn;
    private SwitchButton mHomepageSwithBtn;
    private SwitchButton mSettingSwithBtn;
    private SwitchButton mTagSwithBtn;
    private SwitchButton mSyncWifiSwithBtn;
    private ScrollView mScrollView;
    private UserAccountData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        initTitleBar();
        initView();
        initListener();
        initData();
        EventBus.getDefault().register(this);
    }

    private void initListener() {
        mUserAvatarIv.setOnClickListener(this);
//        mLogoutBtn.setOnClickListener(this);
        mSaveAvatarBtn.setOnClickListener(this);
        mLogBookmark.setOnClickListener(this);
        mHomepageIcon.setOnClickListener(this);
        mBrowserSet.setOnClickListener(this);
        mTag.setOnClickListener(this);
        mSyncInWifi.setOnClickListener(this);
        mIvLogSet.setOnClickListener(this);

        mBookSwithBtn.setOnCheckedChangeListener(this);
        mHomepageSwithBtn.setOnCheckedChangeListener(this);
        mSettingSwithBtn.setOnCheckedChangeListener(this);
        mTagSwithBtn.setOnCheckedChangeListener(this);
        mSyncWifiSwithBtn.setOnCheckedChangeListener(this);
    }

    private void initTitleBar(){
        CommonTitleBar titleBar = (CommonTitleBar)findViewById(R.id.title_bar);
        titleBar.setTitleBarBackground(R.color.common_title_bar_blue);
        titleBar.setTitleColor(R.drawable.common_tv_title_white_selector);
        titleBar.setBackImg(R.drawable.common_title_white_left);
    }

    private void initView(){

        mUserAvatarIv = (CircleImageView)findViewById(R.id.iv_login_user_avatar);
        //mLogoutBtn = (Button) findViewById(R.id.btn_logout);
        mScrollView = (ScrollView) findViewById(R.id.setting_scrollview);
        mSaveAvatarBtn = (Button) findViewById(R.id.btn_save_avatar);
        mUsernameTv = (TextView) findViewById(R.id.tv_login_username);
        mLogBookmark = (RelativeLayout) findViewById(R.id.log_bookmark);
        mHomepageIcon = (RelativeLayout) findViewById(R.id.homepage_icon);
        mBrowserSet = (RelativeLayout) findViewById(R.id.browser_set);
        mTag = (RelativeLayout) findViewById(R.id.tag);
        mSyncInWifi = (RelativeLayout) findViewById(R.id.sync_in_wifi);
        mIvLogSet = (ImageView) findViewById(R.id.iv_log_set);

        mBookSwithBtn = (SwitchButton) findViewById(R.id.sb_bookmark);
        mHomepageSwithBtn = (SwitchButton) findViewById(R.id.sb_homepage_icon);
        mSettingSwithBtn = (SwitchButton) findViewById(R.id.sb_browser_setting);
        mTagSwithBtn = (SwitchButton) findViewById(R.id.sb_tag);
        mSyncWifiSwithBtn = (SwitchButton) findViewById(R.id.sb_sync_in_wifi);
    }

    private void initData(){
        initDefault();
        //获取用户信息
        userData = JuziApp.getUserAccountData();
        if(userData != null){
            String localUserAvatarPath = ConfigManager.getInstance().getLocalUserAvatarPath();
            SimpleLog.d(TAG," userAvatar = "+ userData.getAvatar()+" getAvatarLastModified = "+ userData.getAvatarLastModified() +" localUserAvatarPath = "+localUserAvatarPath);
            if(userData.getAvatar() != null){
                //显示用户头像
                GlideUtils.loadCircleImage(this, userData.getAvatar(), userData.getAvatarLastModified(),
                        mUserAvatarIv,
                        Drawable.createFromPath(localUserAvatarPath),
                        DEFAULT_AVATAR);

                //保存用户头像到本地（重新登录后，本地头像在完成占位显示后，重新更新）
                if(localUserAvatarPath == null || !ConfigManager.getInstance().isLocalUserAvatarValid()){
                    final String path = AVATAR_SAVE_DIR + ConfigManager.getInstance().getAvatarSaveName(false);
                    GlideUtils.loadImageToFile(this.getApplicationContext(), userData.getAvatar(), userData.getAvatarLastModified(), path, new IDownloadCallBack<String>() {
                        @Override
                        public void onDownloadSuccess(String source) {
                            ConfigManager.getInstance().setLocalUserAvatarPath(path);
                        }
                        @Override
                        public void onDownloadFailed(String error) {
                        }
                    });
                }
            }
            if (!TextUtils.isEmpty(userData.getUsername())) {
                mUsernameTv.setText(userData.getUsername());
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        switchFuncBtn(); //更新按钮状态
        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 切换功能按钮的显示状态
     */
    private void switchFuncBtn() {
        if(isEditAvatar && curAvatarBm != null){
            //mLogoutBtn.setVisibility(View.GONE);
            mSaveAvatarBtn.setVisibility(View.VISIBLE);
            if (mScrollView != null) {
                mScrollView.setVisibility(View.GONE);
            }
        }else{
            //mLogoutBtn.setVisibility(View.VISIBLE);
            mSaveAvatarBtn.setVisibility(View.GONE);
            if (mScrollView != null) {
                mScrollView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            //退出登录
            /*case R.id.btn_logout:
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_LOGOUT);
                //云端备份书签。
                BookmarkManager.getInstance().logOutAndSync(this);
                SettingSyncManager.getInstance().syncSetting(SettingSyncManager.SYNC_TYPE_LOGOUT);
                //清空用户数据
                JuziApp.clearUserAccountData();
                AccountKit.logOut();
                Intent intent = new Intent(LogoutActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;*/
            case R.id.btn_save_avatar:
                if (!NetworkUtils.isNetWorkConnected(this)) {
                    CustomToastUtils.getInstance().showTextToast(R.string.account_save_avatar_fail);
                    return;
                }
                if(curAvatarBm == null) return;
                String avatarPath = tempAvatarPath; //存放临时图片的路径
                if(avatarPath == null){
                    tempAvatarName = ConfigManager.getInstance().getAvatarSaveName(true);
                    avatarPath = saveBitmapToFile(curAvatarBm,tempAvatarName);// 保存在SD卡中  TODO IO
                }
                final File file = new File(avatarPath);
                if(file.exists()){
                    showLoadingDialog(); //显示发送对话框
                    //用户头像上传服务器
                    String userToken = ConfigManager.getInstance().getUserToken();
                    AccountLoginManager.getInstance().uploadUserImage(userToken, file, new IUploadCallBack<String>(){
                        @Override
                        public void onUploadSuccess(String imgPath) {
                            //更新本地用户头像
                            String toPath = AVATAR_SAVE_DIR + ConfigManager.getInstance().getAvatarSaveName(false);
                            FileUtils.deleteFileWithPrefix(LogoutActivity.AVATAR_SAVE_DIR,JuziApp.getUserAccountData().getAccountType());
                            boolean moveResult = FileUtils.moveFileByRename(file,toPath);  //TODO IO
                            String avatarPath = moveResult ? toPath : file.getPath();
                            JuziApp.getUserAccountData().setAvatar(avatarPath);

                            long updateTime = System.currentTimeMillis();
                            JuziApp.getUserAccountData().setAvatarLastModified(updateTime); //更新头像修改时间(退出后清空)
                            ConfigManager.getInstance().setLocalUserAvatarLastModified(updateTime); //更新本地保存头像修改时间（退出后不清空）

                            JuziApp.updateUserAccountData(JuziApp.getUserAccountData());
                            //保存头像到本地，退出登录仍然有效
                            ConfigManager.getInstance().setLocalUserAvatarPath(avatarPath);

                            dismissLoadingDialog(); //关闭发送对话框
                            CustomToastUtils.getInstance().showTextToast(R.string.account_save_avatar_success);
                            isEditAvatar = false;
//                            userData.setAvatar(tempAvatarPath);
                            switchFuncBtn();
                        }

                        @Override
                        public void onUploadFailed(String error) {
                            dismissLoadingDialog(); //关闭发送对话框
                            CustomToastUtils.getInstance().showTextToast(R.string.account_save_avatar_fail);
                        }
                    });
                }
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_SAVE_AVATAR);
                break;
            case R.id.iv_login_user_avatar:  //点击用户头像
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_EDIT_AVATAR);
                isEditAvatar = false;  //初始化编辑状态
                if(curAvatarBm != null){
                    curAvatarBm.recycle();
                    curAvatarBm = null;
                }
                showDialog();
                break;
            //书签
            case R.id.log_bookmark:
                clickBookmark();
                break;
            //主页图标
            case R.id.homepage_icon:
                clickHomepage();
                break;
            //浏览器设置
            case R.id.browser_set:
                clickBrowserSetting();
                break;
            //标签
            case R.id.tag:
                clickWarning();
                break;
            //仅在wifi下自动同步
            case R.id.sync_in_wifi:
                clickSyncInWifi();
                break;
            //设置页面
            case R.id.iv_log_set:
                startActivityForResult(new Intent(this,AccountManagementActivity.class) , REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            default:
                break;
        }
    }

    private void clickSyncInWifi() {
        mSyncWifiSwithBtn.slideToChecked(!mSyncWifiSwithBtn.isChecked());
    }

    private void clickWarning() {
        mTagSwithBtn.slideToChecked(!mTagSwithBtn.isChecked());
    }

    private void clickBrowserSetting() {
        mSettingSwithBtn.slideToChecked(!mSettingSwithBtn.isChecked());
    }

    private void clickHomepage() {
        mHomepageSwithBtn.slideToChecked(!mHomepageSwithBtn.isChecked());
    }

    private void clickBookmark() {
        mBookSwithBtn.slideToChecked(!mBookSwithBtn.isChecked());
    }

    private LoadingDialog loadingDialog;
    private boolean isNeedShow = true;  //判断是否需要显示加载框

    private void showLoadingDialog(){
        mUserAvatarIv.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(loadingDialog == null){
                    loadingDialog = new LoadingDialog(LogoutActivity.this);
                    loadingDialog.setMessage(LogoutActivity.this.getString(R.string.feed_back_commit));
                }
                if(isNeedShow){
                    loadingDialog.show();  //显示加载图片对话框
                }
            }
        },500);  //延时目的：当下载时间很小时，不显示加载框

    }
    private void dismissLoadingDialog(){
        isNeedShow = false;
        if(loadingDialog != null){
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ALBUM_SELECT:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }

                break;
            case CAMERA_SELECT:
                if (resultCode == RESULT_OK) {
                    if(tempAvatarName != null){
                        File temp = new File(AVATAR_SAVE_DIR_TEMP + tempAvatarName);
                        cropPhoto(Uri.fromFile(temp));// 裁剪图片
                    }

                }

                break;
            case CROP_SELECT:
                if (data != null) {
                    final Uri uri = data.getData();
                    if (uri == null) {
                        return;
                    }
                    String cropImagePath = getRealFilePathFromUri(getApplicationContext(), uri);
                    if(cropImagePath != null){
                        Bitmap head = BitmapFactory.decodeFile(cropImagePath);
                        if (head != null) {
                            /**
                             * 上传服务器代码
                             */
/*                            tempAvatarName = ConfigManager.getInstance().getAvatarSaveName(true);
                            String path = saveBitmapToFile(head,tempAvatarName);// 保存在SD卡中  TODO IO*/
                            curAvatarBm = head;
                            tempAvatarPath = cropImagePath;
                         GlideUtils.loadCircleImage(LogoutActivity.this,cropImagePath,System.currentTimeMillis(),
                                    mUserAvatarIv,
                                    BitmapUtils.getRoundDrawableByFile(getApplicationContext(),cropImagePath),
                                    DEFAULT_AVATAR);
//                            mUserAvatarIv.setImageBitmap(head);
                            //userData.setAvatar(cropImagePath);
                        }
                    }
                }
                break;
            case REQUEST_CODE:
                if(data == null) return;
                String nickName = data.getStringExtra("nickName");
//                String avatar = data.getStringExtra("avatar");
//                Glide.with(LogoutActivity.this).load(avatar).centerCrop().into(mUserAvatarIv);
                mUsernameTv.setText(nickName);
//                SimpleLog.d(TAG,"avatar = "+avatar +" store avatar = "+JuziApp.getUserAccountData().getAvatar());
                //GlideUtils.loadCircleImage(this,avatar,JuziApp.getInstance().getUserAccountData().getAvatarLastModified(),mUserAvatarIv,Drawable.createFromPath(ConfigManager.getInstance().getLocalUserAvatarPath()),DEFAULT_AVATAR);
                //GlideUtils.loadCircleImage(this,avatar,System.currentTimeMillis(),mUserAvatarIv,null,DEFAULT_AVATAR);
                break;
        }

    }

    private void showDialog(){
        final SelectPictureDialog dialog = new SelectPictureDialog(this);
        dialog.setOnItemClickListener(new SelectPictureDialog.OnItemClickListener() {
            @Override
            public void onAlbum() {
                if (ActivityCompat.checkSelfPermission(LogoutActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Check Permissions Now
                    // Callback onRequestPermissionsResult interceptado na Activity MainActivity
                    ActivityCompat.requestPermissions(LogoutActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }else {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent,ALBUM_SELECT);
                    dialog.dismiss();
                }
            }

            @Override
            public void onCamera() {
                if (ActivityCompat.checkSelfPermission(LogoutActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Check Permissions Now
                    // Callback onRequestPermissionsResult interceptado na Activity MainActivity
                    ActivityCompat.requestPermissions(LogoutActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 0);
                } else {
                    // permission has been granted, continue as usual
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    tempAvatarName = ConfigManager.getInstance().getAvatarSaveName(true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(AVATAR_SAVE_DIR_TEMP,tempAvatarName )));
                    startActivityForResult(intent, CAMERA_SELECT);// 采用ForResult打开
                }

                dialog.dismiss();
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 调用系统的裁剪功能
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        isEditAvatar = true;
        Intent intent = new Intent(this,ImageClipActivity.class);
        intent.setData(uri);
        startActivityForResult(intent,CROP_SELECT);
    }


    /**
     * 根据Uri返回文件绝对路径
     * 兼容了file:///开头的 和 content://开头的情况
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private String saveBitmapToFile(Bitmap mBitmap ,String imgName) {
        if(imgName == null) return null;
        String directory = AVATAR_SAVE_DIR_TEMP;
        File dir = new File(directory);
        if(dir.exists()){  //删掉临时头像目录，保证目录中最多一张临时图片，且与之前的图片不重名
            FileUtils.deleteFileOrDirectory(dir);
        }
        FileUtils.saveBitmapToFile(mBitmap, directory, imgName);
        return directory + imgName; //存储路径
    }

    /**
     * 该方法待定是否删除
     * @param intent
     * @param requestCode
     */
    private void startActivityForResultByOne(Intent intent,int requestCode) {
        try{
            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);
            if(resolveInfos != null && resolveInfos.size()>0){
                ResolveInfo resolveInfo = resolveInfos.get(0);
                if(resolveInfo != null){
                    String pkg = resolveInfo.activityInfo.packageName;
                    String cls = resolveInfo.activityInfo.name;
                    intent.setComponent(new ComponentName(pkg, cls));
                    startActivityForResult(intent, requestCode);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(curAvatarBm != null){
            curAvatarBm.recycle();
            curAvatarBm = null;
        }
        if(loadingDialog != null){
            if(loadingDialog.isShowing()){ loadingDialog.dismiss();}
            loadingDialog = null;
        }

        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onLogOutEvent(LogOutEvent event) {
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sb_bookmark:
                if (isChecked) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_BOOKMARK_ON);
                } else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_BOOKMARK_OFF);
                    //ConfigManager.getInstance().setSyncBookmarkTimeStamp("");
                }
                ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_BOOKMARK, isChecked);
                ConfigWrapper.apply();
                break;
            case R.id.sb_homepage_icon:
                if (isChecked) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_HOME_LOGO_ON);
                } else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_HOME_LOGO_OFF);
                    UserHomeSiteManager.getInstance().setHomeSiteSyncTime(ConfigWrapper.get(ConfigDefine.USER_ID, ""), "");
                }
                ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_HOMEPAGE, isChecked);
                ConfigWrapper.apply();
                break;
            case R.id.sb_browser_setting:
                if (isChecked) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_SETTING_ON);
                } else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_SETTING_OFF);
                    SettingSyncManager.getInstance().setSettingSyncTime(ConfigWrapper.get(ConfigDefine.USER_ID, ""), "");
                }
                ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_BROWSER_SETTING, isChecked);
                ConfigWrapper.apply();
                break;
            case R.id.sb_tag:
                if (isChecked) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_TAB_ON);
                } else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_TAB_OFF);
                }
                ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_TAG, isChecked);
                ConfigWrapper.apply();
                break;
            case R.id.sb_sync_in_wifi:
                if (isChecked) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_ONLY_WIFI_ON);
                } else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.PERSONAL_CENTER, GoogleConfigDefine.PC_CLOUD_SYNC, GoogleConfigDefine.PC_CLOUD_SYNC_ONLY_WIFI_OFF);
                }
                ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI, isChecked);
                ConfigWrapper.apply();
                break;
            default:
                break;
        }
    }

    private void initDefault() {
        boolean bookmark = ConfigManager.getInstance().isBookmarkSync();
        boolean homepage = ConfigManager.getInstance().isHomesiteSync();
        boolean setting = ConfigManager.getInstance().isSettingSync();
        boolean wifi = ConfigManager.getInstance().isOnlyWifiSync();
        mBookSwithBtn.setChecked(bookmark);
        mHomepageSwithBtn.setChecked(homepage);
        mSettingSwithBtn.setChecked(setting);
        //mTagSwithBtn.setChecked(tag);
        mSyncWifiSwithBtn.setChecked(wifi);
    }

}