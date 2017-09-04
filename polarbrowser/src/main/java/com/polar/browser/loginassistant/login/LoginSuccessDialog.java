package com.polar.browser.loginassistant.login;

import android.content.Context;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by FKQ on 2017/4/5.
 */

public class LoginSuccessDialog extends CommonBaseDialog {

    public LoginSuccessDialog(Context context) {
        super(context, R.style.common_dialog);
        setContentView(R.layout.dialog_login_success);
        setCanceledOnTouchOutside(false);
    }

    public LoginSuccessDialog(Context context, int theme) {
        super(context, R.style.common_dialog);
    }
}
