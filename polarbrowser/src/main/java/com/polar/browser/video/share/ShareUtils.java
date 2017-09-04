package com.polar.browser.video.share;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.utils.CustomToastUtils;

import java.util.List;

/**
 * 分享工具类
 *
 * @author dpk
 */
public class ShareUtils {

    private static final String TAG = ShareUtils.class.getSimpleName();

    public static void share(Context c, String packageName,
                             ShareContent shareContent) {
        if(shareContent == null){
            CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
            return;
        }
        List<PackageInfo> packages = JuziApp.getInstance().getPackageManager()
                .getInstalledPackages(0);
        String pName = null;
        for (int i = 0; i < packages.size(); i++) {
            pName = packages.get(i).packageName;
            if (TextUtils.equals(pName, packageName)) {
                try{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    if((ShareContent.TYPE_SHARE_PICTURE == shareContent.getType() || ShareContent.TYPE_SHARE_MIX == shareContent.getType() )
                            && shareContent.getImgUri() != null){
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, shareContent.getImgUri());
                        if(ShareContent.TYPE_SHARE_MIX == shareContent.getType()){
                            intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());
                        }
                    }else{
                        intent.setType("text/plain");  //NOTE: 不要用text/* 否则提示 Please attach only photos or a single video.
                        intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());// WhatsApp 和 Twitter 默认显示这个文本
                    }

                    if(shareContent.getTitle() != null){
                        intent.putExtra(Intent.EXTRA_TITLE, shareContent.getTitle());
                        intent.putExtra(Intent.EXTRA_SUBJECT, shareContent.getTitle());
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage(packageName);
                    Intent openInChooser = new Intent(intent);
                    c.startActivity(openInChooser);
                }catch (Exception e){
                    e.printStackTrace();
                    CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
                }
                break;
            }
            if (i == packages.size() - 1) {
                CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
            }
        }
    }


    public static void share2Facebook(Context context, ShareContent shareContent) {
        share(context, "com.facebook.katana", shareContent);
    }

    public static void share2Twitter(Context context, ShareContent shareContent) {
        share(context, "com.twitter.android", shareContent);
    }

    public static void share2Whatsapp(Context context, ShareContent shareContent) {
        share(context, "com.whatsapp", shareContent);
    }

    public static void share2Msg(Context context, ShareContent shareContent) {

        try{
            if(shareContent != null){
                Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("smsto:"));
                if((ShareContent.TYPE_SHARE_PICTURE == shareContent.getType() || ShareContent.TYPE_SHARE_MIX == shareContent.getType())
                        && shareContent.getImgUri() != null){
                    // 图片分享
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, shareContent.getImgUri());
                    if(ShareContent.TYPE_SHARE_MIX == shareContent.getType()){
                        intent.putExtra("sms_body", shareContent.getContent());
                    }
                }else{
                    intent.setType("text/plain");
//                    intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());
                    intent.putExtra("sms_body", shareContent.getContent());
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // activityInfo.name = com.android.mms.ui.ComposeMessageMms
                // activityInfo.packageName = com.android.mms
                String packageName = null;
                String activityName = null;
                List<ResolveInfo> list = JuziApp.getInstance().getPackageManager()
                        .queryIntentActivities(intent, 0);
                for (int i = 0; i < list.size(); i++) {
                    packageName = list.get(i).activityInfo.packageName;
                    if (TextUtils.equals(packageName, "com.android.mms")) {
                        activityName = list.get(i).activityInfo.name;
                        break;
                    }
                }
                if (packageName != null && activityName != null) {
                    intent.setClassName(packageName, activityName);
                }
                context.startActivity(intent);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
    }


    public static void share2Mail(Context context, ShareContent shareContent) {
        try {
            if (shareContent != null) {
                Uri uri = Uri.parse("mailto:");
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra(Intent.EXTRA_EMAIL, true);
                if(shareContent.getTitle() != null){
                    intent.putExtra(Intent.EXTRA_SUBJECT, shareContent.getTitle());
                }

                if((ShareContent.TYPE_SHARE_PICTURE == shareContent.getType() || ShareContent.TYPE_SHARE_MIX == shareContent.getType())
                        && shareContent.getImgUri() != null){
                    intent.putExtra(Intent.EXTRA_STREAM, shareContent.getImgUri());
                    if(ShareContent.TYPE_SHARE_MIX == shareContent.getType()){
                        intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());
                    }
                }else{
                    intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
    }


    public static void cpLink(Context context, ShareContent shareContent) {
        try{
            if (shareContent != null && shareContent.getShareUrl() != null) {
                // 得到剪贴板管理器
                ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(shareContent.getShareUrl().trim());
                CustomToastUtils.getInstance().showTextToast(R.string.copy_link);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
    }

    public static void onSystemShare(Context context,ShareContent shareContent) {
        try {
            if(shareContent != null){
                Intent intent = new Intent(Intent.ACTION_SEND);
                if((ShareContent.TYPE_SHARE_PICTURE == shareContent.getType() || ShareContent.TYPE_SHARE_MIX == shareContent.getType())
                        && shareContent.getImgUri() != null){
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, shareContent.getImgUri());
                    if(ShareContent.TYPE_SHARE_MIX == shareContent.getType()){
                        intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());
                    }
                }else{
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, shareContent.getContent());
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
    }


}
