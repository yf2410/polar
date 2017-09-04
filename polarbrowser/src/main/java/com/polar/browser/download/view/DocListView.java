package com.polar.browser.download.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.polar.browser.R;
import com.polar.browser.bean.DocInfo;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CursorDataParserUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.OpenFileUtils;

import java.io.File;
import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_DOC;

/**
 * Created by saifei on 17/1/3.
 * 文档 列表
 */

public class DocListView extends AbstractFileListView<DocInfo> {
    public DocListView(Context context) {
        this(context, null);
    }

    public DocListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public String type() {
        return TYPE_DOC;
    }



    @Override
    protected void bindView(View view, int position, DocInfo info, boolean isScrollState) {
        DocumentFileItem documentFileItem = (DocumentFileItem) view;
        documentFileItem.bind(info);
    }

    @Override
    protected View newView(Context context, DocInfo data, ViewGroup parent, int type) {
        return new DocumentFileItem(context);
    }

    @Override
    protected List<DocInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseDocumentsFromCursor(cursor,queryHandler);
    }

    @Override
    protected void onClickFile(AdapterView<?> parent, View view, int position, long id) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,GoogleConfigDefine.DOWNLOAD_FILE_DOC_ITEM);
        try{
            DocInfo info = fileBaseAdapter.getItem(position);
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
