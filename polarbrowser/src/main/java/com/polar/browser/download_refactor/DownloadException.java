package com.polar.browser.download_refactor;

/**
 * 下载自定义异常
 */
public class DownloadException extends Exception {
    private static final long serialVersionUID = -1485056582359467494L;

    private ExceptionCode mExceptionCode;

    public enum ExceptionCode {
        TargetFilePathIsPlacedByDir, // 目标文件路径被目录占坑
        TargetDirPathIsPlacedByFile, // 目标目录路径被文件占坑
        TargetDirAndOptionUnavaliable, // 目标目录和备选目录都不可用

        UnknownException,
    };

    private static String toString(ExceptionCode exceptionCode) {
        switch (exceptionCode) {
            case TargetFilePathIsPlacedByDir:
                return "TargetFilePathIsPlacedByDir";
            case TargetDirPathIsPlacedByFile:
                return "TargetDirPathIsPlacedByFile";
            case TargetDirAndOptionUnavaliable:
                return "TargetDirAndOptionUnavaliable";

            case UnknownException:
            default:
                return "UnknownException";
        }
    }

    public DownloadException(ExceptionCode exceptionCode) {
        this(exceptionCode, toString(exceptionCode));
    }

    public DownloadException(ExceptionCode exceptionCode,
                             String detailMessage) {
        this(detailMessage);
        this.setExceptionCode(exceptionCode);
    }

    public DownloadException(ExceptionCode exceptionCode, Throwable throwable) {
        this(throwable);
        this.setExceptionCode(exceptionCode);
    }

    public DownloadException(ExceptionCode exceptionCode, String detailMessage,
                             Throwable throwable) {
        this(detailMessage, throwable);
        this.setExceptionCode(exceptionCode);
    }

    private DownloadException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    private DownloadException(String detailMessage) {
        super(detailMessage);
    }

    private DownloadException(Throwable throwable) {
        super(throwable);
    }

    public ExceptionCode getExceptionCode() {
        return mExceptionCode;
    }

    public void setExceptionCode(ExceptionCode mExceptionCode) {
        this.mExceptionCode = mExceptionCode;
    }
}
