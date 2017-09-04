package com.polar.browser.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IDownloadImgCallBack;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.vclibrary.network.api.Api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * 基于 HttpClient 进行的封装，主要的功能：
 * <ol>
 * <li>自动识别 APN 的代理设置</li>
 * <li>设置 HTTP Header 中的 User Agent</li>
 * <li>Thread-Safe 的连接池</li>
 * </ol>
 *
 * @author zhangxu
 */
public class NetworkUtils {
	/**
	 * 域名解析失败
	 */
	public static final int ERR_DNS_ERROR = -1;
	/**
	 * 连接超时
	 */
	public static final int ERR_CONNECT_TIMEOUT = -2;
	/**
	 * 连接被拒绝
	 */
	public static final int ERR_CONNECT_REFUSED = -3;
	/**
	 * 协议错误，例如代理服务器协议错误等
	 */
	public static final int ERR_PROTOCOL_ERROR = -4;
	/**
	 * 其它连接错误归到此错误码
	 */
	public static final int ERR_CONNECT_ERROR = -5;
	/**
	 * 下载超时
	 */
	public static final int ERR_DOWNLOAD_TIMEOUT = -6;
	/**
	 * 没有收到数据
	 */
	public static final int ERR_DOWNLOAD_EMPTY_BODY = -7;
	/**
	 * HTTP Status Code 不是 2xx
	 */
	public static final int ERR_DOWNLOAD_ERROR = -8;
	/**
	 * 数据错误，例如被运营商劫持
	 */
	public static final int ERR_DOWNLOAD_INVALID_DATA = -9;
	/**
	 * 数据IO错误，例如服务器主动关闭了连接，连接被重置，网卡被断开等等
	 */
	public static final int ERR_DOWNLOAD_IO_ERROR = -10;

	/**
	 * URL 格式不对
	 */
	public static final int ERR_URL_FORMAT_ERROR = -97;
	/**
	 * 被用户终止
	 */
	public static final int ERR_CANCELLED = -98;
	/**
	 * 无法恢复的错误
	 */
	public static final int ERR_FATAL_ERROR = -99;

	// 打点用，统计网络的类型
	public static final int NET_UNKNOWN = 0;
	public static final int NET_WIFI = 1;
	public static final int NET_2G = 2;
	public static final int NET_3G = 3;

	private static final String TAG = "HttpEngine";
	private static final boolean DEBUG = true;
	// getCurrentUserAgent() 里面用
	private static final Object sLockForLocaleSettings = new Object();
	private static final Locale sLocale = Locale.getDefault();
	private static String userAgentString = null;

	/**
	 * 创建普通的 http 链接
	 */
	public static HttpClient createHttpClient(HttpHost proxy) {
		return internalCreateHttpClient(proxy, null, 0, null);
	}

	public static HttpClient createHttpClient(HttpHost proxy, int rtimeout) {
		return internalCreateHttpClient(proxy, null, 0, null, rtimeout);
	}

	/**
	 * 创建 https 加密链接
	 */
	public static HttpClient createHttpsClient(HttpHost proxy, SocketFactory sslSocketFactory) {
		return internalCreateHttpClient(proxy, "https", 443, sslSocketFactory);
	}

	private static HttpClient internalCreateHttpClient(HttpHost proxy, String protocol, int port,
													   SocketFactory socketFactory) {
		return internalCreateHttpClient(proxy, protocol, port,
				socketFactory, 0);
	}

