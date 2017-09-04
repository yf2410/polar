package com.polar.browser.download;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.download.download.FileDetailOperateView;
import com.polar.browser.download.download.FileMoreClickView;
import com.polar.browser.download.uncompress.UncompressFolderAdapter;
import com.polar.browser.download.uncompress.UncompressInfo;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.i.IDownloadViewOperateDelegate;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SoftInputMethodUtils;
import com.polar.browser.utils.UncompressPrefs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yd_lzk on 2016/11/16.
 */

public class DecompresstionFolderActivity extends LemonBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener,
        IDownloadViewOperateDelegate.IEditCallback, com.polar.browser.download.ICallback {

    private static final String TAG = DecompresstionFolderActivity.class.getSimpleName();

    private ListView listview;
    private List<UncompressInfo> list;
    private ImageView mEmptyPic;
    private UncompressFolderAdapter otherAdapter;

    private String defaultDownloadPath;
    private static String currentPath;


    private FileDetailOperateView operateView;
//    private TextView editBtn;

    private boolean isEditing;
    protected int longClickItemPosition;

    private QueryHandler queryHandler;
    private List<String> downloadRootDirList = new ArrayList<String>();
    private View bottomEditBtn;
    private View bottomEditLayout;
    private FileMoreClickView fileMoreClickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decompress_folder);
        queryHandler = new QueryHandler(getApplicationContext(), new WeakReference<>(this));
        listview = (ListView) this.findViewById(R.id.listview);
        mEmptyPic = (ImageView)this.findViewById(R.id.autio_file_empty);
        listview.setOnItemClickListener(this);
        listview.setOnItemLongClickListener(longClickListener);
        otherAdapter = new UncompressFolderAdapter(this.getApplicationContext(), null,this);
        listview.setAdapter(otherAdapter);
        initTitleBar();
        initBottomOperateView();
        initBottomEditLayout();
        initData();
    }

    private void initBottomEditLayout() {
        bottomEditBtn = findViewById(R.id.btn_edit);
        bottomEditLayout = findViewById(R.id.bottom_edit_layout);
        bottomEditBtn.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    private void initTitleBar() {
        CommonTitleBar titleBar = (CommonTitleBar)findViewById(R.id.title_bar);
        titleBar.setTitle(R.string.submenu_file_others_decompression);
        titleBar.findViewById(R.id.common_img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        editBtn = (TextView) titleBar.findViewById(R.id.common_tv_setting);
//        editBtn.setVisibility(View.VISIBLE);
//        editBtn.setText(getString(R.string.edit));
//        editBtn.setOnClickListener(this);
    }

    private void initBottomOperateView() {
        operateView = (FileDetailOperateView) findViewById(R.id.bottom_edit_bar);
        operateView.init(this);
    }

    public void initData(){
        // 当前存储位置
        defaultDownloadPath = ConfigManager.getInstance().getCurrentDownloadFolder();
        if (defaultDownloadPath.equals("")){
            defaultDownloadPath = VCStoragerManager.getInstance().getDefaultDownloadDirPath();
        }
        File file = new File(defaultDownloadPath);
        refreshData(defaultDownloadPath);
    }

    /* 根据路径生成一个包含路径的列表 */
    private List<UncompressInfo> buildListForSimpleAdapter(String path) {
        File[] files = new File(path).listFiles();
        List<UncompressInfo> list = new ArrayList<UncompressInfo>();
        if (files == null) {
            return list;
        }
        boolean isUncompressRoot = FileUtils.isEqualFilePath(defaultDownloadPath,path); //解压根目录
        for (File file : files) {
            UncompressInfo info = new UncompressInfo();
            int type = file.isDirectory() ? UncompressInfo.TYPE_DIR : UncompressInfo.TYPE_FILE;
            if(type==UncompressInfo.TYPE_DIR){
                if(isUncompressRoot){
                    type = UncompressInfo.TYPE_ROOT;
                }
            }else{
                if(isUncompressRoot){
                    continue;  //在已解压根目录下不显示压缩文件
                }
            }

            info.setType(type);
            // 只添加非隐藏文件
            if (!file.getName().startsWith(".")) {
                info.setName(file.getName());
                info.setPath(file.getPath());
                info.setDate(file.lastModified());
                info.setSize(file.length());
                // 获取该目录下，有多少个文件夹
                info.setChildren(getChildFileCount(file));
                list.add(info);
            }
        }
        return list;
    }

    public static List<UncompressInfo> buildListForOtherUncompressDir(String  defaultDownloadPath){

        List<UncompressInfo> infoList = new ArrayList<UncompressInfo>();
        try{
            Map<String,?> map = UncompressPrefs.getAll();
            Set<String> keySets = map.keySet();
            String tempDownloadPath = VCStoragerManager.getInstance().getDownloadDataDirPath();
            //Step1 添加默认下载路径
            File[] files = new File(defaultDownloadPath).listFiles();
            if (files != null) {
                for (File file : files) {
                    UncompressInfo info = new UncompressInfo();
                    int type = file.isDirectory() ? UncompressInfo.TYPE_DIR : UncompressInfo.TYPE_FILE;
                    if(type==UncompressInfo.TYPE_DIR && !file.getName().startsWith(".")){  //目录，且非隐藏文件

                        if(FileUtils.isEqualFilePath(file.getPath(),tempDownloadPath)){  //不包括临时下载路径
                            String tempStr = file.getPath();
                            if(file.getPath().endsWith("/")){
                                tempStr = tempStr.substring(0,tempStr.length()-1);
                            }
                            if(keySets.contains(tempStr)){  //避免重复添加目录
                                keySets.remove(tempStr);
                                UncompressPrefs.remove(tempStr);
                                UncompressPrefs.apply();
                            }
                            continue;
                        }
                        info.setType(UncompressInfo.TYPE_ROOT);
                        info.setName(file.getName());
                        info.setPath(file.getPath());
                        info.setDate(file.lastModified());
                        info.setSize(file.length());
                        // 获取该目录下，有多少个文件夹
                        info.setChildren(getChildFileCount(file));
                        infoList.add(info);
                        if(keySets.contains(info.getPath())){  //避免重复添加目录
                            keySets.remove(info.getPath());
                        }
                        //将路径添加到UncompressPrefs
                        if(!UncompressPrefs.contains(info.getPath())){
                            UncompressPrefs.put(info.getPath(),false);
                            UncompressPrefs.apply();
                        }
                    }
                }
            }
            //Step2 添加非默认下载路径下的解压目录
            for(String filePath : keySets){   //Note: filePath 可能是文件 or 目录
                try{
                    SimpleLog.d(TAG,"buildListForOtherUncompressDir -- filePath = "+filePath);
                    File originFile = new File(filePath);
                    File uncompressDirFile = null;
                    if(originFile.isDirectory()){  //解压目录
                        uncompressDirFile = originFile;
                        SimpleLog.d(TAG,"buildListForOtherUncompressDir -- key is dir ");
                    }else{  //解压源文件
                        SimpleLog.d(TAG,"buildListForOtherUncompressDir -- key is file ");
                        String uncompressDirPath =  filePath.contains(".") ? filePath.substring(0,filePath.lastIndexOf('.')) : filePath;  // 解压之后的目录名
                        uncompressDirFile = new File(uncompressDirPath);
                        SimpleLog.d(TAG,"buildListForOtherUncompressDir -- fileName =  uncompressDirFile path = "+uncompressDirFile.getPath());
                    }

                    boolean isExist = uncompressDirFile.exists();
                    boolean isDir = uncompressDirFile.isDirectory();

                    if(isExist && isDir ){  //存在，已解压
                        UncompressInfo info = new UncompressInfo();
                        info.setType(UncompressInfo.TYPE_ROOT);
                        // 只添加非隐藏文件
                        info.setName(uncompressDirFile.getName());
                        info.setPath(uncompressDirFile.getPath());
                        info.setDate(uncompressDirFile.lastModified());
                        info.setSize(uncompressDirFile.length());
                        // 获取该目录下，有多少个文件夹
                        info.setChildren(getChildFileCount(uncompressDirFile));
                        infoList.add(info);
                    }else {
                        //删除UncompressPrefs中无用数据，文件未解压时，删掉
                        UncompressPrefs.remove(filePath);
                        UncompressPrefs.apply();
                        SimpleLog.d(TAG,"buildListForOtherUncompressDir  UncompressPrefs  remove path"+filePath);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return infoList;
    }

    private void delete(List<UncompressInfo> list) {
        if(list == null || list.size()<1) return;
        String where = buildWhere(list);
        queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
                null,
                MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns.DATA + " in " + where,
                null);
    }

    private void deleteDir(List<UncompressInfo> list) {
        SimpleLog.d(TAG,"deleteDir -- ");
        if(list == null || list.size()<1) return;
        for(UncompressInfo info : list) {
            if (info.getPath() == null) continue;
            try{
                queryHandler.deleteDir(info.getPath());
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }


    private String buildWhere(List<UncompressInfo> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (UncompressInfo info : list) {
            if(info.getPath() == null) continue;
            String path = info.getPath().replace("'", "''");
            sb.append("'"+path+"'").append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        return sb.toString();
    }

    private static class QueryHandler extends MediaDBProvider {

        private WeakReference<DecompresstionFolderActivity> outViewSoftRef;

        public QueryHandler(Context context,WeakReference<DecompresstionFolderActivity> reference) {
            super(context);
            this.outViewSoftRef = reference;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if(!isOuterViewVvalid(outViewSoftRef)) return;

        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if(!isOuterViewVvalid(outViewSoftRef)) return;
            outViewSoftRef.get().refreshData(currentPath);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            if(!isOuterViewVvalid(outViewSoftRef)) return;
            DecompresstionFolderActivity outView = outViewSoftRef.get();
            outView.operateView.onExitEditMode();
            outView.refreshData(currentPath);
            QueryUtils.notifyFileCountChanged(Constants.TYPE_ALL);
        }

        private boolean isOuterViewVvalid(WeakReference<DecompresstionFolderActivity> outView){
            return outView != null && outView.get() != null;
        }

    }

    /**
     * 按 类型，时间，名称 排序
     * @param list
     */
    private void orderBy(List<UncompressInfo> list) {
        if(list == null || list.size() == 0) return;
        Collections.sort(list, new Comparator<UncompressInfo>() {
            @Override
            public int compare(UncompressInfo o1, UncompressInfo o2) {
                try{
                    if(o1.getType() != o2.getType()){   //类型
                        return o2.getType() - o1.getType();
                    }else if(o1.getDate() != o2.getDate()){  //时间
                        return (int)(o2.getDate() - o1.getDate());
                    }else{   //名称
                        return o1.getName().toLowerCase().trim().compareTo(o2.getName().toLowerCase().trim());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return 0;
                }
            }
        });

    }

    /**
     * 获取目录下子文件夹个数
     *
     * @param file
     * @return
     */
    private static int getChildFileCount(File file) {
        File[] files = file.listFiles();
        int folderCount = 0;
        if (files != null) {
            for (File f : files) {
                folderCount++;
            }
        }
        return folderCount;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(currentPath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:
                changeMode();
                break;
        }

    }

    @Override
    public void more() {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, GoogleConfigDefine.DOWNLOAD_EDIT_MORE);
        Map<String,UncompressInfo> selectedMap =  otherAdapter.getSelectedData();
        if (!selectedMap.isEmpty()) {
            if (selectedMap.size() == 1) {
                // 选择了一条
                UncompressInfo info = otherAdapter.getSelectedData().entrySet().iterator().next().getValue();
//                showMoreDialogSingle(info);
                if(fileMoreClickView == null)
                    fileMoreClickView = new FileMoreClickView(DecompresstionFolderActivity.this, this, Collections.singletonList(info), operateView.getViewMore());
                fileMoreClickView.setMulti(false);
            } else {

            }

            fileMoreClickView.showMenu();
        }
    }

    private void showMoreDialogSingle(final UncompressInfo info) {

        String[] items = {
                getResources().getString(R.string.download_change_storage_location),
                getResources().getString(R.string.download_rename),
                getResources().getString(R.string.download_file_property)
        };
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:// 更改存储位置
                        changeStorageLocation(info);
                        operateView.onExitEditMode();
                        break;
                    case 1:// 重命名
                        reName(info);
                        operateView.onExitEditMode();
                        break;
                    case 2:// 文件属性
                        FilePropActivity.start(DecompresstionFolderActivity.this, info);
                        operateView.onExitEditMode();
                        break;
                    default:
                        break;
                }

            }
        };
        showContextDialog(items, listener);
    }


    private void showMoreDialogMulti() {
        String[] items = {
                getResources().getString(R.string.delete)
        };
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0://删除
                        delete();
                        break;
                    default:
                        break;
                }
            }
        };
        showContextDialog(items, listener);
    }


    @Override
    public void share() {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_EDIT_SHARE);
        if (otherAdapter == null) {
            return;
        }
        Map<String,UncompressInfo > data = otherAdapter.getSelectedData();
        startShare(data);

    }

    public void startShare(Map<String,UncompressInfo > data) {
        if (data.isEmpty()) {
            return;
        }
        ArrayList<Uri> uris = new ArrayList<>();
        for (String key : data.keySet()) {
            uris.add(Uri.fromFile(new File(key)));
        }

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE
                : Intent.ACTION_SEND);
        if (multiple) {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }
        intent.setType(shareType());
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
        //分享后退出编辑状态
        operateView.onExitEditMode();
    }

    protected String shareType() {
        return "application/vnd.android.package-archive";
    }

    private void changeStorageLocation(UncompressInfo info) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, GoogleConfigDefine.DOWNLOAD_FILE_MOVE_ITEM);
        ArrayList<String> paths = new ArrayList<>();
        paths.add(info.getPath());
        Intent intent = new Intent(this, ChooseDirActivity.class);
        intent.putExtra(CommonData.KEY_CURRENT_DOWN_FOLDER, VCStoragerManager.getInstance().getPhoneStorage());
        intent.putExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_PHONE);
        intent.putStringArrayListExtra("old_paths", paths);
        startActivityForResult(intent, 100);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 200) {
            switch (requestCode) {
                case 100:
                    List<String> old_paths = data.getStringArrayListExtra("old_paths");
                    final String destPath = data.getStringExtra("dest_path");
                    if(destPath == null) return;
                    for (int i = 0; i < old_paths.size(); i++) {
                        final String oldPath = old_paths.get(i);
                        File oldFile = new File(oldPath);
                        String targetPath = FileUtils.assembleFilePath(destPath,oldFile.getName());

                        if(oldPath.equals(targetPath)) continue;  //如果目标路径等于文件当前路径则不做任何操作

                        if(FileUtils.moveFile1(oldPath, targetPath)){  //如果更改存储位置成功
                            try{
                                queryHandler.updateFilePath(oldFile.getName(),oldPath,targetPath);
                                refreshData(currentPath);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void showContextDialog(String[] items, AdapterView.OnItemClickListener listener) {
        ListDialog dialog = new ListDialog(this);
        dialog.setItems(items, -1);
        dialog.setOnItemClickListener(listener);
        dialog.show();
    }

    private void reName(final UncompressInfo info) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_FILE_RENAME);
        if (TextUtils.isEmpty(info.getPath())) {
            return;
        }
        // 弹框
        final CommonDialog dialog = new CommonDialog(this);
        dialog.setTitle(R.string.download_rename);
        dialog.setCenterView(R.layout.dialog_rename);
        final EditText etName = (EditText) dialog.findViewById(R.id.et_name);
        final String name = info.getName();
        final String folder = info.getPath().substring(0, info.getPath().lastIndexOf("/") + 1);
        etName.setText(name);

        dialog.setBtnOkListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newName = etName.getText().toString();
                if(newName.equals(name)){  //名称没有改变
                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    dialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newName.replace(" ", ""))) {
                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
                    return;
                }
                // 	处理文件名为 ".xxx" 情况
                if (newName.lastIndexOf('.') == 0) {
                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
                    return;
                }
                // 判断是否有重名文件
                if (new File(folder + newName).exists() && !TextUtils.equals(newName, name)) {
                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    CustomToastUtils.getInstance().showDurationToast(R.string.download_file_name_exists, 3000);
                    return;
                }
                final String fName = newName;
                File directory = new File(folder);
                File from      = new File(directory, name);
                File to        = new File(directory, newName);
                from.renameTo(to);

                queryHandler.updateFileName(newName,from.getPath(),to.getPath());
                SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                dialog.dismiss();
            }
        });
        dialog.setBtnCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                dialog.dismiss();
            }
        });
        dialog.show();
        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                etName.setFocusable(true);
                int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    etName.setSelection(0,dot);
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etName, InputMethodManager.SHOW_FORCED);
            }
        }, 200);
    }


    @Override
    public void delete() {
        if(fileMoreClickView!=null)
            fileMoreClickView.setIsShowing(false);
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_EDIT_DELETE);
        if (otherAdapter == null) {
            return;
        }
        Map<String, UncompressInfo> data = otherAdapter.getSelectedData();
        showDeleteDialog(data);
    }

    protected void showDeleteDialog(final Map<String, UncompressInfo> data) {
        final CommonDialog dialog = new CommonDialog(this, "", getResources().getString(R.string.file_delete_hint));
        dialog.setTitle(getResources().getString(R.string.delete));
        dialog.setBtnCancel(getResources().getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_CANCEL);
                dialog.dismiss();
            }
        });
        dialog.setBtnOk(getResources().getString(R.string.delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_CONFIRM);
                dialog.dismiss();
                startDelete(data);
            }
        });
        dialog.show();
    }

    public void startDelete(Map<String, UncompressInfo> data) {
        if (data.isEmpty()) {
            return;
        }

        final ArrayList<UncompressInfo> list = new ArrayList<UncompressInfo>();
        final ArrayList<UncompressInfo> dirList = new ArrayList<UncompressInfo>();
        for (Map.Entry<String, UncompressInfo> entry : data.entrySet()) {
            if(entry.getValue() == null) continue;
            if(entry.getValue().getType() == UncompressInfo.TYPE_FILE){
                list.add(entry.getValue());
            }else{
                dirList.add(entry.getValue());
            }
        }
        //删除文件
        for(UncompressInfo info : list){
            FileUtils.delFile1(info.getPath());
        }
        //删除目录
        for(UncompressInfo info : dirList){
            FileUtils.delFolder1(info.getPath());
        }
        delete(list);
        deleteDir(dirList);
    }

    public void refreshData(String path){
        if(path == null) return;
        currentPath = path;

        //刷新数据之前退出编辑状态
        if(operateView.isEditing()){
            operateView.onExitEditMode();
        }

        if(FileUtils.isEqualFilePath(defaultDownloadPath,path)){  //下载目录，添加非下载目录解压文件
            list = buildListForOtherUncompressDir(defaultDownloadPath);
            initDownloadRootDirList(list);
        }else{
            list = buildListForSimpleAdapter(path);
        }

        orderBy(list);  // 排序

        if(list == null || list.size() == 0){
            bottomEditBtn.setEnabled(false);
            listview.setVisibility(View.GONE);
            mEmptyPic.setVisibility(View.VISIBLE);
        }else{
            bottomEditBtn.setEnabled(true);
            mEmptyPic.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
            otherAdapter.changeData(list);
        }

    }

    /**
     * 记录根目录下包括哪些目录。
     * @param list
     */
    public void initDownloadRootDirList(List<UncompressInfo> list){
        if(list == null) return;
        downloadRootDirList.clear();
        for (UncompressInfo info : list){
            downloadRootDirList.add(info.getPath());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(otherAdapter.isEditMode()){
            CheckBox cb = ((CheckBox)view.findViewById(R.id.uncompress_checkbox));
            cb.setChecked(!cb.isChecked());
        }else {
            UncompressInfo info = (UncompressInfo) otherAdapter.getItem(position);
            if (info.getType() != UncompressInfo.TYPE_FILE) {  //如何点击的是目录，则显示目录下面的文件
                refreshData(info.getPath());
            } else {   //点击文件，打开
                Intent intent = null;
                if(info.getPath()!=null){
                    if(OpenFileUtils.checkEndsWithInStringArray(info.getPath(), getResources()
                            .getStringArray(R.array.fileEndingImage)) ){
                        intent = new Intent(this, ImageGalleryActivity.class);
                        intent.putStringArrayListExtra("imagePaths", (ArrayList<String>) otherAdapter.getAllImagePaths());
                        intent.putExtra("position", otherAdapter.getAllImagePaths().indexOf(info.getPath()));
                        startActivity(intent);
                        return;
                    }
                    OpenFileUtils.openFile(new File(info.getPath()), this.getApplication());
                }
            }
        }

    }

    public boolean isEditing() {
        return isEditing;
    }

    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (isEditing()) return true;

            UncompressInfo info = (UncompressInfo) otherAdapter.getItem(position);
            if(info == null || info.getType() != UncompressInfo.TYPE_FILE) return true;  //不处理文件夹

            longClickItemPosition = position;
            String[] items = {
                    getResources().getString(R.string.share),
                    getResources().getString(R.string.download_change_storage_location),
                    getResources().getString(R.string.download_rename),
                    getResources().getString(R.string.download_file_property),
                    getResources().getString(R.string.delete),
            };
            showContextDialog(items, dialogItemClickListener);
            return true;
        }
    };

    protected AdapterView.OnItemClickListener dialogItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            UncompressInfo info = (UncompressInfo) otherAdapter.getItem(longClickItemPosition);
            switch (position) {
                case 0: //分享
                    Map<String,UncompressInfo> shareMap = new HashMap<>();
                    shareMap.put(info.getPath(),info);
                    startShare(shareMap);
                    break;
                case 1://更改存储位置
                    changeStorageLocation(info);
                    break;
                case 2://重命名
                    reName(info);
                    break;
                case 3://文件属性
                    FilePropActivity.start(DecompresstionFolderActivity.this, info);
                    break;
                case 4://删除
                    Map<String,UncompressInfo> deleteMap = new HashMap<>();
                    deleteMap.put(info.getPath(),info);
                    showDeleteDialog(deleteMap);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 返回逻辑
     */
    public void onClickBackBtn(){
        if (operateView.isEditing()) {  //点击返回按钮退出编辑模式。
            operateView.onExitEditMode();
            return;
        }

        if(FileUtils.isEqualFilePath(defaultDownloadPath,currentPath)){  //判断是否到达根目录
            Intent intent = new Intent(this,FileClassifyDetailActivity.class);
            intent.putExtra("type", Constants.TYPE_ZIP);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        }else {
            if(downloadRootDirList.contains(currentPath)){
                refreshData(defaultDownloadPath);
            }else{
                refreshData(new File(currentPath).getParent());
            }
        }
    }

    @Override
    public void onBackPressed() {
        onClickBackBtn();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (operateView.isEditing()) {
            operateView.onExitEditMode();
        }
    }

    @Override
    public void onExitEditMode() {
        if(fileMoreClickView!=null)
            fileMoreClickView.setIsShowing(false);
        isEditing = false;
//        editBtn.setText(getString(R.string.edit));
        bottomEditLayout.setVisibility(View.VISIBLE);
        operateView.setVisibility(View.GONE);
        otherAdapter.changeEditState(false);
    }

    @Override
    public void onIntoEditMode() {
        isEditing = true;
//        editBtn.setText(getString(R.string.complete));
        bottomEditLayout.setVisibility(View.GONE);
        otherAdapter.changeEditState(true);
    }

    @Override
    public void setAllChecked(boolean allChecked) {
        if(fileMoreClickView!=null)
            fileMoreClickView.setIsShowing(false);
        if(allChecked){
            otherAdapter.selectAll();
        }else{
            otherAdapter.deSelectAll();
        }
    }

    @Override
    public int getCheckedCount() {
        return otherAdapter.getCheckedCount();
    }

    @Override
    public int getDataCount() {
        return otherAdapter.getCount();
    }

    @Override
    public void onFilePathChanged(int token, Object cookie, Uri uri, ContentValues values, String selection, String[] selectionArgs) { }

    @Override
    public void onFileCountChanged() { }

    @Override
    public void edit() { }

    @Override
    public void complete() {
        onExitEditMode();
    }

    @Override
    public void clear() { }

   private void changeMode() {
        if (!isEditing) {
            sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_FILE_EDIT);
            operateView.onIntoEditMode("");
        } else {
            sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_EDIT_COMPLETE);
            operateView.onExitEditMode();
        }
    }

    @Override
    public void onShow(boolean show, boolean allSelected) {
         operateView.checkCheckAllButton(Constants.TYPE_DECOMPRESS);
    }

    public boolean isContainFolder(){
        Map<String,UncompressInfo> map = otherAdapter.getSelectedData();
        for(Map.Entry<String,UncompressInfo> entry : map.entrySet()){
            try {
                if(UncompressInfo.TYPE_FILE != entry.getValue().getType() ){
                    return true;
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    private void sendGoogleStatistics(String type) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, type);
    }

}
