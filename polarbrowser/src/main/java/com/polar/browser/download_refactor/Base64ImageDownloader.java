
package com.polar.browser.download_refactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.DownloadException.ExceptionCode;
import com.polar.browser.download_refactor.dialog.DownloadBase64ImgDialog;
import com.polar.browser.download_refactor.util.BackgroundHandler;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.manager.ThreadManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Base64ImageDownloader {

    private static final String BASE64URL = "http://base64/";

    public static boolean isBase64Image(String imgStr) {
        if (!TextUtils.isEmpty(imgStr)) {
            if (imgStr.startsWith("data:") && imgStr.contains("base64")) {
                return true;
            }
            if (imgStr.startsWith(BASE64URL)) {
                return true;
            }
        }

        return false;
    }

//    static class Base64ImageSavingDialog extends SmartDialog {
//
//        public String mMimeType;
//        public String mReferer;
//
//        Base64ImageSavingDialog(Context context) {
//            super(context);
//        }
//    }

    public static void checkAndSaveImage(Context context, final String imgData, final String mimeType, final String referer) {
        if (TextUtils.isEmpty(imgData)) {
            return;
        }

        // 暂时测试，直接下载，不弹框
     // bugfix(21061): 无法从图片数据中解析出正常文件名，因此直接以
        // 当前时间戳为名，加上正常扩展名。暂时如此，有空再整理。
        final String fileName = getImageName(imgData);
//        generateBase64Image(imgData, fileName, mimeType, referer);
        
        // TODO 弹出弹框下载图片
        Intent intent = new Intent(context, DownloadBase64ImgDialog.class);
        intent.putExtra("filename", fileName);
        intent.putExtra("imgData", imgData);
        intent.putExtra("mimeType", mimeType);
        intent.putExtra("referer", referer);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context instanceof ContextWrapper) {
            ((ContextWrapper) context).startActivity(intent);
        }
        /*
        final Context context = KApplication.getInstance().getTopActivity();

        Base64ImageSavingDialog smartDialog = new Base64ImageSavingDialog(context);
        smartDialog.mMimeType = mimeType;
        smartDialog.mReferer = referer;

        String[] buttonText = new String[] {
                context.getString(R.string.dialog_btn_download),
                context.getString(R.string.cancel)
        };

        // bugfix(21061): 无法从图片数据中解析出正常文件名，因此直接以
        // 当前时间戳为名，加上正常扩展名。暂时如此，有空再整理。
        final String fileName = getImageName(imgData);
        String saveFileMsg =
                String.format(context.getString(R.string.s_download_hint_ask),
                        fileName);
        smartDialog.setView(SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG,
                saveFileMsg,
                null,
                buttonText);

        smartDialog.setKSmartDialogListener(new KSmartDialogListener() {
            @Override
            public void onDialogClosed(int whichButton, boolean[] checkState) {
                if (whichButton == SmartDialog.BUTTON_POSITIVE) {
                    generateBase64Image(imgData, fileName, mimeType, referer);
                }
            }
        });
        smartDialog.dialogEnterAnimation();
        */
    }
    
    @SuppressLint("SimpleDateFormat") private static String getImageName(final String imgStr) {
        int endIndex = imgStr.indexOf(";");
        String mimeType = imgStr.substring(5, endIndex);
        String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType);
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String fileName = simpleDateFormat.format(new Date()) + "." + extension;
        String imgDir = PathResolver.getDownloadFileDir(null);
        File temPathFile = new File(imgDir, fileName);
        String imgFilePath = temPathFile.getAbsolutePath();
        int nameCount = getImageExtenCount(imgFilePath, extension);
        return getFileName(fileName, extension, nameCount);
    }

    public static void generateBase64Image(final String imgStr, final String fileName,
            final String mimeType, final String referer) {// 对字节数组字符串进行Base64解码并生成图片
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(imgStr)) // 图像数据为空
                    return;

                int endIndex = imgStr.indexOf(";");
                String mimeType = imgStr.substring(5, endIndex);
                endIndex = imgStr.indexOf(",");
                String data = imgStr.substring(endIndex + 1);

                try
                {
                    // Base64解码
                    // byte[] b = Base64Decoder.decodeToBytes(data);
                    byte[] b = Base64.decode(data, 0);
                    for (int i = 0; i < b.length; ++i)
                    {
                        if (b[i] < 0)
                        {// 调整异常数据
                            b[i] += 256;
                        }
                    }

                    // 生成jpeg图片
                    String imgDir = PathResolver.getDownloadFileDir(null);
                    imgDir = tryToCreateTaskDir(imgDir, fileName);
                    File temPathFile = new File(imgDir, fileName);
                    String imgFilePath = temPathFile.getAbsolutePath();
                    String base64Path = BASE64URL + fileName;
                    Context context = JuziApp.getInstance().getApplicationContext();

                    insert(base64Path, imgFilePath, mimeType, referer, b.length);
                    saveToFile(imgFilePath, b);

//                    DownloadUtil.notifyMediaScannerNewFile(context,
//                            imgFilePath, mimeType);
                    DownloadUtil.requestMediaScan(imgFilePath);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        BackgroundHandler.executeOnFullTaskExecutor(saveRunnable);
    }

    /**
     * @param taskSaveDirString 目标保存目录
     * @param fileNameString 目标文件名
     * @return 如有，建议保存路径
     * @throws DownloadException
     */
    private static String tryToCreateTaskDir(String taskSaveDirString,
            String fileNameString) throws DownloadException {
        File targetFile = new File(taskSaveDirString, fileNameString);
        String savePathString = targetFile.getAbsolutePath();
        try {
            // 目标路径可写
            if (DownloadFileUtils.isFilePathCanWrite(savePathString, true))
                return taskSaveDirString;
        } catch (DownloadException e) {
            // android上应少对抗，暂时只统计
            e.printStackTrace();
        }

        // 如果用户有设置路径，则这里就是用户设置的路径，如果没有可以尝试用app默认下载路径
        String userSaveDirString = PathResolver.getDownloadFileDir(null);
        if (null == userSaveDirString)
            userSaveDirString = "";
        if (null == taskSaveDirString)
            taskSaveDirString = "";
        HashMap<String, String> reportExtra = new HashMap<String, String>();
        reportExtra.put("task_save_dir", taskSaveDirString);
        reportExtra.put("user_save_dir", userSaveDirString);
        if (TextUtils.isEmpty(userSaveDirString)
                || savePathString.startsWith(userSaveDirString)) {
            alertUserNeedChangeSaveDir(reportExtra);
            throw new DownloadException(
                    ExceptionCode.TargetDirAndOptionUnavaliable);
        }

        File userSavePathFile = new File(userSaveDirString,
                fileNameString);
        try {
            if (DownloadFileUtils.isFilePathCanWrite(
                    userSavePathFile.getAbsolutePath(), true)) {
                // 用户设置的默认保存路径可用，使用这个路径
                return userSaveDirString;
            }
        } catch (DownloadException e) {
            // android上应少对抗，暂时只统计
            e.printStackTrace();
        }
        alertUserNeedChangeSaveDir(reportExtra);
        throw new DownloadException(
                ExceptionCode.TargetDirAndOptionUnavaliable);
    }

    private static class Task implements Runnable{
        private HashMap<String,String> mReportExtra;

        Task(HashMap<String,String> reportExtra){
            this.mReportExtra=reportExtra;
        }
        @Override
        public void run() {
            alertUserNeedChangeSaveDir(mReportExtra);
        }
    }

    private static void alertUserNeedChangeSaveDir(
            final HashMap<String, String> reportExtra) {
//        if (!ThreadUtils.runningOnUiThread()) {
//            ThreadUtils.postOnUiThreadDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    alertUserNeedChangeSaveDir(reportExtra);
//                }
//            }, 1);
//            return;
//        }

        ThreadManager.postDelayedTaskToUIHandler(new Task(reportExtra),1000);

        // TODO 更改下载目录路径
        
        /*
        final Context context = DownloadEnvironment.getUiContext();
        SmartDialog smartDialog = new SmartDialog(context);


        String[] buttonText = new String[] {
                context.getString(R.string.btn_download_change),
                context.getString(R.string.btn_download_cancel),
        };

        String dlgString =
                context.getString(R.string.download_save_dir_unavailable);
        smartDialog.setView(SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG,
                dlgString,
                null,
                buttonText);

        smartDialog.setKSmartDialogListener(new KSmartDialogListener() {
            @Override
            public void onDialogClosed(int whichButton, boolean[] checkState) {
                if (SmartDialog.BUTTON_POSITIVE == whichButton) {
                    // change
                    ToolkitActivity.startToolKitActivity(context, ToolkitActivity.TOOLKIT_LAYOUT_STORAGESETTING);
                }
            }
        });

        smartDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
            }
        });

        smartDialog.dialogEnterAnimation();
        */
        
        
        
    }

    private static String getFileName(String fileName, String extension, int nameCount) {
        if (nameCount == 0) {
            return fileName;
        }
        int index = fileName.indexOf("." + extension);

        return fileName.substring(0, index) + "(" + nameCount + ")." + extension;
    }

    private static int getImageExtenCount(String path, String extension) {
        String baseFile = path;
        if (!TextUtils.isEmpty(extension)) {
            baseFile = path.substring(0, path.length() - extension.length() - 1);
        }

        int countDown = 0;
        File file = new File(path);
        while (file.exists() && countDown < 1000) {
            countDown++;
            path = baseFile + "(" + countDown + ")" + "." + extension;
            file = new File(path);
        }

        return countDown;
    }

    private static boolean saveToFile(String filePath, byte[] data) {
        if (TextUtils.isEmpty(filePath) || data == null) {
            return false;
        }
        OutputStream out;
        try {
            File file = new File(filePath);
            file.createNewFile();
            out = new FileOutputStream(filePath);
            out.write(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*
     * hook method simulate the real download task insert to db
     */
    private static void insert(String url, String fileName, String mimeType, String referer, int size){
        Base64ImageRequest request = new Base64ImageRequest(url, fileName, mimeType, size, referer);
        DownloadManager.getInstance().createDownload(request);
    }
}
