package com.polar.browser.utils;

import android.text.TextUtils;

import com.polar.browser.manager.JavaScriptManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UrlUtils {

	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";
	public static final String PROTOCOL_MARK = "://";
	public static final String PROTOCOL_MARK_G = "/";

	public static final String GOOD_IRI_CHAR = "a-zA-Z0-9";
	public static final String TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL =
			"(?:"
					+ "(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])"
					+ "|(?:biz|b[abdefghijmnorstvwyz])"
					+ "|(?:cat|help|date|com|club|cool|desi|coop|c[acdfghiklmnoruvxyz])"
					+ "|d[ejkmoz]"
					+ "|(?:edu|e[cegrstu])"
					+ "|f[ijkmor]"
					+ "|(?:gov|g[abdefghilmnpqrstuwy])"
					+ "|h[kmnrtu]"
					+ "|(?:info|int|i[delmnoqrst])"
					+ "|(?:jobs|j[emop])"
					+ "|k[eghimnprwyz]"
					+ "|l[abcikrstuvy]"
					+ "|(?:mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])"
					+ "|(?:name|net|n[acefgilopruz])"
					+ "|(?:org|om)"
					+ "|(?:pro|p[aefghklmnrstwy])"
					+ "|qa"
					+ "|r[eosuw]"
					+ "|s[abcdeghijklmnortuvyz]"
					+ "|(?:tel|travel|t[cdfghjklmnoprtvwz])"
					+ "|u[agksyz]"
					+ "|(?:video|v[aceginu])"
					+ "|w[fs]"
					+ "|(?:xn\\-\\-0zwm56d|xn\\-\\-11b5bs3a9aj6g|xn\\-\\-80akhbyknj4f|xn\\-\\-9t4b11yi5a|xn\\-\\-deba0ad|xn\\-\\-g6w251d|xn\\-\\-hgbk6aj7f53bba|xn\\-\\-hlcj6aya9esc7a|xn\\-\\-jxalpdlp|xn\\-\\-kgbechtv|xn\\-\\-zckzah)"
					+ "|y[etu]"
					+ "|z[amw]"
					// special domains
					+ "|lol"
					+ "|xyz"
					+ "|onl"
					+ "|guitars"
					+ "|link"
					+ "|pics"
					+ "|club"
					+ "|coffee"
					+ "|florist"
					+ "|house"
					+ "|international"
					+ "|solar"
					+ "|holiday"
					+ "|marketing"
					+ "|ceo"
					+ "|international))";
	public static final Pattern WEB_URL = Pattern
			.compile(
					"((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
							+ "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
							+ "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
							+ "((?:(?:[" + GOOD_IRI_CHAR + "][" + GOOD_IRI_CHAR + "\\-]{0,64}\\.)+" // named
							// host
							+ TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL
							+ "|(?:(?:25[0-5]|2[0-4]" // or ip address
							+ "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
							+ "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
							+ "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
							+ "|[1-9][0-9]|[0-9])))"
							+ "(?:\\:\\d{1,5})?)" // plus option port number
							+ "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~" // plus
							// option
							// query
							// params
							+ "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?");


	public static boolean hasUrlInText(String text) {
		Pattern p = WEB_URL;
		Matcher m = p.matcher(text);
		while (m.find()) {
			if (acceptMatchUrl(m.group().toString(), 0, m.group().length())) {
				return true;
			}
		}
		return false;
	}

	public static String checkUrlIsContainsHttp (String url) {
		if (!url.contains(PROTOCOL_MARK)) {
			url = HTTP_PREFIX + url;
		}
		return url;
	}

	public static String getUrlInText(String text) {
		String url = "";
		if (!TextUtils.isEmpty(text)) {
			Pattern p = WEB_URL;
			Matcher m = p.matcher(text);
			while (m.find()) {
				if (acceptMatchUrl(m.group().toString(), 0, m.group().length())) {
					url = m.group().toString();
					return url;
				}
			}
		}
		return url;
	}

	/**
	 * 指定的text是否为url
	 *
	 * @param text
	 * @return
	 */
	public static boolean isUrl(String text) {
		if (TextUtils.isEmpty(text))
			return false;
		text = text.trim();
		Pattern p = WEB_URL;
		Matcher m = p.matcher(text);
		if (m.find()) {
			if (m.group().equals(text)) {
				return true;
			}
		}
		return false;
	}

	public static void saveSearchRecord(String text) {

	}

	public static boolean acceptMatchUrl(CharSequence s, int start, int end) {
		if (start == 0) {
			return true;
		}
		if (s.charAt(start - 1) == '@') {
			return false;
		}
		return true;
	}

	public static String getHost(String url) {
		String domain = null;
		try {
			URI u = new URI(url);
			System.out.println("__log-- 退出回调 拉出本地集合bookmarkList 元素" + u);
			domain = u.getHost();
			System.out.println("__log-- 退出回调 拉出本地集合bookmarkList 元素" + domain);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		}
		return domain;
	}

	/**
	 * 检测比较两个url是否相同
	 * http，https，/
	 * @param url1 后台下发标准地址
	 * @param url2 历史记录，用户自定义
     * @return
     */
	public static boolean checkUrlIsSame(String url1, String url2) {
		if (TextUtils.isEmpty(url1) || TextUtils.isEmpty(url2)) {
			return false;
		}
		if (TextUtils.equals(url1,url2)) {
			return true;
		}
		if (TextUtils.equals(filterUrl(url1),filterUrl(url2))) {
			return true;
		}
		return false;
	}

	/**
	 * 过滤url中https，/，不带http
	 * 过滤后url=http://xxx.xxx.xxx
	 *
	 * @author FKQ
	 * @time 2016/11/10 15:05
	 */
	private static String filterUrl(String url) {
		if (url.startsWith(HTTPS_PREFIX)) {
			url = url.replace(HTTPS_PREFIX, HTTP_PREFIX);
		}
		if (!url.startsWith(HTTP_PREFIX)) {
			url = HTTP_PREFIX + url;
		}
		if (url.endsWith(PROTOCOL_MARK_G)) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	/**
	 * 检查url的匹配规则,是否是youtube视频播放页面
	 *
	 * @param url
	 * @return
	 */
	public static boolean matchYoutubeVideoUrl(String url) {
		if (TextUtils.isEmpty(url))
			return false;
		String host = UrlUtils.getHost(url);
		if (host != null && host.toUpperCase().contains("YOUTUBE.COM")
				&& url.contains("watch?v=") || url.contains("watch?list=")) {
			return true;
		}
		return false;
	}

	/**
	 * 检查url的匹配规则,是否是instagram页面
	 *
	 * @param url
	 * @return
	 */
	public static boolean matchInstagramUrl(String url) {
//		String host = UrlUtils.getHost(url);
		if (TextUtils.equals(url, JavaScriptManager.INSTAGRAM_HOME)) {
			return true;
		}
		return false;
	}

	/**
	 * 检查url的host是否匹配Facebook
	 * @param rawUrl
	 * @return
	 */
	public static boolean matchFbHost(String rawUrl) {
		String host = UrlUtils.getHost(rawUrl);
		if (TextUtils.equals(host, JavaScriptManager.FACEBOOK_HOST)) {
			return true;
		}
		return false;
	}
}
