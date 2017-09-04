package com.polar.browser.push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Administrator on 2016/6/8.
 */
public class CustomFirebaseInstanceIDService extends FirebaseInstanceIdService {
	private static final String TAG = "FirebaseIDService";

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. Note that this is called when the InstanceID token
	 * is initially generated so this is where you would retrieve the token.
	 */
	@Override
	public void onTokenRefresh() {
		// Get updated InstanceID token.
		String token = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + token);
		sendRegistrationToServer(token);
	}

	/**
	 * Persist token to third-party servers.
	 * <p>
	 * Modify this method to associate the user's FCM InstanceID token with any server-side account
	 * maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token) {
		// Add custom implementation, as needed.
	}
}
