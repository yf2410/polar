package com.polar.browser.download.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.savedpage.SavedPageNode;
import com.polar.browser.download.savedpage.SavedPageUtil;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.statistics.Statistics;

import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;

/**
 * Created by saifei on 17/1/3.
 * 其他文件 列表
 */

public class WebPageListView extends AbstractFileListView<SavedPageNode> {
    public WebPageListView(Context context) {
        this(context, null);
    }

    public WebPageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public String type() {
        return TYPE_WEB_PAGE;
    }



    @Override
    protected void bindView(View view, int position, SavedPageNode info, boolean isScrollState) {
        WebPageItem webPageItem = (WebPageItem) view;
        webPageItem.bind(info);
    }

    @Override
    protected View newView(Context context, SavedPageNode data, ViewGroup parent, int type) {
        return new WebPageItem(context);
    }

    @Override
    protected List<SavedPageNode> parseListFromCursor(Cursor cursor) {
        return null;
    }

    @Override
    protected void onClickFile(AdapterView<?> parent, View view, int position, long id) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, GoogleConfigDefine.DOWNLOAD_FILE_WEB_ITEM);
        SavedPageNode mNode = fileBaseAdapter.getItem(position);
        Intent intent = new Intent(this.getContext(), BrowserActivity.class);
        intent.setAction(CommonData.ACTION_LOAD_SAVED_PAGES);
        intent.putExtra(CommonData.EXTRA_LOAD_SAVED_PAGES_DATA, mNode.file.getAbsolutePath());
        getContext().startActivity(intent);
        ((Activity) getContext()).overridePendingTransition(0, 0);
    }

    @Override
    protected void loadList() {
        List<SavedPageNode> list = SavedPageUtil.getSavedPageList(mActivity);
        fileBaseAdapter.updateData(list);
        mActivity.updateEmptyView(list==null|| ListUtils.isEmpty(list));
    }

    @Override
    protected String shareType() {
        return "message/rfc822";
    }

    @Override
    protected void delete(List<SavedPageNode> list) {
        showDeleteDialog(list);
    }

    @Override
    protected void startDelete(List<SavedPageNode> checkedList) {
        mActivity.onExitEditMode();
        if (checkedList.isEmpty()) {
            return;
        }

        for (int i = 0; i < checkedList.size(); i++) {
            FileUtils.delete(checkedList.get(i).getPath());
        }
        loadList();
    }
}
