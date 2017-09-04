package com.polar.browser.bookmark;

import android.text.TextUtils;

import com.polar.browser.common.data.CommonData;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ZipUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BookmarkStorage {

	private static final String TAG = "BookmarkStorage";

	private Integer mMaxId = BookmarkNodeDefine.BASE_ID;

	private final List<BookmarkInfo> mLocalBookmarkList = new ArrayList<>();
	private final List<BookmarkInfo> mOnLineBookmarkList = new ArrayList<>();

	List<BookmarkInfo> queryBookmarkInfo() {
		return bookmarkList();
	}

	public boolean isEmpty() {
		return bookmarkList().isEmpty();
	}

	public void init(File file) {
		loadFromFile(file);
	}

	void createFile(File file) {
		saveBookmarkFile(file);
	}

	/**
	 * 从文件中得到Bookmark
	 *
	 * @param file
	 */
	private void loadFromFile(File file) {
		byte[] jsonBytes = ZipUtil.unGZip(FileUtils.readFile(file));//解压
		if (jsonBytes != null && jsonBytes.length > 0) {
			try {
				JSONObject jsonObject = new JSONObject(new String(jsonBytes));
				//{"maxid":1,"roots":{"bookmark":{"name":"书签","children":[],"id":1,"type":"folder"}}}
				loadBookmarkInfoFromJson(jsonObject, false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				SimpleLog.e(e);
			}
		}
	}

	/**
	 * 保存bookmark到文件中
	 *
	 * @param file
	 */
	void saveBookmarkFile(File file) {
		JSONObject json = createJson();
		FileUtils.writeFile(file, ZipUtil.gZip(json.toString().getBytes()));//压缩文件保存
	}

	/**
	 * 添加收藏
	 *
	 * @param name
	 * @param url
	 */
	void addBookmark(String name, String url) {
		synchronized (bookmarkList()) {
			BookmarkInfo info = new BookmarkInfo();
			info.id = ++mMaxId;
			info.name = name;
			info.url = url;
			info.type = BookmarkNodeDefine.NODE_TYPE_URL;
			bookmarkList().add(0, info);
		}
	}

	/**
	 * 更新收藏
	 *
	 * @param name
	 * @param url
	 */
	void updateBookmark(int id, String name, String url) {
		synchronized (bookmarkList()) {
			BookmarkInfo info = null;
			for (int i = 0; i < bookmarkList().size(); i++) {
				info = bookmarkList().get(i);
				if (id == info.id) {
					info.name = name;
					info.url = url;
					return;
				}
			}
		}
	}

	/**
	 * 根据id删除收藏
	 *
	 * @param id
	 */
	void deleteBookmarkById(int id) {
		synchronized (bookmarkList()) {
			Iterator<BookmarkInfo> iter = bookmarkList().iterator();
			while (iter.hasNext()) {
				BookmarkInfo info = iter.next();
				if (info.id == id) {
					iter.remove();
					break;
				}
			}
		}
	}

	/**
	 * 根据批量的id删除收藏
	 *
	 */
	public void deleteBookmarkByIdList(List<Integer> idList) {
		Iterator<Integer> iter = idList.iterator();
		while (iter.hasNext()) {
			Integer id = iter.next();
			deleteBookmarkById(id);
		}
	}

	/**
	 * 根据url删除收藏
	 *
	 * @param url
	 */
	void deleteBookmarkByUrl(String url) {
		synchronized (bookmarkList()) {
			Iterator<BookmarkInfo> iter = bookmarkList().iterator();
			while (iter.hasNext()) {
				BookmarkInfo info = iter.next();
				if (info.url.equals(url)) {
					iter.remove();
					break;
				}
			}
		}
	}

	void changeItemPos(int beforePos, int afterPos) {
		synchronized (bookmarkList()) {
			BookmarkInfo info = bookmarkList().get(beforePos);
			bookmarkList().remove(beforePos);
			bookmarkList().add(afterPos, info);
		}
	}

	boolean isUrlExist(String url) {
		synchronized (bookmarkList()) {
			Iterator<BookmarkInfo> iter = bookmarkList().iterator();
			BookmarkInfo info;
			String infoUrl;
			while (iter.hasNext()) {
				info = iter.next();
				infoUrl = info.url;
				if (checkUrlSame(infoUrl, url)) {
					return true;
				}
			}
		}
		return false;
	}

	BookmarkInfo queryBookmarkInfoByUrl(String url) {
		synchronized (bookmarkList()) {
			Iterator<BookmarkInfo> iter = bookmarkList().iterator();
			BookmarkInfo info;
			String infoUrl;
			while (iter.hasNext()) {
				info = iter.next();
				infoUrl = info.url;
				if (checkUrlSame(infoUrl, url)) {
					return info;
				}
			}
		}
		return null;
	}

	/**
	 * 比较网址是否相同
	 *
	 * @param infoUrl
	 * @param url
	 * @return
	 */
	private boolean checkUrlSame(String infoUrl, String url) {
		if (!TextUtils.isEmpty(infoUrl)) {
			if (infoUrl.endsWith("/")) {
				infoUrl = infoUrl.substring(0, infoUrl.length() - 1);
			}
			if (infoUrl.startsWith("http://")) {
				infoUrl = infoUrl.substring(7, infoUrl.length());
			}
			if (infoUrl.startsWith("https://")) {
				infoUrl = infoUrl.substring(8, infoUrl.length());
			}
		}
		if (!TextUtils.isEmpty(url)) {
			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
			if (url.startsWith("http://")) {
				url = url.substring(7, url.length());
			}
			if (url.startsWith("https://")) {
				url = url.substring(8, url.length());
			}
		}
		if (infoUrl.equals(url)) {
			return true;
		}
		return false;
	}

	/**
	 * 创建bookmark的JSON对象
	 */
	private JSONObject createJson() {
//		testData();
		JSONObject json = new JSONObject();
		JSONObject roots = new JSONObject();
		JSONObject bookmark = createBookmarkNode();
		try {
			roots.put(BookmarkNodeDefine.NODE_BOOKMARK, bookmark);
			json.put(BookmarkNodeDefine.NODE_MAXID, mMaxId);
			json.put(BookmarkNodeDefine.NODE_ROOTS, roots);
		} catch (JSONException ignored) {
		}
		SimpleLog.d(TAG, "json格式书签：" + json.toString());
		return json;
	}
	/**
	 * 测试数据
	 */
//	private void testData() {
//		BookmarkInfo info1 = new BookmarkInfo();
//		info1.name = "百度";
//		info1.url = "http://m.baidu.com/";
//		info1.type = BookmarkNodeDefine.NODE_TYPE_URL;
//		
//		mLocalBookmarkList.add(info1);
//		
//		BookmarkInfo info2 = new BookmarkInfo();
//		info2.name = "新浪";
//		info2.url = "http://sina.cn/";
//		info2.type = BookmarkNodeDefine.NODE_TYPE_URL;
//		
//		mLocalBookmarkList.add(info2);
//		
//		BookmarkInfo info3 = new BookmarkInfo();
//		info3.name = "优酷";
//		info3.url = "http://www.youku.com/";
//		info3.type = BookmarkNodeDefine.NODE_TYPE_URL;
//		
//		mLocalBookmarkList.add(info3);
//		for (int i = 0; i < 1000; ++i) {
//			BookmarkInfo info = new BookmarkInfo();
//			info.name = "百度" + String.valueOf(i);
//			info.url = "http://m.baidu.com/" + String.valueOf(i);
//			info.type = BookmarkNodeDefine.NODE_TYPE_URL;
//			mLocalBookmarkList.add(info);
//		}
//	}

	/**
	 * 创建bookmark节点
	 *
	 * @return
	 */
	private JSONObject createBookmarkNode() {
		JSONObject bookmark = new JSONObject();
		try {
			// TODO: 奇怪的地方，顺序不对。经查，JSONObject不能保证顺序
			bookmark.put(BookmarkNodeDefine.NODE_NAME, BookmarkNodeDefine.NODE_NAME_VALUE);
			bookmark.put(BookmarkNodeDefine.NODE_CHILDREN, bookmarkInfoToJson());
			bookmark.put(BookmarkNodeDefine.NODE_ID, BookmarkNodeDefine.BASE_ID);
			bookmark.put(BookmarkNodeDefine.NODE_TYPE, BookmarkNodeDefine.NODE_TYPE_FOLDER);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		}
		return bookmark;
	}

	private List<BookmarkInfo> bookmarkList() {
		return AccountLoginManager.getInstance().isUserLogined() ? mOnLineBookmarkList : mLocalBookmarkList;
	}

	/**
	 * 将内存中的书签数据转化为JSON
	 *
	 * @return
	 */
	private JSONArray bookmarkInfoToJson() {
		JSONArray array = new JSONArray();
		synchronized (bookmarkList()) {
			if (!bookmarkList().isEmpty()) {
				int id = BookmarkNodeDefine.BASE_ID;
				for (BookmarkInfo info : bookmarkList()) {
					JSONObject object = new JSONObject();
					try {
						object.put(BookmarkNodeDefine.NODE_NAME, info.name);
						object.put(BookmarkNodeDefine.NODE_ID, ++id);
						object.put(BookmarkNodeDefine.NODE_TYPE, info.type);
						object.put(BookmarkNodeDefine.NODE_URL, info.url);
						array.put(object);
					} catch (JSONException e) {
						SimpleLog.e(e);
					}
				}
				mMaxId = id;
			}
		}
		return array;
	}

	/**
	 * 将书签的内容部分转化为Json string
	 *
	 * @return
	 */
	String toJsonString() {
		JSONArray array = bookmarkInfoToJson();
		return array.toString();
	}

	/**
	 * 将JSON对象转换成bookmarkInfo
	 *
	 * @param object
	 */
	private boolean loadBookmarkInfoFromJson(JSONObject object, boolean isImport) {
		boolean isSuccess = false;
		if (!isImport) {
			synchronized (bookmarkList()) {
				bookmarkList().clear();
			}
		}
		if (object.has(BookmarkNodeDefine.NODE_ROOTS)) {
			try {
				object = object.getJSONObject(BookmarkNodeDefine.NODE_ROOTS);
				if (object.has(BookmarkNodeDefine.NODE_BOOKMARK)) {
					object = object.getJSONObject(BookmarkNodeDefine.NODE_BOOKMARK);
					if (object.has(BookmarkNodeDefine.NODE_CHILDREN)) {
						isSuccess = loadChildren(object.getJSONArray(BookmarkNodeDefine.NODE_CHILDREN), isImport);
					}
				}
			} catch (JSONException e) {
				SimpleLog.e(e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	boolean importBookmarkFromFile(File file, boolean isImport) {
		boolean isSuccess = false;
		if (file.exists() && file.length() <= CommonData.QUERY_JSON_MAX_SIZE) {
			byte[] jsonBytes = ZipUtil.unGZip(FileUtils.readFile(file));
			if (jsonBytes != null && jsonBytes.length > 0) {
				try {
					JSONObject jsonObject = new JSONObject(new String(jsonBytes));
					isSuccess = loadBookmarkInfoFromJson(jsonObject, isImport);
				} catch (Exception e) {
					isSuccess = false;
				}
			}
		}
		return isSuccess;
	}

	public boolean importBookmarkFromFile(File file) {
		return importBookmarkFromFile(file, true);
	}


	/**
	 * 加载Children节点数据（真实收藏夹数据）
	 *
	 * @param children
	 */
	private boolean loadChildren(JSONArray children, boolean isImport) {
		boolean isSuccess = true;
		if (children != null) {
			for (int i = 0; i < children.length(); ++i) {
				JSONObject item;
				try {
					item = children.getJSONObject(i);
					BookmarkInfo info = new BookmarkInfo();
					info.id = item.getInt(BookmarkNodeDefine.NODE_ID);
					info.name = item.getString(BookmarkNodeDefine.NODE_NAME);
					info.url = item.getString(BookmarkNodeDefine.NODE_URL);
					info.type = item.getString(BookmarkNodeDefine.NODE_TYPE);
					if (isImport) {
						if (!isUrlExist(info.url)) {
							synchronized (bookmarkList()) {
								bookmarkList().add(info);
							}
						}
					} else {
						synchronized (bookmarkList()) {
							bookmarkList().add(info);
						}
					}
				} catch (JSONException e) {
					SimpleLog.e(e);
					isSuccess = false;
				}
			}
		} else {
			isSuccess = false;
		}
		synchronized (bookmarkList()) {
			SimpleLog.d(TAG, "mLocalBookmarkList:" + bookmarkList().toString());
		}
		return isSuccess;
	}
}
