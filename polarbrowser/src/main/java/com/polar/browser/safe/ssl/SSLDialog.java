package com.polar.browser.safe.ssl;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.net.http.SslCertificate.DName;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.manager.ConfigManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SSLDialog extends Activity implements OnClickListener {
    private RelativeLayout mSSLLayout;
    private long mErrContextId;
    private SafeSslErrorHandler mSafeSslErrorHandlerRef = SafeSslErrorHandler.getInstance();
    private int mLevel;
    private static final int LEVEL_SSL_DIALOG = 0;
    private static final int LEVEL_CERTIFICATE_DIALOG = 1;
    private CommonCheckBox1 mCbNeverRemind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ssl_dialog);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mErrContextId = bundle.getLong(SafeSslErrorHandler.SafeSslErrorContext.KEY);
            initView();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(getSafeSslErrorContext() != null){
            getSafeSslErrorContext().cancelAndFinish();
        }
        this.finish();
    }

    private void initView() {
        mSSLLayout = (RelativeLayout) findViewById(R.id.layout_ssl_activity);
        inflateSSLDialog();
    }

    @SuppressLint("NewApi") 
    private void inflateSSLDialog() {
        mLevel = LEVEL_SSL_DIALOG;
        mSSLLayout.removeAllViews();
        View sslDialog = LayoutInflater.from(this).inflate(R.layout.dialog_ssl, mSSLLayout);
        sslDialog.findViewById(R.id.btn_continue).setOnClickListener(this);
        sslDialog.findViewById(R.id.text_continue_tip).setOnClickListener(this);
        sslDialog.findViewById(R.id.btn_cancel).setOnClickListener(this);
        mCbNeverRemind = (CommonCheckBox1) sslDialog.findViewById(R.id.cb_never_remind);
        ((TextView) sslDialog.findViewById(R.id.error_info)).setText(getErrorInfo());
        ((TextView) sslDialog.findViewById(R.id.text_continue_tip)).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_continue:
                if (mCbNeverRemind.isChecked()) {
                    ConfigManager.getInstance().setSafetyWarningEnabled(false);
                }
                if (getSafeSslErrorContext() != null) {
                    getSafeSslErrorContext().proceedAndFinish();
                }
                this.finish();
                break;
            case R.id.text_continue_tip:
                showCertificateDialog();
                break;
            case R.id.btn_cancel:
                if (getSafeSslErrorContext() != null) {
                    getSafeSslErrorContext().cancelAndFinish();
                }
                this.finish();
                break;
            case R.id.btn_confirm:
                inflateSSLDialog();
                break;
        }
    }

    private void showCertificateDialog() {
        if (getSafeSslErrorContext() != null && getSafeSslErrorContext().getError() != null) {
            mLevel = LEVEL_CERTIFICATE_DIALOG;
            mSSLLayout.removeAllViews();
            View certificateDialog = LayoutInflater.from(this).inflate(
                    R.layout.dialog_ssl_certificate, mSSLLayout);
            certificateDialog.findViewById(R.id.btn_confirm).setOnClickListener(this);

            ((TextView) certificateDialog.findViewById(R.id.certificate_info))
                    .setText(getErrorInfo());

            LinearLayout issuedToLayout = (LinearLayout) certificateDialog
                    .findViewById(R.id.layout_issued_to);
            LinearLayout issuedByLayout = (LinearLayout) certificateDialog
                    .findViewById(R.id.layout_issued_by);

            TextView txtIssuedTo = (TextView)certificateDialog.findViewById(R.id.issued_to);
            TextView txtIssuedBy = (TextView)certificateDialog.findViewById(R.id.issued_by);
            TextView txtValidTo = (TextView)certificateDialog.findViewById(R.id.valid_to);
            TextPaint tpIssuedTo = txtIssuedTo.getPaint();
            tpIssuedTo.setFakeBoldText(true);
            TextPaint tpIssuedBy = txtIssuedBy.getPaint();
            tpIssuedBy.setFakeBoldText(true);
            TextPaint tpValidTo = txtValidTo.getPaint();
            tpValidTo.setFakeBoldText(true);
            
            DName issuedTo = getSafeSslErrorContext().getError().getCertificate().getIssuedTo();
            DName issuedBy = getSafeSslErrorContext().getError().getCertificate().getIssuedBy();
            String cNameTo = "", oNameTo = "", uNameTo = "", cNameBy = "", oNameBy = "", uNameBy = "";
            if (issuedTo != null) {
                cNameTo = issuedTo.getCName();
                oNameTo = issuedTo.getOName();
                uNameTo = issuedTo.getUName();
            }
            if (issuedBy != null) {
                cNameBy = issuedBy.getCName();
                oNameBy = issuedBy.getOName();
                uNameBy = issuedBy.getUName();
            }

            if (!TextUtils.isEmpty(cNameTo)) {
                issuedToLayout.addView(getIssuedView(getString(R.string.ssl_common_name), cNameTo));
            }
            if (!TextUtils.isEmpty(oNameTo)) {
                issuedToLayout
                        .addView(getIssuedView(getString(R.string.ssl_organization), oNameTo));
            }
            if (!TextUtils.isEmpty(uNameTo)) {
                issuedToLayout.addView(getIssuedView(getString(R.string.ssl_organization_unit),
                        uNameTo));
            }

            if (!TextUtils.isEmpty(cNameBy)) {
                issuedByLayout.addView(getIssuedView(getString(R.string.ssl_common_name), cNameBy));
            }
            if (!TextUtils.isEmpty(oNameBy)) {
                issuedByLayout
                        .addView(getIssuedView(getString(R.string.ssl_organization), oNameBy));
            }
            if (!TextUtils.isEmpty(uNameBy)) {
                issuedByLayout.addView(getIssuedView(getString(R.string.ssl_organization_unit),
                        uNameBy));
            }

            Date validNotAfter = getSafeSslErrorContext().getError().getCertificate()
                    .getValidNotAfterDate();
            Date validNotBefore = getSafeSslErrorContext().getError().getCertificate()
                    .getValidNotBeforeDate();
            LinearLayout validLayout = (LinearLayout) certificateDialog
                    .findViewById(R.id.layout_valid);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            validLayout.addView(getIssuedView(getString(R.string.certificate_time),
                    dateFormat.format(validNotBefore)));
            validLayout.addView(getIssuedView(getString(R.string.valid_until),
                    dateFormat.format(validNotAfter)));
        }
    }

    private CharSequence getErrorInfo() {
//        int error = -1;
//        if (getSafeSslErrorContext() != null && getSafeSslErrorContext().getError() != null) {
//            error = getSafeSslErrorContext().getError().getPrimaryError();
//        }
//        switch (error) {
//            case SslError.SSL_NOTYETVALID:
//            case SslError.SSL_INVALID:
//                return getString(R.string.ssl_invalid);
//            case SslError.SSL_EXPIRED:
//                return getString(R.string.ssl_expired);
//            case SslError.SSL_IDMISMATCH:
//                return getString(R.string.ssl_idmismatch);
//            case SslError.SSL_UNTRUSTED:
//                return getString(R.string.dialog_ssl_content);
//            case SslError.SSL_DATE_INVALID:
//                return getString(R.string.ssl_date_invalid);
//        }
        return getString(R.string.ssl_security_tip);
    }
    
    private LinearLayout layout;
    private LinearLayout getIssuedView(String name, String value) {
        if (TextUtils.isEmpty(name))
            return null;
        if (TextUtils.isEmpty(value))
            value = "";

        layout = new LinearLayout(this);
        layout.removeAllViews();
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, getResources().getDimensionPixelSize(R.dimen.issued_padding_top), 0, 0);
        TextView textName = new TextView(this);
        textName.setText(name);
        textName.setTextColor(getResources().getColor(R.color.dialog_ssl_text_font_color));
        TextView textValue = new TextView(this);
        textValue.setText(value);
        textValue.setTextColor(getResources().getColor(R.color.dialog_ssl_text_font_color));
        layout.addView(textName);
        layout.addView(textValue);
        return layout;
    }

    private SafeSslErrorHandler.SafeSslErrorContext getSafeSslErrorContext() {
        return mSafeSslErrorHandlerRef.getErrContext(mErrContextId);
    }

    @Override
    public void onBackPressed() {
        if(mLevel == LEVEL_SSL_DIALOG){
            this.finish();
        }else if(mLevel == LEVEL_CERTIFICATE_DIALOG){
            inflateSSLDialog();
        }
    }
    
    @Override
    protected void onDestroy() {
        mLevel = -1; 
        super.onDestroy();
    }

}