	private static HttpClient internalCreateHttpClient(HttpHost proxy, String protocol, int port,
													   SocketFactory socketFactory, int rtimeout) {
		// 连接超时：10s-->5s
		final int CONNECTION_TIMEOUT = 5 * 1000;
		// 服务器无响应超时: 默认30s, 调用者可以配置该参数。
		int STALE_TIMEOUT = 30 * 1000;
		// 出于手机本身的性能考虑，限制 HttpEngine 发起的最大连接数为 10
		final int MAX_CONNECTIONS = 10;
		// Socket Buffer Size = 8KB
		final int SOCKET_BUF_SIZE = 8192;
		if ((rtimeout / 1000) > 0) {
			STALE_TIMEOUT = rtimeout;
			if (DEBUG) {
				SimpleLog.d(TAG, "Set transfer response timeout(ms):" + STALE_TIMEOUT);
			}
		}
		String useragent = getCurrentUserAgent();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, useragent);
		ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTIONS);
		ConnManagerParams.setTimeout(params, CONNECTION_TIMEOUT); // Android 1.6 中只有调用此方法设置的连接超时才会生效
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		// 服务器如果超过指定的时间间隔没有数据传输，那么就会抛出 SocketTimeoutException
		HttpConnectionParams.setSoTimeout(params, STALE_TIMEOUT);
		// HttpClient 支持连接的 Stale Check，也就是说在发送请求之前判断一下请求是否已经被服务器端关闭了。通过 CMWAP 代理上网的时候有可能会出现这种情况。
		HttpConnectionParams.setStaleCheckingEnabled(params, true);
		HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUF_SIZE);
		ConnRouteParams.setDefaultProxy(params, proxy);
		SchemeRegistry registry = new SchemeRegistry();
		// 20120306 zhangxu 注意无论你是用什么协议连接，在 CMWAP 上，都要先用 http 协议去连接 WAP 代理，所以必须要注册 http 的 scheme
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		// 这里才是我们要额外支持的协议
		if (!TextUtils.isEmpty(protocol)) {
			registry.register(new Scheme(protocol, socketFactory, port));
		}
		return new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);
	}

	public static int queryNetworkType(Context context) {
		if (isWifiConnected(context)) {
			return NET_WIFI;
		}
		TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(
				Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			switch (telephonyManager.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return NET_2G;
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case 8: //NETWORK_TYPE_HSDPA
				case 9: //NETWORK_TYPE_HSUPA
				case 10: // NETWORK_TYPE_HSPA
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case 12: //NETWORK_TYPE_EVDO_B
				case 13: //NETWORK_TYPE_LTE
				case 14: //NETWORK_TYPE_EHRPD
				case 15: //NETWORK_TYPE_HSPAP
					return NET_3G;
				default:
					break;
			}
		}
		return NET_UNKNOWN;
	}

	/**
	 * add for check wifi net connection desc: wifi 开关打开，但是wifi没有连上的话仍然是false
	 * 只有wifi正常网络已经建立,正常获取IP后才返回true
	 */
	public static boolean isWifiConnected(Context c) {
		// 原有方法在android4.0上返回值总是为true，修改为以下方式
		boolean isWifiConnected = false;
		try {
			ConnectivityManager connecManager = (ConnectivityManager) c.getApplicationContext().getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = connecManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mNetworkInfo != null) {
				isWifiConnected = mNetworkInfo.isConnected();
			}
		} catch (Exception ex) {
			//java.lang.NullPointerException
			//   at android.os.Parcel.readException(Parcel.java:1333)
			//   at android.os.Parcel.readException(Parcel.java:1281)
			//   at android.net.IConnectivityManager$Stub$Proxy.getNetworkInfo(IConnectivityManager.java:830)
			//   at android.net.ConnectivityManager.getNetworkInfo(ConnectivityManager.java:387)
		}
		return isWifiConnected;
	}

	/**
	 * 网络是否连接。wifi or gprs
	 */
	public static boolean isNetWorkConnected(Context c) {
		try {
			ConnectivityManager connecManager = (ConnectivityManager) c.getApplicationContext().getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = connecManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mNetworkInfo != null) {
				if (mNetworkInfo.isConnected()) {
					return true;
				}
			}
			NetworkInfo gprs = connecManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (gprs != null) {
				if (gprs.isConnected()) {
					return true;
				}
			}
		} catch (Exception ex) {
			//java.lang.NullPointerException
			//   at android.os.Parcel.readException(Parcel.java:1333)
			//   at android.os.Parcel.readException(Parcel.java:1281)
			//   at android.net.IConnectivityManager$Stub$Proxy.getNetworkInfo(IConnectivityManager.java:830)
			//   at android.net.ConnectivityManager.getNetworkInfo(ConnectivityManager.java:387)
		}
		return false;
	}

	/**
	 * gprs网络是否连接
	 */
	public static boolean isGprsConnected(Context c) {
		try {
			ConnectivityManager connecManager = (ConnectivityManager) c.getApplicationContext().getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo gprs = connecManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (gprs != null) {
				if (gprs.isConnected()) {
					return true;
				}
			}
		} catch (Exception ex) {
			//java.lang.NullPointerException
			//   at android.os.Parcel.readException(Parcel.java:1333)
			//   at android.os.Parcel.readException(Parcel.java:1281)
			//   at android.net.IConnectivityManager$Stub$Proxy.getNetworkInfo(IConnectivityManager.java:830)
			//   at android.net.ConnectivityManager.getNetworkInfo(ConnectivityManager.java:387)
		}
		return false;
	}

	/**
	 * 网络是否连接。所有网络，包括WiFi和数据
	 */
	public static boolean isAllNetWorkConnected(Context c) {
		try {
			ConnectivityManager connecManager = (ConnectivityManager) c
					.getApplicationContext().getSystemService(
							Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = connecManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		} catch (Exception ex) {
		}
		return false;
	}

	/**
	 * 从指定 URL 下载数据到给定的 OutputStream 上。可以用来下载文件，也可以下载到缓冲区内。
	 *
	 * @param httpClient      指定下载任务的 HttpClient
	 * @param url             要下载的 URL。
	 * @param os              给定 OutputStream。如果要下载到文件，可以用 FileOutputStream。
	 * @param progressHandler 进度的handler
	 * @return 一共下载了多少字节。如果出错，则返回小于 0 的值，见 HttpEngine.ERR_xxx 的定义。注意：如果是在下载过程中出错，给定的 OutputStream 可能已经写入了部分数据。
	 * @see NetworkUtils#UrlDownloadToStream(String, OutputStream, int)
	 */
	public static int UrlDownloadToStream(HttpClient httpClient, String url, OutputStream os,
										  IProgressHandler progressHandler) {
		return UrlDownloadToStream(httpClient, url, os, 0, progressHandler);
	}

	/**
	 * 从指定 URL 下载数据到给定的 OutputStream 上。支持断点续传。
	 *
	 * @param httpClient      指定下载任务的 HttpClient
	 * @param url             要下载的 URL。
	 * @param os              给定 OutputStream。如果要下载到文件，可以用 FileOutputStream。
	 * @param resumeFrom      从指定的字节数位置开始下载
	 * @param progressHandler 进度的handler
	 * @param maxSize         数据可能的最大尺寸，如果超过该大小，则可能被运营商劫持了，停止该数据下载。
	 * @param fileSize        数据文件的实际大小。如果返回的数据大小与fileSize不匹配，则可能是CDN缓存的旧数据，停止该数据下载。
	 * @return 一共下载了多少字节。如果出错，则返回小于 0 的值，见 HttpEngine.ERR_xxx 的定义。注意：如果是在下载过程中出错，给定的 OutputStream 可能已经写入了部分数据。
	 */
	public static int UrlDownloadToStream(HttpClient httpClient, String url, OutputStream os, long resumeFrom,
										  IProgressHandler progressHandler, long maxSize, long fileSize) {
		final HttpGet httpGet;
		try {
			httpGet = new HttpGet(url);
		} catch (Exception e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Failed to create HttpGet from [" + url + "]" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_URL_FORMAT_ERROR, e.getLocalizedMessage());
			}
			return ERR_URL_FORMAT_ERROR;
		}
		if (resumeFrom > 0) {
			httpGet.addHeader("Range", String.format("bytes=%d-", resumeFrom));
		}
		int downloadBytes = internalRequest(httpClient, httpGet, os, progressHandler, maxSize, fileSize);
		return downloadBytes;
	}

	public static int UrlDownloadToStream(HttpClient httpClient, String url, OutputStream os, long resumeFrom,
										  IProgressHandler progressHandler) {
		return UrlDownloadToStream(httpClient, url, os, resumeFrom, progressHandler, 0, 0);
	}

	/**
	 * 向指定的 URL POST 数据，然后将服务器返回的数据写入给定的 OutputStream。
	 *
	 * @param httpClient      指定下载任务的 HttpClient
	 * @param url             要请求的 URL。
	 * @param requestBuffer   向服务器发送的数据
	 * @param os              给定 OutputStream。如果要下载到文件，可以用 FileOutputStream。
	 * @param progressHandler 进度的handler
	 * @return 一共下载了多少字节。如果出错，则返回小于 0 的值，见 HttpEngine.ERR_xxx 的定义。注意：如果是在下载过程中出错，给定的 OutputStream
	 * 可能已经写入了部分数据，不过返回值总是小于0的数字。
	 */
	public static int PostForm(HttpClient httpClient, String url, byte[] requestBuffer, OutputStream os,
							   IProgressHandler progressHandler) {
		HttpPost httpPost = new HttpPost(url);
		ByteArrayEntity entity = new ByteArrayEntity(requestBuffer);
		entity.setContentType("application/x-www-form-urlencoded");
		httpPost.setEntity(entity);
		if (progressHandler != null) {
			progressHandler.onRequest(httpPost);
		}
		int downloadBytes = internalRequest(httpClient, httpPost, os, progressHandler);
		return downloadBytes;
	}

	public static int PostFormForUpload(HttpClient httpClient, String url, byte[] requestBuffer, OutputStream os,
							   IProgressHandler progressHandler) {
		HttpPost httpPost = new HttpPost(url);
		ByteArrayEntity entity = new ByteArrayEntity(requestBuffer);
		entity.setContentType("text/plain; charset=utf-8");
		httpPost.setEntity(entity);
		if (progressHandler != null) {
			progressHandler.onRequest(httpPost);
		}
		return internalRequest(httpClient, httpPost, os, progressHandler);
	}

	public static int PostStream(HttpClient httpClient, String url, InputStream inputStream, OutputStream os,
								 IProgressHandler progressHandler) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		InputStreamEntity entity = new InputStreamEntity(inputStream, inputStream.available());
		entity.setContentType("application/x-www-form-urlencoded");
		httpPost.setEntity(entity);
		if (progressHandler != null) {
			progressHandler.onRequest(httpPost);
		}
		int downloadBytes = internalRequest(httpClient, httpPost, os, progressHandler);
		return downloadBytes;
	}

	public static int PostStreamWithRange(HttpClient httpClient, String url, InputStream inputStream, OutputStream os,
										  IProgressHandler progressHandler, long rangeFrom, long rangeTo) throws IOException {
		long availLength = inputStream.available();
		if (availLength <= rangeFrom) {
			throw new EOFException();
		}
		int length = Math.min((int) (availLength - rangeFrom), (int) (rangeTo - rangeFrom));
		int ret = 0;
		if (length > 0) {
			HttpPost httpPost = null;
			try {
				httpPost = new HttpPost(url);
			} catch (Exception ex) {
				if (DEBUG) {
					SimpleLog.i(TAG, "Failed to create httpPost from [" + url + "]" + ex);
				}
			}
			if (httpPost != null) {
				inputStream.skip(rangeFrom);
				InputStreamEntity entity = new InputStreamEntity(inputStream, length);
				entity.setContentType("application/x-www-form-urlencoded");
				httpPost.setEntity(entity);
				if (progressHandler != null) {
					progressHandler.onRequest(httpPost);
				}
				ret = internalRequest(httpClient, httpPost, os, progressHandler);
				if (ret > 0) {
					ret = 0;
				}
			} else {
				ret = ERR_URL_FORMAT_ERROR;
			}
		}
		return ret;
	}

	/**
	 * @see NetworkUtils#UrlDownloadToStream(HttpClient, String, OutputStream, long, IProgressHandler)
	 * @see NetworkUtils#PostForm(HttpClient, String, byte[], OutputStream, IProgressHandler)
	 */
	private static int internalRequest(HttpClient httpClient, HttpUriRequest request, OutputStream os,
									   IProgressHandler progressHandler) {
		return internalRequest(httpClient, request, os, progressHandler, 0, 0);
	}

	@SuppressWarnings("resource")
	private static int internalRequest(HttpClient httpClient, HttpUriRequest request, OutputStream os,
									   IProgressHandler progressHandler, long maxSize, long fileSize) {
		if (DEBUG) {
			SimpleLog.i(TAG, "HTTP Request -> " + request.getURI().toString() + "; maxSize=" + maxSize + "; fileSize="
					+ fileSize);
		}
		// 20150716 debug 时更改请求超时时长，方便测试人员测试
		if (AppEnv.DEBUG) {
			// 请求超时
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
			// 读取超时
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
		}
		// 20130312 zhangxu 改进：增加击穿缓存的 HTTP 头
		request.setHeader("Accept", "*/*");
		request.setHeader("Cache-Control", "no-cache");
		request.setHeader("Pragma", "no-cache");
		HttpResponse response = null;
		try {
			response = httpClient.execute(request);
		} catch (UnknownHostException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect UnknownHostException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_DNS_ERROR, e.getLocalizedMessage());
			}
			return ERR_DNS_ERROR;
		} catch (ConnectTimeoutException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect ConnectTimeoutException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_CONNECT_TIMEOUT, e.getLocalizedMessage());
			}
			return ERR_CONNECT_TIMEOUT;
		} catch (SocketTimeoutException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect SocketTimeoutException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_CONNECT_TIMEOUT, e.getLocalizedMessage());
			}
			return ERR_CONNECT_TIMEOUT;
		} catch (HttpHostConnectException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect HttpHostConnectException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_CONNECT_REFUSED, e.getLocalizedMessage());
			}
			return ERR_CONNECT_REFUSED;
		} catch (ClientProtocolException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect ClientProtocolException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_PROTOCOL_ERROR, e.getLocalizedMessage());
			}
			return ERR_PROTOCOL_ERROR;
		} catch (IOException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect IOException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_CONNECT_ERROR, e.getLocalizedMessage());
			}
			return ERR_CONNECT_ERROR;
		} catch (Exception e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Connect Exception" + e);
			}
			// 出现这个异常表明程序中出现了设计上没有考虑到的问题
			if (progressHandler != null) {
				progressHandler.onError(ERR_FATAL_ERROR, e.getLocalizedMessage());
			}
			return ERR_FATAL_ERROR;
		}
		HttpEntity entity = null;
		InputStream is = null;
		int downloadedBytes = 0;
		boolean isCanceled = false;
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine != null) {
				if (progressHandler != null) {
					progressHandler.onServerResponse(response);
				}
				int statusCode = statusLine.getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					entity = response.getEntity();
					is = entity.getContent();
					long total = entity.getContentLength();
					/*add by caizhiping 2012/10/18 由于可能被运营商劫持，所以这里判断一下返回数据是否超过指定的最大值*/
					if (DEBUG) {
						SimpleLog.i(TAG, "HTTP Response getContentLength=" + total);
					}
					if (maxSize > 0 && total > maxSize) {
						if (progressHandler != null) {
							progressHandler.onError(ERR_DOWNLOAD_INVALID_DATA, "invalidate data");
						}
						return ERR_DOWNLOAD_INVALID_DATA;
					}
                    /*end by caizhiping 2012/10/18*/
                    /*add by caizhiping 2012/11/21 由于CDN缓存的问题，可能下载的数据是旧数据。这里先匹配大小 */
					if (fileSize > 0 && total > 0 && total != fileSize) {
						if (progressHandler != null) {
							progressHandler.onError(ERR_DOWNLOAD_INVALID_DATA, "invalidate data");
						}
						return ERR_DOWNLOAD_INVALID_DATA;
					}
                    /*end by caizhiping 2012/11/21*/
					if (is != null) {
						byte[] buffer = new byte[4096];
						int read = 0;
						while ((read = is.read(buffer)) > 0) {
							os.write(buffer, 0, read);
							downloadedBytes += read;
							if (progressHandler != null) {
								progressHandler.onProgress(downloadedBytes, total);
							}
                            /*add by caizhiping 2012/10/26 由于可能被运营商劫持，所以这里判断一下已读取数据是否超过指定的最大值*/
							if (maxSize > 0 && downloadedBytes > maxSize) {
								if (progressHandler != null) {
									progressHandler.onError(ERR_DOWNLOAD_INVALID_DATA, "invalidate data");
								}
								return ERR_DOWNLOAD_INVALID_DATA;
							}
                            /*end by caizhiping 2012/10/26*/
                            /*add by caizhiping 2012/11/21 由于CDN缓存的问题，可能下载的数据是旧数据。这里先比较大小*/
							if (fileSize > 0 && downloadedBytes > fileSize) {
								if (DEBUG) {
									SimpleLog.i(TAG, "HTTP Response downloadedBytes=" + downloadedBytes);
								}
								if (progressHandler != null) {
									progressHandler.onError(ERR_DOWNLOAD_INVALID_DATA, "invalidate data");
								}
								return ERR_DOWNLOAD_INVALID_DATA;
							}
                            /*end by caizhiping 2012/11/21*/
						}
                        /*add by caizhiping 2012/11/21 由于CDN缓存的问题，可能下载的数据是旧数据。下载完毕后再匹配一次文件大小*/
						if (DEBUG) {
							SimpleLog.i(TAG, "HTTP Response downloadedBytes=" + downloadedBytes);
						}
						if (fileSize > 0 && downloadedBytes != fileSize) {
							if (progressHandler != null) {
								progressHandler.onError(ERR_DOWNLOAD_INVALID_DATA, "invalidate data");
							}
							return ERR_DOWNLOAD_INVALID_DATA;
						}
                        /*end by caizhiping 2012/11/21*/
					} else {
						if (progressHandler != null) {
							progressHandler.onError(ERR_DOWNLOAD_EMPTY_BODY, "Empty body");
						}
						return ERR_DOWNLOAD_EMPTY_BODY;
					}
				} else {
					if (DEBUG) {
						SimpleLog.w(TAG, "statusLine=" + statusLine.toString());
					}
					if (progressHandler != null) {
						progressHandler.onError(ERR_DOWNLOAD_ERROR, statusLine.toString());
					}
					return ERR_DOWNLOAD_ERROR;
				}
			} else {
				if (progressHandler != null) {
					progressHandler.onError(ERR_DOWNLOAD_EMPTY_BODY, "Empty header");
				}
				return ERR_DOWNLOAD_EMPTY_BODY;
			}
		} catch (InterruptedException e) {
			if (DEBUG) {
				SimpleLog.w(TAG, "Cancelled by thread.");
			}
            /* add by caizhiping 2013/11/13
             * 在某些机型(例如小米2)使用关闭InputStream的方式会阻塞住，实际上仍会继续走流量
             * 在查阅资料后使用HttpUriRequest.abort()的方法来取消下载
             */
			try {
				request.abort();
				isCanceled = true;
			} catch (Exception ex) {
			}
            /* end by caizhping 2013/11/13 */
			return ERR_CANCELLED;
		} catch (SocketTimeoutException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Download SocketTimeoutException" + e);
			}
			if (progressHandler != null) {
				progressHandler.onError(ERR_DOWNLOAD_TIMEOUT, e.getLocalizedMessage());
			}
			return ERR_DOWNLOAD_TIMEOUT;
		} catch (IOException e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Download IOException" + e);
			}
			// 引起 IO 异常的原因很多，例如服务器主动关闭了连接，连接被重置，网卡被断开等等。为了方便定位问题，出现 IO 异常时把信息提供给用户反馈
			if (progressHandler != null) {
				progressHandler.onError(ERR_DOWNLOAD_IO_ERROR, e.getLocalizedMessage());
			}
			return ERR_DOWNLOAD_IO_ERROR;
		} catch (Exception e) {
			if (DEBUG) {
				SimpleLog.i(TAG, "Download Exception" + e);
			}
			// 出现这个异常表明程序中出现了设计上没有考虑到的问题，为了便于工程师定位问题，我们要将异常的信息提供给用户反馈
			if (progressHandler != null) {
				progressHandler.onError(ERR_FATAL_ERROR, e.getLocalizedMessage());
			}
			return ERR_FATAL_ERROR;
		} finally {
			if (!isCanceled) {
				// 释放连接
				try {
					if (is != null) {
						is.close();
						is = null;
					}
				} catch (Exception ex) {
				}
				try {
					if (entity != null) {
						entity.consumeContent();
						entity = null;
					}
				} catch (Exception ex) {
				}
			}
		}
		return downloadedBytes;
	}

	/**
	 * 返回当前的 APN。
	 *
	 * @return 统一返回小写名称，例如 cmwap。
	 */
	public static String getApnName(Context context) {
		final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
		String result = "UNKNOWN";
		Cursor c = null;
		try {
			c = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
			if (c != null) {
				if (c.moveToFirst()) {
					String user = c.getString(c.getColumnIndex("apn"));
					if (!TextUtils.isEmpty(user)) {
						result = user.toLowerCase();
					}
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				SimpleLog.e(TAG, "Failed to getApnName():" + e);
			}
		} finally {
			FileUtils.closeCursor(c);
		}
		if (DEBUG) {
			SimpleLog.i(TAG, "APN = " + result);
		}
		return result;
	}

	/**
	 * 读取系统默认配置的代理
	 */
	@SuppressWarnings("deprecation")
	public static HttpHost getApnProxy(Context context) {
		String proxyHost = android.net.Proxy.getDefaultHost();
		int proxyPort = android.net.Proxy.getDefaultPort();
		if ((proxyHost != null) && (proxyHost.length() > 0)) {
			return new HttpHost(proxyHost, proxyPort);
		} else {
			return null;
		}
	}

	/**
	 * 生成 User Agent 字符串。部分代码参考了 android.webkit.WebSettings。
	 *
	 * @return Current UserAgent String.
	 */
	public static synchronized String getCurrentUserAgent() {
		if (userAgentString != null) {
			return userAgentString;
		}
		Locale locale;
		synchronized (sLockForLocaleSettings) {
			locale = sLocale;
		}
		StringBuffer buffer = new StringBuffer();
		// 常见的 MODEL 字符串有：
		// Nexus One
		// GT-I9000
		// HTC Desire
		final String model = Build.MODEL;
		if (model.length() > 0) {
			buffer.append(model);
		}
		buffer.append("; ");
		// VERSION 字符串一般是:
		// 1.6
		// 2.2
		final String version = Build.VERSION.RELEASE;
		if (version.length() > 0) {
			buffer.append(version);
		} else {
			// default to "1.0"
			buffer.append("1.0");
		}
		buffer.append("; ");
		// 常见的 DISPLAY 字符串有：
		// FRF50
		// ECLAIR
		// ERE27
		// CUPCAKE.eng.root.20090817.155710（来自 hiapk 的某个 ROM）
		final String display = Build.DISPLAY;
		if (display.length() > 0) {
			final int MAX_LENGTH = 48;
			if (display.length() > MAX_LENGTH) {
				buffer.append(display.substring(0, MAX_LENGTH));
				buffer.append("...");
			} else {
				buffer.append(display);
			}
		}
		buffer.append("; ");
		// zh-cn
		final String language = locale.getLanguage();
		if (language != null) {
			buffer.append(language.toLowerCase());
			final String country = locale.getCountry();
			if (country != null) {
				buffer.append("-");
				buffer.append(country.toLowerCase());
			}
		} else {
			// default to "en"
			buffer.append("en");
		}
		// BUG FIX: 有的 ROM，机型里面带了 \r，导致 User Agent 添加到 HTTP Header 之后破坏了 HTTP Header
		// 改进方案：过滤所有的非字符
		String strValue = buffer.toString();
		StringBuilder filter = new StringBuilder();
		for (int i = 0; i < strValue.length(); i++) {
			char ch = strValue.charAt(i);
			if (ch >= ' ' && ch <= '~') {
				filter.append(ch);
			}
		}
		userAgentString = filter.toString();
		return userAgentString;
	}

	/**
	 * <p>
	 * 考虑到有的用户不会截屏，只通过 IM 或者电话向客服报告问题，所以需要有一个比较简单的方式能让用户把问题说清楚，包括当前网络的配置。这里我们把网络的配置用数字编码的形式 dump 出来，用户只要念数字就可以了。
	 * </p>
	 * <h1>编码规则：</h1>从左往右数，<br/>
	 * 网络类型，WIFI = 0, CMWAP = 1, CMNET = 2, UNIWAP = 3, UNINET = 4, 3GWAP = 5, 3GNET = 6, CTWAP = 7, CTNET = 8，其它 = 9<br/>
	 * 如果网络类型 = 9，那么紧接着会有 APN 的名字，如果有代理服务器，那么后面还会有代理服务器的 IP 和端口，例如
	 * <p/>
	 * <pre>
	 * 9 CTMMS 10.0.0.200 80
	 * </pre>
	 *
	 * @return 编码后的字符串
	 */
	@SuppressWarnings("deprecation")
	public static String dumpNetworkConfig(Context c) {
		int apnCode = 9;
		String apn = null;
		do {
			// WiFi
			if (isWifiConnected(c)) {
				apnCode = 0;
				break;
			}
			// GPRS/3G
			apn = NetworkUtils.getApnName(c);
			if (apn.equals("cmwap")) {
				apnCode = 1;
				break;
			}
			if (apn.equals("cmnet")) {
				apnCode = 2;
				break;
			}
			if (apn.equals("uniwap")) {
				apnCode = 3;
				break;
			}
			if (apn.equals("uninet")) {
				apnCode = 4;
				break;
			}
			if (apn.equals("3gwap")) {
				apnCode = 5;
				break;
			}
			if (apn.equals("3gnet")) {
				apnCode = 6;
				break;
			}
			if (apn.equals("ctwap")) {
				apnCode = 7;
				break;
			}
			if (apn.equals("ctnet")) {
				apnCode = 8;
				break;
			}
		} while (false);
		StringBuilder result = new StringBuilder();
		result.append(apnCode);
		result.append(" ");
		if (apnCode > 8 && apn != null) {
			result.append(apn);
		}
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (proxyHost != null) {
			result.append(" ");
			result.append(proxyHost);
			result.append(" ");
			result.append(android.net.Proxy.getDefaultPort());
		}
		return result.toString();
	}

	/**
	 * 返回当前接入点的代理设置。如果没有设置代理，则返回 null。如果当前连接为 WiFi，也返回 null。
	 */
	public static HttpHost getCurrentProxy(Context c) {
		// WiFi 无代理设置
		if (!isWifiConnected(c)) {
			return getApnProxy(c);
		}
		return null;
	}

	public static void downloadBm(String url, IDownloadImgCallBack callBack) {
		try {
			byte[] data = getImage(url);
			if (data != null) {
				Bitmap bitmap = null;
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// bitmap
				FileUtils.saveFile(bitmap, VCStoragerManager.getInstance().getTmpPath());
				callBack.onDownloadSuccess(data);
			}else {
				callBack.onDownloadFailed("data is null");
			}
		} catch (Exception e) {
			callBack.onDownloadFailed(e.toString());
			e.printStackTrace();
		}
	}

	public static byte[] getImage(String path) throws Exception{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		InputStream inStream = conn.getInputStream();
		if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
			return readStream(inStream);
		}
		return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while( (len=inStream.read(buffer)) != -1){
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inStream.close();
		return outStream.toByteArray();
	}

	public InputStream getImageStream(String path) throws Exception{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
			return conn.getInputStream();
		}
		return null;
	}


	/**
	 * 将错误码翻译成用户可以理解的文字。如果是未定义的错误，则返回 null.
	 */
