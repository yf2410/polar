package com.polar.browser.download_refactor.util;

import android.annotation.SuppressLint;

import com.polar.browser.utils.SimpleLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class FileUtil {
	private static final String TAG = "FileUtil";
	
	@SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sSDF = new SimpleDateFormat("\r\n\r\nyyyy-MM-dd HH:mm:ss.Z : ");
	
	final static private HashMap<String, String> MIMETYPE_SET;
	
	static {
		MIMETYPE_SET = new HashMap<String, String>();
		MIMETYPE_SET.put(".3gp", "video/3gpp");
		MIMETYPE_SET.put(".apk", "application/vnd.android.package-archive");
		MIMETYPE_SET.put(".asf", "video/x-ms-asf");
		MIMETYPE_SET.put(".avi", "video/x-msvideo");
		MIMETYPE_SET.put(".bin", "application/octet-stream");
		MIMETYPE_SET.put(".bmp", "image/bmp");
		MIMETYPE_SET.put(".c", "text/plain");
		MIMETYPE_SET.put(".class", "application/octet-stream");
		MIMETYPE_SET.put(".conf", "text/plain");
		MIMETYPE_SET.put(".cpp", "text/plain");
		MIMETYPE_SET.put(".doc", "application/msword");
		MIMETYPE_SET.put(".exe", "application/octet-stream");
		MIMETYPE_SET.put(".gif", "image/gif");
		MIMETYPE_SET.put(".gtar", "application/x-gtar");
		MIMETYPE_SET.put(".gz", "application/x-gzip");
		MIMETYPE_SET.put(".h", "text/plain");
		MIMETYPE_SET.put(".htm", "text/html");
		MIMETYPE_SET.put(".html", "text/html");
		MIMETYPE_SET.put(".jar", "application/java-archive");
		MIMETYPE_SET.put(".java", "text/plain");
		MIMETYPE_SET.put(".jpeg", "image/jpeg");
		MIMETYPE_SET.put(".jpg", "image/jpeg");
		MIMETYPE_SET.put(".js", "application/x-javascript");
		MIMETYPE_SET.put(".log", "text/plain");
		MIMETYPE_SET.put(".m3u", "audio/x-mpegurl");
		MIMETYPE_SET.put(".m4a", "audio/mp4a-latm");
		MIMETYPE_SET.put(".m4b", "audio/mp4a-latm");
		MIMETYPE_SET.put(".m4p", "audio/mp4a-latm");
		MIMETYPE_SET.put(".m4u", "video/vnd.mpegurl");
		MIMETYPE_SET.put(".m4v", "video/x-m4v");
		MIMETYPE_SET.put(".mov", "video/quicktime");
		MIMETYPE_SET.put(".mp2", "audio/x-mpeg");
		MIMETYPE_SET.put(".mp3", "audio/x-mpeg");
		MIMETYPE_SET.put(".mp4", "video/mp4");
		MIMETYPE_SET.put(".mpc", "application/vnd.mpohun.certificate");
		MIMETYPE_SET.put(".mpe", "video/mpeg");
		MIMETYPE_SET.put(".mpeg", "video/mpeg");
		MIMETYPE_SET.put(".mpg", "video/mpeg");
	    MIMETYPE_SET.put(".mpg4", "video/mp4");
		MIMETYPE_SET.put(".mpga", "audio/mpeg");
		MIMETYPE_SET.put(".msg", "application/vnd.ms-outlook");
		MIMETYPE_SET.put(".ogg", "audio/ogg");
		MIMETYPE_SET.put(".pdf", "application/pdf");
		MIMETYPE_SET.put(".png", "image/png");
		MIMETYPE_SET.put(".pps", "application/vnd.ms-powerpoint");
		MIMETYPE_SET.put(".ppt", "application/vnd.ms-powerpoint");
		MIMETYPE_SET.put(".prop", "text/plain");
		MIMETYPE_SET.put(".rar", "application/x-rar-compressed");
		MIMETYPE_SET.put(".rc", "text/plain");
		MIMETYPE_SET.put(".rmvb", "video/x-pn-realaudio");
		MIMETYPE_SET.put(".rtf", "application/rtf");
		MIMETYPE_SET.put(".sh", "text/plain");
		MIMETYPE_SET.put(".tar", "application/x-tar");
		MIMETYPE_SET.put(".tgz", "application/x-compressed");
		MIMETYPE_SET.put(".txt", "text/plain");
		MIMETYPE_SET.put(".wav", "audio/x-wav");
		MIMETYPE_SET.put(".wma", "audio/x-ms-wma");
		MIMETYPE_SET.put(".wmv", "audio/x-ms-wmv");
		MIMETYPE_SET.put(".wps", "application/vnd.ms-works");
		//MIMETYPE_SET.put(".xml", "text/xml");
		MIMETYPE_SET.put(".xml", "text/plain");
		MIMETYPE_SET.put(".z", "application/x-compress");
		MIMETYPE_SET.put(".zip", "application/zip");
		MIMETYPE_SET.put("", "*/*");
		MIMETYPE_SET.put(".amr", "audio/amr");
	}
	
	
    public static String normalizeMimeType(String type) {
        if (type == null) {
            return null;
        }

        type = type.trim().toLowerCase(Locale.US);

        final int semicolonIndex = type.indexOf(';');
        if (semicolonIndex != -1) {
            type = type.substring(0, semicolonIndex);
        }
        return type;
    }
	
// TODO Remove unused code found by UCDetector
// 	public static final String getFileExtension(File f) {
// 		if (f != null) {
// 			String fName = f.getName();
// 			int dotPosition = fName.lastIndexOf(".");
// 			String ext = "";
// 			if (dotPosition >= 0) {
// 				ext = fName.substring(dotPosition).toLowerCase();
// 			}
// 			return ext;
// 		}
// 		return "";
// 	}
	
// TODO Remove unused code found by UCDetector
// 	public static final String getMIMEType(File f) {
// 		String type = "";
// 		
// 		String ext = getFileExtension(f);
// 		if (MIMETYPE_SET.containsKey(ext)) {
// 			type = MIMETYPE_SET.get(ext);
// 		} else {	// 如果不在预定义列表中，则由用户选择
// 			type = "*/*";
// 		}
// 		
// 		return type;
// 	}
	
// TODO Remove unused code found by UCDetector
// 	public static final String getMIMEType(String filename) {
// 		String type = "";
// 		
// 		int dotPosition = filename.lastIndexOf(".");
// 		String ext = "";
// 		if (dotPosition >= 0) {
// 			ext = filename.substring(dotPosition).toLowerCase();
// 		}
// 		if (MIMETYPE_SET.containsKey(ext)) {
// 			type = MIMETYPE_SET.get(ext);
// 		} else {	
// 			type = "*/*";
// 		}
// 		return type;
// 	}
	
	public static boolean saveFile(InputStream stream, File target) {
		boolean bSuccess = true;
		FileOutputStream os = null;
		File temp_f = new File(target.getParentFile(), target.getName() + ".tmp");
		try {
	        os = new FileOutputStream(target);
	        byte[] buffer = new byte[8 * 1024];
			int readedLength = stream.read(buffer);
			while (readedLength >= 0) {
	        	os.write(buffer, 0, readedLength);
        		readedLength = stream.read(buffer);
			}
			temp_f.renameTo(target);
		} catch (FileNotFoundException e) {
			bSuccess = false;
			SimpleLog.e(e);
		} catch (IOException e) {
			bSuccess = false;
			SimpleLog.e(e);
		} catch (Exception e) {
			bSuccess = false;
			SimpleLog.e(e);
		} finally {
			if (os != null) {
				try { 
					os.close();
				} catch (IOException e) {
				}
				os = null;
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
				stream = null;
			}
		}
		
		if(!bSuccess) {
			temp_f.delete();
		}
		return bSuccess;
	}
	
	public static String readFile(File file){
	    String content = "";
        File dir = file;
        if(!dir.exists())
            return "";
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            is = new FileInputStream(dir);
            bis = new BufferedInputStream(is);  
            int count = bis.available();
            byte[] buffer = new byte[count];
            bis.read(buffer);
            content = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(is!=null)
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
            if(bis!=null)
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return content;
	}
	
	
// TODO Remove unused code found by UCDetector
// 	/**
// 	 * 追加内容至文件末尾
// 	 * @param content 需要追加的文件内容
// 	 * @param target 目标文件全路径
// 	 * @return true if success, else return false
// 	 */
// 	public static boolean appendContent2File(String content, String target) {
// 		boolean bSuccess = false;
// 		BufferedWriter out = null;
//         try {
//             out = new BufferedWriter(new OutputStreamWriter(
//                     new FileOutputStream(target, true)));
//             out.write(sSDF.format(new Date()));
//             out.write(content);
//             bSuccess = true;
//         } catch (Exception e) {
//         	KLog.e(TAG, e.getMessage(), e);
//         } finally {
//             try {
//                 if (out != null) out.close();
//             } catch (IOException e) {
//                 KLog.e(TAG, e.getMessage(), e);
//             } catch (Exception e) {
//             	KLog.e(TAG, e.getMessage(), e);
//             }
//         }
// 		return bSuccess;
// 	}
	
// TODO Remove unused code found by UCDetector
//     public static String readLine(File file) throws IOException {
//     	String output = null;
//     	BufferedReader input = new BufferedReader (new FileReader(file));  
//         StringBuffer buffer = new StringBuffer();  
//         String text;  
//               
//         while((text = input.readLine()) != null) {  
//             buffer.append(text + "\n");  
//         }
//         
//         input.close();
//         input = null;
//         
//         output = buffer.toString();   
//         return output;
//     }
    
// TODO Remove unused code found by UCDetector
// 	public static boolean delete(File file) {
// 		if (file == null) return true;
// 		if (!file.exists()) return true;
// 		
// 		boolean flag = true;
// 		if (file.isDirectory()) {
// 			flag = deleteFolder(file);
// 		}
// 		flag = file.delete();
// 		
// 		return flag;
// 	}
	
	public static boolean deleteFolder(File folder) {
		if (!folder.exists()) return true;
		if (!folder.isDirectory()) return false;
		
		boolean flag = true;
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				flag = deleteFolder(f);
			}
			flag = f.delete();
		}
		
		return flag;
	}
}