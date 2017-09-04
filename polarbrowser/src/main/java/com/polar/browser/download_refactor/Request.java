package com.polar.browser.download_refactor;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Request {
    /**
     * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
     * {@link android.net.ConnectivityManager#TYPE_MOBILE}.
     */
    public static final int NETWORK_MOBILE = 1 << 0;

    /**
     * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
     * {@link android.net.ConnectivityManager#TYPE_WIFI}.
     */
    public static final int NETWORK_WIFI = 1 << 1;

    /**
     * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
     * {@link android.net.ConnectivityManager#TYPE_BLUETOOTH}.
     * @hide
     */
    public static final int NETWORK_BLUETOOTH = 1 << 2;

    public static final int NETWORK_ALL = ~0;
    public static final int NETWORK_NO_MOBILE = NETWORK_WIFI | NETWORK_BLUETOOTH;

    private Uri mUri;
    private String mDestFilePath;
    private String mReferer;
    private List<Pair<String, String>> mRequestHeaders = new ArrayList<Pair<String, String>>();
    private String mMimeType;
    private int mAllowedNetworkTypes = Request.NETWORK_NO_MOBILE; // default no network types allowed
    private boolean mRoamingAllowed = true;
    private boolean mMeteredAllowed = true;
    private boolean mIsVisibleInDownloadsUi = true;
    private boolean mAutoStart = true; // 默认自动开始下载任务
    private boolean mScannable = false;
    /** if a file is designated as a MediaScanner scannable file, the following value is
     * stored in the database column {@link com.polar.download.Downloads.Impl#COLUMN_MEDIA_SCANNED}.
     */
    private static final int SCANNABLE_VALUE_YES = 0;
    // value of 1 is stored in the above column by DownloadProvider after it is scanned by
    // MediaScanner
    /** if a file is designated as a file that should not be scanned by MediaScanner,
     * the following value is stored in the database column
     * {@link com.polar.download.Downloads.Impl#COLUMN_MEDIA_SCANNED}.
     */
    private static final int SCANNABLE_VALUE_NO = 2;

    /**
     * This download is visible but only shows in the notifications
     * while it's in progress.
     */
    public static final int VISIBILITY_VISIBLE = 0;

    /**
     * This download is visible and shows in the notifications while
     * in progress and after completion.
     */
    public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;

    /**
     * This download doesn't show in the UI or in the notifications.
     */
    public static final int VISIBILITY_HIDDEN = 2;

    /** can take any of the following values: {@link #VISIBILITY_HIDDEN}
     * {@link #VISIBILITY_VISIBLE_NOTIFY_COMPLETED},
     * {@link #VISIBILITY_VISIBLE},
     */
    private int mNotificationVisibility = VISIBILITY_VISIBLE;

    /**
     * @param uri the HTTP URI to download.
     */
    public Request(Uri uri) {
        if (uri == null) {
            throw new NullPointerException();
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
        }
        mUri = uri;
    }

    Request(String uriString) {
        setUriString(uriString);
    }

    // 供Flash插件下载等特殊需求更新uri之用，请不要随意使用此方法。
    public Request setUriString(String uriString) {
        mUri = Uri.parse(uriString);
        return this;
    }

    /**
     * Set the local destination for the downloaded file. Must be a file URI to a path on
     * external storage, and the calling application must have the WRITE_EXTERNAL_STORAGE
     * permission.
     * <p>
     * The downloaded file is not scanned by MediaScanner.
     * But it can be made scannable by calling {@link #allowScanningByMediaScanner()}.
     * <p>
     * By default, downloads are saved to a generated filename in the shared download cache and
     * may be deleted by the system at any time to reclaim space.
     *
     * @return this object
     */
    public Request setDestinationFilePath(String filePath) {
        mDestFilePath = filePath;
        return this;
    }

    /**
     * 设置下载文件的本地目标存放目录。其他信息同{@link #setDestinationFilePath(String)}。
     *
     * @param destinationDir 目标目录
     * @param filename 目标文件名
     * @return this object
     * @throws IllegalStateException If destinationDir cannot be found or created.
     */
    public Request setDestinationInDir(String destinationDir, String filename) {
        final File file = new File(destinationDir);
        if (null == file) {
            throw new IllegalStateException("Failed to open " + destinationDir + "as File");
        } else if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() +
                        " already exists and is not a directory");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: "+
                        file.getAbsolutePath());
            }
        }
        setDestinationFromBase(file, filename);
        return this;
    }

    private void setDestinationFromBase(File base, String subPath) {
        if (subPath == null) {
            throw new NullPointerException("subPath cannot be null");
        }
        mDestFilePath = Uri.withAppendedPath(Uri.fromFile(base), subPath).getPath();
    }

    /**
     * If the file to be downloaded is to be scanned by MediaScanner, this method
     * should be called before {@link com.polar.download.DownloadManager#enqueue(Request)} is called.
     */
    public void allowScanningByMediaScanner() {
        mScannable = true;
    }
    
    public void setRequestReferer(String referer) {
        mReferer = referer;
    }

    /**
     * Add an HTTP header to be included with the download request.  The header will be added to
     * the end of the list.
     * @param header HTTP header name
     * @param value header value
     * @return this object
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">HTTP/1.1
     *      Message Headers</a>
     */
    public Request addRequestHeader(String header, String value) {
        if (header == null) {
            throw new NullPointerException("header cannot be null");
        }
        if (header.contains(":")) {
            throw new IllegalArgumentException("header may not contain ':'");
        }
        if (value == null) {
            value = "";
        }
        mRequestHeaders.add(Pair.create(header, value));
        return this;
    }

    /**
     * Set the MIME content type of this download.  This will override the content type declared
     * in the server's response.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">HTTP/1.1
     *      Media Types</a>
     * @return this object
     */
    public Request setMimeType(String mimeType) {
        mMimeType = mimeType;
        return this;
    }

    /**
     * Control whether a system notification is posted by the download manager while this
     * download is running or when it is completed.
     * If enabled, the download manager posts notifications about downloads
     * through the system {@link android.app.NotificationManager}.
     * By default, a notification is shown only when the download is in progress.
     *<p>
     * It can take the following values: {@link #VISIBILITY_HIDDEN},
     * {@link #VISIBILITY_VISIBLE},
     * {@link #VISIBILITY_VISIBLE_NOTIFY_COMPLETED}.
     *<p>
     * If set to {@link #VISIBILITY_HIDDEN}, this requires the permission
     * android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
     *
     * @param visibility the visibility setting value
     * @return this object
     */
    public Request setNotificationVisibility(int visibility) {
        mNotificationVisibility = visibility;
        return this;
    }

    /**
     * Restrict the types of networks over which this download may proceed.
     * By default, all network types are allowed. Consider using
     * {@link #setAllowedOverMetered(boolean)} instead, since it's more
     * flexible.
     *
     * @param flags any combination of the NETWORK_* bit flags.
     * @return this object
     */
    public Request setAllowedNetworkTypes(int flags) {
        mAllowedNetworkTypes = flags;
        return this;
    }

    /**
     * Set whether this download may proceed over a roaming connection.  By default, roaming is
     * allowed.
     * @param allowed whether to allow a roaming connection to be used
     * @return this object
     */
    public Request setAllowedOverRoaming(boolean allowed) {
        mRoamingAllowed = allowed;
        return this;
    }

    /**
     * Set whether this download may proceed over a metered network
     * connection. By default, metered networks are allowed.
     *
     * @see android.net.ConnectivityManager#isActiveNetworkMetered()
     */
    public Request setAllowedOverMetered(boolean allow) {
        mMeteredAllowed = allow;
        return this;
    }

    /**
     * Set whether this download should be displayed in the system's Downloads UI. True by
     * default.
     * @param isVisible whether to display this download in the Downloads UI
     * @return this object
     */
    public Request setVisibleInDownloadsUi(boolean isVisible) {
        mIsVisibleInDownloadsUi = isVisible;
        return this;
    }

    public Request setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
        return this;
    }

    /**
     * @return ContentValues to be passed to DownloadProvider.insert()
     */
    ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        assert mUri != null;
        values.put(Downloads.Impl.COLUMN_URI, mUri.toString());

        if (mDestFilePath != null) {
            values.put(Downloads.Impl.COLUMN_FILE_PATH, mDestFilePath);
        } else {
            throw new IllegalStateException("Why mDestinationUri == null ?");
        }
        // is the file supposed to be media-scannable?
        values.put(Downloads.Impl.COLUMN_MEDIA_SCANNED, (mScannable) ? SCANNABLE_VALUE_YES :
                SCANNABLE_VALUE_NO);

        if (!mRequestHeaders.isEmpty()) {
            encodeHttpHeaders(values);
        }

        putIfNonNull(values, Downloads.Impl.COLUMN_REFERER, mReferer);
        putIfNonNull(values, Downloads.Impl.COLUMN_MIME_TYPE, mMimeType);

        values.put(Downloads.Impl.COLUMN_VISIBILITY, mNotificationVisibility);
        values.put(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, mAllowedNetworkTypes);
        values.put(Downloads.Impl.COLUMN_ALLOW_ROAMING, mRoamingAllowed);
        values.put(Downloads.Impl.COLUMN_ALLOW_METERED, mMeteredAllowed);
        values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, mIsVisibleInDownloadsUi);

        if (!mAutoStart) {
            values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_PAUSED);
            values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PAUSED_BY_APP);
        }

        return values;
    }

    private void encodeHttpHeaders(ContentValues values) {
        int index = 0;
        for (Pair<String, String> header : mRequestHeaders) {
            String headerString = header.first + ": " + header.second;
            values.put(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX + index, headerString);
            index++;
        }
    }

    private void putIfNonNull(ContentValues contentValues, String key, Object value) {
        if (value != null) {
            contentValues.put(key, value.toString());
        }
    }
}
