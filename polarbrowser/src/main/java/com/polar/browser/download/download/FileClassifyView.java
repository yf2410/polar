package com.polar.browser.download.download;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;

import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_APK;
import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;
import static com.polar.browser.download_refactor.Constants.TYPE_DOC;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_OTHER;
import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;
import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;
import static com.polar.browser.utils.QueryUtils.KEY_APK;
import static com.polar.browser.utils.QueryUtils.KEY_AUDIO;
import static com.polar.browser.utils.QueryUtils.KEY_DOCUMENT;
import static com.polar.browser.utils.QueryUtils.KEY_IMAGE;
import static com.polar.browser.utils.QueryUtils.KEY_OTHER;
import static com.polar.browser.utils.QueryUtils.KEY_VIDEO;
import static com.polar.browser.utils.QueryUtils.KEY_WEB_PAGE;
import static com.polar.browser.utils.QueryUtils.KEY_ZIP_FILE;
import static com.polar.browser.utils.QueryUtils.queryMap;

/**
 * Created by saifei on 16/12/28.
 * 文件分类 视图
 */

public class FileClassifyView extends LinearLayout implements View.OnClickListener{

    private TextView tv_video_count;
    private TextView tv_audio_count;
    private TextView tv_image_count;
    private TextView tv_document_count;
    private TextView tv_other_files_count;
    private TextView tv_zip_file_count;
    private TextView tv_apk_count;
    private TextView tv_webPage_count;
    private TextView tv_video;
    private TextView tv_image;
    private TextView tv_music;
    private TextView tv_document;
    private TextView tv_zip;
    private TextView tv_apk;
    private TextView tv_web_page;
    private TextView tv_other;

    public FileClassifyView(Context context) {
        this(context, null);
    }

