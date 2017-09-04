/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.polar.browser.download_refactor.handler;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.polar.browser.R;
import com.polar.browser.download_refactor.Base64ImageDownloader;
import com.polar.browser.download_refactor.DownloadManagerCheck;
import com.polar.browser.download_refactor.FlashPluginInstaller;
import com.polar.browser.download_refactor.Request;
import com.polar.browser.download_refactor.util.KFile;
import com.polar.browser.utils.CustomToastUtils;

//import android.net.WebAddress;

/**
 * Handle download requests
 */
public class DownloadHandler {

    private static final String TAG = "DownloadHandler";
//    private final MainController mMainController;
//    public DownloadHandler(MainController mainController) {
//        mMainController = mainController;
//    }

    private static DownloadHandler mInstance;

    private DownloadHandler() {
    }

    public static DownloadHandler getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadHandler();
        }
        return mInstance;
    }

    /**
     * Notify the host application a download should be done, or that
     * the data should be streamed if a streaming viewer is available.
     * @param context Activity requesting the download.
     * @param url The full url to the content that should be downloaded
     * @param userAgent User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype The mimetype of the content reported by the server
     * @param referer The referer associated with the downloaded url
     * @param privateBrowsing If the request is coming from a private browsing tab.
     */
    public void onDownloadStart(Context context, String url,
            String userAgent, String contentDisposition, String mimetype, long contentLength,
            String referer, String cookies, boolean privateBrowsing) {
        onDownloadStart(context,url,userAgent,contentDisposition,mimetype,contentLength,referer,cookies,privateBrowsing,true);
    }

    public void onDownloadStart(Context context, String url,
                                String userAgent, String contentDisposition, String mimetype, long contentLength,
                                String referer, String cookies, boolean privateBrowsing,boolean isNeedConfirm) {
        if (TextUtils.isEmpty(url))
            return;

        // 异常处理。小概率activity为null导致崩溃
        if (context == null)
            return;

        // 不是来自页面点击
//        if (null == mMainController.getTabControl().getCurrentTab())
//            return;

        // 非http或者https的schema也不处理
        if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url))
            return;

        // flash插件安装支持
//        if (handleFlashPlugin(activity, url, userAgent, contentDisposition, mimetype, contentLength, referer,
//                privateBrowsing))
//            return;

        // Play or Download. 视频和音频
//        if (handleAudioAndVideo(activity, url, userAgent, contentDisposition, mimetype, contentLength, referer,
//                privateBrowsing))
//            return;

        // 处理点击下载链接触发的空白页
