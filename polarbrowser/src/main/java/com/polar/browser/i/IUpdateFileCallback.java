package com.polar.browser.i;

import java.io.File;

public interface IUpdateFileCallback {
	public void notifyFileDownload(String fileName, File file);
}
