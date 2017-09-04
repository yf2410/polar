package com.polar.browser.impl;

import android.content.Context;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.homepage.sitelist.recommand.exception.HomeSiteExistException;
import com.polar.browser.homepage.sitelist.recommand.exception.OutOfMaxNumberException;
import com.polar.browser.i.IAddFavDelegate;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.shortcut.MatchResult;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;

import java.io.File;
import java.sql.SQLException;

public class AddFavImpl implements IAddFavDelegate {

	private static final String ICON_EXTENSION = ".png";

	@Override
	public void addFav() {
		if (!TabViewManager.getInstance().isCurrentHome()) {
			BookmarkManager.getInstance().addBookmark(
					TabViewManager.getInstance().getCurrentTitle(),
					TabViewManager.getInstance().getCurrentUrl());
		}
	}
//	private File initShortcutIcon(Context c) {
//		File file = new File(c.getFilesDir() + File.separator + DEFAULT_SHORTCUT_ICON);
//		if (!file.exists()) {
//			try {
//				FileUtils.copyAssetsFile(c, DEFAULT_SHORTCUT_ICON, file);
//			} catch (IOException e) {
//				SimpleLog.e(e);
//			}
//		}
//		
//		return file;
//	}

	@Override
	public void addShortcut(final Context c) {
//		File defaultIcon = initShortcutIcon(c);
		String url = TabViewManager.getInstance().getCurrentUrl();
		String title = TabViewManager.getInstance().getCurrentTitle();
		MatchResult result = ParseConfig.getMatchResult(url);
		if (result.id != -1) {
			if (!TextUtils.isEmpty(result.title)) {
//				title = result.title;
			}
			File icon = new File(ParseConfig.sFolderPath + File.separator + String.valueOf(result.id) + ICON_EXTENSION);
			if (icon.exists()) {
				SysUtils.createShotcut(c, url, title, icon.getAbsolutePath());
			} else {
				SysUtils.createShotcut(c, url, title, null);
			}
		} else {
			SysUtils.createShotcut(c, url, title, null);
		}
		if (ConfigWrapper.get(url, false)) {
			CustomToastUtils.getInstance().showTextToast(R.string.already_add_shortcut_successful);
		} else {
			CustomToastUtils.getInstance().showTextToast(R.string.add_shortcut_successful);
		}
//		final String fTitle = title;
//		ThreadManager.postDelayedTaskToLogicHandler(new Runnable() {
//			
//			@Override
//			public void run() {
//				if (SysUtils.isShortcutExist(c, fTitle)) {
//					SimpleLog.d("shortcut", "exist");
//				} else {
//					SimpleLog.d("shortcut", "doesn't exist");
//				}
//			}
//		}, 3000);
		ConfigWrapper.put(url, true);
		ConfigWrapper.apply();
	}

	@Override
	public String getTitle() {
		return TabViewManager.getInstance().getCurrentTitle();
	}

	@Override
	public String getUrl() {
		return TabViewManager.getInstance().getCurrentUrl();
	}

	@Override
	public void addLogo() {
		String url = TabViewManager.getInstance().getCurrentUrl();
		String title = TabViewManager.getInstance().getCurrentTitle();
		try {
			String iconPath = String.format("%s/%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
					CommonData.ICON_DIR_NAME, UrlUtils.getHost(url));
			Site site = new Site(title, url, iconPath);
			if (SiteManager.getInstance().add2Home(site,true)) {
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
			}
		} catch (SQLException e) {
			e.printStackTrace();
            // TODO: 2016/10/14 失败
        } catch (OutOfMaxNumberException e) {
			e.printStackTrace();
			CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_max_tip);
		} catch (HomeSiteExistException e) {
			e.printStackTrace();
			CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
		}
	}

	@Override
	public void addFav(String title, String url) {
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
			title = TabViewManager.getInstance().getCurrentTitle();
			url = TabViewManager.getInstance().getCurrentUrl();
		}
		BookmarkManager.getInstance().addBookmark(title, url);
	}

	@Override
	public void addLogo(String title, String url) {
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
			title = TabViewManager.getInstance().getCurrentTitle();
			url = TabViewManager.getInstance().getCurrentUrl();
		}
        try {
			String iconPath = String.format("%s/%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
					CommonData.ICON_DIR_NAME, UrlUtils.getHost(url));
			if (SiteManager.getInstance().add2Home(new Site(title, url, iconPath),true)) {
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
			}
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (OutOfMaxNumberException e) {
            e.printStackTrace();
            CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_max_tip);
        } catch (HomeSiteExistException e) {
            e.printStackTrace();
            CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
        }

    }

	@Override
	public void addShortcut(Context c, String title, String url) {
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
			title = TabViewManager.getInstance().getCurrentTitle();
			url = TabViewManager.getInstance().getCurrentUrl();
		}
		MatchResult result = ParseConfig.getMatchResult(url);
		if (result.id != -1) {
			if (!TextUtils.isEmpty(result.title)) {
//				title = result.title;
			}
			File icon = new File(ParseConfig.sFolderPath + File.separator + String.valueOf(result.id) + ICON_EXTENSION);
			if (icon.exists()) {
				SysUtils.createShotcut(c, url, title, icon.getAbsolutePath());
			} else {
				SysUtils.createShotcut(c, url, title, null);
			}
		} else {
			SysUtils.createShotcut(c, url, title, null);
		}
	}
}
