package com.polar.browser.download.view;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.bean.ApkInfo;
import com.polar.browser.utils.CursorDataParserUtils;

import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_APK;

/**
 * Created by saifei on 16/12/30.
 * <p>
 * 安装包列表
 */

public class ApkListView extends AbstractFileListView<ApkInfo> {

    private static final String TAG = "ApkListView";

    public ApkListView(Context context) {
        this(context, null);
    }

    public ApkListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String type() {
        return TYPE_APK;
    }

    @Override
    protected View newView(Context context, ApkInfo data, ViewGroup parent, int type) {
        return new ApkItemView(context);
    }

    @Override
    protected List<ApkInfo> parseListFromCursor(Cursor cursor) {
        return CursorDataParserUtils.parseAPKsFromCursor(cursor,getContext());
    }

    @Override
    protected void bindView(View view, int position, ApkInfo data, boolean isScrollState) {
        ApkItemView itemView = (ApkItemView) view;
        itemView.bind(data,isScrollState);
    }

}
