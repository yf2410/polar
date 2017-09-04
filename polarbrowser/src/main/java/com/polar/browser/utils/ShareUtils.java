package com.polar.browser.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;

import java.io.File;
import java.util.List;

/**
 * 分享工具类
 *
 * @author dpk
 */
public class ShareUtils {
	public static void share(Context c, String packageName, String content,
							 String filePath) {
		List<PackageInfo> packages = JuziApp.getInstance().getPackageManager()
				.getInstalledPackages(0);
		String pName = null;
		for (int i = 0; i < packages.size(); i++) {
			pName = packages.get(i).packageName;
			if (TextUtils.equals(pName, packageName)) {
				if (TextUtils.isEmpty(filePath)) {
					CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
					return;
				}
				File file = new File(filePath);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_TEXT, content);// WhatsApp 和
				// Twitter 默认显示这个文本
				intent.putExtra(Intent.EXTRA_TITLE, "example_title");
				intent.putExtra(Intent.EXTRA_SUBJECT, "example_subject");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setPackage(packageName);
				Intent openInChooser = new Intent(intent);
				c.startActivity(openInChooser);
				break;
			}
			if (i == packages.size() - 1) {
				CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
			}
		}
	}
}
