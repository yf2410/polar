package com.polar.browser.safe.ssl;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;

import com.polar.browser.JuziApp;

import java.util.HashMap;
import java.util.Map;


/**
 * 用于处理ssl证书无效的响应
 * 仅用于ui线程
 */
public class SafeSslErrorHandler {

	private static SafeSslErrorHandler mInstance;

	public static SafeSslErrorHandler getInstance() {
		if (null == mInstance) {
			synchronized (SafeSslErrorHandler.class) {
				if (null == mInstance) {
					mInstance = new SafeSslErrorHandler();
				}
			}
		}
		return mInstance;
	}

    private long mErrContextNum = 0;
    @SuppressLint("UseSparseArrays") private Map<Long, SafeSslErrorContext> mErrContexts = new HashMap<Long, SafeSslErrorContext>();


    public SafeSslErrorContext getErrContext(long errContextId) {
        if (!mErrContexts.containsKey(errContextId))
            return null;
        return mErrContexts.get(Long.valueOf(errContextId));
    }

    public void removeErrContext(long errContextId) {
        if (!mErrContexts.containsKey(errContextId))
            return;
        mErrContexts.remove(Long.valueOf(errContextId));
    }

    public void onReceivedSslError(final SslErrorHandler handler, final SslError error) {
        Long errContextId = Long.valueOf(mErrContextNum++);
        SafeSslErrorContext sslErrorContext = new SafeSslErrorContext(handler, error,errContextId.longValue());
        mErrContexts.put(errContextId, sslErrorContext);
        Bundle bundle = new Bundle();
        bundle.putLong(SafeSslErrorContext.KEY, errContextId.longValue());
        Intent intent = new Intent(JuziApp.getInstance().getApplicationContext(), SSLDialog.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //mainController.getContext().startActivity(intent);
        JuziApp.getInstance().getApplicationContext().startActivity(intent);
    }

    public static class SafeSslErrorContext {
        public static final String KEY = "SafeSslErrorContext";

        private SslErrorHandler handler;
        private SslError        error;
        private long            id;

        public SafeSslErrorContext(SslErrorHandler handler, SslError error,
                                   long id) {
            this.handler = handler;
            this.error = error;
           // this.mainController = mainController;
            //this.tab = tab;
            this.id = id;
        }

        public SslErrorHandler getHandler() {
            return handler;
        }

        public SslError getError() {
            return error;
        }

//        public MainController getMainController() {
//            return mainController;
//        }
//
//        public KTab getTab() {
//            return tab;
//        }

        public void proceedAndFinish() {
            handler.proceed();
            SafeSslErrorHandler.getInstance().removeErrContext(id);
        }

        public void cancelAndFinish() {
            handler.cancel();
            SafeSslErrorHandler.getInstance().removeErrContext(id);
        }
    }
}
