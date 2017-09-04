package com.polar.browser.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;
import com.polar.browser.utils.PermissionsHelper;

import java.util.ArrayList;

/**
 * Created by duan on 16/9/1.
 */
public class SetPermissionDialog extends CommonBaseDialog implements android.view.View.OnClickListener{

    private View mRoot;
    private View mViewP1;
    private View mViewP2;

    private Context mContext;
    private ArrayList<String> mPermissions;

    /** 强制用户开启存储空间权限,不开启的话,不让其使用 **/
    private boolean mForceExit;

    public SetPermissionDialog(Context context) {
        super(context);
        init();
    }

    public SetPermissionDialog(Context context, int theme, ArrayList<String> permissions) {
        super(context, theme);
        this.mContext = context;
        this.mPermissions = permissions;
        init();
    }

    private void init() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.dialog_set_permission, null);
        addContentView(mRoot, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mViewP1 = mRoot.findViewById(R.id.ll_p1);
        mViewP2 = mRoot.findViewById(R.id.ll_p2);

        for (String permission : mPermissions) {
            if (TextUtils.equals(permission, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mViewP1.setVisibility(View.VISIBLE);
                mForceExit = true;
            } else if (TextUtils.equals(permission, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                mViewP2.setVisibility(View.VISIBLE);
            }
        }

        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);

        if (mForceExit) {
            setCancelable(false);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismiss();
                if (mForceExit) {
                    ((Activity) mContext).finish();
                }
                break;
            case R.id.btn_ok:
                PermissionsHelper.showAppDetail(mContext);
                dismiss();
                break;
            default:
                break;
        }

    }
}
