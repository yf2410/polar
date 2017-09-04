package com.polar.browser.crashhandler;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;

import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.env.AppEnv;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.SafeAsyncTask;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ZipUtil;

import org.apache.http.client.HttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 负责上传crash信息的task
 * 分为两种情况：
 * 1. 上传整个文件夹的所有crash信息
 * 2. 上传单个crash文件
 * <p/>
 * 这两种情况通过构造函数来进行区分
 *
 * @author dpk
 */
public class CrashUploadTask extends SafeAsyncTask<String, Void, Integer> {

	private static final String TAG = "CrashUploadTask";
	private static final String UPLOAD_URL = RequestAPI.SERVER_API_ADDRESS + RequestAPI.UPLOAD_CRASH;
	private CommonDialog mDialog;
	private IExit mExit;
	private Context mContext;
	private static final String ANR_PREF = "AnrPref";
	private static final String ANR_LAST_TRACE = "ANR_LAST_TS";
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private boolean mIsUploadAllCrash = false;

	/**
	 * 如果要上传的是crash目录下所有文件，则需要设置isUploadAllCrash为true，同时执行任务时传入文件夹的路经
	 * @param dialog
	 * @param exit
	 * @param context
	 * @param isUploadAllCrash
     */
	public CrashUploadTask(CommonDialog dialog, IExit exit, Context context, boolean isUploadAllCrash) {
		mDialog = dialog;
		mExit = exit;
		mContext = context;
		mIsUploadAllCrash = isUploadAllCrash;
	}

	/**
	 * 如果要上传的只是单个crash文件，则直接调用本方法即可，执行任务时传入文件的路经
	 *
	 * @param dialog
	 * @param exit
	 * @param context
	 */
	public CrashUploadTask(CommonDialog dialog, IExit exit, Context context) {
		this(dialog, exit, context, false);
	}


	/**
	 * 如果是上传所有的crash信息，则params[0]需要传递的是文件夹路经；如果只上传一个crash文件，则param[0]需要传递的是某个文件的路经
	 */
	@Override
	protected Integer doInBackground(String... params) {
		try {
			upload(params[0]);
			upload(params[1]);
		} catch(Throwable e) {
		}
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		try {
			if (mDialog != null) {
				mDialog.dismiss();
			}
		} catch (Exception e) {
			SimpleLog.e(e);
		} finally {
			if (mExit != null) {
				mExit.exit();
			}
		}
	}

