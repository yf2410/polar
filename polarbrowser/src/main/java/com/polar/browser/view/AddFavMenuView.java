package com.polar.browser.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.bookmark.BookmarkInfo;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.bookmark.EditBookmarkActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.homepage.sitelist.recommand.exception.HomeSiteExistException;
import com.polar.browser.homepage.sitelist.recommand.exception.OutOfMaxNumberException;
import com.polar.browser.i.IAddFavDelegate;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.shortcut.MatchResult;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.Site;

import java.io.File;
import java.sql.SQLException;

/**
 * 添加网址界面,逻辑在mDelegate中
 *
 * @author dpk
 */
public class AddFavMenuView extends RelativeLayout implements OnClickListener {

	private View mMenuArea;
	private View mBackground;
	private TextView mAddFavBtn;
	private TextView mAddLogoBtn;
	private TextView mAddShortcutBtn;
	private TextView mTitle;
	private View mEditBtn;

	private IAddFavDelegate mDelegate;
	private boolean mIsInit = false;
	private Button mBtnSave;
	private boolean isAddFav = true;
	private boolean favCanClickable = true;
	private boolean logoCanClickable = true;
	private boolean shortcutCanClickable = true;
	private boolean isAddShortcut = false;
	private boolean isAddLogo = true;
	public AddFavMenuView(Context context) {
		this(context, null);
	}

	public AddFavMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(IAddFavDelegate delegate) {
		LayoutInflater.from(getContext()).inflate(R.layout.view_addfav, this);
		mIsInit = true;
		mDelegate = delegate;
		mMenuArea = findViewById(R.id.addfav_menu);
		mBackground = findViewById(R.id.addfav_menu_background);
		mBtnSave = (Button) findViewById(R.id.btn_add_save);
		mAddFavBtn = (TextView) findViewById(com.polar.browser.R.id.btn_add_fav);
		mAddLogoBtn = (TextView) findViewById(com.polar.browser.R.id.btn_add_logo);
		mAddShortcutBtn = (TextView) findViewById(R.id.btn_add_shortcut);
		mTitle = (TextView) findViewById(com.polar.browser.R.id.addfav_title);
		mEditBtn = findViewById(R.id.btn_edit);
		mTitle.setText(mDelegate.getTitle());
		mBtnSave.setOnClickListener(this);
		mAddFavBtn.setOnClickListener(this);
		mAddLogoBtn.setOnClickListener(this);
		mAddShortcutBtn.setOnClickListener(this);
		mBackground.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
	}