    public FileClassifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
//        setPadding(DensityUtil.dip2px(getContext(), 22), DensityUtil.dip2px(getContext(), 22),
//                DensityUtil.dip2px(getContext(), 22), DensityUtil.dip2px(getContext(), 22));
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);

        inflate(getContext(), R.layout.view_file_classify_layout, this);

        initView();

    }


    private void initView() {

        tv_video_count = (TextView)findViewById(R.id.tv_video_count);
        tv_audio_count = (TextView)findViewById(R.id.tv_audio_count);
        tv_image_count = (TextView)findViewById(R.id.tv_image_count);
        tv_document_count = (TextView)findViewById(R.id.tv_document_count);

        tv_zip_file_count = (TextView)findViewById(R.id.tv_zip_file_count);
        tv_apk_count = (TextView)findViewById(R.id.tv_apk_count);
        tv_webPage_count = (TextView)findViewById(R.id.tv_offline_files_count);
        tv_other_files_count = (TextView)findViewById(R.id.tv_other_files_count);

        tv_video = (TextView)findViewById(R.id.tv_video);
        tv_image = (TextView)findViewById(R.id.tv_image);
        tv_music = (TextView)findViewById(R.id.tv_music);
        tv_document = (TextView)findViewById(R.id.tv_document);

        tv_zip = (TextView)findViewById(R.id.tv_zip);
        tv_apk = (TextView)findViewById(R.id.tv_apk);
        tv_web_page = (TextView)findViewById(R.id.tv_web_page);
        tv_other = (TextView)findViewById(R.id.tv_other);

        setListeners();

    }

    private void setListeners() {
        tv_video.setOnClickListener(this);
        tv_image.setOnClickListener(this);
        tv_music.setOnClickListener(this);
        tv_document.setOnClickListener(this);

        tv_zip.setOnClickListener(this);
        tv_apk.setOnClickListener(this);
        tv_web_page.setOnClickListener(this);
        tv_other.setOnClickListener(this);

    }

    public void refreshUi() {
        if(queryMap!=null&&!queryMap.isEmpty()){
            tv_video_count.setText(String.valueOf(queryMap.get(KEY_VIDEO)));
            tv_audio_count.setText(String.valueOf(queryMap.get(KEY_AUDIO)));
            tv_image_count.setText(String.valueOf(queryMap.get(KEY_IMAGE)));
            tv_document_count.setText(String.valueOf(queryMap.get(KEY_DOCUMENT)));

            tv_zip_file_count.setText(String.valueOf(queryMap.get(KEY_ZIP_FILE)));
            tv_apk_count.setText(String.valueOf(queryMap.get(KEY_APK)));
            tv_webPage_count.setText(String.valueOf(queryMap.get(KEY_WEB_PAGE)));
            tv_other_files_count.setText(String.valueOf(queryMap.get(KEY_OTHER)));
        }
    }

    public void refreshUi(List<String> changedFileTypes) {
        if(queryMap!=null&&!queryMap.isEmpty()){
            if(changedFileTypes.contains(TYPE_VIDEO))
            tv_video_count.setText(String.valueOf(queryMap.get(KEY_VIDEO)));
            if(changedFileTypes.contains(TYPE_AUDIO))
            tv_audio_count.setText(String.valueOf(queryMap.get(KEY_AUDIO)));
            if(changedFileTypes.contains(TYPE_IMAGE))
            tv_image_count.setText(String.valueOf(queryMap.get(KEY_IMAGE)));
            if(changedFileTypes.contains(TYPE_DOC))
            tv_document_count.setText(String.valueOf(queryMap.get(KEY_DOCUMENT)));

            if(changedFileTypes.contains(TYPE_ZIP))
            tv_zip_file_count.setText(String.valueOf(queryMap.get(KEY_ZIP_FILE)));
            if(changedFileTypes.contains(TYPE_APK))
            tv_apk_count.setText(String.valueOf(queryMap.get(KEY_APK)));
            if(changedFileTypes.contains(TYPE_WEB_PAGE))
            tv_webPage_count.setText(String.valueOf(queryMap.get(KEY_WEB_PAGE)));
            if(changedFileTypes.contains(TYPE_OTHER))
            tv_other_files_count.setText(String.valueOf(queryMap.get(KEY_OTHER)));
        }
    }



    public void refreshUi(final String type) {
        ThreadManager.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                switch (type){
                    case TYPE_APK:
                        tv_apk_count.setText(String.valueOf(queryMap.get(TYPE_APK)));
                        break;
                    case TYPE_AUDIO:
                        tv_audio_count.setText(String.valueOf(queryMap.get(TYPE_AUDIO)));
                        break;
                    case TYPE_DOC:
                        tv_document_count.setText(String.valueOf(queryMap.get(TYPE_DOC)));
                        break;
                    case TYPE_IMAGE:
                        tv_image_count.setText(String.valueOf(queryMap.get(TYPE_IMAGE)));
                        break;
                    case TYPE_OTHER:
                        tv_other_files_count.setText(String.valueOf(queryMap.get(TYPE_OTHER)));
                        break;
                    case TYPE_VIDEO:
                        tv_video_count.setText(String.valueOf(queryMap.get(TYPE_VIDEO)));
                        break;
                    case TYPE_WEB_PAGE:
                        tv_webPage_count.setText(String.valueOf(queryMap.get(TYPE_WEB_PAGE)));
                        break;
                    case TYPE_ZIP:
                        tv_zip_file_count.setText(String.valueOf(queryMap.get(TYPE_ZIP)));
                        break;
                }
            }
        });


    }

    @Override
    public void onClick(View v) {
        String type = null;
        String statisticType = null;
        switch (v.getId()) {
            case R.id.tv_video:
                type = TYPE_VIDEO;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_VIDEO;
                break;
            case R.id.tv_image:
                type = TYPE_IMAGE;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_IMAGE;
                break;
            case R.id.tv_document:
                type = TYPE_DOC;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_DOC;
                break;
            case R.id.tv_music:
                type = TYPE_AUDIO;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_AUDIO;
                break;
            case R.id.tv_zip:
                type = TYPE_ZIP;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_RAR;
                break;
            case R.id.tv_apk:
                type = TYPE_APK;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_APK;
                break;
            case R.id.tv_web_page:
                type = TYPE_WEB_PAGE;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_WEB;
                break;
            case R.id.tv_other:
                type = TYPE_OTHER;
                statisticType = GoogleConfigDefine.DOWNLOAD_FILE_OTHER;
                break;
        }
        Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, statisticType);

        FileClassifyDetailActivity.start(getContext(),type);
        ((Activity)getContext()).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }
}
