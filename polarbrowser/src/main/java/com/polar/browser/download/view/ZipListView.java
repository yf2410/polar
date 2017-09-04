package com.polar.browser.download.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.polar.browser.R;
import com.polar.browser.bean.ZipInfo;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CursorDataParserUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.OpenFileUtils;

import java.io.File;
import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;

/**
 * Created by saifei on 17/1/5.
 */

public class ZipListView extends AbstractFileListView<ZipInfo> {

    public ZipListView(Context context) {
        super(context);
    }

    public ZipListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String type() {
        return TYPE_ZIP;
    }

    @Override
    protected void bindView(View view, int position, ZipInfo data, boolean isScrollState) {
        ZipFileItem item = (ZipFileItem) view;
        item.bind(data);
    }

    @Override
    protected View newView(Context context, ZipInfo data, ViewGroup parent, int type) {
        return new ZipFileItem(context);
    }

    @Override
    protected List<ZipInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseCompressFilesFromCursor(cursor, queryHandler);
    }

    @Override
    protected void onClickFile(AdapterView<?> parent, View view, int position, long id) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_FILE_RAR_ITEM);
        ZipInfo info = fileBaseAdapter.getItem(position);
        try{
        if (info != null && info.getPath() != null) {
            OpenFileUtils.openFile(new File(info.getPath()), getContext());
            return;  //文件可用
        }
        }catch (Exception e){
            e.printStackTrace();
            CustomToastUtils.getInstance().showDurationToast(e.toString(),5000);
        }
        CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
    }
}
