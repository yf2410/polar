package com.polar.browser.push.api;

import com.polar.browser.vclibrary.bean.Site;

import java.util.List;

/**
 * Created by FKQ on 2016/11/10.
 */

public interface IAllSiteListCallback {

    void notifyQueryResult(List<Site> siteList);

    void listIsNull();

    void error(Exception e);

}
