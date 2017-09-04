package com.polar.browser.bookmark;

/**
 * IBookmarkObserver接口，用于通知Bookmark的改变
 * <p/>
 * 谁需要知道Bookmark的改变，就实现一个该接口，并调用BookmarkManager.registerObserver进行注册
 * 当Bookmark改变时，会回调notifyChanged()方法，实现该函数即可
 *
 * @author dpk
 */
public interface IBookmarkObserver {

	public void notifyBookmarkChanged(boolean isAdd,boolean showTip);
}
