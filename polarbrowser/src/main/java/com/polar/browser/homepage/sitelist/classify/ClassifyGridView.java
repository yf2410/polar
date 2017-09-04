package com.polar.browser.homepage.sitelist.classify;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.polar.browser.R;
import com.polar.browser.homepage.customlogo.IComplete;

import java.util.ArrayList;
import java.util.List;

public class ClassifyGridView extends GridView {

    private ClassifyAdapter mAdapter;
    private IComplete complete;
    private List<ClassifyItemInfo> classifyItemInfoList = new ArrayList<>();
    private int[] imgResources = new int[]{R.drawable.logo_video, R.drawable.logo_social, R.drawable.logo_news, R.drawable.logo_game, R.drawable.logo_sports, R.drawable.logo_music, R.drawable.logo_shopping, R.drawable.logo_life};
    private int[] titleResources = new int[]{R.string.navigate_title_video,
            R.string.navigate_title_social,
            R.string.navigate_title_news,
            R.string.navigate_title_games,
            R.string.navigate_title_sports,
            R.string.navigate_title_music,
            R.string.navigate_title_shop,
            R.string.navigate_title_life};

    public ClassifyGridView(Context context) {
        this(context, null);
    }

    public ClassifyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setSelector(R.drawable.empty_selector);
        int spacing = 1;
        setVerticalSpacing(spacing);
        setHorizontalSpacing(spacing);
        this.setNumColumns(2);
        mAdapter = new ClassifyAdapter(getContext());
        this.setAdapter(mAdapter);
        refreshData();
    }

    public IComplete getComplete() {
        return complete;
    }


    public void setComplete(IComplete complete) {
        this.complete = complete;
    }


    /**
     * 重新加载数据
     * <p>
     * 推荐	1
     * <p>
     * 视频	2
     * 社交	3
     * 新闻	4
     * 游戏	5
     * 体育	6
     * 音乐	7
     * 购物	8
     * 生活	9
     * public static final int TYPE_RECOMMAND = 1;
     * public static final int TYPE_VIDEO = 2;
     * public static final int TYPE_SOCIETY = 3;
     * public static final int TYPE_NEWS = 4;
     * public static final int TYPE_GAME = 5;
     * public static final int TYPE_SPORTS = 6;
     * public static final int TYPE_MUSIC = 7;
     * public static final int TYPE_SHOPPING = 8;
     * public static final int TYPE_LIFE = 9;
     */
    public void refreshData() {
        for (int i = 0; i <= 7; i++) {
            classifyItemInfoList.add(new ClassifyItemInfo(i + 2, getResources().getString(titleResources[i]), imgResources[i]));
        }
        mAdapter.updateData(classifyItemInfoList);
    }
}
