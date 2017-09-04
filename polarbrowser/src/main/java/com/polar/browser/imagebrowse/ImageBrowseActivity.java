package com.polar.browser.imagebrowse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SimpleLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ImageBrowseActivity extends Activity implements View.OnClickListener {

    public static final String IMAGE_LIST = "image_list";

    private RecyclerView mRvImages;
    private ImageBrowseAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private List<ImageInfo> mData;
    private boolean isLoadingMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browse);
        Statistics.sendOnceStatistics(
                GoogleConfigDefine.PAGE_OPERATION, GoogleConfigDefine.INFO_FLOAT_ICON);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        findViewById(R.id.back).setOnClickListener(this);
        mRvImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (dy > 0 && visibleItem > mAdapter.getItemCount() - 6 && !isLoadingMore) {
                    SimpleLog.d("onScrolled", "will load more -- - -- - ");
                    JavaScriptManager.injectAlbumInsJs(TabViewManager.getInstance().getCurrentTabView().getContentView().getWebView());
                    isLoadingMore = true;
                }
            }
        });
    }

    private void initView() {
        mRvImages = (RecyclerView) findViewById(R.id.rv_images);
        mLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mRvImages.setLayoutManager(mLayoutManager);
        mRvImages.addItemDecoration(new GridDivider());
    }

    private void initData() {
        String imgs = getIntent().getStringExtra(ImageBrowseActivity.IMAGE_LIST);
        if (TextUtils.isEmpty(imgs)) {
            // 数据为空，处理
            return;
        }
        mData = getImageList(imgs);
        mAdapter = new ImageBrowseAdapter(this, mData);
        mRvImages.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ImageBrowseActivity.this, ImageBrowseGalleryActivity.class);
                intent.putExtra("position", position);
                intent.putExtra(IMAGE_LIST, new ImageListData(mData));
                startActivity(intent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.FULL_MODE_PICTURE);
                overridePendingTransition(R.anim.alpha_scale_in, 0);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra(ImageBrowseActivity.IMAGE_LIST)) {
            String imgs = getIntent().getStringExtra(ImageBrowseActivity.IMAGE_LIST);
            if (TextUtils.isEmpty(imgs)) {
                // 数据为空，处理
                return;
            }
            mData = getImageList(imgs);
            mAdapter.notifyDataSetChanged();
            isLoadingMore = false;
            SimpleLog.d("onScrolled", "load more complete ~~~ ");
        }
    }

    private List<ImageInfo> getImageList(String imgs) {
        SimpleLog.d("getImageList", "imgs = " + imgs);
        List<ImageInfo> list = new ArrayList<>();
        if (imgs != null && imgs.length() > 0) {
            JSONObject object;
            try {
                object = new JSONObject(imgs);
                if (object != null && object.has("imgs")) {
                    JSONArray jArray = object.optJSONArray("imgs");
                    JSONObject jObj;
                    for (int i = 0; jArray != null && i < jArray.length(); ++i) {
                        jObj = jArray.getJSONObject(i);
                        String url = jObj.optString("url", "");
                        int width = jObj.optInt("width", 0);
                        int height = jObj.optInt("height", 0);
                        String profPic = jObj.optString("profPic", "");
                        String username = jObj.optString("username", "");
                        ImageInfo info = new ImageInfo(url , width , height , profPic , username);
                        list.add(info);
                    }
                }
            } catch (JSONException e) {
            }
        }
        return list;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }

}
