package com.polar.browser.download;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bean.ImageItemInfo;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.download.download.ImageMoreClickView;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.CursorDataParserUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;

public class ImageFolderDetailsActivity extends LemonBaseActivity implements View.OnClickListener, ICallback, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = ImageFolderDetailsActivity.class.getSimpleName();
    private static final String[] PROJECTION = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
    };

    private static final int INDEX_ID = 0;
    private static final int INDEX_DISPLAY_NAME = 1;
    private static final int INDEX_DATA = 2;

    private long folder_id;
    private GridView gridview;
    private String folder_name;
    private QueryHandler queryHandler;
    private ImageItemAdapter imageItemAdapter;
    private View bottomOperateView;
    private ImageView mImgBack;
//    private TextView editBtn;
    private TextView bm_allselect;
    private TextView bm_delete;
    private TextView bm_more;
    private CommonCheckBox1 mCbCheckAll;

    private boolean mSelectAll;

    private static final int DIALOG_TYPE_SINGLE_SELECTED = 0;
    private static final int DIALOG_TYPE_MULTI_SELECTED = 1;
    //更改位置
    private static final int REQUEST_CHANGE_LOCATION = 100;
    private TextView bm_complete;
    private View bottomEditLayout;
    ImageMoreClickView imageMoreClickView = null;
    private View editTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_folder_details);
    }

    private void initViews() {
        gridview = (GridView) findViewById(R.id.gridview);
        queryHandler = new QueryHandler(this.getApplicationContext());
        imageItemAdapter = new ImageItemAdapter(getApplicationContext(), null, this);
        gridview.setAdapter(imageItemAdapter);
        gridview.setOnItemClickListener(this);
        gridview.setOnItemLongClickListener(this);

        initTitleBar();
        initBottomOperateLayout();
        initBottomEditLayout();

    }

    private void initBottomEditLayout() {
        bottomEditLayout = findViewById(R.id.bottom_edit_layout);
        editTv = findViewById(R.id.btn_edit);
        editTv.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        folder_id = getIntent().getLongExtra("folder_id", -1);
        folder_name = getIntent().getStringExtra("folder_name");
        SimpleLog.d(TAG, "folder_id = " + folder_id + " folder_name = " + folder_name);
        initViews();
        queryImages(folder_id);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    private void initBottomOperateLayout() {
        bottomOperateView = findViewById(R.id.bottom_operate_bar);
        bottomOperateView.setVisibility(View.GONE);
        bm_allselect = (TextView) bottomOperateView.findViewById(R.id.bm_allselect);
        mCbCheckAll = (CommonCheckBox1) bottomOperateView.findViewById(R.id.cb_check_all);
        bm_delete = (TextView) bottomOperateView.findViewById(R.id.bm_delete);
        bm_more = (TextView) bottomOperateView.findViewById(R.id.bm_more);
        bm_complete = (TextView) bottomOperateView.findViewById(R.id.bm_complete);

        bm_delete.setEnabled(false);
        bm_more.setEnabled(false);

        bm_allselect.setOnClickListener(this);
        bm_delete.setOnClickListener(this);
        bm_more.setOnClickListener(this);
        bm_complete.setOnClickListener(this);
        mCbCheckAll.setOnClickListener(this);
    }

    private void initTitleBar() {
        CommonTitleBar titleBar = (CommonTitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(folder_name);
        titleBar.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageItemAdapter.isEditMode()) {
                    if(imageMoreClickView!=null&&imageMoreClickView.isShowing()){
                        imageMoreClickView.setIsShowing(false);
                    } else{
                        exitEditMode();
                    }
                }else{
                    finish();
                }

            }
        });