	/**
	 * 执行上传任务
	 *
	 * @param crashFilePath
	 */
	private void upload(String crashFilePath) {
		try {
			SimpleLog.e(TAG, "crashFilePath:" + crashFilePath);
			if (crashFilePath.contains(CrashHandler.ANR_DIR)) {
				exportANRTraces();
			}
			File file = new File(crashFilePath);
			if (mIsUploadAllCrash) { // 上传全部crash
				File[] currentPathFiles = file.listFiles();
				// 需要将crash目录所有的文件进行上传
				if (currentPathFiles != null) {
					for (File currentFile : currentPathFiles) {
						uploadOneFile(currentFile);
					}
				}
			} else { // 上传单个crash文件
				uploadOneFile(file);
			}
			Thread.sleep(1000);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
	}

	/**
	 * 上传单个文件，对文件进行了压缩
	 *
	 * @param file
	 * @return
	 */
	private boolean uploadOneFile(File file) {
		if (file.exists()) {
			byte[] crashInfoBytes = FileUtils.readFile(file);
			byte[] gzipInfoBytes = ZipUtil.gZip2(crashInfoBytes);
			if (gzipInfoBytes != null && gzipInfoBytes.length > 0) {
				HttpClient httpClient = NetworkUtils
						.createHttpClient(NetworkUtils
								.getCurrentProxy(mContext));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				String uploadUrl = Statistics.appendBasicStat(UPLOAD_URL);
				uploadUrl = Statistics.appendAdvancedStat(uploadUrl);
				uploadUrl = Statistics.appendArg(uploadUrl, Statistics.COMPRESS_SWITCHER, "on");
				if (file.getAbsolutePath().contains(CrashHandler.ANR_DIR)) {
					uploadUrl = Statistics.appendArg(uploadUrl, "type", "anr");
				}
				int result = NetworkUtils.PostForm(httpClient, uploadUrl,
						gzipInfoBytes, bos, null);
				if (result >= 0) { // 上传成功，需要把crash文件删除
					FileUtils.deleteFileOrDirectory(file);
					return true;
				} else { // 上传失败，不删除文件，以备下次进行上传
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Format of anr trace dumped by art runtime varies from that by dalvik
	 * runtime. For dalvik, format of trace header is:
	 *
	 * <pre>
	 * ----- pid <pid> at yyyy-MM-dd HH:mm:ss -----
	 * Cmd line: <process name>
	 * </pre>
	 *
	 * For art, format of trace header is:
	 *
	 * <pre>
	 * ----- pid <pid> at yyyy-MM-dd HH:mm:ss -----
	 * Cmdline: <process name>             Original command line: <unset>
	 * </pre>
	 *
	 * @author songzz
	 */
	private static class ANRTraceHeader {
		final String pidTimeLine;
		final String cmdlineLine;

		static ANRTraceHeader readFromTraces(BufferedReader reader) throws IOException {
			String line;
			String pidTime = null;
			String cmdline = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("-----") && line.contains("pid")) {
					pidTime = line;
					break;
				}
			}
			if (pidTime != null) {
				cmdline = reader.readLine();
			}
			if (pidTime != null && cmdline != null) {
				return new ANRTraceHeader(pidTime, cmdline);
			}
			return null;
		}

		private ANRTraceHeader(String pidTimeLine,
							   String cmdlineLine) {
			this.pidTimeLine = pidTimeLine;
			this.cmdlineLine = cmdlineLine;
		}

		long getTime() {
			int off;
			off = pidTimeLine.indexOf("at");
			if (off == -1)
				return 0;
			off += 2; // skip "at"
			int end = pidTimeLine.indexOf("-----", off);
			String t = pidTimeLine.substring(off, end).trim();
			SimpleDateFormat formatter;
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			try {
				Date date = (Date) formatter.parse(t);
				return date.getTime();
			} catch (ParseException e) {
				SimpleLog.e(TAG, "parse anr trace time error");
			}
			return 0;
		}

		boolean isFromProcess(String processName) {
			int offset = cmdlineLine.indexOf(':');
			if (offset == -1)
				return false;
			String cmdline = cmdlineLine.substring(offset + 1).trim();
			if(!cmdline.startsWith(processName))
				return false;
			final int cmdlineLength = cmdline.length();
			final int processNameLength = processName.length();
			return (cmdlineLength == processNameLength)
					|| (cmdlineLength > processNameLength && Character.isWhitespace(cmdline.codePointAt(processNameLength)));
		}
	}

	private File getANRLogDir() {
		String path = Environment.getExternalStorageDirectory().getPath();
		path += CrashHandler.ANR_DIR;
		File dir = new File(path);
		if (ensureDirExist(dir))
			return dir;
		else
			return null;
	}

	private boolean ensureDirExist(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory())
				return true;
			else {
				return dir.delete() && dir.mkdir();
			}
		} else {
			dir.mkdirs();
			return dir.isDirectory();
		}
	}

	private void writeMemoryInfo(BufferedWriter writer) throws IOException {
		android.os.Debug.MemoryInfo memoryInfo = new android.os.Debug.MemoryInfo();
		Debug.getMemoryInfo(memoryInfo);
		writer.write("\n\n----VCBrowser Memory Info----\n");
		writer.write("\ndalvikPss (KB):" + memoryInfo.dalvikPss);
		writer.write("\nnativePss (KB):" + memoryInfo.nativePss);
		writer.write("\notherPss (KB):" + memoryInfo.otherPss);
		writer.write("\ndalvikPrivateDirty (KB):" + memoryInfo.dalvikPrivateDirty);
		writer.write("\nnativePrivateDirty (KB):" + memoryInfo.nativePrivateDirty);
		writer.write("\notherPrivateDirty (KB):" + memoryInfo.otherPrivateDirty);
		writer.write("\ndalvikSharedDirty (KB):" + memoryInfo.dalvikSharedDirty);
		writer.write("\nnativeSharedDirty (KB):" + memoryInfo.nativeSharedDirty);
		writer.write("\notherSharedDirty (KB):" + memoryInfo.otherSharedDirty);

		ActivityManager.MemoryInfo deviceMemoryInfo = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(deviceMemoryInfo);

		writer.write("\n\n----Device Memory Info----\n");
		writer.write("\ndevice.availMemory (kb):" + deviceMemoryInfo.availMem/1024);
		if (Build.VERSION.SDK_INT >= 16) {
			writer.write("\ndevice.usedMemory (kb):" + (deviceMemoryInfo.totalMem - deviceMemoryInfo.availMem)/1024);
			writer.write("\ndevice.totalMemory (kb):" + deviceMemoryInfo.totalMem/1024);
		}
		writer.write("\ndevice.isLowMemory :" + deviceMemoryInfo.lowMemory);
		writer.write("\ndevice.thresholdMemory (kb):" + deviceMemoryInfo.threshold/1024);
	}

	private File[] exportANRTraces() {
		File anrDir = new File("/data/anr/");
		File[] anrTraceFiles = anrDir.listFiles();
		if (anrTraceFiles == null || anrTraceFiles.length == 0)
			return null;
		CrashHandler.getInstance().collectDeviceInfo();
		ArrayList<File> array = new ArrayList<File>();
		// get last update ts
		SharedPreferences settings = mContext.getSharedPreferences(
				ANR_PREF, 0);
		long last = settings.getLong(ANR_LAST_TRACE, 0);
		long maxTime = 0;
		for (File f : anrTraceFiles) {
			if (!(f.isFile() && f.canRead()))
				continue;
			BufferedReader reader = null;
			BufferedWriter writer = null;
			File df = null;
			try {
				reader = new BufferedReader(new FileReader(f), 1024);
				ANRTraceHeader header = ANRTraceHeader.readFromTraces(reader);
				if (header == null)
					continue;
				if (!header.isFromProcess(AppEnv.PACKAGE_NAME))
					continue;
				long time = header.getTime();
				if (time <= last)
					continue;
				long timestamp = System.currentTimeMillis();
				String timevalue = "TIMESTAMP=" + String.valueOf(timestamp) + "\n";
				String currentTime = formatter.format(new Date());
				String fileName = "anr-" + currentTime + "-" + timestamp + "-" + f.getName() + ".log";
				df = new File(getANRLogDir(), fileName);
				writer = new BufferedWriter(new FileWriter(df), 1024);
				CrashHandler.getInstance().writeDeviceInfo(writer);
				writeMemoryInfo(writer);
				writer.write("\n\n\n");
				writer.write(timevalue);
				writer.write("ANRDUMP=");
				writer.write(header.pidTimeLine);
				writer.write('\n');
				writer.write(header.cmdlineLine);
				writer.write('\n');
				String line;
				while ((line = reader.readLine()) != null) {
					writer.write(line);
					writer.write('\n');
					if (line.startsWith("-----")) // the end of this trace
						break;
				}

				array.add(df);
				if (time > maxTime)
					maxTime = time;
			} catch ( Throwable e) {
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// DO NOTHING
					}
				}
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						array.remove(df);
						df.delete();
					}
				}
			}
		}

		if (maxTime <= last)
			return null;
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(ANR_LAST_TRACE, maxTime);
		editor.apply();

		File[] fs = new File[array.size()];
		array.toArray(fs);
		return fs;
	}
}
