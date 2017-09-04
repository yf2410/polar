package com.polar.browser.loginassistant.login;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.facebook.accountkit.AccountKit;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.i.IUploadCallBack;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.LogOutEvent;
import com.polar.browser.sync.SettingSyncManager;
import com.polar.browser.sync.UserHomeSiteManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.GlideUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.login.PostUserAccountJson;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.view.SelectGenderDialog;
import com.polar.browser.view.SelectPictureDialog;
import com.polar.browser.view.clipview.view.CircleImageView;


import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by yangfan on 2017/5/10.
 */

public class AccountManagementActivity extends LemonBaseActivity implements View.OnClickListener{

    private RelativeLayout mAvatar;
    private RelativeLayout mNickname;
    private RelativeLayout mGender;
    private RelativeLayout mBirth;
    private String tempAvatarName;  //头像临时名称

    //编辑头像
    private static final int ALBUM_SELECT = 1;
    private static final int CAMERA_SELECT = 2;
    private static final int CROP_SELECT = 3;
    private static final int REQUEST_CODE = 4;
    private TextView mPersonGender;
    private TimePickerView timePickerViewDate;
    private TextView mBrith;
    private TextView mUserName;
    private TextView mTvGender;
    private CircleImageView mIvAvatar;
    private TextView mtvNickname;
    private Button mLogoutBtn;
    private boolean isEditAvatar;
    private Bitmap curAvatarBm;
    private String tempAvatarPath; //头像临时路径
    private UserAccountData userAccountData;
    //头像保存路径
    public static final String AVATAR_SAVE_DIR = VCStoragerManager.getInstance().getImageDirPath() + "avatar/";
    public static final String AVATAR_SAVE_DIR_TEMP = AVATAR_SAVE_DIR + "temp/";  //临时头像，使用完毕及时删除
    private boolean isChanged = false;
    private int RESULT_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);
        initTitleBar();
        initView();
        initListener();
        initData();
    }

    private void initData() {
        userAccountData = JuziApp.getUserAccountData();
        SimpleLog.d("tag" , "userdata" + userAccountData.toString());
        reflushUI(userAccountData);
    }

    /**
     * 第一次进来获取到的用户信息
     */
    private void reflushUI(UserAccountData data) {
        //性别
        if(!TextUtils.isEmpty(data.getGender())){
            if(data.getGender().equals("M")){
                mTvGender.setText(getResources().getString(R.string.male));
            }else if(data.getGender().equals("F")){
                mTvGender.setText(getResources().getString(R.string.female));
            }
        }
        //头像
        if(!TextUtils.isEmpty(data.getAvatar())){
            String localUserAvatarPath = ConfigManager.getInstance().getLocalUserAvatarPath();
            //Glide.with(AccountManagementActivity.this).load(data.getAvatar()).centerCrop().into(mIvAvatar);
            GlideUtils.loadCircleImage(this,data.getAvatar(),data.getAvatarLastModified(),
                    mIvAvatar,
                    Drawable.createFromPath(localUserAvatarPath),
                    LogoutActivity.DEFAULT_AVATAR);
        }
        //昵称
        if(!TextUtils.isEmpty(data.getUsername())){
            mtvNickname.setText(data.getUsername());
        }
        //生日
        if(!TextUtils.isEmpty(data.getBirthday())){
            mBrith.setText(data.getBirthday());
        }
    }

    private void initTitleBar() {
        CommonTitleBar titleBar = (CommonTitleBar)findViewById(R.id.title_bar);
        titleBar.findViewById(titleBar.getLeftButtonId()).setOnClickListener(this);
        //titleBar.setTitleColor(R.drawable.common_tv_title_white_selector);
        titleBar.setBackImg(R.drawable.common_title_left);
    }

    private void initView() {
        mAvatar = (RelativeLayout) findViewById(R.id.avatar);
        mIvAvatar = (CircleImageView) findViewById(R.id.iv_login_user_avatar);
        mNickname = (RelativeLayout) findViewById(R.id.nickname);
        mtvNickname = (TextView) findViewById(R.id.tv_person_nickname);
        mGender = (RelativeLayout) findViewById(R.id.gender);
        mTvGender = (TextView) findViewById(R.id.tv_person_gender);
        mBirth = (RelativeLayout) findViewById(R.id.date_of_birth);
        mPersonGender = (TextView) findViewById(R.id.tv_person_gender);
        mBrith = (TextView) findViewById(R.id.tv_person_brith);
        mUserName = (TextView) findViewById(R.id.tv_person_nickname);
        mLogoutBtn = (Button) findViewById(R.id.btn_logout);
    }

    private void initListener() {
//        mAvatar.setOnClickListener(this);
        mNickname.setOnClickListener(this);
        mGender.setOnClickListener(this);
        mBirth.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //头像
//            case R.id.avatar:
//                if(curAvatarBm != null){
//                    curAvatarBm.recycle();
//                    curAvatarBm = null;
//                }
//                isChanged = true;
//                chooseAvatar();
//                break;
            //昵称
            case R.id.nickname:
                isChanged = true;
                Intent intent = new Intent();
                CharSequence text = mtvNickname.getText().toString();
                intent.setClass(this , UserEditNicknameActivity.class);
                intent.putExtra("nickName" , text);
                startActivityForResult(intent , REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            //性别
            case R.id.gender:
                isChanged = true;
                chooseGender();
                break;
            //出生日期
            case R.id.date_of_birth:
                isChanged = true;
                chooseBirth();
                break;
            case R.id.btn_logout:
                /*if(isChanged){
                    uploadAccountInformation(userAccountData);
                    uploadAccountInforAvager();
                }*/
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_LOGOUT);
                intent = new Intent(AccountManagementActivity.this,LoginActivity.class);
                //云端备份书签。
                BookmarkManager.getInstance().logOutAndSync(this);
                //首页数据同步
                //UserHomeSiteManager.getInstance().syncHomeSite(UserHomeSiteManager.SYNC_TYPE_LOGOUT);
                //浏览器设置同步
                SettingSyncManager.getInstance().syncSetting(SettingSyncManager.SYNC_TYPE_LOGOUT);
                //清空用户数据
                JuziApp.clearUserAccountData();
                AccountKit.logOut();
                EventBus.getDefault().post(new LogOutEvent());
                startActivity(intent);
                finish();
                break;
            case R.id.common_img_back:
                intent  = new Intent();
                intent.putExtra("nickName" , userAccountData.getUsername());
//                intent.putExtra("avatar" , userAccountData.getAvatar());
                this.setResult(RESULT_CODE, intent);
                finish();
            default:
                break;
        }
    }

    /**
     * 上传用户头像
     */
    private void uploadAccountInforAvager() {
        if(curAvatarBm == null) return;
        String avatarPath = tempAvatarPath; //存放临时图片的路径
        if(avatarPath == null){
            tempAvatarName = ConfigManager.getInstance().getAvatarSaveName(true);
            avatarPath = saveBitmapToFile(curAvatarBm,tempAvatarName);// 保存在SD卡中  TODO IO
        }
        final File file = new File(avatarPath);
        if(file.exists()){
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
                    userAccountData.setAvatar(avatarPath);

                    long updateTime = System.currentTimeMillis();
                    userAccountData.setAvatarLastModified(updateTime); //更新头像修改时间(退出后清空)
                    ConfigManager.getInstance().setLocalUserAvatarLastModified(updateTime); //更新本地保存头像修改时间（退出后不清空）
                    JuziApp.updateUserAccountData(userAccountData);
                    //保存头像到本地，退出登录仍然有效
                    ConfigManager.getInstance().setLocalUserAvatarPath(avatarPath);

                    isEditAvatar = false;
                }

                @Override
                public void onUploadFailed(String error) {
                    //dismissLoadingDialog(); //关闭发送对话框
                }
            });
        }
    }

    /**
     * 上传用户信息
     */
    private void uploadAccountInformation(UserAccountData data) {
        SimpleLog.d("User" , data.toString());
//        JuziApp.updateUserAccountData(data);
        PostUserAccountJson postUserAccountJson = new PostUserAccountJson();
        postUserAccountJson.setBirthday(data.getBirthday()) ;
        postUserAccountJson.setName(data.getUsername());
        postUserAccountJson.setNickname(data.getNickName());
        if(!TextUtils.isEmpty(data.getGender())){
            postUserAccountJson.setGender(data.getGender());
        }else {
            postUserAccountJson.setGender("U");
        }
        postUserAccountJson.setToken(data.getToken());
        postUserAccountJson.setAppName(AccountLoginManager.APP_NAME);
        postUserAccountJson.setAge(data.getAge());
        String s = postUserAccountJson.toString();
        Api.getInstance().uploadAccountInformation(postUserAccountJson).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
            response.body();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                int i = 0;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if(data == null) return;
                String nickName = data.getStringExtra("nickName");
                mUserName.setText(nickName);
                userAccountData.setUsername(nickName);
                break;
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
                            curAvatarBm = head;
                            tempAvatarPath = cropImagePath;
                            mIvAvatar.setImageBitmap(head);
                        }
                    }
                }
                break;
        }
    }

    private void cropPhoto(Uri uri) {
        isEditAvatar = true;
        Intent intent = new Intent(this,ImageClipActivity.class);
        intent.setData(uri);
        startActivityForResult(intent,CROP_SELECT);
    }

    /**
     * 选择出生日期
     */
    private void chooseBirth() {
        timePickerViewDate = new TimePickerView(AccountManagementActivity.this, TimePickerView.Type.YEAR_MONTH_DAY);
        //timePickerViewDate.setTitle("选择时间");
        timePickerViewDate.setCyclic(false);
        timePickerViewDate.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                if(date.getTime() > System.currentTimeMillis()){
                    CustomToastUtils.getInstance().showTextToast(R.string.input_again);
                    return;
                }

                // 初始化时设置 日期和时间模式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String birth = sdf.format(date);
                String[] split = birth.split("-");
                int year = Integer.parseInt(split[0]);
                if(year >= 1970 && year <= 1979){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.USER_70);
                }else if(year >= 1980 && year <= 1989){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.USER_80);
                }else if(year >= 1990 && year <= 1999){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.USER_90);

                }else if(year >= 2000 && year <= 2009){
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.USER_00);
                }else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.USER_OTHER);
                }
                mBrith.setText(birth);
                userAccountData.setBirthday(birth);
            }
        });
        timePickerViewDate.show();
    }

    /**
     * 选择性别
     */
    private void chooseGender() {
        final SelectGenderDialog dialog = new SelectGenderDialog(this);
        dialog.setOnItemClickListener(new SelectGenderDialog.OnItemClickListener() {
            @Override
            public void onMale() {
                mPersonGender.setText(R.string.male);
                userAccountData.setGender("M");
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.EDIT_MALE);
                dialog.dismiss();
            }

            @Override
            public void onFemale() {
                mPersonGender.setText(R.string.female);
                userAccountData.setGender("F");
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.EDIT_FEMALE);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 选择头像
     */
    private void chooseAvatar() {
        final SelectPictureDialog dialog = new SelectPictureDialog(this);
        dialog.setOnItemClickListener(new SelectPictureDialog.OnItemClickListener() {
            @Override
            public void onAlbum() {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResultByOne(intent,ALBUM_SELECT);
                dialog.dismiss();
            }

            @Override
            public void onCamera() {
                if (ActivityCompat.checkSelfPermission(AccountManagementActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AccountManagementActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 0);
                } else {
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

    @Override
    protected void onStop() {
        super.onStop();
        if(!isChanged)return;
        uploadAccountInformation(userAccountData);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent  = new Intent();
            intent.putExtra("nickName" , userAccountData.getUsername());
//            intent.putExtra("avatar" , userAccountData.getAvatar());
            this.setResult(RESULT_CODE, intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
