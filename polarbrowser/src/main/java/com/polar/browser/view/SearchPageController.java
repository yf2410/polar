package com.polar.browser.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.IShowOrHideDelegate;
import com.polar.browser.impl.WebViewClientImpl;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.DensityUtil;

/**
 * Created by saifei on 16/9/14.
 * 查找页面
 * 逻辑控制
 */
public class SearchPageController implements View.OnClickListener, IShowOrHideDelegate ,WebViewClientImpl.OnNeedHideCallBack{


    private LemonBaseActivity activity;

    private ISearchPageCallBack mSearchCallBack;
    private View searchPageTopLayout;
    private EditText inputEt;
    private View searchControlLayout;
    private View prevLayout;
    private View nextLayout;
    private ImageButton closeImageButton;
    private TextView searchResultTv;
    private boolean isNeedShow = false;
    // 容纳实际页面内容的引用
    private ViewGroup mContentFrameRef;
//    private TextView clear_search_tv;
    private View searchInputLayout;
    private View nextIv;
    private ImageView prevIv;


    public SearchPageController(LemonBaseActivity activity, ISearchPageCallBack mSearchCallBack, ViewGroup content) {
        this.activity = activity;
        this.mSearchCallBack = mSearchCallBack;
        this.mContentFrameRef = content;
        init();
    }

    private void init() {


        searchPageTopLayout = findViewById(R.id.search_page_top);
        hideWithoutAnim();
        inputEt = (EditText) findViewById(R.id.edit_text_search_page);


        searchControlLayout = findViewById(R.id.search_control_layout);

        prevLayout = findViewById(R.id.search_page_prev_layout);
        prevIv  = (ImageView) findViewById(R.id.search_page_prev_iv);
        nextLayout = findViewById(R.id.search_page_next_layout);
        nextIv = findViewById(R.id.search_page_next_iv);


        closeImageButton = (ImageButton) findViewById(R.id.imagebutton_close_search_page);

        searchResultTv = (TextView) findViewById(R.id.search_page_result_tv);
//        clear_search_tv = (TextView) findViewById(R.id.clear_search_tv);

        searchInputLayout = findViewById(R.id.search_input_layout);

        setListeners();

    }

    private void setListeners() {
        closeImageButton.setOnClickListener(this);

        inputEt.addTextChangedListener(mTextWatcher);
        inputEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_ENTER){
                    hideIm();
                    mSearchCallBack.onSearch(inputEt.getText().toString());

                    inputEt.clearFocus();
                    searchResultTv.setVisibility(TextUtils.isEmpty(inputEt.getText().toString())?View.GONE:View.VISIBLE);
                    searchControlLayout.setVisibility(TextUtils.isEmpty(inputEt.getText().toString()) ?View.GONE:View.VISIBLE);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.FIND_IN_PAGE,GoogleConfigDefine.COMMIT_KEY_WORD);
                }
                return false;
            }
        });

//        inputEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                searchResultTv.setVisibility(hasFocus?View.GONE:View.VISIBLE);
//
//                searchControlLayout.setVisibility(hasFocus ?View.GONE:View.VISIBLE);
//            }
//        });

        prevLayout.setOnClickListener(this);
        nextLayout.setOnClickListener(this);
//        clear_search_tv.setOnClickListener(this);

    }


    private View findViewById(int id) {
        return activity.findViewById(id);
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            searchResultTv.setTextColor(activity.getResources().getColor(R.color.black54));
            searchResultTv.setText("");
            mSearchCallBack.onSearch(s.toString());
            searchResultTv.setVisibility(TextUtils.isEmpty(s.toString())?View.GONE:View.VISIBLE);
//            clear_search_tv.setVisibility(TextUtils.isEmpty(s.toString()) ? View.GONE : View.VISIBLE);
            searchControlLayout.setVisibility(TextUtils.isEmpty(s.toString()) ?View.GONE:View.VISIBLE);
        }
    };

    private void resetInputLayout() {
        searchResultTv.setText("");
        inputEt.setText("");
        inputEt.setHint(activity.getString(R.string.search_page_tip));
        searchControlLayout.setVisibility(View.GONE);
//        clear_search_tv.setVisibility(View.GONE);
        searchResultTv.setVisibility(View.GONE);
        searchInputLayout.setBackgroundColor(Color.WHITE);
    }


    @Override
    public void show() {
        searchPageTopLayout.setVisibility(View.VISIBLE);
        modifyContentLayouMargin();
    }

    public void showWithoutAnim() {
        searchPageTopLayout.setVisibility(View.VISIBLE);
        inputEt.requestFocus();
        showIm(inputEt);


    }

    private void modifyContentLayouMargin() {

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentFrameRef.getLayoutParams();
        if (lp.topMargin <= 0) {
            lp.setMargins(0, searchPageTopLayout.getHeight() - DensityUtil.dip2px(activity, 4) - 1, 0, 0);
            mContentFrameRef.setLayoutParams(lp);
        }
    }

    @Override
    public void hide() {


    }


    public void hideWithoutAnim() {
        hideIm();
        searchPageTopLayout.setVisibility(View.GONE);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentFrameRef.getLayoutParams();
        lp.setMargins(0, 0, 0, 0);
        mContentFrameRef.setLayoutParams(lp);
    }

    @Override
    public boolean isShown() {
        return searchPageTopLayout.getVisibility() == View.VISIBLE;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imagebutton_close_search_page:
                onClickClose();

                break;
            case R.id.search_page_prev_layout:
                mSearchCallBack.onPrev();
                Statistics.sendOnceStatistics(GoogleConfigDefine.FIND_IN_PAGE,GoogleConfigDefine.SWITCH_PREV);
                break;
            case R.id.search_page_next_layout:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FIND_IN_PAGE,GoogleConfigDefine.SWITCH_NEXT);
                mSearchCallBack.onNext();
                break;
            case R.id.clear_search_tv:
                resetInputLayout();

                break;

        }

    }

    public void onClickClose() {
        setNeedShow(false);
        mSearchCallBack.onClickClose();
        resetInputLayout();
        searchPageTopLayout.setVisibility(View.GONE);
        hideIm();
    }

    public void setSearchResultCount(int current, int total) {
        if(current==0&&total==0){
            searchResultTv.setTextColor(activity.getResources().getColor(R.color.search_no_result_color));

        }else{
            searchResultTv.setTextColor(activity.getResources().getColor(R.color.black54));
        }
        searchResultTv.setText(current + "/" + total);

        prevLayout.setEnabled(total!=0&&current!=1);
        nextLayout.setEnabled(total!=0&&current!=total);
        prevIv.setEnabled(total!=0&&current!=1);
        nextIv.setEnabled(total!=0&&current!=total);


    }


    public boolean isNeedShow() {
        return isNeedShow;
    }

    public void setNeedShow(boolean needShow) {
        isNeedShow = needShow;
        if (!needShow) {
            resetInputLayout();
        }
    }

    private void showIm(EditText editText) {
        InputMethodManager inputManager =
                (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    private void hideIm() {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    public void onNeedHide() {
        if(isShown()){
            setNeedShow(false);
            hideWithoutAnim();
        }
    }
}
