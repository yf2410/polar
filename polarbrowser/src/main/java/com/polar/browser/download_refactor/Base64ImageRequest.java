package com.polar.browser.download_refactor;

import android.content.ContentValues;
public class Base64ImageRequest extends Request {
    long mContentSize;

    public Base64ImageRequest(String urlString, String filePath, String mimeType, long contentSize, String referer) {
        super(urlString);

        assert (contentSize > 0);
        mContentSize = contentSize;

        setDestinationFilePath(filePath);
        setRequestReferer(referer);
        setMimeType(mimeType);
    }

    ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_SUCCESS);
        values.put(Downloads.Impl.COLUMN_VIRUSCHECK, Downloads.Impl.VIRUSCHECK_OK);
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, mContentSize);
        values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, mContentSize);
        return values;
    }
}