	public void initTitle() {
		if (mTitle != null && mDelegate != null) {
			String url = mDelegate.getUrl();
			String title = mDelegate.getTitle();
			if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(url)) {
				BookmarkInfo info = BookmarkManager.getInstance()
						.queryBookmarkInfoByUrl(url);
				if (info != null) {
					if (!TextUtils.isEmpty(info.name)) {
						title = info.name;
					}
				}
			}
			if (TextUtils.isEmpty(title)) {
				title = url;
			}
			mTitle.setText(title);
			//判断该网页是否添加过书签、快捷方式、手机桌面（改变图标显示）
			isAddFav = true;
			isAddLogo = true;
			/*Drawable addFavDrawables = getResources().getDrawable(R.drawable.add_favyes);
			addFavDrawables.setBounds(0, 0, addFavDrawables.getMinimumWidth(), addFavDrawables.getMinimumHeight());
			mAddFavBtn.setCompoundDrawables(null, addFavDrawables, null, null);

			Drawable addLogoDrawables = getResources().getDrawable(R.drawable.add_logoyes);
			addLogoDrawables.setBounds(0, 0, addLogoDrawables.getMinimumWidth(), addLogoDrawables.getMinimumHeight());
			mAddLogoBtn.setCompoundDrawables(null, addLogoDrawables, null, null);*/

			if (ConfigWrapper.get(url, false)) {
				isAddShortcut = true;
				Drawable addShortcutDrawables = getResources().getDrawable(R.drawable.edit_phone_pre);
				addShortcutDrawables.setBounds(0, 0, addShortcutDrawables.getMinimumWidth(), addShortcutDrawables.getMinimumHeight());
				mAddShortcutBtn.setCompoundDrawables(null, addShortcutDrawables, null, null);
				//mAddShortcutBtn.setClickable(false);
				shortcutCanClickable = false;
				isAddShortcut = false;
			} else {
				isAddShortcut = false ;
				Drawable addShortcutDrawable = getResources().getDrawable(R.drawable.add_shortcut_nor);
				addShortcutDrawable.setBounds(0, 0, addShortcutDrawable.getMinimumWidth(), addShortcutDrawable.getMinimumHeight());
				mAddShortcutBtn.setCompoundDrawables(null, addShortcutDrawable, null, null);
				shortcutCanClickable = true;
				isAddShortcut = false;
			}

			try {
				if (!BookmarkManager.getInstance().isUrlExist(url) && !SiteManager.getInstance().exist(new Site(title, url))) {
					Drawable addFavDrawables = getResources().getDrawable(R.drawable.add_favyes_pre);
					addFavDrawables.setBounds(0, 0, addFavDrawables.getMinimumWidth(), addFavDrawables.getMinimumHeight());
					mAddFavBtn.setCompoundDrawables(null, addFavDrawables, null, null);

					Drawable addLogoDrawables = getResources().getDrawable(R.drawable.add_logoyes_pre);
					addLogoDrawables.setBounds(0, 0, addLogoDrawables.getMinimumWidth(), addLogoDrawables.getMinimumHeight());
					mAddLogoBtn.setCompoundDrawables(null, addLogoDrawables, null, null);

					favCanClickable = true;
					logoCanClickable = true;
				}else {
					if (!BookmarkManager.getInstance().isUrlExist(url)) {
						Drawable addFavDrawable = getResources().getDrawable(R.drawable.add_favyes_nor);
						addFavDrawable.setBounds(0, 0, addFavDrawable.getMinimumWidth(), addFavDrawable.getMinimumHeight());
						mAddFavBtn.setCompoundDrawables(null, addFavDrawable, null, null);
						favCanClickable = true;
						isAddFav = false;
					} else {
						Drawable addFavDrawables = getResources().getDrawable(R.drawable.edit_bookmark_pre);
						addFavDrawables.setBounds(0, 0, addFavDrawables.getMinimumWidth(), addFavDrawables.getMinimumHeight());
						mAddFavBtn.setCompoundDrawables(null, addFavDrawables, null, null);
						//mAddFavBtn.setClickable(false);
						favCanClickable = false;
						isAddFav = false;
					}
					try {
						if (!SiteManager.getInstance().exist(new Site(title,url))) {
							Drawable addLogoDrawable = getResources().getDrawable(R.drawable.add_logoyes_nor);
							addLogoDrawable.setBounds(0, 0, addLogoDrawable.getMinimumWidth(), addLogoDrawable.getMinimumHeight());
							mAddLogoBtn.setCompoundDrawables(null, addLogoDrawable, null, null);
							logoCanClickable = true;
							isAddLogo = false;
						} else {
							Drawable addLogoDrawables = getResources().getDrawable(R.drawable.edit_scanner_pre);
							addLogoDrawables.setBounds(0, 0, addLogoDrawables.getMinimumWidth(), addLogoDrawables.getMinimumHeight());
							mAddLogoBtn.setCompoundDrawables(null, addLogoDrawables, null, null);
							//mAddLogoBtn.setClickable(false);
							logoCanClickable = false;
							isAddLogo = false;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isInit() {
		return mIsInit;
	}

	public void show() {
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.slide_in_from_bottom);
		mMenuArea.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_in);
		mBackground.startAnimation(backgroundAnim);
		setVisibility(View.VISIBLE);
	}

	public void hide() {
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.menu_slide_out_to_bottom);
		mMenuArea.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_out);
		mBackground.startAnimation(backgroundAnim);
		backgroundAnim.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mBackground)) {
			hide();
		} else if(v.equals(mBtnSave)){
			hide();

			//添加书签、快捷方式、手机桌面
			if(isAddFav && isAddLogo && isAddShortcut){
				addBookmark();
				addLogo();
				addShortcut(getContext());
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
				return;
			}
			//添加书签、快捷方式
			if(isAddFav && isAddLogo){
				addBookmark();
				addLogo();
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
				return;
			}
			//添加书签、手机桌面
			if(isAddFav && isAddShortcut){
				addBookmark();
				addShortcut(getContext());
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
				return;
			}
			//添加快捷方式、手机桌面
			if(isAddLogo && isAddShortcut){
				addLogo();
				addShortcut(getContext());
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
				return;
			}
			if (isAddFav) {
				BookmarkManager.goFavPage();
				if (mDelegate != null) {
					mDelegate.addFav();
				}
				Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_FAV);
			}
			if (isAddShortcut) {
				Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_SHORTCUT);
				String model = SystemUtils.getModel();
				if (model.equals("ZTE N918St")) {
					Toast toast = Toast.makeText(JuziApp.getAppContext(),
							R.string.add_shortcut_failed, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				if (mDelegate != null) {
					mDelegate.addShortcut(getContext());
				}
			}

			if (isAddLogo) {
				if (mDelegate != null) {
					mDelegate.addLogo();
				}
				Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_LOGO);
			}
		}else if (v.equals(mAddFavBtn)) {
			if(!favCanClickable){
				CustomToastUtils.getInstance().showImgToast(R.string.already_add_bookmark_tips, R.drawable.address_bookmark_star_added);
				return;
			}
			if(isAddFav){
				isAddFav = false;
				Drawable addFavDrawable = getResources().getDrawable(R.drawable.add_favyes_nor);
				addFavDrawable.setBounds(0, 0, addFavDrawable.getMinimumWidth(), addFavDrawable.getMinimumHeight());
				mAddFavBtn.setCompoundDrawables(null, addFavDrawable, null, null);
			}else{
				isAddFav = true;
				Drawable addFavDrawables = getResources().getDrawable(R.drawable.add_favyes_pre);
				addFavDrawables.setBounds(0, 0, addFavDrawables.getMinimumWidth(), addFavDrawables.getMinimumHeight());
				mAddFavBtn.setCompoundDrawables(null, addFavDrawables, null, null);
			}
			//hide();
		} else if (v.equals(mAddShortcutBtn)) {
			//hide();
			if(!shortcutCanClickable){
				CustomToastUtils.getInstance().showTextToast(R.string.already_add_shortcut_successful);
				return;
			}
			if (isAddShortcut) {
				isAddShortcut = false;
				Drawable addShortcutDrawable = getResources().getDrawable(R.drawable.add_shortcut_nor);
				addShortcutDrawable.setBounds(0, 0, addShortcutDrawable.getMinimumWidth(), addShortcutDrawable.getMinimumHeight());
				mAddShortcutBtn.setCompoundDrawables(null, addShortcutDrawable, null, null);
			} else {
				isAddShortcut = true;
				Drawable addShortcutDrawables = getResources().getDrawable(R.drawable.add_shortcut_pre);
				addShortcutDrawables.setBounds(0, 0, addShortcutDrawables.getMinimumWidth(), addShortcutDrawables.getMinimumHeight());
				mAddShortcutBtn.setCompoundDrawables(null, addShortcutDrawables, null, null);
			}



			/*Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_SHORTCUT);
			String model = SystemUtils.getModel();
			if (model.equals("ZTE N918St")) {
				Toast toast = Toast.makeText(JuziApp.getAppContext(),
						R.string.add_shortcut_failed, Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			if (mDelegate != null) {
				mDelegate.addShortcut(getContext());
			}*/
		} else if (v.equals(mAddLogoBtn)) {
			//hide();
			if(!logoCanClickable){
				CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
				return;
			}
			if(isAddLogo){
				isAddLogo = false;
				Drawable addLogoDrawable = getResources().getDrawable(R.drawable.add_logoyes_nor);
				addLogoDrawable.setBounds(0, 0, addLogoDrawable.getMinimumWidth(), addLogoDrawable.getMinimumHeight());
				mAddLogoBtn.setCompoundDrawables(null, addLogoDrawable, null, null);
			}else{
				isAddLogo = true;
				Drawable addLogoDrawables = getResources().getDrawable(R.drawable.add_logoyes_pre);
				addLogoDrawables.setBounds(0, 0, addLogoDrawables.getMinimumWidth(), addLogoDrawables.getMinimumHeight());
				mAddLogoBtn.setCompoundDrawables(null, addLogoDrawables, null, null);
			}
			/*if (mDelegate != null) {
				mDelegate.addLogo();
			}
			Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_LOGO);*/
		} else if (v.equals(mEditBtn)) {
			hide();
			Intent intent = new Intent(getContext(), EditBookmarkActivity.class);
			String name = TabViewManager.getInstance().getCurrentTitle();
			String url = TabViewManager.getInstance().getCurrentUrl();
			intent.putExtra(BookmarkInfo.KEY_NAME, name);
			intent.putExtra(BookmarkInfo.KEY_URL, url);
			intent.putExtra("isAddFav" , isAddFav);
			intent.putExtra("isAddShortcut" , isAddShortcut);
			intent.putExtra("isAddLogo" , isAddLogo);
			getContext().startActivity(intent);
			((Activity) getContext()).overridePendingTransition(
					R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_BTN_EDIT);
		}
	}

	private void addShortcut(Context context) {
		String model = SystemUtils.getModel();
		if (model.equals("ZTE N918St")) {
			Toast toast = Toast.makeText(JuziApp.getAppContext(),
					R.string.add_shortcut_failed, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		String url = TabViewManager.getInstance().getCurrentUrl();
		String title = TabViewManager.getInstance().getCurrentTitle();
		MatchResult result = ParseConfig.getMatchResult(url);
		if (result.id != -1) {
			if (!TextUtils.isEmpty(result.title)) {
//				title = result.title;
			}
			File icon = new File(ParseConfig.sFolderPath + File.separator + String.valueOf(result.id) + ".png");
			if (icon.exists()) {
				SysUtils.createShotcut(context, url, title, icon.getAbsolutePath());
			} else {
				SysUtils.createShotcut(context, url, title, null);
			}
		} else {
			SysUtils.createShotcut(context, url, title, null);
		}
		ConfigWrapper.put(url, true);
		ConfigWrapper.apply();
		Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_SHORTCUT);
	}

	private void addLogo() {
		String url = TabViewManager.getInstance().getCurrentUrl();
		String title = TabViewManager.getInstance().getCurrentTitle();
		try {
			String iconPath = String.format("%s/%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
					CommonData.ICON_DIR_NAME, UrlUtils.getHost(url));
			Site site = new Site(title, url, iconPath);
			if (SiteManager.getInstance().add2Home(site,true)) {
				CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_add_ok);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// TODO: 2016/10/14 失败
		} catch (OutOfMaxNumberException e) {
			e.printStackTrace();
			CustomToastUtils.getInstance().showTextToast(R.string.edit_logo_max_tip);
		} catch (HomeSiteExistException e) {
			e.printStackTrace();
			CustomToastUtils.getInstance().showTextToast(R.string.already_edit_logo_add_ok);
		}
		Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_LOGO);
	}

	private void addBookmark() {
		BookmarkManager.getInstance().addBookmark( mDelegate.getTitle() , mDelegate.getUrl() , false );
		Statistics.sendOnceStatistics(GoogleConfigDefine.ADD_FAVORITE, GoogleConfigDefine.ADD_FAVORITE_TYPE_FAV);
	}
}
