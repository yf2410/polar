package org.sprite2d.apps.pp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class PainterHandler extends Handler {

	public static final int MSG_DRAW = 0x102;
	public static final int MSG_START = 0x103;
	public static final int MSG_PAUSE = 0x104;

	private IPainterThread mThread;

	private boolean mPaused;
	
	public PainterHandler(Looper looper, IPainterThread thread) {
		super(looper);
		mThread = thread;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_DRAW:
			if (!mPaused) {
				mThread.drawBitmap();
				this.sendEmptyMessage(MSG_DRAW);
			}
			break;
		case MSG_START:
			mPaused = false;
			this.sendEmptyMessage(MSG_DRAW);
			break;
		case MSG_PAUSE:
			mPaused = true;
			break;
		default:
			break;
		}
	}
}
