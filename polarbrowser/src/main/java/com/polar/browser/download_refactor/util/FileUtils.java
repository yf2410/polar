
package com.polar.browser.download_refactor.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.FileLock;

//import com.ijinshan.browser.utils.KLog;
//import com.ijinshan.browser.view.impl.UploadHandler;

public class FileUtils {
//    private static UploadHandler mFileUploadHandler = null;

    public static Object deserializeFromFile(File file) {
        if (file == null || !file.exists())
            return null;

        Object o = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        FileLock lock = null;

        try {
            fis = new FileInputStream(file);
            lock = fis.getChannel().lock(0L, Long.MAX_VALUE, true);
            ois = new ObjectInputStream(fis);
            o = ois.readUnshared();
            return o;
        } catch (FileNotFoundException e) {
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
        } catch (Exception e) {
            file.delete();
        } catch (OutOfMemoryError oom) {
        } finally {
            if (lock != null)
                try {
                    lock.release();
                } catch (IOException e) {
                }
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
            }
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public static boolean serializeToFile(Serializable o, File file) {
        if (o == null || file == null)
            return false;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        FileLock lock = null;

        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            lock = fos.getChannel().lock();

            oos.writeUnshared(o);
            oos.flush();
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError oom) {
        } finally {
            if (oos != null)
                try {
                    oos.reset();
                } catch (IOException e) {
                }
            if (lock != null)
                try {
                    lock.release();
                } catch (IOException e) {
                }
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
            }
        }
        return false;
    }

//    public static void openFileChooser(Activity activity, ValueCallback<Uri> uploadFile,
//            String acceptType, String capture) {
//        if (null == activity) {
//            return;
//        }
//        mFileUploadHandler = new UploadHandler(activity, uploadFile, acceptType, capture);
//        mFileUploadHandler.openFileChooser(uploadFile, acceptType, capture);
//    }
//
//    public static UploadHandler getFileUploadHandler() {
//        return mFileUploadHandler;
//    }
//
//    public static void resetFileUploadHandler() {
//        if (mFileUploadHandler != null) {
//            mFileUploadHandler = null;
//        }
//    }
//
//    public static void dismissFileUploadHandler() {
//        if (mFileUploadHandler != null) {
//            mFileUploadHandler.dimiss();
//        }
//    }

    public static boolean copyFile(String srcPath, String destPath) {
        try {
            File srcFile = new File(srcPath);
            if (srcFile.exists()) {
                FileInputStream input = new FileInputStream(srcFile);
                File libFile = new File(destPath);
                if (!libFile.exists()) {
                    libFile.createNewFile();
                }
                else {
                    libFile.delete();
                    libFile.createNewFile();
                }
                FileOutputStream output = new FileOutputStream(libFile);

                byte[] buf = new byte[1024 * 4];
                int count = 0;
                while (true) {
                    count = input.read(buf);
                    if (count == -1)
                        break;
                    output.write(buf, 0, count);
                }
                input.close();
                output.flush();
                output.close();
                buf = null;
                return true;
            }
            else {
                return false;
            }
        } catch (Exception ex) {
//            if(KLog.DEBUG){
//                KLog.e("FileUtils", "copyFile", ex);
//            }
            return false;
        }
    }


    public static String assembleFilePath(String path,String name){
        String filePath = null;
        if (path == null || name == null) return filePath;
        try{
            filePath = path.endsWith("/") ? path + name : path + File.separator+name;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  filePath;
    }

    public static boolean moveFile1(String oldPath, String newPath) {
        if(copyFile1(oldPath, newPath)){  //删除之前，确保复制成功
            return delete(oldPath);
        }
        return false;
    }


    public static boolean copyFile1(String oldPath, String newPath) {
        try {
            if (oldPath == null || newPath == null) return false;
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newFile = new File(newPath);
            if (oldfile.exists() && !newFile.exists()) {  //文件存在时
                if(!newFile.getParentFile().exists()) newFile.getParentFile().mkdirs();
                InputStream inStream = new FileInputStream(oldPath);  //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[4096];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile())
                return delFile1(fileName);
            else
                return delFolder1(fileName);
        }
    }

    public static boolean delFile1(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean delFolder1(String folderPath) {
        if(folderPath == null) return false;
        if (!folderPath.endsWith(File.separator)) {
            folderPath = folderPath + File.separator;
        }
        File dirFile = new File(folderPath);
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }

        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                delFile1(files[i].getAbsolutePath());
            } else if (files[i].isDirectory()) {
                delFolder1(files[i].getAbsolutePath());
            }
        }
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEqualFilePath(String path1,String path2){
        if (path1 == null || path2 == null) return false;
        try{
            String clearPath1 = path1.endsWith("/") ? path1.substring(0,path1.length()-1): path1;
            String clearPath2 = path2.endsWith("/") ? path2.substring(0,path2.length()-1): path2;
            return  clearPath1.equals(clearPath2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  false;
    }
}
