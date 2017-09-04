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

package com.polar.browser.download_refactor;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;

//import com.android.internal.R;

/**
 * Manages the storage space consumed by Downloads Data dir. When space falls below
 * a threshold limit (set in resource xml files), starts cleanup of the Downloads data dir
 * to free up space.
 */
class StorageManager {
    /** the max amount of space allowed to be taken up by the downloads data dir */
//    private static final long sMaxdownloadDataDirSize =
//            Resources.getSystem().getInteger(R.integer.config_downloadDataDirSize) * 1024 * 1024;
            // 部分ROM可能会去掉这些参数，默认值保证安全即可
    private static long sMaxdownloadDataDirSize = 128*1024*1024*1024; // default 64GB
    static {
        try {
            // TODO Fix me
//            sMaxdownloadDataDirSize =
//                    Resources.getSystem().getInteger(R.integer.config_downloadDataDirSize) * 1024 * 1024;
        } catch (Throwable ignored) {
            //sMaxdownloadDataDirSize = 0;
        }
    }

    /** threshold (in bytes) beyond which the low space warning kicks in and attempt is made to
     * purge some downloaded files to make space
     */
//    private static final long sDownloadDataDirLowSpaceThreshold =
//            Resources.getSystem().getInteger(
//                    R.integer.config_downloadDataDirLowSpaceThreshold)
//                    * sMaxdownloadDataDirSize / 100;

    private static long sDownloadDataDirLowSpaceThreshold = 200*1024*1024; // 200MB
    static {
        try {
            // TODO Fix me
//            sDownloadDataDirLowSpaceThreshold =
//                    Resources.getSystem().getInteger(
//                            R.integer.config_downloadDataDirLowSpaceThreshold)
//                            * sMaxdownloadDataDirSize / 100;
        } catch (Throwable ignored) {
            //sDownloadDataDirLowSpaceThreshold = 0;
        }
    }

    /** see {@link Environment#getExternalStorageDirectory()} */
    private final File mExternalStorageDir;

    /** see {@link Context#getFilesDir()} */
    private final File mInternalStorateDir;

    /** how often do we need to perform checks on space to make sure space is available */
    private static final int FREQUENCY_OF_CHECKS_ON_SPACE_AVAILABILITY = 1024 * 1024; // 1MB
    private int mBytesDownloadedSinceLastCheckOnSpace = 0;

    /** misc members */
    private final Context mContext;

    public StorageManager(Context context) {
        mContext = context;
        mExternalStorageDir = Environment.getExternalStorageDirectory();
        mInternalStorateDir = context.getFilesDir();
        startThreadToCleanupDatabaseAndPurgeFileSystem();
    }

    /** How often should database and filesystem be cleaned up to remove spurious files
     * from the file system and
     * The value is specified in terms of num of downloads since last time the cleanup was done.
     */
    private static final int FREQUENCY_OF_DATABASE_N_FILESYSTEM_CLEANUP = 250;
    private int mNumDownloadsSoFar = 0;

    synchronized void incrementNumDownloadsSoFar() {
        if (++mNumDownloadsSoFar % FREQUENCY_OF_DATABASE_N_FILESYSTEM_CLEANUP == 0) {
            startThreadToCleanupDatabaseAndPurgeFileSystem();
        }
    }
    /* start a thread to cleanup the following
     *      remove spurious files from the file system
     *      remove excess entries from the database
     */
    private Thread mCleanupThread = null;
    private synchronized void startThreadToCleanupDatabaseAndPurgeFileSystem() {
        if (mCleanupThread != null && mCleanupThread.isAlive()) {
            return;
        }
        mCleanupThread = new Thread() {
            @Override
            public void run() {
                trimDatabase();
            }
        };
        mCleanupThread.start();
    }

    void verifySpaceBeforeWritingToFile(String path, long length)
            throws StopRequestException {
        // do this check only once for every 1MB of downloaded data
        if (incrementBytesDownloadedSinceLastCheckOnSpace(length) <
                FREQUENCY_OF_CHECKS_ON_SPACE_AVAILABILITY) {
            return;
        }
        verifySpace(path, length);
    }

