package com.polar.browser.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 通用SqliteHelper类
 * 可以通过该SqliteHelper类来执行自定义数据库的操作
 * 定义了增、删、改、查等通用接口
 *
 * @author dpk
 *
 * 1.目前未对单引号进行处理，请务必小心
 * 2.所有的执行都会吞掉异常，只能对返回值进行判断，对于void的只能呵呵了
 * 3.mReadableDb和mWritableDb实际是对同一个对象的引用
 * 4.建议早日更换Ormlite等第三方框架
 * commented by Hongjian Liu
 *
 */
public class SqliteHelper {

	private static final String TAG = "SqliteHelper";

	private static final int DB_OLDEST_VERSION = 1;

	private SQLiteDatabase mWritableDb;
	private SQLiteDatabase mReadableDb;

	private boolean mIsDbOpened = false;

	// 需要通过外部传入，因为OpenHelper中定义了数据库名字，如何创建表等与具体数据库有关的内容
	private SQLiteOpenHelper mOpenHelper;

	//sqlite3数据库并不支持多线程写入，所以用mLock作同步
	private Object mLock = new Object();

	public SqliteHelper(SQLiteOpenHelper openHelper) {
		mOpenHelper = openHelper;
	}

	private void ensureDbOpened() {
		if (mIsDbOpened)
			return;
		synchronized (mLock) {
			try {
				mReadableDb = mOpenHelper.getReadableDatabase();
				mWritableDb = mOpenHelper.getWritableDatabase();
				mIsDbOpened = true;
			} catch (Exception e) {
				SimpleLog.d(TAG, "[ensureDbOpened] exception" + e);
				mReadableDb = null;
				mWritableDb = null;
				mIsDbOpened = false;
			}
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

	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				return this.mWritableDb.update(table, values, whereClause, whereArgs);
			} catch (Exception e) {
				SimpleLog.e(TAG, "[update]" + Log.getStackTraceString(e));
			}
			return 0;
		}
	}

	public long insert(String table, ContentValues values) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				return this.mWritableDb.insert(table, null, values);
			} catch (Exception e) {
				SimpleLog.e(TAG, "[insert]" + Log.getStackTraceString(e));
			}
			return -1l;
		}
	}

	public void execSqlWrite(String sql) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				this.mWritableDb.execSQL(sql);
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

	public long replace(String table, ContentValues values) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				return this.mWritableDb.replace(table, null, values);
			} catch (Exception e) {
				SimpleLog.e(TAG, "[insert]" + Log.getStackTraceString(e));
			}
			return -1l;
		}
	}

	public int forceInsertAll(String table, ContentValues[] valuesArray) {
		int totalInserted = 0;
		synchronized (mLock) {
			ensureDbOpened();
			if (this.mWritableDb != null) {
				beginTransaction();
				for (ContentValues values : valuesArray) {
					if (values != null) {
						try {
							if (this.mWritableDb.replace(table, null, values) == -1)
								continue;
							totalInserted++;
						} catch (Exception e) {
							SimpleLog.e(TAG, "[insertAll]" + Log.getStackTraceString(e));
						}
					}
				}
				setTransactionSuccessful();
				endTransaction();
			}
		}
		return totalInserted;
	}

	public int delete(String table, String whereClause, String[] whereArgs) {
		synchronized (mLock) {
			ensureDbOpened();
			try {
				return this.mWritableDb.delete(table, whereClause, whereArgs);
			} catch (Exception e) {
				SimpleLog.e(TAG, "[delete]" + Log.getStackTraceString(e));
			}
			return 0;
		}
	}

	public void beginTransaction() {
		ensureDbOpened();
		try {
			this.mWritableDb.beginTransaction();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[beginTransaction]" + Log.getStackTraceString(e));
		}
	}

	public void setTransactionSuccessful() {
		ensureDbOpened();
		try {
			this.mWritableDb.setTransactionSuccessful();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[setTransactionSuccessful]" + Log.getStackTraceString(e));
		}
	}

	public void endTransaction() {
		ensureDbOpened();
		try {
			this.mWritableDb.endTransaction();
		} catch (Exception e) {
			SimpleLog.e(TAG, "[endTransaction]" + Log.getStackTraceString(e));
		}
	}

	public void close() {
		synchronized (mLock) {
			if (mIsDbOpened) {
				if (mWritableDb != null)
					mWritableDb.close();
				if (mReadableDb != null)
					mReadableDb.close();
			}
			mIsDbOpened = false;
		}
	}
}