//        editBtn = (TextView) titleBar.findViewById(R.id.common_tv_setting);
//        editBtn.setVisibility(View.VISIBLE);
//        editBtn.setText(getString(R.string.edit));
//        editBtn.setOnClickListener(this);
    }

    private void queryImages(long id) {
        queryHandler.startQueryImages(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:
                changeMode();
                break;
            case R.id.bm_allselect:
                if(imageMoreClickView!=null)
                    imageMoreClickView.setIsShowing(false);
                selectAll();
                break;
//            case R.id.bm_send:
//                send();
////                sendGoogleStatistics(GoogleConfigDefine.PIC_SEND);
//                break;
            case R.id.bm_delete:
                if(imageMoreClickView!=null)
                    imageMoreClickView.setIsShowing(false);
                showDeleteDialog();
//                sendGoogleStatistics(GoogleConfigDefine.PIC_DELETE);
                break;
            case R.id.bm_more:
                showMoreDialog();
//                sendGoogleStatistics(GoogleConfigDefine.PIC_MORE);
                break;
            case R.id.cb_check_all:
                if(imageMoreClickView!=null)
                    imageMoreClickView.setIsShowing(false);
                bm_allselect.performClick();
                break;
            case R.id.bm_complete:
                exitEditMode();
                break;

        }
    }

    private void showDeleteDialog() {
        final CommonDialog dialog = new CommonDialog(this, "", getString(R.string.file_delete_hint));
        dialog.setTitle(getString(R.string.delete));
        dialog.setBtnCancel(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setBtnOk(getString(R.string.delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                delete();
//                sendGoogleStatistics(GoogleConfigDefine.PIC_DELETE_CONFIRM);
            }
        });
        dialog.show();
    }

    private void showMoreDialog() {
        ArrayList<ImageItemInfo> list = new ArrayList<ImageItemInfo>();
        if (imageItemAdapter.getSelectedData().size() == 1) {
            ImageItemInfo info = imageItemAdapter.getSelectedData().entrySet().iterator().next().getValue();
            list.add(info);
            showDialog(DIALOG_TYPE_SINGLE_SELECTED, list);
        } else {
            for (Map.Entry<String, ImageItemInfo> entry : imageItemAdapter.getSelectedData().entrySet()) {
                list.add(entry.getValue());
            }
            showDialog(DIALOG_TYPE_MULTI_SELECTED, list);
        }
    }

    private void showDialog(int type, final List<ImageItemInfo> list) {
//        String[] items_single = {
//                getString(R.string.menu_more_set_wallpaper),
//                getString(R.string.download_rename),
//                getString(R.string.download_change_storage_location),getString(R.string.download_file_property)
//        };
//        String[] items_multi = {
//                getString(R.string.download_change_storage_location)
//        };
//        AdapterView.OnItemClickListener listener_multi = new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                switch (position) {
//                    case 0:// 更改存储位置
//                        changeStorageLocation(list);
////                        sendGoogleStatistics(GoogleConfigDefine.PIC_CHANGE_LOCATION);
//                        break;
//                }
//                exitEditMode();
//            }
//        };
//
//        AdapterView.OnItemClickListener listener_single = new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                switch (position) {
//                    case 0:// 设为手机壁纸
//                        setImageWallpaper(list.get(0));
////                        sendGoogleStatistics(GoogleConfigDefine.PIC_SET_WALLPAPER);
//                        break;
//                    case 1:// 重命名
//                        reName(list.get(0));
////                        sendGoogleStatistics(GoogleConfigDefine.PIC_RENAME);
//                        break;
//                    case 2:// 更改存储位置
//                        changeStorageLocation(list);
////                        sendGoogleStatistics(GoogleConfigDefine.PIC_CHANGE_LOCATION);
//                        break;
//                    case 3://文件属性
//
//                        FilePropActivity.start(ImageFolderDetailsActivity.this,list.get(0));
//                    default:
//                        break;
//                }
//                exitEditMode();
//            }
//        };

        switch (type) {
            case DIALOG_TYPE_SINGLE_SELECTED:
//                showContextDialog(items_single, listener_single);
                if(imageMoreClickView==null)
                    imageMoreClickView = new ImageMoreClickView(this, bm_more);
                imageMoreClickView.setImageItemInfos(list);
                imageMoreClickView.setMulti(false);


                break;
            case DIALOG_TYPE_MULTI_SELECTED:
//                showContextDialog(items_multi, listener_multi);
                if(imageMoreClickView == null)
                    imageMoreClickView = new ImageMoreClickView(this, bm_more);
                imageMoreClickView.setImageItemInfos(list);
                imageMoreClickView.setMulti(true);
                break;
        }
        if(imageMoreClickView!=null)
            imageMoreClickView.showMenu();
    }


    public void setImageWallpaper(ImageItemInfo info) {
        Intent intent = new Intent(this, SetWallpaperActivity.class);
        intent.putExtra("image_path", info.getPath());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    public void changeStorageLocation(List<ImageItemInfo> list) {
        ArrayList<String> paths = new ArrayList<>();
        for (ImageItemInfo info : list) {
            paths.add(info.getId() + "--" + info.getPath());
        }
        Intent intent = new Intent(this, ChooseDirActivity.class);
        intent.putExtra(CommonData.KEY_CURRENT_DOWN_FOLDER, VCStoragerManager.getInstance().getPhoneStorage());
        intent.putExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_PHONE);
        intent.putExtra("from", GoogleConfigDefine.PIC);
        intent.putStringArrayListExtra("old_paths", paths);
        startActivityForResult(intent, 100);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 200) {
            switch (requestCode) {
                case REQUEST_CHANGE_LOCATION:
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
                                Uri ringUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, oldId);
                                ContentValues values = new ContentValues(3);
                                values.put(MediaStore.Images.Media.DATA, targetPath);
                                values.put(MediaStore.Images.Media.DISPLAY_NAME, oldFile.getName());
                                queryHandler.startUpdate(1, null, ringUri, values, null, null);
                            } catch (Exception e) {
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

    public void reName(final ImageItemInfo info) {
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
                if (newName.equals(name)) {  //名称没有改变
//                    hideSoftInputFromWindow(getApplicationContext(),etName);
                    hideSoftInputFromWindow();
                    dialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newName.replace(" ", ""))) {
//                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    hideSoftInputFromWindow();
                    CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
                    return;
                }
                // 	处理文件名为 ".xxx" 情况
                if (newName.lastIndexOf(".") == 0) {
//                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    hideSoftInputFromWindow();
                    CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
                    return;
                }
                // 判断是否有重名文件
                if (new File(folder + newName).exists() && !TextUtils.equals(newName, name)) {
//                    SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                    hideSoftInputFromWindow();
                    CustomToastUtils.getInstance().showDurationToast(R.string.download_file_name_exists, 3000);
                    return;
                }
                final String fName = newName;
                File directory = new File(folder);
                File from = new File(directory, name);
                File to = new File(directory, newName);
                from.renameTo(to);

                queryHandler.updateFileName(newName, from.getPath(), to.getPath());

//                sendGoogleStatistics(GoogleConfigDefine.PIC_RENAME_CONFIRM);
//                SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
                hideSoftInputFromWindow();
                dialog.dismiss();
            }
        });
        dialog.setBtnCancelListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftInputFromWindow();
