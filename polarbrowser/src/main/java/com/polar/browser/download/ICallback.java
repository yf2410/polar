package com.polar.browser.download;

/**
 * Created by yd_lp on 2016/10/21.
 */

public interface ICallback {
    /**
     * @show 是否显示底部menu true 显示， false 隐藏
     * @param allSelected 是否已全选 true 是 false 否
     * */
    public void onShow(boolean show, boolean allSelected);
}
