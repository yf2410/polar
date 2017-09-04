package com.polar.browser.download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.folder.FileInfo;
import com.polar.browser.folder.FileItem;
import com.polar.browser.folder.IRefresh;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChooseDirActivity extends LemonBaseActivity implements View.OnClickListener {

    private static final String TAG = ChooseDirActivity.class.getSimpleName();

    private ListView mLvFolders;
    private TextView mBtnConfirm;
    //private TextView mBtnCancel;
    private TextView rootTV;
    private RecyclerView recyclerView;


    private IRefresh mRefresh = new IRefresh() {

        @Override
        public void refreshListItems(String path, boolean isFolder) {
            Log.d(TAG, "refreshListItems path = " + path);
            refresh(path);
        }
    };
    private FileListAdapter mAdapter;
    private String mCurrentPath;
    private String mRootPath;
    private List<FileInfo> list;
    private int mDownType;
    private ArrayList<String> old_paths;
    private String mFrom;
    private ChooseDirAdapter chooseDirAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_dir);
        initViews();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initViews() {

        mLvFolders = (ListView) findViewById(R.id.lv_folders);
        mBtnConfirm = (TextView) findViewById(R.id.btn_confirm);
        //mBtnCancel = (TextView) findViewById(R.id.btn_cancel);

        rootTV = (TextView) findViewById(R.id.tv_root);
        rootTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh(mRootPath);
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_choose_path);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        chooseDirAdapter = new ChooseDirAdapter(null, this.getApplicationContext());
        recyclerView.setAdapter(chooseDirAdapter);
        chooseDirAdapter.setOnItemClickListener(new ChooseDirAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String selectedPath) {
                if (selectedPath == null) return;
                String temp = mCurrentPath.substring(0, (mCurrentPath.indexOf(selectedPath) + selectedPath.length()));
                refresh(temp);
            }
        });

        mBtnConfirm.setOnClickListener(this);
        //mBtnCancel.setOnClickListener(this);
        findViewById(R.id.common_img_back).setOnClickListener(this);

        mAdapter = new FileListAdapter(this, mRefresh);

    }

    public void initData() {
        old_paths = getIntent().getStringArrayListExtra("old_paths");
        mFrom = getIntent().getStringExtra("from");
        mCurrentPath = getIntent().getStringExtra(CommonData.KEY_CURRENT_DOWN_FOLDER);
        mDownType = getIntent().getIntExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_PHONE);
        if (mCurrentPath != null && !mCurrentPath.endsWith(File.separator)) {
            mCurrentPath = mCurrentPath + File.separator;
        }
        mRootPath = mCurrentPath;
        if (mCurrentPath != null && !TextUtils.isEmpty(mCurrentPath)) {
            refresh(mCurrentPath);
        }
    }

    private void refresh(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        mCurrentPath = path;
        list = buildListForSimpleAdapter(path);
        orderByName(list);
        mAdapter.updateData(list);
        mLvFolders.setAdapter(mAdapter);
        mLvFolders.setSelection(0);
        updateCurrentFolder();
    }

    private void updateCurrentFolder() {
        String path = mCurrentPath;
        String path2 = mRootPath;
        if (path2.endsWith("/")) {
            path2 = path2.substring(0, path2.length() - 1);
        }
        if (mDownType == CommonData.DOWN_TYPE_PHONE) {
            path = path.replace(path2, getString(R.string.download_folder_phone));
        } else if (mDownType == CommonData.DOWN_TYPE_SD_CARD) {
            path = path.replace(path2, getString(R.string.download_folder_sd));
        }
        splitPath(path);
    }

    private void splitPath(String path) {
        String directoryArr[] = path.split("/");
        if (directoryArr != null && directoryArr.length > 0) {
            String paramsArr[] = new String[directoryArr.length - 1];
            for (int i = 1; i < directoryArr.length; i++) {
                paramsArr[i - 1] = directoryArr[i];
            }
            rootTV.setText(directoryArr[0]);
            chooseDirAdapter.setData(paramsArr);
        }
    }

    /* 根据路径生成一个包含路径的列表 */
    private List<FileInfo> buildListForSimpleAdapter(String path) {
        File[] files = new File(path).listFiles();
        List<FileInfo> list = new ArrayList<FileInfo>();
        if (files == null) {
            return list;
        }
        for (File file : files) {
            FileInfo info = new FileInfo();
            info.directory = file.isDirectory();
            info.name = file.getName();
            // 只添加目录 & 非隐藏文件
            if (info.directory && !info.name.startsWith(".")) {
                info.path = file.getPath();
                // 获取该目录下，有多少个文件夹
                info.children = getChildFolderCount(file);
                list.add(info);
            }
        }
        return list;
    }

    /**
     * 按照文件名称排序
     *
     * @param list
     */
    private void orderByName(List<FileInfo> list) {
        Collections.sort(list, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    /**
     * 获取目录下子文件夹个数
     *
     * @param file
     * @return
     */
    private int getChildFolderCount(File file) {
        File[] files = file.listFiles();
        int folderCount = 0;
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    folderCount++;
                }
            }
        }
        return folderCount;
    }

    /**
     * 跳转到上一层
     *
     * @return true，有上级； false，没上级
     */
    private boolean goToParent() {
        File file = new File(mCurrentPath);
        File str_pa = file.getParentFile();
        if (str_pa == null) {
            return false;
        } else if (TextUtils.equals(mRootPath, mCurrentPath)) {
            return false;
        } else {
            mCurrentPath = str_pa.getAbsolutePath();
            refresh(mCurrentPath);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                // 保存选择的路径
                if (!mCurrentPath.endsWith(File.separator)) {
                    mCurrentPath = mCurrentPath + File.separator;
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra("old_paths", old_paths);
                intent.putExtra("dest_path", mCurrentPath);
                setResult(200, intent);
//                Statistics.sendOnceStatistics(GoogleConfigDefine.FILE_MANAGER_STATISTICS, mFrom, GoogleConfigDefine.COMMON_CHANGE_LOCATION_CONFIRM);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                finish();
                break;
            case R.id.common_img_back:
                onBackPressed();
                break;
           /* case R.id.btn_cancel:
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                finish();
                break;*/
        }
    }

    @Override
    public void onBackPressed() {
        if (!goToParent()) {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        }
    }

    private class FileListAdapter extends JZBaseAdapter<FileInfo> {

        private IRefresh mRefresh;

        public FileListAdapter(Context context, IRefresh refresh) {
            super(context);
            mRefresh = refresh;
        }

        @Override
        public View newView(Context context, FileInfo data, ViewGroup parent, int type) {
            return new FileItem(context, mRefresh);
        }

        @Override
        public void bindView(View view, int position, FileInfo data) {
            FileItem item = (FileItem) view;
            item.bind(data);
        }

    }


}
