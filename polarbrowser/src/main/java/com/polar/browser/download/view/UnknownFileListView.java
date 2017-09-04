package com.polar.browser.download.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.polar.browser.R;
import com.polar.browser.bean.UnknownInfo;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CursorDataParserUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.OpenFileUtils;

import java.io.File;
import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_OTHER;

/**
 * Created by saifei on 17/1/3.
 * 其他文件 列表
 */

public class UnknownFileListView extends AbstractFileListView<UnknownInfo> {
    public UnknownFileListView(Context context) {
        this(context, null);
    }

    public UnknownFileListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public String type() {
        return TYPE_OTHER;
    }



    @Override
    protected void bindView(View view, int position, UnknownInfo info, boolean isScrollState) {
        UnknownFileItem unknownFileItem = (UnknownFileItem) view;
        unknownFileItem.bind(info);
    }

    @Override
    protected View newView(Context context, UnknownInfo data, ViewGroup parent, int type) {
        return new UnknownFileItem(context);
    }

    @Override
    protected List<UnknownInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseUnknownFilesFromCursor(cursor,queryHandler);
    }

    @Override
    protected void onClickFile(AdapterView<?> parent, View view, int position, long id) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_FILE_OTHER_ITEM);
        try{
            UnknownInfo info = fileBaseAdapter.getItem(position);
            if(info.getPath()!=null){
                OpenFileUtils.openFile(new File(info.getPath()),getContext());
                return;  //文件可用
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
    }
}
