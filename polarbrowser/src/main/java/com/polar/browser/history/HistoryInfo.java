package com.polar.browser.history;

import java.util.Date;

/**
 * 历史记录结构体
 *
 * @author dpk
 */
public class HistoryInfo {
	public long id;
	public String url;
	public String title;
	public int src;
	public Date timestamp;
	public int count;
	public boolean isChecked = false;
}
