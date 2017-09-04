package com.polar.browser.loginassistant;

import android.content.Context;

import com.polar.browser.bean.LoginAccountInfo;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;

import java.util.List;

public class LoginAssistantManager {
	private static String TAG = "LoginAssistantManager";
	private static LoginAssistantManager sInstance;
	private LoginDBHelper mDbHelper;

	public static LoginAssistantManager getInstance() {
		if (sInstance != null) {
			return sInstance;
		}
		synchronized (LoginDBHelper.class) {
			if (sInstance == null) {
				sInstance = new LoginAssistantManager();
			}
		}
		return sInstance;
	}

	public void init(Context c) {
		mDbHelper = LoginDBHelper.getInstance();
		mDbHelper.init(c);
		SimpleLog.d(TAG, "init");
	}

	/**
	 * 页面调用，提示保存账号密码
	 *
	 * @param url
	 * @param username
	 * @param password
	 */
	public void saveUsernamePassword(final String url, final String username, final String password) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				SimpleLog.d(TAG, "addAccount");
				if (mDbHelper != null) {
					mDbHelper.addAccount(url, username, password);
				}
			}
		};
		ThreadManager.postTaskToIOHandler(r);
	}

	/**
	 * 获取填写账号密码js
	 *
	 * @param url
	 * @return
	 */
	public String getFillFormJs(final String url) {
		LoginAccountInfo info = getUserNamePasswordByUrl(url);
		if (info != null &&
				info.getUsername() != null &&
				info.getPassword() != null &&
				!info.getUsername().isEmpty() &&
				!info.getPassword().isEmpty()) {
			//String js = new String("window.vcInstance.fillForm('"+url+"','"+info.username+"','"+info.password+"');");
//			String js = new String("window.vcInstance.fillForm('"+url+"','"+info.username+"','"+info.password+"');");
			// 优化 String换成StringBuilder
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("window.vcInstance.fillForm('");
			stringBuilder.append(url);
			stringBuilder.append("','");
			stringBuilder.append(replaceSingleQuote(info.getUsername()));
			stringBuilder.append("','");
			stringBuilder.append(replaceSingleQuote(info.getPassword()));
			stringBuilder.append("');");
			String js = stringBuilder.toString();
			return js;
		}
		return null;
	}

	/**
	 * single quotes handler for js
	 * <p>
	 * replaceAll方法中采用正则表达式来处理，所以'要被转成\'就要用\\\\来替换\,用\\'替换'
	 *
	 * @param s
	 * @return
	 */
	private String replaceSingleQuote(String s) {
		if (s != null) {

			return s.replaceAll("'", "\\\\\\'");
		} else {
			return null;
		}
	}

	/**
	 * 通过url获取账号密码
	 *
	 * @param url
	 * @return
	 */
	public LoginAccountInfo getUserNamePasswordByUrl(String url) {
		List<LoginAccountInfo> loginAccountInfos = mDbHelper.queryAccountByUrl(url);
		if (loginAccountInfos != null && loginAccountInfos.size() > 0) {
			return loginAccountInfos.get(0);
		}
		return null;
	}

	public void updateUsernamePassword(final String host, final String username, final String password) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				mDbHelper.updateAccountByUrl(host, username, password);
			}
		};
		ThreadManager.postTaskToIOHandler(runnable);
	}
}