//        processBlankPage();

        onDownloadStartNoStream(context, url, userAgent, contentDisposition,
                mimetype, contentLength, referer, cookies, privateBrowsing, isNeedConfirm);
    }
    
    /*
    private void processBlankPage() {
        // for blank page, after started download, need close blank page
        int currentIndex = mMainController.getTabControl().getCurrentIndex();
        // for after click download link then go to multi-windows case, don't
        // delete the blank page now,
        // need implementation it later
        if (mMainController.isShowWinList())
            return;

        try {
            if (!mMainController.getTabControl().getCurrentWebView().isInitialNavigation())
                return;

            if (mMainController.getTabControl().getTabCount() == 1) {
                mMainController.getTabControl().removeTab(mMainController.getTabControl().getCurrentTab());
                mMainController.showNewWebWindow(true);
                KTab current = mMainController.getTabControl().getCurrentTab();
                if (current != null) {
                    current.setFromThirdApp(true);
                    mMainController.resetButtonsStatus();
                }

                return;
            }

            boolean fromThird = mMainController.getTabControl().getCurrentTab().isFromThirdApp();
            KTab tab;
            tab = mMainController.getTabControl().getCurrentTab().getParentTab();
            if (tab == null)
                tab = mMainController.getTabControl().getTab(currentIndex - 1);

            mMainController.getTabControl().removeTab(mMainController.getTabControl().getCurrentTab());
            if (tab != null) {
                mMainController.getTabControl().setCurrentTab(tab);
                mMainController.attachTabToContentView(tab);
                if (mMainController.isStartPages(tab))
                    mMainController.showNewWebWindow(false);
            }
            if (fromThird) {
                // showNewWebWindow(true);
                mMainController.getTabControl().getCurrentTab().setFromThirdApp(true);
                mMainController.resetButtonsStatus();
            }
        } finally {
            KTab nextTab = mMainController.getTabControl().getTab(currentIndex + 1);
            if (nextTab != null && nextTab.getWebView().isInitialNavigation())
            {
                mMainController.getTabControl().removeTab(nextTab);
            }
        }
    }*/

    public void onDownloadImage(Context context, String url, String userAgent, String referer,  String mimetype, String cookies, boolean privateBrowsing,boolean isNeedConfirm) {
        if (Base64ImageDownloader.isBase64Image(url)) {
            Base64ImageDownloader.checkAndSaveImage(context, url, userAgent, referer);
        } else {
            onDownloadStart(context, url, userAgent, null, mimetype, 0, referer, cookies, privateBrowsing,isNeedConfirm);
        }
    }

    /**
     * Notify the host application a download should be done, even if there
     * is a streaming viewer available for this type.
     * @param context Activity requesting the download.
     * @param url The full url to the content that should be downloaded
     * @param userAgent User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype The mimetype of the content reported by the server
     * @param referer The referer associated with the downloaded url
     * @param privateBrowsing If the request is coming from a private browsing tab.
     */
    public void onDownloadStartNoStream(Context context,
                                        String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength, String referer, String cookies,
                                        boolean privateBrowsing, boolean needConfirm) {

        // TODO Fix me ! WebAddress can not find ~
        // java.net.URI is a lot stricter than KURL so we have to encode some
        // extra characters. Fix for b 2538060 and b 1634719
//        WebAddress webAddress;
//        try {
//            webAddress = new WebAddress(url);
//            webAddress.setPath(encodePath(webAddress.getPath()));
//        } catch (Exception e) {
//            // This only happens for very bad urls, we want to chatch the
//            // exception here
//            Log.e(TAG, "Exception trying to parse url:" + url);
//            return;
//        }
//
//        if (webAddress.getScheme().equalsIgnoreCase("content")) {
//            // 我们并未支持本地资源映射，content scheme也不应执行下载，因此直接内部返回
//            return;
//        }
//
//        // DownloadManager.Request
//        String addressString = webAddress.toString();
        String addressString = url;
        Uri uri = Uri.parse(addressString);
        final Request request;
        try {
            request = new Request(uri);
        } catch (IllegalArgumentException e) {
//            Toast.makeText(context, "Can only download \"http\" or \"https\" URLs.",
//                    Toast.LENGTH_SHORT).show();
            CustomToastUtils.getInstance().showTextToast(R.string.download_error);
            return;
        }

        // let this downloaded file be scanned by MediaScanner - so that it can
        // show up in Gallery app, for example.
        request.allowScanningByMediaScanner();
        // XXX: Have to use the old url since the cookies were stored using the
        // old percent-encoded url.
//        String cookies = CookieManager.getInstance().getCookie(url, false);
//        String cookies = CookieManager.getInstance().getCookie(url);
        // TODO 0725 注释了取 cookies 方法
//        String cookies = KBrowserEngine.getInstance().getCoreEnv().getCookieManager().getCookie(url);
        request.addRequestHeader("cookie", cookies);
        request.addRequestHeader("User-Agent", userAgent);
        request.addRequestHeader("Referer", referer);
        request.setRequestReferer(referer);
        request.setNotificationVisibility(
                Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (mimetype == null) {
            if (TextUtils.isEmpty(addressString)) {
                return;
            }
            // We must have long pressed on a link or image to download it. We
            // are not sure of the mimetype in this case, so do a head request
            new FetchUrlMimeType(context, request, addressString, cookies, referer,
                    userAgent, needConfirm).start();
        } else {
            // 延迟到Confirm之后生成目标文件路径
//            String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
//            String destinationDir = PathResolver.getDownloadFileDir("");
//            try {
//                request.setDestinationInDir(destinationDir, filename);
//            } catch (IllegalStateException ex) {
//                // This only happens when directory Downloads can't be created or it isn't a directory
//                // this is most commonly due to temporary problems with sdcard so show appropriate string
//                Log.w(TAG, "Exception trying to create Download dir:", ex);
//                Toast.makeText(context, "USB storage unavailable", Toast.LENGTH_SHORT).show();
//                return;
//            }

            request.setMimeType(mimetype);

            new DownloadManagerCheck(context, request, userAgent, addressString, mimetype, contentDisposition, contentLength, referer, needConfirm).check();
//            DownloadManager.getInstance().enqueue(request);
//            Toast.makeText(activity, "Starting download…", Toast.LENGTH_SHORT)
//                    .show();
        }
    }

    /**
     * 针对视频、音频，询问用户是现在播放，还是进行下载
     *
     * @param activity Activity requesting the download.
     * @param url The full url to the content that should be downloaded
     * @param userAgent User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype The mimetype of the content reported by the server
     * @param referer The referer associated with the downloaded url
     * @param privateBrowsing If the request is coming from a private browsing tab.
     * @return true则这个函数会接管剩下的流程，false则由调用者继续流程
     */
    private boolean handleAudioAndVideo(Activity activity, String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength, String referer,
                                       boolean privateBrowsing) {
//       TODO:fixme
        String typeString = KFile.getMajorForMimetype(mimetype);
        if (typeString == null) {
            return false;
        }
        if (typeString.equalsIgnoreCase(KFile.AUDIO)
                || typeString.equalsIgnoreCase(KFile.VIDEO)) {
        	// TODO Fix me 
//            new DownloadSmartDialogs.PlayNowOrDownload(this, activity, url, userAgent,
//                    contentDisposition, mimetype, contentLength, referer, privateBrowsing).showDialog();
            return true;
        }

        return false;
    }

    /**
     * flash插件下载且自动安装的特殊处理
     *
     * @param activity
     * @param url
     * @param userAgent
     * @param contentDisposition
     * @param mimetype
     * @param referer
     * @param privateBrowsing
     * @return
     */
    private boolean handleFlashPlugin(final Activity activity, final String url, final String userAgent,
                                      final String contentDisposition, final String mimetype, final long contentLength, final String referer,
                                      boolean privateBrowsing) {
        
        ///*TODO:fixme
        if (!FlashPluginInstaller.isFlashPluginInstallUrl(url))
            return false;
        confirmDownloadFlash(activity, this, url, userAgent, contentDisposition, mimetype, contentLength, referer, privateBrowsing);
        return true;
    }
    /**
     * 询问用户是否确认要下载：
     * 确认，则添加下载；否则取消下载
     */
    public void confirmDownloadFlash(final Context context, DownloadHandler downloadHandler,
            final String url, final String userAgent, final String contentDisposition,
            final String mimetype, final long contentLength,  final String referer, final boolean privateBrowsing) {
//        Context mContext = BrowserActivity.getInstance();
//        SmartDialog smartDialog = new SmartDialog(mContext);
//        String[] buttonText = new String[] {
//                (mContext.getString(R.string.btn_download_confirm)),
//                (mContext.getString(R.string.cancel))
//        };
//
//        smartDialog.setView(SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG,
//                mContext.getString(R.string.flash_plugin_installer_prompt), null, buttonText);
//        smartDialog.setKSmartDialogListener(new SmartDialog.KSmartDialogListener() {
//            @Override
//            public void onDialogClosed(int whichButton, boolean[] checkState) {
//                if (whichButton == SmartDialog.BUTTON_POSITIVE) {
//                    String cleanedUrl = url.substring(0, url.length() - FlashPluginInstaller.key.length());
//                    onDownloadStartNoStream(context, cleanedUrl, userAgent, contentDisposition, mimetype, contentLength, referer, privateBrowsing, false);
//                    }
//            }
//        });
//        smartDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface arg0) {
//                return;
//            }
//        });
//        smartDialog.dialogEnterAnimation();
    }
    // This is to work around the fact that java.net.URI throws Exceptions
    // instead of just encoding URL's properly
    // Helper method for onDownloadStartNoStream
    private static String encodePath(String path) {
        char[] chars = path.toCharArray();

        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return path;
        }

        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
