package com.polar.browser.push;

import com.polar.browser.vclibrary.bean.db.PushedSystemNews;
import com.polar.browser.vclibrary.bean.db.SystemNews;

import java.util.Date;

/**
 * Created by James on 2016/8/15.
 */
public class Converter {
	public static SystemNews convert(String version, PushedSystemNews pushedSystemNews) {
		return new SystemNews(pushedSystemNews.getTitle(), pushedSystemNews.getUrl(), new Date());
	}
}
