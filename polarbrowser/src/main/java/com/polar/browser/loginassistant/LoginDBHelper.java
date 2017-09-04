package com.polar.browser.loginassistant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.polar.browser.bean.LoginAccountInfo;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SqliteHelper;

import java.util.ArrayList;
import java.util.List;

public class LoginDBHelper {

	private static final String TAG = "LoginDBHelper";

	private static final String DB_NAME = "loginassistant.db";

	private static final String TABLE_NAME_MAIN = "login_main";

	private static final int DB_VERSION = 1;

	private static final int LIMIT_STORE_ACCOUNT = 1000;

	private static final int DELETE_NUMBER = 500;

	private static final String COL_ID = "id";
	private static final String COL_URL = "url";
	private static final String COL_USERNAME = "username";
	private static final String COL_PASSWORD = "password";
	private static final String COL_TS = "ts";

	private static final String SQL_CREATE_TABLE_MAIN = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME_MAIN + "' (" +
			COL_ID + " integer NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
			COL_URL + " text," + COL_USERNAME + " text," + COL_PASSWORD + " text," + COL_TS + " timestamp)";

	private static final String SQL_QUERY_LIMIT = String.format("SELECT * FROM '%s' ORDER BY ts DESC LIMIT ?", TABLE_NAME_MAIN);

	private static LoginDBHelper sInstance;

	private DbOpenHelper mDbOpenHelper;

	private SqliteHelper mSqliteHelper;

	private Context mContext;

	private LoginDBHelper() {
	}

	public static LoginDBHelper getInstance() {
		if (sInstance != null) {
			return sInstance;
		}
		synchronized (LoginDBHelper.class) {
			if (sInstance == null) {
				sInstance = new LoginDBHelper();
			}
		}
		return sInstance;
	}

	public void init(Context c) {
		mContext = c.getApplicationContext();
		mDbOpenHelper = new DbOpenHelper(mContext);
		mSqliteHelper = new SqliteHelper(mDbOpenHelper);
	}

	/**
	 * 添加一条账号
	 *
	 * @param url
	 * @param username
	 * @param password
	 */
	public void addAccount(String url, String username, String password) {
		username = replaceSingleQuote(username);
		password = replaceSingleQuote(password);
		String sql = String.format("INSERT INTO '%s' VALUES(NULL, '%s', '%s', '%s'," +
				"datetime('now','localtime'))", TABLE_NAME_MAIN, url, username, password);
		mSqliteHelper.execSqlWrite(sql);
	}

	/**
	 * single quotes handler
	 * @param s
	 * @return
	 */
	private String replaceSingleQuote(String s) {
		if (s != null) {
			return s.replaceAll("'", "''");
		} else {
			return null;
		}
	}

	/**
	 * 删除一条账号
	 *
	 * @param url
	 */
	public void deleteAccountByUrl(String url) {
		String sql = String.format("DELETE FROM '%s' WHERE %s = '%s'",
				TABLE_NAME_MAIN, COL_URL, url);
		mSqliteHelper.execSqlWrite(sql);
	}

	/**
	 * 根据url更新用户名密码
	 *
	 * @param url
	 * @param username
	 * @param password
	 */
	public void updateAccountByUrl(String url, String username, String password) {
		username = replaceSingleQuote(username);
		password = replaceSingleQuote(password);
		String sql = String.format("UPDATE %s SET %s = '%s' , %s = '%s' WHERE %s = '%s'  ",
				TABLE_NAME_MAIN, COL_USERNAME, username, COL_PASSWORD, password, COL_URL, url);
		mSqliteHelper.execSqlWrite(sql);
	}

	/**
	 * 查询某url下账号
	 *
	 * @param url
	 * @return
	 */
	public List<LoginAccountInfo> queryAccountByUrl(String url) {
		String sql = String.format("SELECT * FROM '%s' WHERE %s = '%s'  ", TABLE_NAME_MAIN, COL_URL, url);
		Cursor cursor = mSqliteHelper.query(sql, null);
		List<LoginAccountInfo> result = new ArrayList<LoginAccountInfo>();
		convertCursorToListInfo(cursor, result);
		return result;
	}

	public List<LoginAccountInfo> queryLoginAccount(int limitCount, boolean isDistinctUrl) {
		List<LoginAccountInfo> result = new ArrayList<LoginAccountInfo>();
		Cursor c = null;
		c = mSqliteHelper.query(SQL_QUERY_LIMIT, new String[]{String.valueOf(limitCount)});
		convertCursorToListInfo(c, result);
		return result;
	}

	private int convertCursorToListInfo(Cursor c, List<LoginAccountInfo> result) {
		if (c != null && !c.isClosed() && c.getCount() > 0) {
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				LoginAccountInfo info = new LoginAccountInfo();
				info.setId(c.getInt(c.getColumnIndex(COL_ID)));
				info.setUrl(c.getString(c.getColumnIndex(COL_URL)));
				info.setUsername(c.getString(c.getColumnIndex(COL_USERNAME)));
				info.setPassword(c.getString(c.getColumnIndex(COL_PASSWORD)));
				info.setTimestamp(c.getString(c.getColumnIndex(COL_TS)));
				result.add(info);
			}
		}
		if (c != null) {
			FileUtils.closeCursor(c);
		}
		return result.size();
	}

	public void destroy() {
		mSqliteHelper.close();
	}

	private static class DbOpenHelper extends SQLiteOpenHelper {
		public DbOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			SimpleLog.d(TAG, "[onCreate] --> sql:" + SQL_CREATE_TABLE_MAIN);
			db.execSQL(SQL_CREATE_TABLE_MAIN);
			SimpleLog.d(TAG, "[onCreate] --> sql>>time:");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			SimpleLog.d(TAG, "[onUpgrade] --> sql>>oldVersion=" + oldVersion + "newVersion=" + newVersion);
		}

		/* 从3.0之后在继承SQLiteOpenHelper的类中，都需要重写该方法（该方法默认抛出异常），否则在数据库降级的时候会抛出异常。 */
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dealWithDbDowngrap(db, oldVersion, newVersion);
		}

		private void dealWithDbDowngrap(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
