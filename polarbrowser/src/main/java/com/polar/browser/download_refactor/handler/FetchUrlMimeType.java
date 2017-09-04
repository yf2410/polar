/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.polar.browser.download_refactor.DownloadManagerCheck;
import com.polar.browser.download_refactor.Request;
import com.polar.browser.utils.SimpleLog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class is used to pull down the http headers of a given URL so that
 * we can analyse the mimetype and make any correction needed before we give
 * the URL to the download manager.
 * This operation is needed when the user long-clicks on a link or image and
 * we don't know the mimetype. If the user just clicks on the link, we will
 * do the same steps of correcting the mimetype down in
 * android.os.webkit.LoadListener rather than handling it here.
 *
 */
class FetchUrlMimeType extends Thread {

    private final static String LOGTAG = "FetchUrlMimeType";

    private Context mContext;
    private Request mRequest;
    private String mUri;
    private String mCookies;
    private String mReferer;
    private String mUserAgent;
    private boolean mNeedConfirm;

    public FetchUrlMimeType(Context context, Request request,
                            String uri, String cookies, String referer, String userAgent, boolean needConfirm) {
        mContext = context.getApplicationContext();
        mRequest = request;
        mUri = uri;
        mCookies = cookies;
        mReferer = referer;
        mUserAgent = userAgent;
        mNeedConfirm = needConfirm;
    }

    @Override
    public void run() {

        // 20160831 放开 IllegalArgumentException ,方便验证问题是否解决

        HttpURLConnection httpUrlConnection = null;
        String mimeType = null;
        String contentDisposition = null;
        long contentLength = -1;
        try {
            URL url = new URL(mUri);
            URLConnection urlConnection = url.openConnection();
            httpUrlConnection = (HttpURLConnection) urlConnection;
            if (mCookies != null && mCookies.length() > 0) {
                httpUrlConnection.addRequestProperty("Cookie", mCookies);
            }
            httpUrlConnection.connect();

            int code = httpUrlConnection.getResponseCode();
            SimpleLog.e(LOGTAG, "FetchUrlMimeType: code = " + code);
            if (code == 200) {
                mimeType = httpUrlConnection.getHeaderField("Content-Type");
                if (!TextUtils.isEmpty(mimeType)) {
                    final int semicolonIndex = mimeType.indexOf(';');
                    if (semicolonIndex != -1) {
                        mimeType = mimeType.substring(0, semicolonIndex);
                    }
                }
                contentDisposition = httpUrlConnection.getHeaderField("Content-Disposition");

                String contentLengthStr = httpUrlConnection.getHeaderField("Content-Length");
                if (!TextUtils.isEmpty(contentLengthStr)) {
                    contentLength = Long.parseLong(contentLengthStr);
                }
            }

            SimpleLog.e(LOGTAG, "FetchUrlMimeType: mimeType = " + mimeType);
            SimpleLog.e(LOGTAG, "FetchUrlMimeType: contentDisposition = " + contentDisposition);
            SimpleLog.e(LOGTAG, "FetchUrlMimeType: contentLength = " + contentLength);
        } catch (IOException ex) {
            Log.e(LOGTAG, "Download failed: " + ex);
            return;
        } finally {
            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
        }

       if (mimeType != null) {
           if (mimeType.equalsIgnoreCase("text/plain") ||
                   mimeType.equalsIgnoreCase("application/octet-stream")) {
               String newMimeType =
                       MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                           MimeTypeMap.getFileExtensionFromUrl(mUri));
               if (newMimeType != null) {
                   mimeType = newMimeType;
                   mRequest.setMimeType(newMimeType);
               }
           }
       }

       // Start the download
//        DownloadManager manager = DownloadManager.getInstance();
//        manager.enqueue(mRequest);
        new DownloadManagerCheck(mContext, mRequest, mUserAgent, mUri, mimeType, contentDisposition, contentLength, mReferer, mNeedConfirm).check();
    }

}
