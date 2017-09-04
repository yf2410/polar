/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.polar.browser.manager.VCStoragerManager;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some helper functions for the download manager
 */
public class Helpers {
    public static final Random sRandom = new Random(SystemClock.uptimeMillis());

    /** Regex used to parse content-disposition headers */
    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");

    private static final Object sUniqueLock = new Object();

    private Helpers() {
    }

    /*
     * Parse the Content-Disposition HTTP Header. The format of the header
     * is defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html
     * This header provides a filename for content that is going to be
     * downloaded to the file system. We only support the attachment type.
     */
    private static String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(1);
            }
        } catch (IllegalStateException ex) {
             // This function is defined as returning null when it can't parse the header
        }
        return null;
    }

    /**
     * Creates a filename (where the file should be saved) from info about a download.
     */
    static String generateSaveFile(
            Context context,
            String url,
            String path,
            String contentDisposition,
            String contentLocation,
            String mimeType,
            long contentLength,
            StorageManager storageManager) throws StopRequestException {
        if (contentLength < 0) {
            contentLength = 0;
        }
        storageManager.verifySpace(path, contentLength);
        File base = null;
        path = getFullPath(path, mimeType, base);
        return path;
    }

    private static String getFullPath(String filename, String mimeType, File base)
            throws StopRequestException {
        String extension;
        int dotIndex = filename.lastIndexOf('.');
        boolean missingExtension = dotIndex < 0 || dotIndex < filename.lastIndexOf('/');

        // Destination is explicitly set - do not change the extension
        if (missingExtension) {
            extension = "";
        } else {
            extension = filename.substring(dotIndex);
            filename = filename.substring(0, dotIndex);
        }

        boolean recoveryDir = Constants.RECOVERY_DIRECTORY.equalsIgnoreCase(filename + extension);

        if (base != null) {
            filename = base.getPath() + File.separator + filename;
        }

        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "target file: " + filename + extension);
        }

        synchronized (sUniqueLock) {
            final String path = chooseUniqueFilenameLocked(
                    filename, extension, recoveryDir);

            // Claim this filename inside lock to prevent other threads from
            // clobbering us. We're not paranoid enough to use O_EXCL.
            try {
                File file = new File(path);
                File parent = file.getParentFile();

                // Make sure the parent directories exists before generates new file
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                file.createNewFile();
            } catch (IOException e) {
                throw new StopRequestException(Downloads.Impl.STATUS_FILE_ERROR,
                        "Failed to create target file " + path, e);
            }
            return path;
        }
    }

    private static String chooseFilename(String url, String hint, String contentDisposition,
            String contentLocation, int destination) {
        String filename = null;

        // First, try to use the hint from the application, if there's one
        if (hint != null && !hint.endsWith("/")) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "getting filename from hint");
            }
            int index = hint.lastIndexOf('/') + 1;
            if (index > 0) {
                filename = hint.substring(index);
            } else {
                filename = hint;
            }
        }

        // If we couldn't do anything with the hint, move toward the content disposition
        if (filename == null && contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "getting filename from content-disposition");
                }
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }

        // If we still have nothing at this point, try the content location
        if (filename == null && contentLocation != null) {
            String decodedContentLocation = Uri.decode(contentLocation);
            if (decodedContentLocation != null
                    && !decodedContentLocation.endsWith("/")
                    && decodedContentLocation.indexOf('?') < 0) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "getting filename from content-location");
                }
                int index = decodedContentLocation.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedContentLocation.substring(index);
                } else {
                    filename = decodedContentLocation;
                }
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null
                    && !decodedUrl.endsWith("/") && decodedUrl.indexOf('?') < 0) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "getting filename from uri");
                    }
                    filename = decodedUrl.substring(index);
                }
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "using default filename");
            }
            filename = Constants.DEFAULT_DL_FILENAME;
        }

        // The VFAT file system is assumed as target for downloads.
        // Replace invalid characters according to the specifications of VFAT.
        filename = replaceInvalidVfatCharacters(filename);

        return filename;
    }

    private static String chooseExtensionFromMimeType(String mimeType, boolean useDefaults) {
        String extension = null;
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "adding extension from type");
                }
                extension = "." + extension;
            } else {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "couldn't find extension for " + mimeType);
                }
            }
        }
        if (extension == null) {
            if (mimeType != null && mimeType.toLowerCase(java.util.Locale.US).startsWith("text/")) {
                if (mimeType.equalsIgnoreCase("text/html")) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "adding default html extension");
                    }
                    extension = Constants.DEFAULT_DL_HTML_EXTENSION;
                } else if (useDefaults) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "adding default text extension");
                    }
                    extension = Constants.DEFAULT_DL_TEXT_EXTENSION;
                }
            } else if (useDefaults) {
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "adding default binary extension");
                }
                extension = Constants.DEFAULT_DL_BINARY_EXTENSION;
            }
        }
        return extension;
    }

    private static String chooseExtensionFromFilename(String mimeType, int destination,
            String filename, int lastDotIndex) {
        String extension = null;
        if (mimeType != null) {
            // Compare the last segment of the extension against the mime type.
            // If there's a mismatch, discard the entire extension.
            String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    filename.substring(lastDotIndex + 1));
            if (typeFromExt == null || !typeFromExt.equalsIgnoreCase(mimeType)) {
                extension = chooseExtensionFromMimeType(mimeType, false);
                if (extension != null) {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "substituting extension from type");
                    }
                } else {
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "couldn't find extension for " + mimeType);
                    }
                }
            }
        }
        if (extension == null) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "keeping extension");
            }
            extension = filename.substring(lastDotIndex);
        }
        return extension;
    }

    private static String chooseUniqueFilenameLocked(String filename,
            String extension, boolean recoveryDir) throws StopRequestException {
        String fullFilename = filename + extension;
        if (!new File(fullFilename).exists() && (!recoveryDir)) {
            return fullFilename;
        }
        filename = filename + Constants.FILENAME_SEQUENCE_SEPARATOR;
        /*
        * This number is used to generate partially randomized filenames to avoid
        * collisions.
        * It starts at 1.
        * The next 9 iterations increment it by 1 at a time (up to 10).
        * The next 9 iterations increment it by 1 to 10 (random) at a time.
        * The next 9 iterations increment it by 1 to 100 (random) at a time.
        * ... Up to the point where it increases by 100000000 at a time.
        * (the maximum value that can be reached is 1000000000)
        * As soon as a number is reached that generates a filename that doesn't exist,
        *     that filename is used.
        * If the filename coming in is [base].[ext], the generated filenames are
        *     [base]-[sequence].[ext].
        */
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullFilename = filename + sequence + extension;
                if (!new File(fullFilename).exists()) {
                    return fullFilename;
                }
                if (Constants.LOGVV) {
                    Log.v(Constants.TAG, "file with sequence number " + sequence + " exists");
                }
                sequence += sRandom.nextInt(magnitude) + 1;
            }
        }
        throw new StopRequestException(Downloads.Impl.STATUS_FILE_ERROR,
                "failed to generate an unused filename on internal download storage");
    }

    /**
     * Checks whether this looks like a legitimate selection parameter
     */
    public static void validateSelection(String selection, Set<String> allowedColumns) {
        try {
            if (selection == null || selection.isEmpty()) {
                return;
            }
            Lexer lexer = new Lexer(selection, allowedColumns);
            parseExpression(lexer);
            if (lexer.currentToken() != Lexer.TOKEN_END) {
                throw new IllegalArgumentException("syntax error");
            }
        } catch (RuntimeException ex) {
            if (Constants.LOGV) {
                Log.d(Constants.TAG, "invalid selection [" + selection + "] triggered " + ex);
            } else {
                Log.d(Constants.TAG, "invalid selection triggered " + ex);
            }
            throw ex;
        }

    }

    // expression <- ( expression ) | statement [AND_OR ( expression ) | statement] *
    //             | statement [AND_OR expression]*
    private static void parseExpression(Lexer lexer) {
        for (;;) {
            // ( expression )
            if (lexer.currentToken() == Lexer.TOKEN_OPEN_PAREN) {
                lexer.advance();
                parseExpression(lexer);
                if (lexer.currentToken() != Lexer.TOKEN_CLOSE_PAREN) {
                    throw new IllegalArgumentException("syntax error, unmatched parenthese");
                }
                lexer.advance();
            } else {
                // statement
                parseStatement(lexer);
            }
            if (lexer.currentToken() != Lexer.TOKEN_AND_OR) {
                break;
            }
            lexer.advance();
        }
    }

    // statement <- COLUMN COMPARE VALUE
    //            | COLUMN IS NULL
    private static void parseStatement(Lexer lexer) {
        // both possibilities start with COLUMN
        if (lexer.currentToken() != Lexer.TOKEN_COLUMN) {
            throw new IllegalArgumentException("syntax error, expected column name");
        }
        lexer.advance();

        // statement <- COLUMN COMPARE VALUE
        if (lexer.currentToken() == Lexer.TOKEN_COMPARE) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_VALUE) {
                throw new IllegalArgumentException("syntax error, expected quoted string");
            }
            lexer.advance();
            return;
        }

        // statement <- COLUMN IS NULL
        if (lexer.currentToken() == Lexer.TOKEN_IS) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_NULL) {
                throw new IllegalArgumentException("syntax error, expected NULL");
            }
            lexer.advance();
            return;
        }

        // didn't get anything good after COLUMN
        throw new IllegalArgumentException("syntax error after column name");
    }

    /**
     * 获取下载的保存目录
     *
     * @param context
     * @param customFolder
     * @return
     */
    public static String getSaveDir(Context context, String customFolder) {
        if (!TextUtils.isEmpty(customFolder))
            return customFolder;

//        String settingDir = SettingsModel.getInstance().getDefaultDownloadPath();
        String settingDir = VCStoragerManager.getInstance().getDownloadDirPath();
        if (!TextUtils.isEmpty(settingDir))
            return settingDir;
        return getDefaultSaveDir(context);
    }

    /**
     * 获取默认下载目录
     *
     * @param context
     * @return
     */
    public static String getDefaultSaveDir(Context context) {
        String state = Environment.getExternalStorageState();
        if (state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return getExternalStoragePublicSaveDir();

        return getInternalStorageSaveDir(context);
    }

    /**
     * 获取Interal Storage上的默认存储目录，app私有目录，卸载时存在这里的文件会被删除
     *
     * @param context
     * @return
     */
    public static String getInternalStorageSaveDir(Context context) {
        File filesDirFile = context.getFilesDir();
        File downloadDirFile = new File(filesDirFile,
                Environment.DIRECTORY_DOWNLOADS);
        return downloadDirFile.getAbsolutePath();
    }

    /**
     * 获取External Storage上的默认存储目录，app私有目录，卸载时存在这里的文件会被删除
     *
     * @param context
     * @return
     */
    public static String getExternalStorageSaveDir(Context context) {
        File filesDirFile = context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS);
        return filesDirFile.getAbsolutePath();
    }

    /**
     * 获取External Storage上的公开存储目录，卸载时不会删除
     *
     * @return
     */
    public static String getExternalStoragePublicSaveDir() {
        File filesDirFile = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        return filesDirFile.getAbsolutePath();
    }

    /**
     * 获取bytes的文本形式
     *
     * @param context
     * @param bytes
     * @return
     */
    public static String getSizeText(Context context, long bytes) {
        if (bytes >= 0) {
            return Formatter.formatFileSize(context, bytes);
        }
        return Formatter.formatFileSize(context, 0);
    }

    /**
     * 嗅探文件路径是否可写
     * @param filePath 嗅探目标文件的全路径
     * @param delAfterExit 嗅探目标文件如果是由嗅探程序创建的，是否要在离开时删除
     * @return
     * @throws DownloadException
     *      ExceptionCode.TargetFilePathIsPlacedByDir
     *      ExceptionCode.TargetDirPathIsPlacedByFile
     */
    public static boolean isFilePathCanWrite(String filePath,
                                             boolean delAfterExit) throws DownloadException {
        if (TextUtils.isEmpty(filePath))
            return false;

        File file = new File(filePath);
        return isDirPathCanWrite(file.getParent(), file.getName(),
                delAfterExit);
    }

    /**
     * 嗅探|dirPath|下文件名是否可写，！！！假定|dirPath|可写
     * @param dirPath 嗅探目标文件所在目录路径
     * @param fileName 嗅探目标文件的文件名
     * @param delAfterExit 嗅探目标文件如果是由嗅探程序创建的，是否要在离开时删除
     * @return
     * @throws DownloadException
     *      ExceptionCode.TargetFilePathIsPlacedByDir
     */
    public static boolean isFileNameCanWrite(String dirPath,
                                             String fileName, boolean delAfterExit) throws DownloadException {
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(fileName))
            return false;

        File file = new File(dirPath, fileName);
        final boolean isFileExists = file.exists(); // 文件是否原来就存在的
        if (isFileExists && !file.isFile())
            throw new DownloadException(
                    DownloadException.ExceptionCode.TargetFilePathIsPlacedByDir);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (!file.exists())
            return false;

        try {
            // 暂时信任canWrite权限判断，否则自己写1byte进行嗅探
            if (!file.canWrite())
                return false;
            return true;
        } finally {
            // 原本不存在，由我们临时创建出来的根据选项参数决定是否删除
            if (!isFileExists && delAfterExit)
                file.delete();
        }
    }

    /**
     * 嗅探目录是否可写，！！！会创建目录
     * @param dirPath 嗅探目标目录的路径
     * @param testFileName 嗅探用的临时文件名，如果isEmpty()则会使用内部临时文件名
     * @param delAfterExit 嗅探用的文件如果是由嗅探程序创建的，是否要在离开时删除
     * @return
     * @throws DownloadException
     *      ExceptionCode.TargetDirPathIsPlacedByFile
     *      ExceptionCode.TargetFilePathIsPlacedByDir
     */
    public static boolean isDirPathCanWrite(String dirPath,
                                            String testFileName, boolean delAfterExit)
            throws DownloadException {
        if (TextUtils.isEmpty(dirPath))
            return false;

        File dirFile = new File(dirPath);
        if (dirFile.exists() && !dirFile.isDirectory())
            throw new DownloadException(
                    DownloadException.ExceptionCode.TargetDirPathIsPlacedByFile);

        if (!dirFile.exists()) {
            if (!dirFile.mkdirs())
                return false;
        }

        if (!dirFile.exists())
            return false;
        if (TextUtils.isEmpty(testFileName)) {
            testFileName = getUniqueName("test_can_write.temp", dirPath);
            delAfterExit = true; // 容错逻辑，不管外部如何设置，内部临时文件都应被删除
        }
        return isFileNameCanWrite(dirPath, testFileName, delAfterExit);
    }

    /**
     * 以|fileName|为baseName，在|customDir|中获得唯一名，重名则附加形如(1)的后缀
     * @param fileName
     * @param customDir
     * @return
     */
    public static String getUniqueName(String fileName, String customDir) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }

        String candidate = fileName;
        int i = 1;
        // 依次尝试各个后缀的文件名，知道找到第一个不重名的文件
        while (true) {
            if (i > 1) {
                // 从第二个文件开始添加数字后缀
                candidate = addSuffixToFileName(fileName, i);
            }

            File file = new File(customDir, candidate);
            if (!file.exists()) {
                // 若文件名不存在，则说明不重名
                return candidate;
            }

            i++;
        }
    }

    /**
     * 给文件名添加数组后缀
     *
     * @param fileName 文件名
     * @param fix 要添加的数字
     * @return 添加后缀后的文件名
     */
    public static String addSuffixToFileName(String fileName, int fix) {
        if (TextUtils.isEmpty(fileName))
            return fileName;

        int sep = fileName.lastIndexOf('.');
        if (sep == -1) {
            // 无扩展名
            return String.format(java.util.Locale.US, "%s(%d)", fileName, fix);
        }

        // 有扩展名，则在文件名后加后缀
        return String.format(java.util.Locale.US, "%s(%d)%s", fileName.substring(0, sep), fix, fileName.subSequence(sep, fileName.length()));
    }

    /**
     * 黑科技兼容接口。具体sdcard的使用权限定义，可能会因不同的硬件厂商而yy出不同的权限定义和用
     * 法。official build和海外主流版本可以参照下面这段解惑。但三星等的无节操可能需要抓到问题时
     * 具体解决。
     * <p>In KitKat, the external storage API was split out to include multiple
     *  volumes; one “primary” and one or more “secondary.”  The primary volume
     *  is, for all intents and purposes, exactly the same as the previous
     * single volume.  All APIs that existed prior to KitKat reference the
     * primary external storage.  The secondary volume(s) modify write
     * permissions a bit; they are globally readable under the same permission
     * described above.  Directories outside the application’s own managed area
     *  (i.e. /Android/data/[PACKAGE_NAME]) are not writable at all by that
     *  application.</p>
     *  /Android/data/<package_name>/files/
     * @param sdcardPathString
     * @param context
     * @return
     */
    public static String getSaveDirPathBySDCardPath(String sdcardPathString,
                                                    Context context) {
        String packageDataString = "/Android/data/" + context.getPackageName()
                + "/files/Download";
        File dirFile = new File(sdcardPathString, packageDataString);
        return dirFile.getAbsolutePath();
    }


    /**
     * A simple lexer that recognizes the words of our restricted subset of SQL where clauses
     */
    private static class Lexer {
        public static final int TOKEN_START = 0;
        public static final int TOKEN_OPEN_PAREN = 1;
        public static final int TOKEN_CLOSE_PAREN = 2;
        public static final int TOKEN_AND_OR = 3;
        public static final int TOKEN_COLUMN = 4;
        public static final int TOKEN_COMPARE = 5;
        public static final int TOKEN_VALUE = 6;
        public static final int TOKEN_IS = 7;
        public static final int TOKEN_NULL = 8;
        public static final int TOKEN_END = 9;

        private final String mSelection;
        private final Set<String> mAllowedColumns;
        private int mOffset = 0;
        private int mCurrentToken = TOKEN_START;
        private final char[] mChars;

        public Lexer(String selection, Set<String> allowedColumns) {
            mSelection = selection;
            mAllowedColumns = allowedColumns;
            mChars = new char[mSelection.length()];
            mSelection.getChars(0, mChars.length, mChars, 0);
            advance();
        }

        public int currentToken() {
            return mCurrentToken;
        }

        public void advance() {
            char[] chars = mChars;

            // consume whitespace
            while (mOffset < chars.length && chars[mOffset] == ' ') {
                ++mOffset;
            }

            // end of input
            if (mOffset == chars.length) {
                mCurrentToken = TOKEN_END;
                return;
            }

            // "("
            if (chars[mOffset] == '(') {
                ++mOffset;
                mCurrentToken = TOKEN_OPEN_PAREN;
                return;
            }

            // ")"
            if (chars[mOffset] == ')') {
                ++mOffset;
                mCurrentToken = TOKEN_CLOSE_PAREN;
                return;
            }

            // "?"
            if (chars[mOffset] == '?') {
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }

            // "=" and "=="
            if (chars[mOffset] == '=') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }

            // ">" and ">="
            if (chars[mOffset] == '>') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }

            // "<", "<=" and "<>"
            if (chars[mOffset] == '<') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && (chars[mOffset] == '=' || chars[mOffset] == '>')) {
                    ++mOffset;
                }
                return;
            }

            // "!="
            if (chars[mOffset] == '!') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                    return;
                }
                throw new IllegalArgumentException("Unexpected character after !");
            }

            // columns and keywords
            // first look for anything that looks like an identifier or a keyword
            //     and then recognize the individual words.
            // no attempt is made at discarding sequences of underscores with no alphanumeric
            //     characters, even though it's not clear that they'd be legal column names.
            if (isIdentifierStart(chars[mOffset])) {
                int startOffset = mOffset;
                ++mOffset;
                while (mOffset < chars.length && isIdentifierChar(chars[mOffset])) {
                    ++mOffset;
                }
                String word = mSelection.substring(startOffset, mOffset);
                if (mOffset - startOffset <= 4) {
                    if (word.equals("IS")) {
                        mCurrentToken = TOKEN_IS;
                        return;
                    }
                    if (word.equals("OR") || word.equals("AND")) {
                        mCurrentToken = TOKEN_AND_OR;
                        return;
                    }
                    if (word.equals("NULL")) {
                        mCurrentToken = TOKEN_NULL;
                        return;
                    }
                }
                if (mAllowedColumns.contains(word)) {
                    mCurrentToken = TOKEN_COLUMN;
                    return;
                }
                throw new IllegalArgumentException("unrecognized column or keyword");
            }

            // quoted strings
            if (chars[mOffset] == '\'') {
                ++mOffset;
                while (mOffset < chars.length) {
                    if (chars[mOffset] == '\'') {
                        if (mOffset + 1 < chars.length && chars[mOffset + 1] == '\'') {
                            ++mOffset;
                        } else {
                            break;
                        }
                    }
                    ++mOffset;
                }
                if (mOffset == chars.length) {
                    throw new IllegalArgumentException("unterminated string");
                }
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }

            // anything we don't recognize
            throw new IllegalArgumentException("illegal character: " + chars[mOffset]);
        }

        private static boolean isIdentifierStart(char c) {
            return c == '_' ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z');
        }

        private static boolean isIdentifierChar(char c) {
            return c == '_' ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9');
        }
    }

    /**
     * Replace invalid filename characters according to
     * specifications of the VFAT.
     * @note Package-private due to testing.
     */
    private static String replaceInvalidVfatCharacters(String filename) {
        final char START_CTRLCODE = 0x00;
        final char END_CTRLCODE = 0x1f;
        final char QUOTEDBL = 0x22;
        final char ASTERISK = 0x2A;
        final char SLASH = 0x2F;
        final char COLON = 0x3A;
        final char LESS = 0x3C;
        final char GREATER = 0x3E;
        final char QUESTION = 0x3F;
        final char BACKSLASH = 0x5C;
        final char BAR = 0x7C;
        final char DEL = 0x7F;
        final char UNDERSCORE = 0x5F;

        StringBuilder sb = new StringBuilder();
        char ch;
        boolean isRepetition = false;
        for (int i = 0; i < filename.length(); i++) {
            ch = filename.charAt(i);
            if ((START_CTRLCODE <= ch &&
                ch <= END_CTRLCODE) ||
                ch == QUOTEDBL ||
                ch == ASTERISK ||
                ch == SLASH ||
                ch == COLON ||
                ch == LESS ||
                ch == GREATER ||
                ch == QUESTION ||
                ch == BACKSLASH ||
                ch == BAR ||
                ch == DEL){
                if (!isRepetition) {
                    sb.append(UNDERSCORE);
                    isRepetition = true;
                }
            } else {
                sb.append(ch);
                isRepetition = false;
            }
        }
        return sb.toString();
    }
}
