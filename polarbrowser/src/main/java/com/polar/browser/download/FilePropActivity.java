package com.polar.browser.download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bean.BaseFileClass;
import com.polar.browser.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by saifei on 17/1/3.
 * 文件属性
 */

public class FilePropActivity  extends LemonBaseActivity{
    private static final String FILE_INFO = "file_info";
    private BaseFileClass baseFile;
    private SimpleDateFormat sdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_prop);
        initData();
        initViews();


    }

    private void initData() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.baseFile =(BaseFileClass)getIntent().getSerializableExtra(FILE_INFO);
    }

    private void initViews() {
        TextView fileNameTv = (TextView) findViewById(R.id.tv_file_name);
        TextView fileSizeTv = (TextView) findViewById(R.id.tv_file_size);
        TextView filePathTv = (TextView) findViewById(R.id.tv_file_path);
        TextView fileDownloadTimeTv = (TextView) findViewById(R.id.tv_file_download_time);
        fileNameTv.setText(baseFile.getName());
        fileSizeTv.setText(FileUtils.formatFileSize(baseFile.getSize()));
        filePathTv.setText(baseFile.getPath());
        fileDownloadTimeTv.setText(sdf.format(baseFile.getDate()*1000));
    }

    public static void start(Context context, BaseFileClass file) {
        Intent starter = new Intent(context, FilePropActivity.class);
        starter.putExtra(FILE_INFO,file);
        context.startActivity(starter);
    }
}
