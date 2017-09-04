package com.polar.browser.bookmark;

public class BookmarkInfo {
	public static final String KEY_NAME = "name";
	public static final String KEY_ID = "id";
	public static final String KEY_TYPE = "type";
	public static final String KEY_URL = "url";

	public String name;
	public int id;
	public String type;
	public String url;
	public boolean isChecked;

	@Override
	public String toString() {
		return String.format("name:%s, id:%d, type:%s, url:%s", name, id, type, url);
	}
}
