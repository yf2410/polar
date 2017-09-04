package com.polar.browser.download_refactor;

/**
 * Created by chenliang on 14-12-10.
 */
public class FileFormatException extends Exception {

//    private static final long serialVersionUID = -897856973823710492L;

    public FileFormatException() { }

    public FileFormatException(String detailMessage) {
        super(detailMessage);
    }
}