//    static public String getErrorCodeDescription(int code) {
//        switch (code) {
//        case NetworkUtils.ERR_DNS_ERROR:
//            return "域名解析失败";
//        case NetworkUtils.ERR_CONNECT_TIMEOUT:
//            return "连接超时";
//        case NetworkUtils.ERR_CONNECT_REFUSED:
//            return "服务器拒绝连接";
//        case NetworkUtils.ERR_PROTOCOL_ERROR:
//            return "服务器协议错误";
//        case NetworkUtils.ERR_CONNECT_ERROR:
//            return "连接错误";
//        case NetworkUtils.ERR_DOWNLOAD_TIMEOUT:
//            return "下载超时";
//        case NetworkUtils.ERR_DOWNLOAD_EMPTY_BODY:
//            return "服务器数据有误";
//        case NetworkUtils.ERR_DOWNLOAD_ERROR:
//            return "下载错误";
//        case NetworkUtils.ERR_URL_FORMAT_ERROR:
//            return "地址错误";
//        case NetworkUtils.ERR_DOWNLOAD_INVALID_DATA:
//            return "数据错误";
//        case NetworkUtils.ERR_DOWNLOAD_IO_ERROR:
//            return "下载IO错误";
//        default:
//            return null;
//        }
//    }
	public interface IProgressHandler {

		void onRequest(HttpEntityEnclosingRequest request);

		void onServerResponse(HttpResponse response);

		void onProgress(long progress, long total) throws InterruptedException;

		void onError(int errorCode, String errorMessage);
	}

	/**
	 * gzip上传文件，回调在这里执行
	 * @param file  传入需要上传的文件
	 * @param isDelSuc  上传成功以后是否删除文件
	 */
	public static void uploadUrlFileWithDel(final File file, final boolean isDelSuc) {
		final RequestBody requestFile =
				RequestBody.create(MediaType.parse("application/x-gzip"), file);

		MultipartBody.Part body =
				MultipartBody.Part.createFormData("aFile", file.getName(), requestFile);

		String descriptionString = "This is a description";
		RequestBody description =
				RequestBody.create(
						MediaType.parse("multipart/form-data"), descriptionString);
		Api.getInstance().uploadUrl(description, body).enqueue(new retrofit2.Callback<ResponseBody>() {
			@Override
			public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				if (response.isSuccessful() && isDelSuc) {
					FileUtils.deleteOnlyFile(file);
				}
			}

			@Override
			public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

			}
		});
	}

	public static void uploadSkFileWithDel(final File file, final boolean isDelSuc) {
		final RequestBody requestFile =
				RequestBody.create(MediaType.parse("*/*"), file);

		MultipartBody.Part body =
				MultipartBody.Part.createFormData("aFile", file.getName(), requestFile);

		String descriptionString = "This is a description";
		RequestBody description =
				RequestBody.create(
						MediaType.parse("multipart/form-data"), descriptionString);
		Api.getInstance().uploadSk(description, body).enqueue(new retrofit2.Callback<ResponseBody>() {
			@Override
			public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				if (response.isSuccessful() && isDelSuc) {
					FileUtils.deleteOnlyFile(file);
				}
			}

			@Override
			public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

			}
		});
	}
}