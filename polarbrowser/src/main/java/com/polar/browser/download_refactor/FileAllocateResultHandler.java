package com.polar.browser.download_refactor;

public interface FileAllocateResultHandler {
    /**
     * 空文件填充结果回调
     * @param result 填充结果：0 OK, >0 stopped, <0 failed.
     * @param msg 结果/错误描述
     */
    void onAllocateResult(int result, String msg);
}