//                SoftInputMethodUtils.hideSoftInputFromWindow(getApplicationContext(),etName);
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
                        (InputMethodManager) etName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etName, 0);
            }
        }, 200);
    }

    private void hideSoftInputFromWindow() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void sendGoogleStatistics(String currentBtn) {
//        Statistics.sendOnceStatistics(GoogleConfigDefine.FILE_MANAGER_STATISTICS,GoogleConfigDefine.PIC,currentBtn);
    }

    private void delete() {
        if (imageItemAdapter == null) {
            return;
        }

        Map<String, ImageItemInfo> data = imageItemAdapter.getSelectedData();
        if (data.isEmpty()) {
            return;
        }

        final ArrayList<ImageItemInfo> list = new ArrayList<ImageItemInfo>();
        for (Map.Entry<String, ImageItemInfo> entry : data.entrySet()) {
            list.add(entry.getValue());
        }

        for (int i = 0; i < list.size(); i++) {
            FileUtils.delete(list.get(i).getPath());
        }

        delete(list);
        //imageItemAdapter.deleteItem(list);
    }

    private void delete(List<ImageItemInfo> list) {
        final String where = buildWhere(list);
        queryHandler.startDelete(2,
                null,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media._ID + " in " + where,
                null);
    }

    private String buildWhere(List<ImageItemInfo> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (ImageItemInfo info : list) {
            sb.append(info.getId()).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        return sb.toString();
    }

    private void send() {
        if (imageItemAdapter == null) {
            return;
        }
        Map<String, ImageItemInfo> data = imageItemAdapter.getSelectedData();
        if (data.isEmpty()) {
            return;
        }
        ArrayList<Uri> uris = new ArrayList<>();
        for (String key : data.keySet()) {
            uris.add(Uri.fromFile(new File(key)));
        }
        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                : android.content.Intent.ACTION_SEND);
        if (multiple) {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void selectAll() {
        if (!mSelectAll) {
//            bm_allselect.setImageResource(R.drawable.common_checkbox1_checked);
            bm_allselect.setText(R.string.check_all);
            imageItemAdapter.selectAll();
            mSelectAll = true;
            mCbCheckAll.setChecked(true);
        } else {
//            bm_allselect.setImageResource(R.drawable.footer_icon_finish);
//            bm_allselect.setText(R.string.check_all_cancel);
            imageItemAdapter.deSelectAll();
            mCbCheckAll.setChecked(false);
            mSelectAll = false;
        }
    }

    private void changeMode() {
//        if (editBtn.getText().toString().equals(getString(R.string.edit))) {
//            editBtn.setText(getString(R.string.complete));
////            bm_allselect.setImageResource(R.drawable.footer_icon_finish);
//            bm_allselect.setText(R.string.check_all);
//            bottomOperateView.setVisibility(View.VISIBLE);
//            imageItemAdapter.changeEditState(true);
////            sendGoogleStatistics(GoogleConfigDefine.PIC_EDIT);
//        } else {
//            editBtn.setText(getString(R.string.edit));
//            bottomOperateView.setVisibility(View.GONE);
//            imageItemAdapter.changeEditState(false);
//        }
        if (!imageItemAdapter.isEditMode()) {
            bottomOperateView.setVisibility(View.VISIBLE);
            bottomEditLayout.setVisibility(View.GONE);
            imageItemAdapter.changeEditState(true);
//            sendGoogleStatistics(GoogleConfigDefine.PIC_EDIT);
        } else {
            bottomOperateView.setVisibility(View.GONE);
            bottomEditLayout.setVisibility(View.VISIBLE);
            imageItemAdapter.changeEditState(false);
        }
    }

    public void exitEditMode() {
//        editBtn.setText(getString(R.string.edit));
//        bm_allselect.setImageResource(R.drawable.footer_icon_finish);
        bm_allselect.setText(R.string.check_all);
        bottomOperateView.setVisibility(View.GONE);
        bottomEditLayout.setVisibility(View.VISIBLE);
        imageItemAdapter.changeEditState(false);
        if(imageMoreClickView!=null)
            imageMoreClickView.setIsShowing(false);
    }

    private void enterEditMode() {
//        editBtn.setText(getString(R.string.complete));
//        bm_allselect.setImageResource(R.drawable.footer_icon_finish);
        bm_allselect.setText(R.string.check_all);
        bottomOperateView.setVisibility(View.VISIBLE);
        imageItemAdapter.changeEditState(true);
        bottomEditLayout.setVisibility(View.GONE);
    }

    @Override
    public void onShow(boolean show, boolean allSelected) {
        enable3Btns(show);
        mSelectAll = allSelected;

        if (allSelected) {
//            bm_allselect.setImageResource(R.drawable.footer_icon_finish_pre);
//            bm_allselect.setText(R.string.check_all_cancel);
        } else {
//            bm_allselect.setImageResource(R.drawable.footer_icon_finish);
            bm_allselect.setText(R.string.check_all);

        }
    }

    public void enable3Btns(boolean enabled) {
//        bm_send.setEnabled(enabled);
        bm_delete.setEnabled(enabled);
        bm_more.setEnabled(enabled);
    }

    private void disableEditBtn() {
//        editBtn.setEnabled(false);
        editTv.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (imageItemAdapter.isEditMode()) {
            if(imageMoreClickView!=null&&imageMoreClickView.isShowing()){
                imageMoreClickView.dismiss();
                imageMoreClickView.setIsShowing(false);
            } else{
                exitEditMode();
            }
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (imageItemAdapter.isEditMode()) {
            exitEditMode();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (imageItemAdapter.isEditMode()) {
            CommonCheckBox1 cb = ((CommonCheckBox1) view.findViewById(R.id.image_item_checkbox));
            cb.setChecked(!cb.isChecked());
            if(imageMoreClickView!=null)
                imageMoreClickView.setIsShowing(false);
        } else {
            ImageGalleryActivity.start(this, position, (ArrayList<String>) imageItemAdapter.getImagePaths());
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        enterEditMode();
        if (imageItemAdapter.isEditMode()) {
            ((CommonCheckBox1) view.findViewById(R.id.image_item_checkbox)).setChecked(true);
        }
//        sendGoogleStatistics(GoogleConfigDefine.PIC_LONGPRESS);
        return true;
    }

    private class QueryHandler extends MediaDBProvider {

        public QueryHandler(Context context) {
            super(context);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null) {
                List<ImageItemInfo> list = CursorDataParserUtils.parseImagesFromCursor(cursor, this);
                if (list != null && list.isEmpty()) {
                    disableEditBtn();
                }
                imageItemAdapter.changeData(list);
            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            queryImages(folder_id);
            ConfigManager.getInstance().notifyFileCountChanged(Constants.TYPE_IMAGE);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            exitEditMode();
            queryImages(folder_id);
            QueryUtils.notifyFileCountChanged(TYPE_IMAGE);
        }
    }


    private class ImageItemAdapter extends BaseAdapter {
        private Context context;
        private List<ImageItemInfo> data;
        private LayoutInflater inflater;
        /**
         * 正常模式 false， 编辑模式 true
         */
        private boolean editMode;
        /**
         * 已选择的条目容器
         */
        private Map<String, ImageItemInfo> map;
        /**
         * 是否enable 底部按钮的callback
         */
        private ICallback callback;

        public ImageItemAdapter(Context context, List<ImageItemInfo> data, ICallback callback) {
            this.context = context;
            this.data = data;
            inflater = LayoutInflater.from(context);
            map = new HashMap<String, ImageItemInfo>();
            this.callback = callback;
        }

        public boolean isEditMode() {
            return editMode;
        }

        public Map<String, ImageItemInfo> getSelectedData() {
            return map;
        }

        public List<ImageItemInfo> getData() {
            return data;
        }

        public List<String> getImagePaths() {
            ArrayList<String> list = new ArrayList<>();
            if (list != null) {
                for (ImageItemInfo info : data) {
                    list.add(info.getPath());
                }
            }
            return list;
        }

        public void changeData(List<ImageItemInfo> list) {
            if (data == null) {
                data = new ArrayList<>();
            }
            data.clear();
            if (list != null) {
                data.addAll(list);
            }
            this.notifyDataSetChanged();
        }

        /**
         * 改变模式
         */
        public void changeEditState(boolean state) {
            editMode = state;
            if (!state) {
                map.clear();
            }
            notifyDataSetChanged();
        }

        /**
         * 全选
         */
        public void selectAll() {
            if (getCount() != 0) {
                map.clear();
                for (ImageItemInfo info : data) {
                    map.put(info.getPath(), info);
                }
                notifyDataSetChanged();
            }
        }

        /**
         * 取消全选
         */
        public void deSelectAll() {
            if (getCount() != 0) {
                map.clear();
                notifyDataSetChanged();
                callback.onShow(false, false);
            }
        }

        /**
         * 删除勾选的条目
         */
        public void deleteItem(List<ImageItemInfo> list) {
            for (int i = 0; i < list.size(); i++) {
                data.remove(list.get(i));
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (data != null && !data.isEmpty()) {
                return data.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (data != null && !data.isEmpty()) {
                return data.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (data != null && !data.isEmpty()) {
                return data.get(position).getId();
            }
            return 0;
        }

        int getCheckedCount() {
            return map.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.file_image_item_list_item, parent, false);
                holder.image_item_icon = (ImageView) convertView.findViewById(R.id.image_item_icon);
                holder.image_item_checkbox = (CommonCheckBox1) convertView.findViewById(R.id.image_item_checkbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ImageItemInfo info = data.get(position);
            Glide.with(context).load(new File(info.getPath())).centerCrop().into(holder.image_item_icon);
            holder.image_item_checkbox.setVisibility(editMode ? View.VISIBLE : View.GONE);
            holder.image_item_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
                                                                       @Override
                                                                       public void onCheckChanged(View v, boolean isChecked) {
                                                                           if (isChecked) {
                                                                               if (!map.containsKey(info.getPath())) {
                                                                                   map.put(info.getPath(), info);
                                                                               }

                                                                               if (getCount() != 0 && getCount() == map.size()) {
                                                                                   callback.onShow(true, true);
                                                                               } else {
                                                                                   callback.onShow(true, false);
                                                                               }
                                                                           } else {
                                                                               if (map.containsKey(info.getPath())) {
                                                                                   map.remove(info.getPath());
                                                                               }

                                                                               if (getCount() != 0 && getCount() != map.size()) {
                                                                                   callback.onShow(true, false);
                                                                               }

                                                                               if (map.isEmpty()) {
                                                                                   callback.onShow(false, false);
                                                                               }
                                                                           }

                                                                            ImageFolderDetailsActivity.this.mCbCheckAll.setChecked(getCheckedCount() == data.size());
                                                                       }
                                                                   }

            );

            if (map.containsKey(info.getPath())) {
                holder.image_item_checkbox.setChecked(true);
            } else {
                holder.image_item_checkbox.setChecked(false);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView image_item_icon;
            CommonCheckBox1 image_item_checkbox;
        }
    }


}
