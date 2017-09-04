package com.polar.browser.homepage.sitelist.recommand;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.homepage.customlogo.IComplete;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.homepage.sitelist.common.SiteAdapter;
import com.polar.browser.homepage.sitelist.common.SiteListView;
import com.polar.browser.homepage.sitelist.recommand.bookmark.SiteFromBookmarkActivity;
import com.polar.browser.homepage.sitelist.recommand.exception.HomeSiteExistException;
import com.polar.browser.homepage.sitelist.recommand.exception.OutOfMaxNumberException;
import com.polar.browser.homepage.sitelist.recommand.history.SiteFromHistoryActivity;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;

import java.sql.SQLException;

/**
 * 推荐列表
 */
public class RecommendView extends SiteListView implements OnClickListener {

    private EditText mEtTitle;
    private EditText mEtUrl;
    private TextView mBtnConfirm;
    private TextView mBtnCancel;
    private CommonDialog dialog;
    private LinearLayout footerView;
    private View mView;

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            String title = mEtTitle.getText().toString();
            String url = mEtUrl.getText().toString();
            if (!TextUtils.isEmpty(title)) {
                title = title.trim();
            }
            if (!TextUtils.isEmpty(url)) {
                url = url.trim();
            }
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(url)) {
                mBtnConfirm.setEnabled(true);
            } else {
                mBtnConfirm.setEnabled(false);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    };

    public RecommendView(Context context) {
        this(context, null);
    }

    public RecommendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        mAdapter = new SiteAdapter(getContext());
//        CustomUrlAddView headerView = new CustomUrlAddView(getContext());
        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.view_head_custom_url, null);
        addHeaderView(headerView, null, true);
        //禁止头部出现分割线
        setHeaderDividersEnabled(false);
        headerView.findViewById(R.id.line_custom_edit).setOnClickListener(this);
        headerView.findViewById(R.id.line_custom_from_history).setOnClickListener(this);
        headerView.findViewById(R.id.line_custom_from_favorite).setOnClickListener(this);
        mView = LayoutInflater.from(getContext()).inflate(R.layout.view_footer, null);
        footerView = (LinearLayout) mView.findViewById(R.id.foot_content);
        addFooterView(mView, null, false);
        //禁止底部出现分割线
        setFooterDividersEnabled(false);
        setAdapter(mAdapter);
    }


    public void setComplete(IComplete complete) {
        mComplete = complete;
    }


    private void openEditDialog() {
        dialog = new CommonDialog(getContext());
        dialog.setBottomView(R.layout.view_bottom_bar);
        dialog.setTitleView(R.layout.view_title);
        dialog.setCenterView(R.layout.view_edit_logo_center);
        mEtTitle = (EditText) dialog.findViewById(R.id.et_title);
        mEtUrl = (EditText) dialog.findViewById(R.id.et_url);
        mEtTitle.addTextChangedListener(mWatcher);
        mEtUrl.addTextChangedListener(mWatcher);
        mBtnConfirm = (TextView) dialog.findViewById(R.id.tv_add);
        mBtnConfirm.setEnabled(false);
        mBtnCancel = (TextView) dialog.findViewById(R.id.tv_cancel);
        mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideIM();
                String siteName = mEtTitle.getText().toString().trim();
                String siteAddr = mEtUrl.getText().toString().trim();
                if (TextUtils.isEmpty(siteName) || TextUtils.isEmpty(siteAddr)) {
                    return;
                }
                siteAddr = UrlUtils.checkUrlIsContainsHttp(siteAddr);
                try {
                    SiteManager siteManager = SiteManager.getInstance();
                    Site site = new Site(siteName, siteAddr);
                    if (siteManager.add2Home(site, true)) {
                        CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
                    }
                } catch (OutOfMaxNumberException e) {
                    e.printStackTrace();
                    CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_max_tip);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (HomeSiteExistException e) {
                    e.printStackTrace();
                    CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mEtTitle.setText("");
                mEtUrl.setText("");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void hideIM() {
        post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && ((Activity) getContext()).getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(((Activity) getContext()).getCurrentFocus().getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.line_custom_edit:
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.SHORTCUT, GoogleConfigDefine.SHORTCUT_CUSTOM_CONFIRM);
                openEditDialog();
                break;
            case R.id.line_custom_from_history:
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.SHORTCUT, GoogleConfigDefine.SHORTCUT_HISTORY);
                openHistoryList();
                break;
            case R.id.line_custom_from_favorite:
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.SHORTCUT, GoogleConfigDefine.SHORTCUT_BOOKMARKS);
                openBookmarkList();
                break;
            default:
                break;
        }
    }

    private void openBookmarkList() {
        Intent intent = new Intent(getContext(), SiteFromBookmarkActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    private void openHistoryList() {
        Intent intent = new Intent(getContext(), SiteFromHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    public void hideEmptyView() {
        if (footerView != null) {
            footerView.setVisibility(View.GONE);
        }
        mView.setVisibility(View.GONE);
    }


    public void showEmptyView() {
        mView.setVisibility(View.VISIBLE);
        if (footerView != null) {
            footerView.setVisibility(View.VISIBLE);
        }
    }

}
