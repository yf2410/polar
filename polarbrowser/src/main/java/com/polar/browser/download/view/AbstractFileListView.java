package com.polar.browser.download.view;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.bean.BaseFileClass;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.download.FilePropActivity;
import com.polar.browser.download.download.FileMoreClickView;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.i.IDownloadViewOperateDelegate;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.SoftReference;
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
 * <p>
 * 安装包列表
 */

public abstract class AbstractFileListView<BEAN extends BaseFileClass> extends ListView implements IDownloadViewOperateDelegate.IEditCallback{
    private static final String TAG = AbstractFileListView.class.getSimpleName();
    protected FileBaseAdapter fileBaseAdapter;
    protected MediaDBProvider queryHandler;
    private boolean isEditing;
    protected FileClassifyDetailActivity mActivity;
    protected int longClickItemPosition;
    private String renameToType;//改名后的 类型，改了扩展名
    public FileMoreClickView fileMoreClickView;

    public abstract String type();


    public AbstractFileListView(Context context) {
        this(context, null);
    }

    public AbstractFileListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        EventBus.getDefault().register(this);
        mActivity = (FileClassifyDetailActivity) getContext();
        queryHandler = new QueryHandler(getContext(), new SoftReference<AbstractFileListView>(this));
        fileBaseAdapter = new FileBaseAdapter(getContext());
        setAdapter(fileBaseAdapter);
        setOnScrollListener(fileBaseAdapter); //设置滚动监听，用于滑动加载优化
        loadList();
        setOnItemClickListener(itemClickListener);
        setOnItemLongClickListener(longClickListener);
    }


    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (isEditing()) { //选择或者取消选择
                fileBaseAdapter.getItem(position).isChecked = !fileBaseAdapter.getItem(position).isChecked;
                mActivity.checkCheckAllButton();
                fileBaseAdapter.notifyDataSetChanged();
                if(fileMoreClickView!=null)
                    fileMoreClickView.setIsShowing(false);
                return;
            }
            onClickFile(parent, view, position, id);
        }
    };

    protected void onClickFile(AdapterView<?> parent, View view, int position, long id) {

        try {
            BEAN info = fileBaseAdapter.getItem(position);
            if (info.getPath() != null) {
                File file = new File(info.getPath());
                if (file.exists() && file.isFile()) {
                    Intent intent = buildIntent(file);
                    getContext().startActivity(intent);
                    return;  //文件可用
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
    }

    private Intent buildIntent(File file) {
        String statisticType = null;
        Intent intent = null;
        switch (type()) {
            case TYPE_APK:
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_APK_ITEM;
                intent = OpenFileUtils.getApkFileIntent(file);
                break;
            case TYPE_VIDEO:
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_VIDEO_ITEM;
                intent = OpenFileUtils.getVideoFileIntent(file);
                break;
            case TYPE_AUDIO:
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_AUDIO_ITEM;
                intent = OpenFileUtils.getAudioFileIntent(file);
                break;
        }
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,statisticType);
        return intent;


    }


    protected void loadList() {
        switch (type()) {
            case TYPE_APK:
                queryHandler.startQueryAPKs();
                break;
            case TYPE_AUDIO:
                queryHandler.startQueryAudios();
                break;
            case TYPE_DOC:
                queryHandler.startQueryDocuments();
                break;
            case TYPE_IMAGE:
                queryHandler.startQueryImageBuckets();
                break;
            case TYPE_OTHER:
                queryHandler.startQueryUnknownFiles();
                break;
            case TYPE_VIDEO:
                queryHandler.startQueryVideos();
                break;

            case TYPE_ZIP:
                queryHandler.startQueryCompressFiles();
                break;

        }

    }

    @Override
    public void edit() {
    }

    @Override
    public void complete() {
        mActivity.onExitEditMode();
    }

    @Override
    public void clear() {
        CustomToastUtils.getInstance().showTextToast("clear");
    }

    protected String getFormatShareContent(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url + " " + getResources().getString(R.string.download_share_from);
        }
        return getResources().getString(R.string.download_share_from);
    }

    @Override
    public void delete() {
        if(fileMoreClickView!=null) fileMoreClickView.setIsShowing(false);
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_EDIT_DELETE);
        List<BEAN> checkedList = fileBaseAdapter.getCheckedItemList();
        showDeleteDialog(checkedList);
    }


    protected void showDeleteDialog(final List<BEAN> checkedList) {
        final CommonDialog dialog = new CommonDialog(getContext(), "", getResources().getString(R.string.file_delete_hint));
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
                startDelete(checkedList);
            }
        });
        dialog.show();
    }

    protected void startDelete(List<BEAN> checkedList) {
        if (checkedList.isEmpty()) {
            return;
        }

        for (int i = 0; i < checkedList.size(); i++) {
            FileUtils.delete(checkedList.get(i).getPath());
        }

        delete(checkedList);
    }

    protected void delete(List<BEAN> list) {
        final String where = buildWhere(list);
        queryHandler.startDelete(2,
                null,
                MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns._ID + " in " + where,
                null);
    }

    protected String buildWhere(List<BEAN> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (BEAN info : list) {
            sb.append(info.getId()).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void setAllChecked(boolean allChecked) {
        fileBaseAdapter.setAllChecked(allChecked);
    }

    @Override
    public int getCheckedCount() {
        return fileBaseAdapter.getCheckItemCount();
    }

    @Override
    public int getDataCount() {
        return fileBaseAdapter.getCount();
    }


    protected void showContextDialog(String[] items, AdapterView.OnItemClickListener listener) {
        ListDialog dialog = new ListDialog(getContext());
        dialog.setItems(items, -1);
        dialog.setOnItemClickListener(listener);
        dialog.show();
    }


    @Override
    public void onExitEditMode() {
        isEditing = false;
        fileBaseAdapter.changeEditeState(false);
        if(fileMoreClickView!=null&&fileMoreClickView.isShowing())
            fileMoreClickView.setIsShowing(false);
        mActivity.hideBottomOperateView();
    }

    @Override
    public void onIntoEditMode() {
        isEditing = true;
        fileBaseAdapter.changeEditeState(true);
        //TODO
    }

    public boolean isEditing() {
        return isEditing;
    }

    //滑动加载优化
    protected boolean isScrollState;
    protected int mFirstVisibleItem;
    protected int mVisibleItemCount;

    class FileBaseAdapter extends JZBaseAdapter<BEAN> implements OnScrollListener{

        FileBaseAdapter(Context context) {
            super(context);
        }

        @Override
        public View newView(Context context, BEAN data, ViewGroup parent, int type) {
            return AbstractFileListView.this.newView(context, data, parent, type);
        }


        @Override
        public void bindView(View view, int position, BEAN info) {
            AbstractFileListView.this.bindView(view, position, info ,isScrollState);
        }


        /**
         * 更改条目编辑状态
         *
         * @param isEditing true 编辑状态，显示checkBox
         */
        void changeEditeState(boolean isEditing) {
            if (getCount() <= 0) {
                return;
            }

            BEAN info;
            for (int i = 0; i < getCount(); i++) {
                info = getData().get(i);
                info.setEditing(isEditing);
                if (!isEditing)
                    info.isChecked = false;
            }
            notifyDataSetChanged();
        }


        /**
         * 全选or反选
         *
         * @param isChecked
         */
        public void setAllChecked(boolean isChecked) {
            if (getCount() <= 0) {
                return;
            }
            if(fileMoreClickView!=null)fileMoreClickView.setIsShowing(false);
            BEAN info;
            for (int i = 0; i < getCount(); i++) {
                info = getData().get(i);
                info.isChecked = isChecked;
            }
            notifyDataSetChanged();
        }

        /**
         * 获取选中条目列表Info
         *
         * @return
         */
        List<BEAN> getCheckedItemList() {
            List<BEAN> list = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                if (getData().get(i).isChecked) {
                    list.add(getData().get(i));
                }
            }
            return list;
        }

        /**
         * 获得选中项的个数
         *
         * @return
         */
        public int getCheckItemCount() {
            return getCheckedItemList().size();
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            SimpleLog.d(TAG,"onScrollStateChanged -----------mFirstVisibleItem = "+mFirstVisibleItem +" mVisibleItemCount = "+mVisibleItemCount);
            if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                isScrollState = false;
                for (int i = mFirstVisibleItem; i < mFirstVisibleItem + mVisibleItemCount; i++) {
                    try {
                        AbstractFileListView.this.bindView(view.getChildAt(i-mFirstVisibleItem), i, mData.get(i),isScrollState);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING){
                isScrollState = true;
            }else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrollState = true;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            SimpleLog.d(TAG,"onScroll -----------mFirstVisibleItem = "+mFirstVisibleItem +" mVisibleItemCount = "+mVisibleItemCount);
            mFirstVisibleItem = firstVisibleItem;
            mVisibleItemCount = visibleItemCount;
        }
    }

    protected abstract void bindView(View view, int position, BEAN data, boolean isScrollState);

    protected abstract View newView(Context context, BEAN data, ViewGroup parent, int type);


    @Override
    public void onFilePathChanged(int token, Object cookie, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        queryHandler.startUpdate(token, cookie, uri, values, selection, selectionArgs);
    }

//    protected void hideSoftInputFromWindow(Context context, EditText etName) {
//        etName.setFocusable(true);
//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
//    }

    public void reName(final BEAN info) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_FILE_RENAME);
        if (TextUtils.isEmpty(info.getPath())) {
            return;
        }
        // 弹框
        final CommonDialog dialog = new CommonDialog(getContext());
        dialog.setTitle(R.string.download_rename);
        dialog.setCenterView(R.layout.dialog_rename);
        final EditText etName = (EditText) dialog.findViewById(R.id.et_name);
        final String name = info.getPath().substring(info.getPath().lastIndexOf("/") + 1);
        final String folder = info.getPath().substring(0, info.getPath().lastIndexOf("/") + 1);
        etName.setText(name);

        dialog.setBtnOkListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newName = etName.getText().toString();
                if (newName.equals(name)) {  //名称没有改变
                    hideSoftInputFromWindow();
                    dialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newName.replace(" ", ""))) {
                    hideSoftInputFromWindow();
                    CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
                    return;
                }
                // 	处理文件名为 ".xxx" 情况
                if (newName.lastIndexOf(".") == 0) {
                    hideSoftInputFromWindow();
                    CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
                    return;
                }
                // 判断是否有重名文件
                if (new File(folder + newName).exists() && !TextUtils.equals(newName, name)) {
                    hideSoftInputFromWindow();
                    CustomToastUtils.getInstance().showDurationToast(R.string.download_file_name_exists, 3000);
                    return;
                }
                File directory = new File(folder);
                File from = new File(directory, name);
                File to = new File(directory, newName);
                from.renameTo(to);
                renameToType = com.polar.browser.utils.FileUtils.getFileType(newName);
                queryHandler.updateFileName(newName, from.getPath(), to.getPath());
                hideSoftInputFromWindow();
                dialog.dismiss();
            }
        });
        dialog.setBtnCancelListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftInputFromWindow();
                dialog.dismiss();
            }
        });
        dialog.show();
        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                etName.setFocusable(true);
                int dot = name.lastIndexOf(".");
                if (dot > 0) {
                    etName.setSelection(0, dot);
                }
                InputMethodManager inputManager =
                        (InputMethodManager)JuziApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etName, 0);
            }
        }, 200);
    }



    private void hideSoftInputFromWindow(){

        InputMethodManager imm = (InputMethodManager) JuziApp.getInstance().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = mActivity.getCurrentFocus();
        if (view == null) {
            view = new View(mActivity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    protected void startShare(List<BEAN> list) {
        ArrayList<Uri> uris = buildUris(list);
        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE
                : Intent.ACTION_SEND);
        if (multiple) {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }
        intent.setType(shareType());
        sendStat();
        getContext().startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)));
    }

    private void sendStat() {
        String type;
        switch (type()){
            case TYPE_APK:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_APK;
                break;
            case TYPE_AUDIO:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_AUDIO;
                break;
            case TYPE_DOC:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_DOC;
                break;
            case TYPE_IMAGE:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_IMAGE;
                break;
            case TYPE_OTHER:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_OTHER;
                break;
            case TYPE_VIDEO:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_VIDEO;
                break;
            case TYPE_WEB_PAGE:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_OTHER;
                break;
            case TYPE_ZIP:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_RAR;
                break;
            default:
                type = GoogleConfigDefine.DOWNLOAD_SHARE_FILE_OTHER;
        }
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, type);
    }

    protected ArrayList<Uri> buildUris(List<BEAN> list) {
        ArrayList<Uri> uris = new ArrayList<>();

        for (BEAN info : list) {
            uris.add(Uri.fromFile(new File(info.getPath())));
        }
        return uris;
    }

    protected String shareType() {
        return "application/vnd.android.package-archive";
    }


    private static class QueryHandler extends MediaDBProvider {

        private SoftReference<AbstractFileListView> outViewSoftRef;

        private QueryHandler(Context context, SoftReference<AbstractFileListView> outView) {
            super(context);
            this.outViewSoftRef = outView;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, final Cursor cursor) {

            if (!isOuterViewInvalid(outViewSoftRef))
                return;
            final AbstractFileListView outView = outViewSoftRef.get();


            ThreadManager.postTaskToIOHandler(new Runnable() {
                @Override
                public void run() {
                    if (cursor != null) {
                        final List<?> list = outView.parseListFromCursor(cursor);

                        ThreadManager.postTaskToUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                outView.fileBaseAdapter.updateData(list);
                                if (list == null || list.isEmpty()) { //TODO 后期如果需要显示空页面。
//                                    disableEditBtn();
//                                    listview.setVisibility(View.GONE);
//                                    mEmptyPic.setVisibility(View.VISIBLE);
                                      outView.setVisibility(View.GONE);
                                      outView.mActivity.updateEmptyView(true);
                                } else {
                                      outView.setVisibility(View.VISIBLE);
                                    outView.mActivity.updateEmptyView(false);
//                                    listview.setVisibility(View.VISIBLE);
//                                    mEmptyPic.setVisibility(View.GONE);

                                }
                                outView.onQueryComplete();
                            }
                        });
                    }

                }
            });
        }


        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if (!isOuterViewInvalid(outViewSoftRef))
                return;
            AbstractFileListView outView = outViewSoftRef.get();
            QueryUtils.notifyFileCountChanged(outView.type());
            if(!TextUtils.isEmpty(outView.renameToType)){
                QueryUtils.notifyFileCountChanged(outView.renameToType);
                outView.renameToType = null;
            }

            outView.loadList();
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
//            exitEditMode();
            if (!isOuterViewInvalid(outViewSoftRef))
                return;
            AbstractFileListView outView = outViewSoftRef.get();
            outView.mActivity.onExitEditMode();
            QueryUtils.notifyFileCountChanged(outView.type());
            outView.loadList();

        }

        private boolean isOuterViewInvalid(SoftReference<AbstractFileListView> outView) {
            return outView != null && outView.get() != null;
        }
    }

    protected void onQueryComplete() {

    }

    protected abstract List<BEAN> parseListFromCursor(Cursor cursor);

    @Override
    public void share() {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_EDIT_SHARE);
        List<BEAN> list = fileBaseAdapter.getCheckedItemList();
        if (!list.isEmpty()) {
            startShare(list);
        }
        mActivity.onExitEditMode();
    }

    @Override
    public void more() {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, GoogleConfigDefine.DOWNLOAD_EDIT_MORE);
        List<BEAN> list = fileBaseAdapter.getCheckedItemList();
        if (!list.isEmpty()) {
            if (list.size() == 1) {
                // 选择了一条
//                showMoreDialogSingle(list);
                if(fileMoreClickView==null)
                    fileMoreClickView = new FileMoreClickView(getContext(),AbstractFileListView.this,list,mActivity.getViewMore());
                fileMoreClickView.setMulti(false);
                fileMoreClickView.showMenu();
            } else {
                // 选择了多条
//                showMoreDialogMulti();
                if(fileMoreClickView==null)
                    fileMoreClickView = new FileMoreClickView(getContext(),AbstractFileListView.this,list,mActivity.getViewMore());
                fileMoreClickView.setMulti(true);
                fileMoreClickView.showMenu();
            }
        }
    }

    private void showMoreDialogSingle(final List<BEAN> infos) {
        final int MOVE_TO = 0;
        final int RENAME = 1;
        final int FILE_ATTR = 2;

        String[] items = {
                getResources().getString(R.string.download_change_storage_location),
                getResources().getString(R.string.download_rename),
                getResources().getString(R.string.download_file_property)
        };
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case MOVE_TO:// 更改存储位置
                        changeLocation(infos);
                        break;
                    case RENAME:// 重命名
                        reName(infos.get(0));
                        mActivity.onExitEditMode();

                        break;
                    case FILE_ATTR:// 文件属性
                        FilePropActivity.start(getContext(), infos.get(0));
                        mActivity.onExitEditMode();
                        break;
                    default:
                        break;
                }

            }
        };
        showContextDialog(items, listener);
    }

    public void changeLocation(List<BEAN> infos) {
        mActivity.onExitEditMode();
        mActivity.changeStorageLocation(infos);
    }


    private void showMoreDialogMulti() {
        String[] items = {
                getResources().getString(R.string.delete)
        };
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final int INDEX_DELETE = 0;
                switch (position) {
                    case INDEX_DELETE://删除
                        delete(fileBaseAdapter.getCheckedItemList());
                        break;
                    default:
                        break;
                }
            }
        };
        showContextDialog(items, listener);
    }


    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (isEditing()) return true;

            longClickItemPosition = position;
            // 删除、分享、移动到、重命名、文件详情
            String[] items = {
                    getResources().getString(R.string.delete),getResources().getString(R.string.share),
                    getResources().getString(R.string.download_change_storage_location),
                    getResources().getString(R.string.download_rename),
                    getResources().getString(R.string.download_file_property),

            };
            showContextDialog(items, dialogItemClickListener);
            return true;
        }
    };

    protected AdapterView.OnItemClickListener dialogItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // 删除、分享、移动到、重命名、文件详情
            final int INDEX_DELETE = 0;
            final int INDEX_SHARE = 1;
            final int INDEX_MOVE_TO = 2;
            final int INDEX_RENAME = 3;
            final int INDEX_FILE_ATTR = 4;
            BEAN info = fileBaseAdapter.getItem(longClickItemPosition);
            ArrayList<BEAN> list = new ArrayList<>();
            list.add(info);
            switch (position) {
                case INDEX_DELETE:
                    showDeleteDialog(list);
                    break;
                case INDEX_SHARE:
                    startShare(list);

                    break;
                case INDEX_MOVE_TO:
                    mActivity.changeStorageLocation(list);

                    break;
                case INDEX_RENAME:
                    reName(info);
                    break;
                case INDEX_FILE_ATTR:
                    FilePropActivity.start(getContext(), info);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onFileCountChanged() {
        loadList();
    }

    public static class EventCallback<BEAN extends BaseFileClass>{

        public int type;
        public List<BEAN> files;

        public static int TYPE_CHANGE_LOCATION = 1;
        public static int TYPE_RENAME = 2;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEvent(EventCallback callback){
        if(EventCallback.TYPE_CHANGE_LOCATION == callback.type){
            changeLocation(callback.files);
        }else{
            reName((BEAN) callback.files.get(0));
        }
    }

}