    void verifySpace(String path, long length) throws StopRequestException {
        resetBytesDownloadedSinceLastCheckOnSpace();
        File dir = null;
        if (Constants.LOGV) {
            Log.i(Constants.TAG, "in verifySpace, path: " + path + ", length: " + length);
        }
        if (path == null) {
            throw new IllegalArgumentException("path can't be null");
        }
        if (path.startsWith(mExternalStorageDir.getPath())) {
            dir = mExternalStorageDir;
        } else if (path.startsWith(mInternalStorateDir.getPath())) {
            dir = mInternalStorateDir;
        } else {
            File pathFile = new File(path);
            if (null != pathFile)
                dir = pathFile.getParentFile();
        }

        if (dir == null) {
            throw new IllegalStateException("invalid combination of path: " + path);
        }
        findSpace(dir, length);
    }

    /**
     * finds space in the given filesystem (input param: root) to accommodate # of bytes
     * specified by the input param(targetBytes).
     * returns true if found. false otherwise.
     */
    private synchronized void findSpace(File root, long targetBytes)
            throws StopRequestException {
        if (targetBytes == 0) {
            return;
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new StopRequestException(Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR,
                    "external media not mounted");
        }

        // is there enough space in the file system of the given param 'root'.
        long bytesAvailable = getAvailableBytesInFileSystemAtGivenRoot(root);
        if (bytesAvailable < sDownloadDataDirLowSpaceThreshold) {
            /*
             * 这里没有做任何释放空间的优化
             * available space is still below the threshold limit.
             *
             * If this is system cache dir, print a warning.
             * otherwise, don't allow downloading until more space
             * is available because downloadmanager shouldn't end up taking those last
             * few MB of space left on the filesystem.
             */
            throw new StopRequestException(Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                    "space in the filesystem rooted at: " + root +
                            " is below 10% availability. stopping this download.");
        }
        if (bytesAvailable < targetBytes) {
            throw new StopRequestException(Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                    "not enough free space in the filesystem rooted at: " + root +
                    " and unable to free any more");
        }
    }

    /**
     * returns the number of bytes available in the downloads data dir
     * TODO this implementation is too slow. optimize it.
     */
    private long getAvailableBytesInDownloadsDataDir(File root) {
        File[] files = root.listFiles();
        long space = sMaxdownloadDataDirSize;
        if (files == null) {
            return space;
        }
        for (File file : files) {
            space -= file.length();
        }
        if (Constants.LOGV) {
            Log.i(Constants.TAG, "available space (in bytes) in downloads data dir: " + space);
        }
        return space;
    }

    private long getAvailableBytesInFileSystemAtGivenRoot(File root) {
        StatFs stat = new StatFs(root.getPath());
        // put a bit of margin (in case creating the file grows the system by a few blocks)
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        long size = stat.getBlockSize() * availableBlocks;
        if (Constants.LOGV) {
            Log.i(Constants.TAG, "available space (in bytes) in filesystem rooted at: " +
                    root.getPath() + " is: " + size);
        }
        return size;
    }

    /**
     * Drops old rows from the database to prevent it from growing too large
     * TODO logic in this method needs to be optimized. maintain the number of downloads
     * in memory - so that this method can limit the amount of data read.
     */
    private void trimDatabase() {
        if (Constants.LOGV) {
            Log.i(Constants.TAG, "in trimDatabase");
        }
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Downloads.Impl.CONTENT_URI,
                    new String[] { Downloads.Impl._ID },
                    Downloads.Impl.COLUMN_STATUS + " >= '200'", null,
                    Downloads.Impl.COLUMN_LAST_MODIFICATION);
            if (cursor == null) {
                // This isn't good - if we can't do basic queries in our database,
                // nothing's gonna work
                Log.e(Constants.TAG, "null cursor in trimDatabase");
                return;
            }
            if (cursor.moveToFirst()) {
                int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
                int columnId = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);
                while (numDelete > 0) {
                    Uri downloadUri = ContentUris.withAppendedId(
                            Downloads.Impl.CONTENT_URI, cursor.getLong(columnId));
                    mContext.getContentResolver().delete(downloadUri, null, null);
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    numDelete--;
                }
            }
        } catch (SQLiteException e) {
            // trimming the database raised an exception. alright, ignore the exception
            // and return silently. trimming database is not exactly a critical operation
            // and there is no need to propagate the exception.
            Log.w(Constants.TAG, "trimDatabase failed with exception: " + e.getMessage());
            return;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private synchronized int incrementBytesDownloadedSinceLastCheckOnSpace(long val) {
        mBytesDownloadedSinceLastCheckOnSpace += val;
        return mBytesDownloadedSinceLastCheckOnSpace;
    }

    private synchronized void resetBytesDownloadedSinceLastCheckOnSpace() {
        mBytesDownloadedSinceLastCheckOnSpace = 0;
    }
}
