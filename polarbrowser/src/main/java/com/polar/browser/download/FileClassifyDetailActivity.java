package com.polar.browser.download;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bean.BaseFileClass;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.download.download.FileDetailOperateView;
import com.polar.browser.download.view.AbstractFileListView;
import com.polar.browser.download.view.ApkListView;
import com.polar.browser.download.view.AudioListView;
import com.polar.browser.download.view.DocListView;
import com.polar.browser.download.view.ImageListView;
import com.polar.browser.download.view.UnknownFileListView;
import com.polar.browser.download.view.VideoListView;
import com.polar.browser.download.view.WebPageListView;
import com.polar.browser.download.view.ZipListView;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_APK;
import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;
import static com.polar.browser.download_refactor.Constants.TYPE_DOC;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_OTHER;
import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;
import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;


/**
 * Created by saifei on 16/12/30.
 * 文件分类详情 页。
 * 图片
 * 视频
 * 音频
 * 等等
 */

public class FileClassifyDetailActivity extends LemonBaseActivity implements IConfigObserver {
    public static final String TYPE = "type";
    public static final String FILE_PATH = "file_path";
    //底部的操作 View
    private FileDetailOperateView operateView;
    private String fileType;
    private CommonTitleBar titleBar;
    private TextView emptyTipTv;
    private View emptyView;
    //解压文件夹(只用于压缩文件列表)
    private View uncompressFolder;
    private TextView folderCountTV;
    private ImageView emptyIv;
    private View bottomEditLayout;
    private View editTv;
    private AbstractFileListView abstractFileListView;

    // 添加保存图片点击打开文件夹功能后，需要保存图片的路径
    private String mFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_classify_detail_layout);
        initData();
        initView();
        setListeners();
    }

    private void initData() {
        ConfigManager.getInstance().registerObserver(this);
        fileType = getIntent().getStringExtra(TYPE);
        mFilePath = getIntent().getStringExtra(FILE_PATH);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(getIntent()!= null){
            fileType = getIntent().getStringExtra(TYPE);
            mFilePath = getIntent().getStringExtra(FILE_PATH);
        }

        if (TYPE_ZIP.equals(fileType)){  //当压缩文件列表，显示解压文件夹
            uncompressFolder.setVisibility(View.VISIBLE);
            int size = DecompresstionFolderActivity.buildListForOtherUncompressDir(VCStoragerManager.getInstance().getDefaultDownloadDirPath()).size();
            if(size<0) size = 0;
            folderCountTV.setText(size+"");
        }else{
            uncompressFolder.setVisibility(View.GONE);
        }

    }

    private void setListeners() {
        titleBar.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_EDIT_COMPLETE);
                if (operateView.isEditing()) {
                    if(abstractFileListView.fileMoreClickView!=null&&abstractFileListView.fileMoreClickView.isShowing()){
                        abstractFileListView.fileMoreClickView.setIsShowing(false);
                        return;
                    }
                    onExitEditMode();
                }else{
                    finish();
                }

            }
        });
    }

    private void initView() {

        LinearLayout root = (LinearLayout) findViewById(R.id.file_classify_root);
        titleBar = (CommonTitleBar) findViewById(R.id.file_classify_titlebar);
//        titleBar.setSettingTxt(getString(R.string.edit));
        operateView = new FileDetailOperateView(this);
        bottomEditLayout = bottomEditLayout();

        //添加 listView 和 空白的 视图
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(emptyView(frameLayout));
        frameLayout.addView(getListViewByType());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(getUncompressFolderView(root));
        root.addView(frameLayout, layoutParams);
        root.addView(bottomEditLayout);
        root.addView(operateView);

    }

    private View bottomEditLayout() {
        this.bottomEditLayout = LayoutInflater.from(this).inflate(R.layout.common_bottom_edit, null);
        editTv = bottomEditLayout.findViewById(R.id.btn_edit);
        editTv.setEnabled(false);
        editTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onIntoEditMode();
            }
        });
        return bottomEditLayout;
    }

    private View emptyView(FrameLayout frameLayout) {
        emptyView = LayoutInflater.from(this).inflate(R.layout.layout_filelist_empty,frameLayout,false);
        emptyTipTv = (TextView)emptyView.findViewById(R.id.tv_empty);
        emptyIv = (ImageView)emptyView.findViewById(R.id.iv_empty);
        emptyView.setVisibility(View.GONE);
        return emptyView;
    }

    private View getUncompressFolderView(ViewGroup parent) {
        uncompressFolder = LayoutInflater.from(this).inflate(R.layout.layout_uncompress_folder,parent,false);
        folderCountTV = (TextView)uncompressFolder.findViewById(R.id.uncompress_count);

        uncompressFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileClassifyDetailActivity.this,DecompresstionFolderActivity.class);
                FileClassifyDetailActivity.this.startActivity(intent);
            }
        });
        return uncompressFolder;
    }


    private View getListViewByType() {


        switch (fileType) {
            case TYPE_APK:
                titleBar.setTitle(R.string.file_classify_apk);
                abstractFileListView = new ApkListView(this);
                break;

            case TYPE_IMAGE:
                titleBar.setTitle(R.string.file_classify_image);
                abstractFileListView = new ImageListView(this);
                if (!TextUtils.isEmpty(mFilePath)) {
                    ((ImageListView)abstractFileListView).setFilePath(mFilePath);
                }
                break;
            case TYPE_VIDEO:
                titleBar.setTitle(R.string.navigate_title_video);
                abstractFileListView = new VideoListView(this);
                break;
            case TYPE_AUDIO:
                titleBar.setTitle(R.string.navigate_title_music);
                abstractFileListView = new AudioListView(this);
                break;
            case TYPE_ZIP:
                titleBar.setTitle(R.string.file_classify_zip_file);
                abstractFileListView =  new ZipListView(this);
                break;
            case TYPE_DOC:
                titleBar.setTitle(R.string.file_classify_document);
                abstractFileListView = new DocListView(this);
                break;
            case TYPE_OTHER:
                titleBar.setTitle(R.string.file_classify_other);
                abstractFileListView = new UnknownFileListView(this);
                break;
            case TYPE_WEB_PAGE:
                titleBar.setTitle(R.string.offline_web_title);
                abstractFileListView = new WebPageListView(this);
                break;
        }

        operateView.init(abstractFileListView);
        return abstractFileListView;
    }


    public static void start(Context context, String type) {
        Intent starter = new Intent(context, FileClassifyDetailActivity.class);
        starter.putExtra(TYPE, type);
        context.startActivity(starter);
    }


    public void onIntoEditMode() {
//        titleBar.setSettingTxt(getString(R.string.complete));
        operateView.onIntoEditMode(fileType);
        bottomEditLayout.setVisibility(View.GONE);
    }

    public void onExitEditMode() {
//        titleBar.setSettingTxt(getString(R.string.edit));
        operateView.onExitEditMode();
        hideBottomOperateView();
    }

    @Override
    public void onBackPressed() {
        if (operateView.isEditing()) {
            if(abstractFileListView.fileMoreClickView!=null&&abstractFileListView.fileMoreClickView.isShowing()){
                abstractFileListView.fileMoreClickView.dismiss();
                abstractFileListView.fileMoreClickView.setIsShowing(false);
                return;
            }
            onExitEditMode();

            return;
        }
        super.onBackPressed();
    }


    /**
     * 点击每个checkBox后，刷新底部操作按钮
     */
    public void checkCheckAllButton() {
        // 正在下载页面
        operateView.checkCheckAllButton(fileType);
    }

    public void changeStorageLocation(List<? extends BaseFileClass> list) {
        sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_FILE_MOVE_ITEM);
        ArrayList<String> paths = new ArrayList<>();
        for (BaseFileClass info : list) {
            paths.add(info.getId() + "--" + info.getPath());
        }
        Intent intent = new Intent(this, ChooseDirActivity.class);
        intent.putExtra(CommonData.KEY_CURRENT_DOWN_FOLDER, VCStoragerManager.getInstance().getPhoneStorage());
        intent.putExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_PHONE);
        intent.putStringArrayListExtra("old_paths", paths);
