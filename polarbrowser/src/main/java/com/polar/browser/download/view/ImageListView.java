package com.polar.browser.download.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.polar.browser.R;
import com.polar.browser.bean.ImageFolderInfo;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.ImageFolderDetailsActivity;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CursorDataParserUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;

/**
 * Created by saifei on 16/12/30.
 */

public class ImageListView extends AbstractFileListView<ImageFolderInfo>{

    private String mFilePath;

    public ImageListView(Context context) {
        this(context, null);
    }

    public ImageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnItemLongClickListener(longClickListener);
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    @Override
    public String type() {
        return TYPE_IMAGE;
    }

    @Override
    protected void onClickFile(AdapterView<?> parent, View view, int position, long id) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_FILE_IMAGE_ITEM);
        ImageFolderInfo imageFolderInfo = fileBaseAdapter.getItem(position);

        Intent intent = new Intent(getContext(), ImageFolderDetailsActivity.class);
        intent.putExtra("folder_id", imageFolderInfo.getId());
        intent.putExtra("folder_name", imageFolderInfo.getName());
        getContext().startActivity(intent);
    }

    @Override
    protected void bindView(View view, int position, ImageFolderInfo imageFolderInfo, boolean isScrollState) {
        ImageListItem imageListItem = (ImageListItem) view;
        imageListItem.bind(imageFolderInfo);

    }

    @Override
    protected View newView(Context context, ImageFolderInfo data, ViewGroup parent, int type) {
        return new ImageListItem(context);
    }

    @Override
    protected List<ImageFolderInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseImageBucketsFromCursor(cursor, queryHandler);
    }

    @Override
    protected ArrayList<Uri> buildUris(List<ImageFolderInfo> list) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (ImageFolderInfo info : list) {
            uris.add(Uri.fromFile(new File(info.getFolderPath())));
        }
        return uris;
    }

    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if(isEditing())return true;
            longClickItemPosition = position;
            String[] items = {
//                    getResources().getString(R.string.download_rename),
                    getResources().getString(R.string.delete)
            };
            showContextDialog(items, mDialogItemClickListener);
            return true;
        }
    };

    protected AdapterView.OnItemClickListener mDialogItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ImageFolderInfo info = fileBaseAdapter.getItem(longClickItemPosition);
            List<ImageFolderInfo> checkedList = new ArrayList<>();
            checkedList.add(info);
            switch (position) {
//                case 0://重命名
//                    reName(info);
//                    break;
                case 0://删除 TODO
                    showDeleteDialog(checkedList);
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void startDelete(List<ImageFolderInfo> checkedList){
        if (fileBaseAdapter == null) {
            return;
        }

        if (checkedList.isEmpty()) {
            return;
        }

        for (int i = 0; i < checkedList.size(); i++) {
            ImageFolderInfo folderInfo = checkedList.get(i);
            if(folderInfo == null) continue;
            if(FileUtils.isEqualFilePath(Environment.getExternalStorageDirectory().toString(),folderInfo.getFolderPath())){  // 要删除的目录是否为根目录
                continue;
            }
            FileUtils.delFolder1(checkedList.get(i).getFolderPath());
        }

        delete(checkedList);
        mActivity.onExitEditMode();
    }

    @Override
    protected String shareType() {
        return "image/";
    }

    @Override
    protected void onQueryComplete() {
        // 查询结束
        if (fileBaseAdapter == null) {
            return;
        }
        if (fileBaseAdapter.isEmpty()) {
            return;
        }
        if (TextUtils.isEmpty(mFilePath)) {
            return;
        }
        String folder = null;
        File file = new File(mFilePath);
        if (file != null && file.exists()) {
            folder = file.getParentFile().getAbsolutePath();
        }
        if (TextUtils.isEmpty(folder)) {
            return;
        }
        for (ImageFolderInfo info : fileBaseAdapter.getData()) {
            if (TextUtils.equals(folder, info.getFolderPath())) {
                Intent intent = new Intent(getContext(), ImageFolderDetailsActivity.class);
                intent.putExtra("folder_id", info.getId());
                intent.putExtra("folder_name", info.getName());
                mActivity.startActivity(intent);
                mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                mActivity.finish();
                return;
            }
        }
    }
}
