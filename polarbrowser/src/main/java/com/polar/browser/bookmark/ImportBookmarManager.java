package com.polar.browser.bookmark;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;

import com.polar.browser.R;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.CustomToastUtils;

public class ImportBookmarManager {

    public static final int IMPORT_FROM_CHROME = 0;
    public static final int IMPORT_FROM_SYSTEM = 1;
    public static final int IMPORT_FROM_FILE = 2;
    private static final Uri mChrome1 = Uri.parse("content://com.android.chrome.browser/bookmarks");
    private static final Uri mChrome2 = Uri.parse("content://com.android.chrome/bookmarks");
    private static final Uri mChrome3 = Uri.parse("content://com.android.chrome.ChromeBrowserProvider/bookmarks");
    private static final Uri mChrome4 = Uri.parse("content://com.android.browser/bookmarks");
    private static final Uri BOOKMARKS_URI = Uri.parse("content://browser/bookmarks");
    private static Uri mCurrentChromeUri;
    private static ImportBookmarManager mInstance;
    private boolean mCheckChrome;
    private Context mContext;
    private IBookmarkObserver mBookmarkObserver;

    static public ImportBookmarManager getInstance() {
        if (mInstance == null) {
            synchronized (ImportBookmarManager.class) {
                if (mInstance == null) {
                    mInstance = new ImportBookmarManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context c, IBookmarkObserver Observer) {
        mContext = c;
        mBookmarkObserver = Observer;
        if (mCheckChrome == false) {
            ThreadManager.postTaskToIOHandler(new Runnable() {
                @Override
                public void run() {
                    getChromeUri();
                    mCheckChrome = true;
                }
            });
        }
    }

    public void unRegisterObserver() {
        mBookmarkObserver = null;
    }

    private void getChromeUri() {
        if (mContext != null && mCurrentChromeUri == null) {
            Cursor cur = null;
            try {
                cur = mContext.getContentResolver().query(mChrome1, null, null, null, null);
                if (cur != null) {
                    cur.close();
                    mCurrentChromeUri = mChrome1;
                    return;
                }
                cur = mContext.getContentResolver().query(mChrome2, null, null, null, null);
                if (cur != null) {
                    cur.close();
                    mCurrentChromeUri = mChrome2;
                    return;
                }
                cur = mContext.getContentResolver().query(mChrome3, null, null, null, null);
                if (cur != null) {
                    cur.close();
                    mCurrentChromeUri = mChrome3;
                    return;
                }
                cur = mContext.getContentResolver().query(mChrome4, null, null, null, null);
                if (cur != null) {
                    cur.close();
                    mCurrentChromeUri = mChrome4;
                    return;
                }
            } catch (Exception e) {
                if (cur != null) {
                    cur.close();
                }
            }
        }
    }

    public boolean hasChromeBookmakr() {
        if (mContext == null) {
            return false;
        }
        if (mCurrentChromeUri != null) {
            return true;
        }
        return false;
    }

    public boolean importBookmarkFromChrome() {
        if (mContext == null || mCurrentChromeUri == null) {
            return false;
        }
        return importBookmark(mCurrentChromeUri, IMPORT_FROM_CHROME);
    }

    public boolean importBookmarkFromSystemBrowser() {
        if (mContext == null) {
            return false;
        }
        return importBookmark(BOOKMARKS_URI, IMPORT_FROM_SYSTEM);
    }

    private boolean importBookmark(final Uri uri, final int type) {
        if (mContext == null || uri == null) {
            return false;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean isSucceed = false;
                Cursor cur = null;
                try {
                    cur = mContext.getContentResolver().query(uri, null, null, null, null);
                    if (cur != null && cur.moveToFirst()) {
                        String title = null;
                        String url = null;
                        do {
                            String string = cur.getString(cur.getColumnIndex(BookmarkColumns.BOOKMARK));
                            if (string.equals("1")) {
                                title = cur.getString(cur.getColumnIndex(BookmarkColumns.TITLE));
                                url = cur.getString(cur.getColumnIndex(BookmarkColumns.URL));
                                BookmarkManager.getInstance().addBookmarkImport(title, url);
                                isSucceed = true;
                            }
                        } while (cur.moveToNext());
                        if (cur != null) {
                            cur.close();
                        }
                    }
                } catch (Exception e) {
                    if (cur != null) {
                        cur.close();
                    }
                }
                if (isSucceed) {
                    CustomToastUtils.getInstance().showImgToast(
                            R.string.add_bookmark_tips,
                            R.drawable.address_bookmark_star_added);
                    BookmarkManager.getInstance().finishBookmarkImport();
                    ThreadManager.postTaskToUIHandler(new Runnable() {
                        @Override
                        public void run() {
                            if (mBookmarkObserver != null) {
                                mBookmarkObserver.notifyBookmarkChanged(true,true);
                            }
                        }
                    });
                } else {
                    if (type == IMPORT_FROM_SYSTEM) {
                        CustomToastUtils.getInstance().showImgToast(
                                R.string.add_bookmark_system_empty,
                                R.drawable.address_bookmark_star_added);
                    } else if (type == IMPORT_FROM_CHROME) {
                        CustomToastUtils.getInstance().showImgToast(
                                R.string.add_bookmark_chrom_empty,
                                R.drawable.address_bookmark_star_added);
                    } else {
                        // TODO do something
                    }
                }
            }
        };
        ThreadManager.postTaskToIOHandler(r);
        return true;
    }

    public static class BookmarkColumns implements BaseColumns {
        /**
         * The URL of the bookmark or history item.
         * <p>Type: TEXT (URL)</p>
         */
        public static final String URL = "url";

        /**
         * The number of time the item has been visited.
         * <p>Type: NUMBER</p>
         */
        public static final String VISITS = "visits";

        /**
         * The date the item was last visited, in milliseconds since the epoch.
         * <p>Type: NUMBER (date in milliseconds since January 1, 1970)</p>
         */
        public static final String DATE = "date";

        /**
         * Flag indicating that an item is a bookmark. A value of 1 indicates a bookmark, a value
         * of 0 indicates a history item.
         * <p>Type: INTEGER (boolean)</p>
         */
        public static final String BOOKMARK = "bookmark";

        /**
         * The user visible title of the bookmark or history item.
         * <p>Type: TEXT</p>
         */
        public static final String TITLE = "title";

        /**
         * The date the item created, in milliseconds since the epoch.
         * <p>Type: NUMBER (date in milliseconds since January 1, 1970)</p>
         */
        public static final String CREATED = "created";

        /**
         * The favicon of the bookmark. Must decode via {@link BitmapFactory#decodeByteArray}.
         * <p>Type: BLOB (image)</p>
         */
        public static final String FAVICON = "favicon";

        /**
         * @hide
         */
        public static final String THUMBNAIL = "thumbnail";

        /**
         * @hide
         */
        public static final String TOUCH_ICON = "touch_icon";

        /**
         * @hide
         */
        public static final String USER_ENTERED = "user_entered";
    }
}
