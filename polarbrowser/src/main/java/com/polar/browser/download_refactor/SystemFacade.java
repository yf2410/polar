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

package com.polar.browser.download_refactor;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SystemFacade {
    private Context mContext;

    public SystemFacade(Context context) {
        mContext = context;
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivity =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
            return null;
        }

        final NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
        if (activeInfo == null) {
            Log.v(Constants.TAG, "network is not available");
        }
        return activeInfo;
    }

    public boolean isActiveNetworkMetered() {
//        final ConnectivityManager conn = ConnectivityManager.from(mContext);
    	ConnectivityManager connectivity =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                Log.w(Constants.TAG, "couldn't get connectivity manager");
                return false;
            }
        return connectivity.isActiveNetworkMetered();
    }

    public boolean isNetworkRoaming() {
        ConnectivityManager connectivity =
            (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
            return false;
        }

        NetworkInfo info = connectivity.getActiveNetworkInfo();
        boolean isMobile = (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
        
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        boolean isRoaming = isMobile && telephonyManager.isNetworkRoaming();
        if (isRoaming) {
            Log.v(Constants.TAG, "network is roaming");
        }
        return isRoaming;
    }

    public Long getMaxBytesOverMobile() {
        ///> TODO:fixme
        //return DownloadManager.getMaxBytesOverMobile(mContext);
        return 0L;
    }

    public Long getRecommendedMaxBytesOverMobile() {
        ///> TODO:fixme
        //return DownloadManager.getRecommendedMaxBytesOverMobile(mContext);
        return 0L;
    }

    public void sendBroadcast(Intent intent) {
        mContext.sendBroadcast(intent);
    }
}
