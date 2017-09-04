package com.polar.browser.imagebrowse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SimpleLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duan on 17/3/20.
 */

public class ImageBrowseView extends RelativeLayout implements View.OnClickListener {

    public static final String IMAGE_LIST = "image_list";

    private RecyclerView mRvImages;
    private ImageBrowseAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private List<ImageInfo> mData = new ArrayList<>();
    private boolean isLoadingMore;

    public ImageBrowseView(Context context) {
        this(context, null);
    }

    public ImageBrowseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_image_browse, this);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mRvImages = (RecyclerView) findViewById(R.id.rv_images);
        mLayoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        mRvImages.setLayoutManager(mLayoutManager);
        mRvImages.addItemDecoration(new GridDivider());
    }

    private void initData() {
        mAdapter = new ImageBrowseAdapter(getContext(), mData);
        mRvImages.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), ImageBrowseGalleryActivity.class);
                intent.putExtra("position", position);
                intent.putExtra(IMAGE_LIST, new ImageListData(mData));
                getContext().startActivity(intent);
                ((Activity)getContext()).overridePendingTransition(R.anim.alpha_scale_in, 0);
                Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.FULL_MODE_PICTURE);
            }
        });
    }

    public ImageBrowseAdapter getAdapter(){
        return mAdapter;
    }

    private void initListener() {
        findViewById(R.id.back).setOnClickListener(this);
        mRvImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (dy > 0 && !isLoadingMore && visibleItem >= mAdapter.getItemCount() - 1) {
                    isLoadingMore = true;
                    ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                        @Override
                        public void run() {
                            JavaScriptManager.injectAlbumInsJs(TabViewManager.getInstance().getCurrentTabView().getContentView().getWebView());
                        }
                    }, 500);
                }
            }
        });
    }

    public void showImages(String imgs) {
        isLoadingMore = false;
        if (TextUtils.isEmpty(imgs)) {
            // 数据为空，处理
            return;
        }
        List<ImageInfo> infos = getImageList(imgs);
        if (infos.isEmpty()) return;
        if (infos.size() == mData.size()) return;
        for (int i = mData.size(); i < infos.size(); i++) {
            mData.add(infos.get(i));
        }
        mAdapter.notifyDataSetChanged();
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
                setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    public void hide() {
        setVisibility(View.GONE);
        mData.clear();
        mAdapter.notifyDataSetChanged();
    }

}
