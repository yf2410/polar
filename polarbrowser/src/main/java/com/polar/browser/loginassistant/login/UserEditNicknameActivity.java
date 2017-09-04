package com.polar.browser.loginassistant.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CursorDataParserUtils;
import com.polar.browser.utils.CustomToastUtils;

/**
 * Created by yangfan on 2017/5/11.
 */

public class UserEditNicknameActivity extends LemonBaseActivity implements View.OnClickListener{

    private TextView mTvSave;
    private ImageView mIvCancel;
    private EditText mEdit;
    private int RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_nickname);
        initTitlerbar();
        initView();
        initListener();
    }

    private void initListener() {
        mTvSave.setOnClickListener(this);
        mIvCancel.setOnClickListener(this);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editText = s.toString();
                if(editText.length() > 0){
                    mIvCancel.setVisibility(View.VISIBLE);
                    mTvSave.setEnabled(true);
                    mIvCancel.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initTitlerbar() {
        CommonTitleBar titleBar = (CommonTitleBar)findViewById(R.id.title_bar);
        //dtitleBar.setTitleColor(R.drawable.common_tv_title_white_selector);
        titleBar.setBackImg(R.drawable.common_title_left);
    }

    private void initView() {
        mTvSave = (TextView) findViewById(R.id.btn_save);
        mIvCancel = (ImageView) findViewById(R.id.iv_cancel);
        mEdit = (EditText) findViewById(R.id.edit_nickname);
        mTvSave.setEnabled(false);
        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName");
        if(!TextUtils.isEmpty(nickName)){
            mEdit.setCursorVisible(true);
            mEdit.setText(nickName);
            mEdit.setSelection(nickName.length());
            mIvCancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                int length = mEdit.getText().toString().length();

                for (int i = 0; i < length; i++) {
                    if (isEmojiCharacter(mEdit.getText().toString().charAt(i))) {
                        CustomToastUtils.getInstance().showTextToast(R.string.input_again);
                        return;
                    }
                }

                if (mEdit.getText().toString().length() >= 4 && mEdit.getText().toString().length() <= 20) {
                    Intent intent = new Intent();
                    intent.putExtra("nickName", mEdit.getText().toString());
                    this.setResult(RESULT_CODE, intent);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT_MANAGE, GoogleConfigDefine.EDIT_NICKNAME);
                    finish();
                } else {
                    CustomToastUtils.getInstance().showTextToast(R.string.input_again);
                }
                break;
            case R.id.iv_cancel:
                mEdit.setText("");
                mIvCancel.setVisibility(View.GONE);
                break;
        }
    }

    private boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
    }
}

