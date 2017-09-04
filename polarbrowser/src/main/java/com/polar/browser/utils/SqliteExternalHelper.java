package com.polar.browser.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 用于操作外部只读数据库的helper类
 *
 * @author dpk
 */
public class SqliteExternalHelper {

	private static final String TAG = "SqliteHelper";

	private static final int DB_OLDEST_VERSION = 1;

	private SQLiteDatabase mReadableDb;

	private boolean mIsDbOpened = false;

	private String mPath;

	//sqlite3数据库并不支持多线程写入，所以用mLock作同步
	private Object mLock = new Object();

	public SqliteExternalHelper(String path) {
		mPath = path;
		SimpleLog.d("City", "db path:" + mPath);
	}

	private void ensureDbOpened() {
		if (mIsDbOpened)
			return;
		synchronized (mLock) {
			try {
				mReadableDb = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY); //mOpenHelper.getReadableDatabase();
//                mWritableDb = mOpenHelper.getWritableDatabase();
			} catch (Exception e) {
				SimpleLog.d(TAG, "[ensureDbOpened] exception" + e);
				mReadableDb = null;
//                mWritableDb = null;
			}
			mIsDbOpened = true;
		}
	}

	public Cursor query(String sql, String[] selectionArgs) {
		ensureDbOpened();
		try {
			return this.mReadableDb.rawQuery(sql, selectionArgs);
		} catch (Exception e) {
			SimpleLog.e(TAG, "[query]" + Log.getStackTraceString(e));
		}
		return null;
	}

	public int getCurDbVersion() {
		ensureDbOpened();
		try {
			return this.mReadableDb.getVersion();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[getCurDbVersion]" + Log.getStackTraceString(e));
		}
		return DB_OLDEST_VERSION;
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		ensureDbOpened();
		try {
			return this.mReadableDb.rawQuery(sql, selectionArgs);
		} catch (Exception e) {
			SimpleLog.e(TAG, "[rawQuery]" + Log.getStackTraceString(e));
		}
		return null;
	}

	public void execSqlWrite(String sql) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				this.mReadableDb.execSQL(sql);
			} catch (Exception e) {
				SimpleLog.e(TAG, "[execSqlWrite]" + Log.getStackTraceString(e));
			}
		}
	}

	public void execSqlRead(String sql) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				this.mReadableDb.execSQL(sql);
			} catch (Exception e) {
				SimpleLog.e(TAG, "[execSqlRead]" + Log.getStackTraceString(e));
			}
		}
	}

	public void beginTransaction() {
		ensureDbOpened();
		try {
			this.mReadableDb.beginTransaction();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[beginTransaction]" + Log.getStackTraceString(e));
		}
	}

	public void setTransactionSuccessful() {
		ensureDbOpened();
		try {
			this.mReadableDb.setTransactionSuccessful();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[setTransactionSuccessful]" + Log.getStackTraceString(e));
		}
	}

	public void endTransaction() {
		ensureDbOpened();
		try {
			this.mReadableDb.endTransaction();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[endTransaction]" + Log.getStackTraceString(e));
		}
	}

	public void close() {
		synchronized (mLock) {
			if (mIsDbOpened) {
				mReadableDb.close();
			}
			mIsDbOpened = false;
		}
	}
}