//        intent.putExtra("from", GoogleConfigDefine.PKG);
        startActivityForResult(intent, 100);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        onExitEditMode();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == 200) {
            switch (requestCode) {
                case 100:
                    List<String> old_paths = data.getStringArrayListExtra("old_paths");
                    final String destPath = data.getStringExtra("dest_path");
                    if (destPath == null) return;
                    for (int i = 0; i < old_paths.size(); i++) {
                        String[] oldInfo = old_paths.get(i).split("--", 2);
                        if (oldInfo[0] == null || oldInfo[1] == null) continue;

                        final long oldId = Long.parseLong(oldInfo[0]);
                        final String oldPath = oldInfo[1];

                        File oldFile = new File(oldPath);
                        String targetPath = FileUtils.assembleFilePath(destPath, oldFile.getName());

                        if (oldPath.equals(targetPath)) continue;  //如果目标路径等于文件当前路径则不做任何操作

                        if (FileUtils.moveFile1(oldPath, targetPath)) {  //如果更改存储位置成功
                            try {
                                Uri ringUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), oldId);
                                ContentValues values = new ContentValues(3);
                                values.put(MediaStore.Video.Media.DATA, targetPath);
                                values.put(MediaStore.Video.Media.DISPLAY_NAME, oldFile.getName());
                                operateView.onFilePathChanged(1, null, ringUri, values, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }

    }


    @Override
    public void notifyChanged(String key, boolean value) {

    }

    @Override
    public void notifyChanged(String key, String value) {
        operateView.fileCountChanged();
    }

    @Override
    public void notifyChanged(String key, int value) {

    }

    @Override
    protected void onDestroy() {
        ConfigManager.getInstance().unregisterObserver(this);
        EventBus.getDefault().unregister(abstractFileListView);
        super.onDestroy();
    }



    public void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty?View.VISIBLE:View.GONE);
//        emptyTipTv.setText(Constants.TYPE_WEB_PAGE.equals(fileType)?getString(R.string.offline_web_empty):"");
        emptyIv.setImageResource(emptyImageIdByType(fileType));
//        findViewById(R.id.common_tv_setting).setEnabled(!isEmpty);
        editTv.setEnabled(!isEmpty);
    }

    private int emptyImageIdByType(String fileType) {

        switch (fileType){
            case Constants.TYPE_APK:
                return R.drawable.apk_empty;
            case Constants.TYPE_AUDIO:
                return R.drawable.music_empty;
            case Constants.TYPE_DOC:
                return R.drawable.documents_empty;
            case Constants.TYPE_IMAGE:
                return R.drawable.photo_empty;
            case Constants.TYPE_OTHER:
                return R.drawable.others_empty;
            case Constants.TYPE_VIDEO:
                return R.drawable.video_empty;
            case Constants.TYPE_WEB_PAGE:
                return R.drawable.offline_website_empty;
            case Constants.TYPE_ZIP:
                return R.drawable.rar_empty;

        }

        return 0;

    }

    private void sendGoogleStatistics(String type) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, type);
    }

    public View getViewMore() {
        return operateView.getViewMore();
    }

    public void hideBottomOperateView() {
        operateView.setVisibility(View.GONE);
        bottomEditLayout.setVisibility(View.VISIBLE);
    }

}
