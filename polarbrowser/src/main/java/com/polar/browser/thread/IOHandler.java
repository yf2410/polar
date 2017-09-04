package com.polar.browser.thread;

import android.os.Looper;
import android.os.Message;

import com.polar.browser.base.BaseHandler;

import java.io.File;

/**
 * 用于处理IO操作的Handler,工作在ThreadIO中
 *
 * @author dpk
 */
public class IOHandler extends BaseHandler {

	public static final int MSG_WRITE = 0;
	public static final int MSG_READ = 1;


	private static IOHandler sInstance;

	private IOHandler(Looper looper) {
		super(looper);
	}

	public static IOHandler getInstance(Looper looper) {
		if (sInstance == null) {
			sInstance = new IOHandler(looper);
		}
		return sInstance;
	}

	// TODO
	public void writeString(File path, String data) {
	}

	public void writeByteArray(File path, Byte[] data) {
	}

	// TODO
	public String readString(File path) {
		String ret = "test";
		return ret;
	}

	public Byte[] readByteArray(File path) {
		Byte[] ret = null;
		return ret;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_WRITE:
				break;
			case MSG_READ:
				break;
			default:
				break;
		}
	}
}
