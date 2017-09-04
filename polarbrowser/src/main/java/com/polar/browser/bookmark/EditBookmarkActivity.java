package com.polar.browser.bookmark;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IAddFavDelegate;
import com.polar.browser.impl.AddFavImpl;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.Site;

import java.sql.SQLException;

public class EditBookmarkActivity extends LemonBaseActivity implements OnClickListener {

	private EditText mEtName;
	private EditText mEtUrl;
	private View mBtnClearName;
	private View mBtnClearUrl;

	private ToggleButton mTbBookmark;
	private ToggleButton mTbAddLogo;
	private ToggleButton mTbShortcut;

	//private ImageView mIvCbBookmark;
	//private ImageView mIvCbAddLogo;
	//private ImageView mIvCbShortcut;

	private int mId = -1;
	private String mName;
	private String mUrl;

	private IAddFavDelegate mAddFavDelegate;

	private CommonTitleBar mTitleBar;
	private ImageView mIvAddFav;
	private ImageView mIvAddLogo;
	private ImageView mIvAddShortcut;

	private boolean isAddFav;
	private boolean isAddShortcut;
	private boolean isAddLogo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmark_edit);
		initView();
		initData();
		initListener();
	}

	private void initView() {
		mEtName = (EditText) findViewById(R.id.et_name);
		mEtUrl = (EditText) findViewById(R.id.et_url);
		mBtnClearName = findViewById(R.id.btn_clear_name);
		mBtnClearUrl = findViewById(R.id.btn_clear_url);
		mTbBookmark = (ToggleButton) findViewById(R.id.tb_bookmark);
		mTbAddLogo = (ToggleButton) findViewById(R.id.tb_add_logo);
		mTbShortcut = (ToggleButton) findViewById(R.id.tb_shortcut);
		//mIvCbBookmark = (ImageView) findViewById(R.id.cb_add_fav);
		//mIvCbAddLogo = (ImageView) findViewById(R.id.cb_add_logo);
		//mIvCbShortcut = (ImageView) findViewById(R.id.cb_add_shortcut);
		mTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		mIvAddFav = (ImageView) findViewById(R.id.iv_add_fav);
		mIvAddLogo = (ImageView) findViewById(R.id.iv_add_logo);
		mIvAddShortcut = (ImageView) findViewById(R.id.iv_add_shortcut);

		mTitleBar.setSettingVisible(true);
		mTitleBar.setSettingTxt(R.string.save);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		final InputMethodManager mInputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				try {
					mInputManager.showSoftInput(mEtName, InputMethodManager.SHOW_IMPLICIT);
					mInputManager.showSoftInputFromInputMethod(
							mEtName.getApplicationWindowToken(),
							InputMethodManager.SHOW_IMPLICIT);
				} catch (Throwable e) {
				}
			}
		}, 500);
	}

	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
            isAddFav = intent.getBooleanExtra("isAddFav" , false);
            isAddShortcut = intent.getBooleanExtra("isAddShortcut" , false);
            isAddLogo = intent.getBooleanExtra("isAddLogo" , false);
			mId = intent.getIntExtra(BookmarkInfo.KEY_ID, -1);
			mName = intent.getStringExtra(BookmarkInfo.KEY_NAME);
			mUrl = intent.getStringExtra(BookmarkInfo.KEY_URL);
			if (!TextUtils.isEmpty(mName)) {
				mEtName.post(new Runnable() {
					@Override
					public void run() {
						mEtName.setText(mName);
						mEtName.setSelection(mName.length());
					}
				});
			}
			if (!TextUtils.isEmpty(mUrl)) {
				mEtUrl.setText(mUrl);
			}
		}
		mAddFavDelegate = new AddFavImpl();
		mTbBookmark.setChecked(true);
		if (mId != -1) {
			// 编辑收藏，不允许取消掉收藏
			mTbBookmark.setEnabled(false);
			mTitleBar.setTitle(R.string.edit_url);
		}

		if(BookmarkManager.getInstance().isUrlExist(mUrl)){
			mIvAddFav.setImageResource(R.drawable.edit_bookmark_pre);
			mTbBookmark.setClickable(false);
		}else {
			mTbBookmark.setClickable(true);
			if(isAddFav){
				//mIvCbBookmark.setImageResource(R.drawable.common_checkbox1_checked);
				mIvAddFav.setImageResource(R.drawable.add_favyes_pre);

			}else {
				//mIvCbBookmark.setImageResource(R.drawable.common_checkbox1_unchecked);
				mIvAddFav.setImageResource(R.drawable.add_favyes_nor);
			}
		}

		/*mIvCbBookmark.setImageResource(R.drawable.common_checkbox1_checked);
		mIvAddFav.setImageResource(R.drawable.edit_bookmark_pre);*/
		//boolean exist = false;
		/*try {
			exist = SiteManager.getInstance().exist(new Site(mName, mUrl));
		} catch (SQLException e) {
			e.printStackTrace();
		}*/

		try {
			if(SiteManager.getInstance().exist(new Site(mName,mUrl))){
				mTbAddLogo.setClickable(false);
				mIvAddLogo.setImageResource(R.drawable.edit_scanner_pre);
			}else {
				mTbAddLogo.setClickable(true);
				if (isAddLogo) {
					mTbAddLogo.setChecked(true);
					//mIvCbAddLogo.setImageResource(R.drawable.common_checkbox1_checked);
					mIvAddLogo.setImageResource(R.drawable.add_logoyes_pre);

				} else {
					mTbAddLogo.setChecked(false);
					//mIvCbAddLogo.setImageResource(R.drawable.common_checkbox1_unchecked);
					mIvAddLogo.setImageResource(R.drawable.add_logoyes_nor);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(ConfigWrapper.get(mUrl, false)){
			mIvAddShortcut.setImageResource(R.drawable.edit_phone_pre);
			mTbShortcut.setClickable(false);
		}else {
			mTbShortcut.setClickable(true);
			if (isAddShortcut) {
				mTbShortcut.setChecked(true);
				//mIvCbShortcut.setImageResource(R.drawable.common_checkbox1_checked);
				mIvAddShortcut.setImageResource(R.drawable.add_shortcut_pre);

			} else {
				mTbShortcut.setChecked(false);
				//mIvCbShortcut.setImageResource(R.drawable.common_checkbox1_unchecked);
				mIvAddShortcut.setImageResource(R.drawable.add_shortcut_nor);
			}
		}
	}

	private void initListener() {
		mBtnClearName.setOnClickListener(this);
		mBtnClearUrl.setOnClickListener(this);
		mEtName.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mBtnClearName.setVisibility(View.VISIBLE);
				} else {
					mBtnClearName.setVisibility(View.GONE);
				}
			}
		});
		mEtName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String name = s.toString();
				if (TextUtils.isEmpty(name)) {
					mBtnClearName.setVisibility(View.GONE);
				} else {
					mBtnClearName.setVisibility(View.VISIBLE);
				}
			}
		});
		mEtUrl.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mBtnClearUrl.setVisibility(View.VISIBLE);
				} else {
					mBtnClearUrl.setVisibility(View.GONE);
				}
			}
		});
		mEtUrl.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String url = s.toString();
				if (TextUtils.isEmpty(url)) {
					mBtnClearUrl.setVisibility(View.GONE);
				} else {
					mBtnClearUrl.setVisibility(View.VISIBLE);
				}
			}
		});
		findViewById(R.id.common_tv_setting).setOnClickListener(this);
		mTbBookmark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SimpleLog.e("OnCheckedChangeListener", "mTbBookmark " + isChecked);
				hideIM();
				if (isChecked) {
					//mIvCbBookmark.setImageResource(R.drawable.common_checkbox1_checked);
					mIvAddFav.setImageResource(R.drawable.add_favyes_pre);
				} else {
					//mIvCbBookmark.setImageResource(R.drawable.common_checkbox1_unchecked);
					mIvAddFav.setImageResource(R.drawable.add_favyes_nor);
				}
			}
		});
		mTbAddLogo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				hideIM();
				SimpleLog.e("OnCheckedChangeListener", "mTbAddLogo " + isChecked);
				if (isChecked) {
					//mIvCbAddLogo.setImageResource(R.drawable.common_checkbox1_checked);
					mIvAddLogo.setImageResource(R.drawable.add_logoyes_pre);
				} else {
					//mIvCbAddLogo.setImageResource(R.drawable.common_checkbox1_unchecked);
					mIvAddLogo.setImageResource(R.drawable.add_logoyes_nor);
				}
			}
		});
		mTbShortcut.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				hideIM();
				SimpleLog.e("OnCheckedChangeListener", "mTbShortcut " + isChecked);
				if (isChecked) {
					//mIvCbShortcut.setImageResource(R.drawable.common_checkbox1_checked);
					mIvAddShortcut.setImageResource(R.drawable.add_shortcut_pre);
					/*ConfigWrapper.put(mUrl, true);
					ConfigWrapper.apply();*/
				} else {
					//mIvCbShortcut.setImageResource(R.drawable.common_checkbox1_unchecked);
					mIvAddShortcut.setImageResource(R.drawable.add_shortcut_nor);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.common_tv_setting:
				// save
				// id != -1; update
				// id == -1; add
				//TODO 点击保存  到收藏界面
				BookmarkManager.goFavPage();
				String name = mEtName.getText().toString();
				String url = mEtUrl.getText().toString();
				if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
					CustomToastUtils.getInstance().showTextToast(R.string.add_logo_empty_tip);
					return;
				}
				name = name.replace(" ", "");
				url = url.replace(" ", "");
				if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
					CustomToastUtils.getInstance().showTextToast(R.string.add_logo_empty_tip);
					return;
				}
				if (!mTbBookmark.isChecked() && !mTbAddLogo.isChecked() && !mTbShortcut.isChecked()) {
					CustomToastUtils.getInstance().showTextToast(R.string.add_logo_checke_empty_tip);
					return;
				}
				// check title url length
				// update
				if (mTbBookmark.isChecked()) {
					if (mId != -1) {
						BookmarkManager.getInstance().updateBookmarkById(mId, name, url);
					} else {
						// 判断url是否存在
						boolean exist = BookmarkManager.getInstance().isUrlExist(url);
						if (exist) {
							BookmarkInfo info = BookmarkManager.getInstance().queryBookmarkInfoByUrl(url);
							if (info != null) {
								BookmarkManager.getInstance().updateBookmarkById(info.id, name, url);
							}
						} else {
							BookmarkManager.getInstance().addBookmark(name, url);
						}
					}
				}
				if (mTbAddLogo.isChecked()) {
					mAddFavDelegate.addLogo(name, url);
				}
				if (mTbShortcut.isChecked()) {
					mAddFavDelegate.addShortcut(this, name, url);
				}

				if(mTbShortcut.isChecked()){
					ConfigWrapper.put(mUrl, true);
					ConfigWrapper.apply();
				}

				BookmarkManager.getInstance().isBookMarkChanged = true;
				finish();
				break;
			case R.id.btn_clear_name:
				mEtName.setText("");
				break;
			case R.id.btn_clear_url:
				mEtUrl.setText("");
				break;
			default:
				break;
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
	}
}
