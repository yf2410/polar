package com.polar.browser.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.ListIconDialog;
import com.polar.browser.env.AppEnv;
import com.polar.browser.history.IHistoryItemClick;
import com.polar.browser.history.IOftenHistoryItemClik;
import com.polar.browser.history.ISearchResultNotifyHindeIM;
import com.polar.browser.history.OftenHistoryAdapter;
import com.polar.browser.i.IHideIMListener;
import com.polar.browser.i.IHideListener;
import com.polar.browser.i.IOpenUrlDelegate;
import com.polar.browser.i.IQuickInputView;
import com.polar.browser.i.ISearchFrame;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.DnsUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.SearchEngineList;
import com.polar.browser.vclibrary.bean.db.SearchRecord;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SearchRecordApi;
import com.polar.browser.vclibrary.util.ImageLoadUtils;
import com.polar.business.search.ISuggestUrl;
import com.polar.business.search.view.QuickInputView.InputDelegate;
import com.polar.business.search.view.SearchResultView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SearchFrame extends RelativeLayout implements
        android.view.View.OnClickListener, ISearchFrame, SearchSuggestionView.IOpenDelegate {
    private static final String TAG = "SearchFrame";
    private static final String HTTP_PREFIX = "http://";
    private static String HORIZONTAL_TOP_MARGIN = "HORIZONTAL_TOP_MARGIN";
    private static String VERTICAL_TOP_MARGIN = "VERTICAL_TOP_MARGIN";
    public EditText mUrlEdit;
    private RelativeLayout mRoot;
    /**
     * www. .com . \ 等 view
     **/
    private IQuickInputView mQuickInputView;
    private TextView mSearchBtn;
    private View mClearBtn;
    /**
     * 搜索推荐结果
     **/
    private SearchResultView mResultView;
    private String mUrl;
    private String mPageUrl;
    /**
     * 剪贴板内容
     */
    private String mContentClipboard;
    private IOpenUrlDelegate mOpenUrlDelegate;
    private IHideListener mHideListener;
    private InputMethodManager mInputManager;
    /**
     * 横屏 margin top
     **/
    private int mHorizontalTopMargin;
    /**
     * www. .com . \ view 是否显示
     **/
    private boolean mQuickInputIsShown;

    /**
     * 竖屏 margin top
     **/
    private int mVerticalTopMargin;
    private View mBrowserView;
    private OftenHistoryAdapter mOftenHistoryAdapter;
    private ListView listViewOftenHistory;
    private LinearLayout cleanTv;

    /**
     * 搜索关键字
     */
    private String mSearchKey;
    /**
     * 搜索关键字列表（用于统计）
     */
    private ArrayList<String> mSearchKeyList = new ArrayList<>();

    private SearchSuggestionView mSuggestionTopView;

    /**
     * 处理QuickInputView输入的内容
     **/
    private InputDelegate mInputDelegate = new InputDelegate() {
        @Override
        public void input(String inputStr) {
            Editable editable = mUrlEdit.getText();
            int start = mUrlEdit.getSelectionStart();
            int end = mUrlEdit.getSelectionEnd();
            if (end - start != 0) {
                // 有选中
                // 清除选中
                editable.delete(start, end);
            }
            if (inputStr.startsWith(".") && editable.toString().endsWith(".")) {
                inputStr = inputStr.substring(1);
            }
            editable.insert(start, inputStr);
        }
    };
    // 点击推荐网址后面的箭头，放到地址栏
    private ISuggestUrl mSuggestUrlImpl = new ISuggestUrl() {

        @Override
        public void addUrl(String url) {
            mUrlEdit.setText(url);
            mUrlEdit.setSelection(url.length());
        }
    };

    private IHistoryItemClick mHistoryItemClickImpl = new IHistoryItemClick() {

        @Override
        public void onClick(String url) {
            if (!url.contains(UrlUtils.PROTOCOL_MARK)) {
                url = UrlUtils.HTTP_PREFIX + url;
            }
            openUrl(url);
            if (url.endsWith(UrlUtils.PROTOCOL_MARK_G)) {
                saveSearchRecord(url,SearchRecord.GO);
            } else {
                saveSearchRecord(url+UrlUtils.PROTOCOL_MARK_G,SearchRecord.GO);
            }
        }

        @Override
        public void onCheckedChange() {
        }
    };

    private ISearchResultNotifyHindeIM mISearchResultNotifyHindeIM = new ISearchResultNotifyHindeIM() {
        @Override
        public void onNotifyHindeIM() {
            hideIM();
        }
    };

    private OnGlobalLayoutListener mOGLListener = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isShown()) {
                return;
            }

            if (mRoot.getHeight() < AppEnv.SCREEN_HEIGHT * 4 / 5) {
                if (!mQuickInputIsShown) {
                    mQuickInputView.setVisibility(View.VISIBLE);
                    cleanTv.setVisibility(View.GONE);
                    mQuickInputIsShown = true;
                }
            } else {
                if (mQuickInputIsShown) {
                    mQuickInputView.setVisibility(View.GONE);
                    if(mOftenHistoryAdapter != null && mOftenHistoryAdapter.getCount() != 0){
                        //设置一定延迟，当软键盘隐藏和清除按钮显示同时发生时，有一定的闪动
                        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                cleanTv.setVisibility(View.VISIBLE);
                            }
                        },10);
                    }else{
                        cleanTv.setVisibility(View.GONE);
                    }
                    mQuickInputIsShown = false;
                }
            }

            // 获取root在窗体的可视区域
//            Rect rect = new Rect();
//            mRoot.getWindowVisibleDisplayFrame(rect);
//            // 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度-->键盘高度)
//            int rootInvisibleHeight = mRoot.getRootView().getHeight()
//                    - rect.bottom;
            // 若不可视区域高度大于100，&& 键盘未显示，则键盘显示
//			 SimpleLog.e("OnGlobalLayoutListener",
//			 "rect.RootView().getHeight() == " +
//			 mRoot.getRootView().getHeight());
//			 SimpleLog.e("OnGlobalLayoutListener", "rect.bottom == " +
//			 rect.bottom);
//			 SimpleLog.e("OnGlobalLayoutListener", "rootInvisibleHeight == " +
//			 rootInvisibleHeight);
//            if ((rootInvisibleHeight > 100 )  // 注销 && !mQuickInputIsShown，该条件不可靠
//                    && rect.bottom != AppEnv.SCREEN_HEIGHT
//                    && rect.bottom != AppEnv.SCREEN_WIDTH) {
//                // fix bug QuickInput隐藏后，横竖屏切换 闪
//                resetQuickInputVisible(true);
//            } else if (rootInvisibleHeight < 100 && mQuickInputIsShown) {
//                // 键盘隐藏
//                resetQuickInputVisible(false);
//            }
        }
    };
    private Runnable showQuickInputViewTask = new Runnable() {
        @Override
        public void run() {
            resetLayout();
        }
    };
    private IHideIMListener mHideIMListener = new IHideIMListener() {
        @Override
        public void onIMHide() {
            ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    resetQuickInputVisible(false);
                }
            }, 150);
        }
    };
    private ImageView mSearchEngineImg;
    private ListIconDialog selectDialog;
    private SearchEngineList engineList;

    private Gson gson;
    private String mLongClickItemUrl;
    private List<SearchRecord> mSearchRecords;


    public SearchFrame(Context context) {
        this(context, null);
    }

    public SearchFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void jumpToSearch(String url, String content) {
//        HistoryManager.getInstance().addSearchHistory(content);
        openUrl(url, true);
    }

    private void openUrl(String url) {
        openUrl(url, false);
    }

    // 更改为隐藏searchView，在webView上加载页面
    private void openUrl(String url, boolean isSearch) {
        if (isSearch) {
            mOpenUrlDelegate.open(Constants.TYPE_FROM_SEARCH, url, isSearch);
        } else {
            mOpenUrlDelegate.open(Constants.TYPE_FROM_ADDR, url, isSearch);
        }
        hideSelf(false);
    }

    /**
     * 初始化，
     *
     * @param delegate 执行打开页面操作的delegate
     */
    public void init(IOpenUrlDelegate delegate, IQuickInputView quickInputView) {
        this.mOpenUrlDelegate = delegate;
        this.mQuickInputView = quickInputView;
        mQuickInputView.init(mInputDelegate);
        mSuggestionTopView.setOpenUrlDelegate(this);
    }

    private void init() {
        gson = new Gson();
        SimpleLog.d("", "MyLog_searchFrame_init");
        LayoutInflater.from(getContext()).inflate(R.layout.activity_search, this);
        mInputManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mHorizontalTopMargin = ConfigWrapper.get(HORIZONTAL_TOP_MARGIN, 0);
        mVerticalTopMargin = ConfigWrapper.get(VERTICAL_TOP_MARGIN, 0);
        initEngineData();
        initView();
        initListener();
    }

    private void initEngineData() {
        String json = ConfigManager.getInstance().getLastEngineList();
        this.engineList = gson.fromJson(json, SearchEngineList.class);
    }

    private void initUrlEdit() {
        mUrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) {
                //str.replace(" ", "");
                String str = String.valueOf(edit.toString()).replace(" ", "");
                if (UrlUtils.isUrl(str) && mPageUrl.equalsIgnoreCase(str)) {
                    mSearchBtn.setText(getContext().getString(R.string.cancel));
                    showResultVieworSuggestionTopView(str,false);
                } else if (UrlUtils.isUrl(str) && mContentClipboard.equalsIgnoreCase(str)) {
                    mSearchBtn.setText(getContext().getString(R.string.go));
                    showResultVieworSuggestionTopView(str,false);
                } else if (UrlUtils.isUrl(str) && !mPageUrl.equalsIgnoreCase(str)
                        && !mContentClipboard.equalsIgnoreCase(str)) {
                    mSearchBtn.setText(getContext().getString(R.string.go));
                    showResultVieworSuggestionTopView(str,false);
                } else if (str.length() > 0 && !str.equalsIgnoreCase(mPageUrl)
                        &&!str.equalsIgnoreCase(mContentClipboard)){
                    mSearchBtn.setText(getContext().getString(R.string.search));
                    mSearchKey = str;
                    showResultVieworSuggestionTopView(str,true);
                } else {
                    // 输入框为空时
                    mResultView.clearResults();
                    mSearchBtn.setText(getContext().getString(R.string.cancel));
                    mClearBtn.setVisibility(View.GONE);
                    showHistoryViewPager();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence text, int arg1, int arg2,
                                      int arg3) {
            }
        });
        // 添加软键盘“前进”按钮监听
        mUrlEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String text = mSearchBtn.getText().toString();
                    if (text.equals(getContext().getString(R.string.search))) {
                        String content = mUrlEdit.getText().toString();
//                                .replace(" " , "");
                        String url = SearchUtils.buildSearchUrl(content, JuziApp.getAppContext());
                        jumpToSearch(url, content);
                        addSearchContent(content);
                        saveSearchRecord(content,SearchRecord.SEARCH);
                    } else if (text.equals(getContext().getString(
                            R.string.cancel))) {
                        if (TextUtils.isEmpty(mUrlEdit.getText().toString().replace(" " , ""))) {
                            CustomToastUtils.getInstance().showTextToast(R.string.edittext_empty_tip);
                        } else {
                            hideSelf(true);
                        }
                        mUrlEdit.requestFocus();
                    } else if (text.equals(getContext().getString(R.string.go))) {
                        handleUrlinText();
                    }
                    return true;
                }
                return false;
            }
        });
        updateUIState();
    }

    private void addSearchContent(String content) {
        mSearchKeyList.add(content);
        if (mSearchKeyList.size() >= 5) {
            FileUtils.saveToJsonFile(FileUtils.getFileWithName(Constants.SK_FILE_NAME), Constants.SK_UPLOAD_KEY, mSearchKeyList);
            mSearchKeyList.clear();
        }
    }

    private void handleUrlinText() {
        String url = mUrlEdit.getText().toString();
//                .replace(" " , "");
        if (!url.contains(UrlUtils.PROTOCOL_MARK)) {
            url = UrlUtils.HTTP_PREFIX + url;
        }
        openUrl(url);

        if (url.endsWith(UrlUtils.PROTOCOL_MARK_G)) {
            saveSearchRecord(url,SearchRecord.GO);
        } else {
            saveSearchRecord(url+UrlUtils.PROTOCOL_MARK_G,SearchRecord.GO);
        }

    }

    private void saveSearchRecord(final String searchContent,final int serchType) {
        if (ConfigManager.getInstance().isPrivacyMode()) {
            return;
        }
        ThreadManager.getIOHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SearchRecord searchRecord = SearchRecordApi.getInstance
                            (CustomOpenHelper.getInstance(getContext())).querySearchRecordExist(searchContent);
                    if (searchRecord == null) {
                        SearchRecordApi.getInstance(CustomOpenHelper.getInstance(getContext()))
                                .insert(new SearchRecord(searchContent,serchType,new Date()));
                    } else {
                        SearchRecordApi.getInstance(CustomOpenHelper.getInstance(getContext()))
                                .deleteBySearchRecordAddr(searchContent);
                        SearchRecordApi.getInstance(CustomOpenHelper.getInstance(getContext()))
                                .insert(new SearchRecord(searchContent,serchType,new Date()));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateUIState() {
        if (mUrl != null) {
            mUrlEdit.setText(mUrl);
            mUrlEdit.selectAll();
            if (mUrl.isEmpty()) {
                mClearBtn.setVisibility(View.GONE);
            } else {
                mClearBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mUrlEdit.setText("");
            mClearBtn.setVisibility(View.GONE);
        }
    }

    private void showSearchFrame(String url) {
        if (mBrowserView != null) {
            mBrowserView.setVisibility(View.GONE);
        }
        mUrl = url;
        ThreadManager.getIOHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<SearchRecord> searchRecords = SearchRecordApi.getInstance
                            (CustomOpenHelper.getInstance(getContext())).queryAllSearchRecordLimit(20);
                    setListView(searchRecords);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        updateUIState();
        showSelf();
    }

    private void initView() {
        SimpleLog.d(TAG, "MyLog_sercher_initView");
        mRoot = this;
        mResultView = (SearchResultView) findViewById(R.id.search_result_view);
        mResultView.registerDelegate(mHistoryItemClickImpl, mSuggestUrlImpl, mISearchResultNotifyHindeIM);
        listViewOftenHistory = (ListView) findViewById(R.id.often_history_listview);
        mSearchEngineImg = (ImageView) findViewById(R.id.search_engine_icon);
        ThreadManager.getUIHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshSearchEngineUI(ConfigManager.getInstance().getSearchEngine());
            }
        },300);

        mSearchEngineImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideIM();
                onSetSearchEngin(false);
            }
        });
        cleanTv = (LinearLayout) findViewById(R.id.tv_clear);
        cleanTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final CommonDialog dialog = new CommonDialog(getContext(),
                        getContext().getString(R.string.tips), getContext().getString(R.string.clear_all));
                dialog.setBtnCancel(getContext().getString(R.string.cancel),
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                dialog.setBtnOk(getContext().getString(R.string.ok), new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
//                        HistoryManager.getInstance().deleteAllOftenHistory();
                        ThreadManager.getIOHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SearchRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).clearAllSearchRecord();
                                    setListView(null);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Statistics.sendOnceStatistics(GoogleConfigDefine.SEARCH,GoogleConfigDefine.CLEAR_SEARCH_RECORD);
                    }
                });
                dialog.show();
//                Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY, GoogleConfigDefine.OFTEN_HISTORY_CLEAR_ALL);
            }
        });
        listViewOftenHistory.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String itemUrl = mOftenHistoryAdapter.getItem(position).getSearchAddr();
                openUrl(itemUrl);
                Statistics.sendOnceStatistics(GoogleConfigDefine.SEARCH,GoogleConfigDefine.VISIT_SEARCH_RECORD);
//                Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY, GoogleConfigDefine.OFTEN_HISTORY_CLICK);
//                if (0 == position || 1 == position || 2 == position || 3 == position || 4 == position) {
//                    Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY, "第" + (position + 1) + "条");
//                }
            }
        });
        listViewOftenHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                HistoryOftenInfo item = mOftenHistoryAdapter.getItem(position);
//                Button viewById = (Button) view.findViewById(R.id.search_record_btn_right);
//                viewById.setVisibility(View.VISIBLE);
                mLongClickItemUrl = mOftenHistoryAdapter.getItem(position).getSearchAddr();
                showPopWindow(view);

                return true;
            }
        });

//        listViewOftenHistory.setOnScrollChangeListener(new OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//            }
//        });
        listViewOftenHistory.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                hideIM();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        mClearBtn = findViewById(R.id.btn_clear);
        mClearBtn.setOnClickListener(this);
        mSearchBtn = (TextView) findViewById(R.id.btn_search);
        mUrlEdit = (EditText) findViewById(R.id.edit_text);
        mSuggestionTopView = new SearchSuggestionView(getContext());
        mResultView.addSearchRecommendHeader(mSuggestionTopView);
        initUrlEdit();
    }

    private void showHistoryViewPager() {
        mResultView.setVisibility(View.GONE);
        mSuggestionTopView.setVisibility(View.GONE);
        mResultView.removeSearchRecommendHeader(mSuggestionTopView);
        listViewOftenHistory.setVisibility(View.VISIBLE);
        listViewOftenHistory.smoothScrollToPosition(0);
    }

    private void showResultVieworSuggestionTopView(String keyWord, boolean isShowSuggestionTopView) {

        mResultView.handleTextChange(keyWord, false);
        listViewOftenHistory.setVisibility(View.GONE);
        mClearBtn.setVisibility(View.VISIBLE);
        mResultView.setVisibility(View.VISIBLE);

        if (isShowSuggestionTopView) {
            if(ConfigManager.getInstance().isShowSuggestion()){
                mSuggestionTopView.setVisibility(View.VISIBLE);
                mResultView.addSearchRecommendHeader(mSuggestionTopView);
                mSuggestionTopView.onSearch(keyWord);
            }else{
                mSuggestionTopView.setVisibility(View.GONE);
                mResultView.removeSearchRecommendHeader(mSuggestionTopView);
            }
        } else {
            mSuggestionTopView.setVisibility(View.GONE);
            mResultView.removeSearchRecommendHeader(mSuggestionTopView);
        }

    }

    private void initListener() {
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(mOGLListener);
        mSearchBtn.setOnClickListener(this);
        //设置点击键盘消失的监听（与网址列表item点击事件冲突，暂时取消键盘点击消失监听）
//		mResultView.setHideImListener(mHideIMListener);
    }

    private void resetLayout() {
        // 获取root在窗体的可视区域
        Rect rect = new Rect();
        mRoot.getWindowVisibleDisplayFrame(rect);
        // 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度-->键盘高度)
        // int rootInvisibleHeight = mRoot.getRootView().getHeight() -
        // rect.bottom;

        //这种方式获取statusBarHeight 在 三星手机上失效。
//        Rect frame = new Rect();
//        ((Activity) getContext()).getWindow().getDecorView()
//                .getWindowVisibleDisplayFrame(frame);

        int statusBarHeight = DensityUtil.getStatusBarHeight(getContext());
        // www. .com . \ view的高度
        int quickInputHeight = DensityUtil.dip2px(getContext(), 40);
        int topMargin = rect.bottom - statusBarHeight - quickInputHeight;
        if (ConfigManager.getInstance().isFullScreen()) {
            topMargin = rect.bottom - quickInputHeight;
        }
        SimpleLog.e("OnGlobalLayoutListener", "mQuickInputIsShown == " +
                mQuickInputIsShown);
        SimpleLog.e("OnGlobalLayoutListener", "topMargin == " + topMargin);
        SimpleLog.e("OnGlobalLayoutListener",
                "mQuickInputView.getTopMargin() == " +
                        mQuickInputView.getTopMargin());
        if (mQuickInputIsShown
                && Math.abs(mQuickInputView.getTopMargin() - topMargin) < 10) {
            // fix bug 横竖屏切换时lenovo会闪
            return;
        }
        mQuickInputView.setTopMargin(topMargin);
        mQuickInputView.showWithAnim();
//		 SimpleLog.e("OnGlobalLayoutListener", "resetLayout  ==Shown");
        if (isVerticalScreen()) {
            // shuping
            ConfigWrapper.put(VERTICAL_TOP_MARGIN, topMargin);
            ConfigWrapper.apply();
            mVerticalTopMargin = topMargin;
        } else if (isLandScreen()) {
            // hengping
            ConfigWrapper.put(HORIZONTAL_TOP_MARGIN, topMargin);
            ConfigWrapper.apply();
            mHorizontalTopMargin = topMargin;
        }
    }

    /**
     * 刷新 www. .com . \ view的显示方向
     */
    public void resetLayoutDelay() {
        mQuickInputView.onOrientationChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_search) {
            String text = mSearchBtn.getText().toString();
            if (text.equals(getContext().getString(R.string.search))) {
                String content = mUrlEdit.getText().toString();
//                        .replace(" " , "");
                SimpleLog.d("zcontent", "content=" + content);
                saveSearchRecord(content,SearchRecord.SEARCH);
                String url = SearchUtils.buildSearchUrl(content, JuziApp.getAppContext());
                jumpToSearch(url, content);
                addSearchContent(content);
            } else if (text.equals(getContext().getString(R.string.cancel))) {
                hideSelf(true);
            } else if (text.equals(getContext().getString(R.string.go))) {
                handleUrlinText();
                // 如果是测试环境，可以构造崩溃，测试dns cache
                if (AppEnv.DEBUG) {
                    String url = mUrlEdit.getText().toString();
                    if (url.startsWith("http://momeng.wangxianbin.com")) {
                        String a = null;
                        a.equals("b");
                    } else if (url.startsWith("http://dnscache.com")) {
                        DnsUtils.preloadDns();
                    }
                }
            }
        } else if (v.getId() == R.id.btn_clear) {
            //添加点击搜索栏删除按钮统计
            Statistics.sendOnceStatistics(GoogleConfigDefine.SEARCH, GoogleConfigDefine.SEARCH_REMOVEBTN);
            mUrlEdit.setText("");
            showHistoryViewPager();
        } else if (v.getId() == R.id.search_record_del_btn) {
            popWindow.dismiss();
            ThreadManager.getIOHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        SearchRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).deleteBySearchRecordAddr(mLongClickItemUrl);
                        final List<SearchRecord> searchRecords = SearchRecordApi.getInstance
                                (CustomOpenHelper.getInstance(getContext())).queryAllSearchRecordLimit(20);
                        setListView(searchRecords);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            Statistics.sendOnceStatistics(GoogleConfigDefine.SEARCH,GoogleConfigDefine.DEL_SEARCH_RECORD_ITEM);
        }
    }

    /**
     * 显示搜索页面
     */
    private void showSelf() {
        SimpleLog.d("", "MyLog_showSelf(显示搜索页面)");
        if (isLandScreen() && mHorizontalTopMargin == 0) {
            ((Activity) getContext()).getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } else if (isVerticalScreen() && mVerticalTopMargin == 0) {
            ((Activity) getContext()).getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        mInputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        // 让EditText获得焦点，但是获得焦点并不会自动弹出键盘
        mUrlEdit.requestFocus();
        setVisibility(View.VISIBLE);
        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                ((Activity) getContext()).getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mUrlEdit, InputMethodManager.SHOW_FORCED);
            }
        }, 400);
    }

    /**
     * 隐藏搜索页面
     */
    public void hideSelf(boolean needShowBrowserView) {
        SimpleLog.d(TAG, "MyLog_隐藏搜索页面");
        if (needShowBrowserView) {
            if (mBrowserView != null) {
                mBrowserView.setVisibility(View.VISIBLE);
            }
        }
        hideIM();
        // 隐藏后清除推荐内容
        setVisibility(View.GONE);
        mResultView.clearResults();
        resetQuickInputVisible(false);
        ThreadManager.postTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                if (mHideListener != null) {
                    mHideListener.onHide();
                }
            }
        });
    }

    /**
     * 设置页面隐藏监听
     *
     * @param listener
     */
    public void setHideListener(IHideListener listener) {
        this.mHideListener = listener;
    }

    private void hideIM() {
//        ((Activity) getContext()).getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mInputManager.hideSoftInputFromWindow(mUrlEdit.getWindowToken(), 0);
    }


    private void showIM() {
        if (mInputManager != null) {
            ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    mUrlEdit.requestFocus();
                    mInputManager.showSoftInput(mUrlEdit, 0);
                }
            },100);
        }
    }

    @Override
    public void show(int source, String pageUrl, View browserView) {
        try {
            mBrowserView = browserView;
//            String contentByClipboar = SysUtils.getContentByClipboar(JuziApp.getAppContext());
//            if (!TextUtils.isEmpty(contentByClipboar)) {
//                mContentClipboard = UrlUtils.getUrlInText(contentByClipboar);
//            } else {
//                mContentClipboard = "";
//            }
            // V1.2.0  产品要求取消每次进入搜索栏时自动粘贴剪贴板功能
            mContentClipboard = "";
//            SimpleLog.d(TAG, "mContentClipboard==" + mContentClipboard);
            switch (source) {
                case CommonData.PAGE_EDIT_CLICK:    //点击主屏
                    mPageUrl = "";
                    showSearchFrame(mContentClipboard);
//                    SimpleLog.d(TAG, "PAGE_EDIT_CLICK_mContentClipboard==" + mContentClipboard);
                    break;
                case CommonData.WEBCONTENT_EDIT_CLICK:        //点击Webview地址栏
                    if (!TextUtils.isEmpty(pageUrl)) {
                        mPageUrl = pageUrl;
                        showSearchFrame(mPageUrl);
//                        SimpleLog.d(TAG, "WEBCONTENT_EDIT_CLICK_mPageUrl==" + mPageUrl);
                    } else {
                        mPageUrl = "";
                    }
                    SimpleLog.d(TAG, "mPageUrl==" + mPageUrl);
                    break;
                case CommonData.NOTIFY_SEARCH_CLICK:    //点击搜索通知栏
//                    SimpleLog.d(TAG, "NOTIFY_SEARCH_CLICK");
                    mPageUrl = "";
                    showSearchFrame(mPageUrl);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            SimpleLog.e(e);
        }
    }

    /**
     * 是否是横屏
     **/
    private boolean isLandScreen() {
        if (AppEnv.SCREEN_WIDTH > AppEnv.SCREEN_HEIGHT) {
            return true;
        }
        return false;
    }

    /**
     * 是否是竖屏
     **/
    private boolean isVerticalScreen() {
        if (AppEnv.SCREEN_WIDTH < AppEnv.SCREEN_HEIGHT) {
            return true;
        }
        return false;
    }

    private PopupWindow popWindow;
    public void showPopWindow(View v){
        if(popWindow==null){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.search_record_popup, null);
            view.findViewById(R.id.search_record_del_btn).setOnClickListener(this);
//            view.findViewById(R.id.lin_popup_setting).setOnClickListener(this);
            popWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popWindow.setOutsideTouchable(false);
            popWindow.setBackgroundDrawable(new BitmapDrawable());
//            popWindow.setAnimationStyle(R.style.pop);
        }
//        WindowManager manager=(WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        @SuppressWarnings("deprecation")
//        //获取xoff
//        int xpos=manager.getDefaultDisplay().getWidth()/2-popWindow.getWidth();
//        popWindow.showAsDropDown(v,AppEnv.SCREEN_WIDTH/2-120,-200);

        popWindow.getWidth();
        int[] itemLocation = new int[2];
        v.getLocationOnScreen(itemLocation);
        popWindow.showAtLocation(v, Gravity.NO_GRAVITY, AppEnv.SCREEN_WIDTH/2-50, itemLocation[1]);
    }

    private void setListView(final List<SearchRecord> searchRecords) {
            ThreadManager.getUIHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (searchRecords != null && searchRecords.size() > 0) {
                        if (mOftenHistoryAdapter == null) {
                            mOftenHistoryAdapter = new OftenHistoryAdapter(JuziApp.getAppContext(), searchRecords, mIOftenHistoryItemClik);
                            listViewOftenHistory.setAdapter(mOftenHistoryAdapter);
                            cleanTv.setVisibility(View.VISIBLE);
                        } else {
                            mOftenHistoryAdapter.setList(searchRecords);
                            mOftenHistoryAdapter.notifyDataSetChanged();
                            cleanTv.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mOftenHistoryAdapter == null) {
                            mOftenHistoryAdapter = new OftenHistoryAdapter(JuziApp.getAppContext(), searchRecords, mIOftenHistoryItemClik);
                            listViewOftenHistory.setAdapter(mOftenHistoryAdapter);
                            cleanTv.setVisibility(View.GONE);
                        } else {
                            mOftenHistoryAdapter.setList(searchRecords);
                            mOftenHistoryAdapter.notifyDataSetChanged();
                            cleanTv.setVisibility(View.GONE);
                        }
                    }
                }
            });
    }

    /**
     * 设置快捷输入栏 和 清除按钮的状态（不同时显示）
     * Note:
     * 1、快捷输入栏显示时，清除按钮必隐藏
     * 2、快捷输入栏隐藏时，有数据时清除按钮显示，无数据时清除按钮隐藏；
     */
    private void resetQuickInputVisible(boolean isShowQuickInput){
        if(isShowQuickInput){  //快捷输入栏显示时，隐藏清除按钮
            cleanTv.setVisibility(View.GONE); //先隐藏清除按钮
            resetLayout();
            mQuickInputIsShown = true;
        }else{
            mQuickInputView.setVisibility(View.GONE);  //隐藏快捷输入栏
            if(mOftenHistoryAdapter != null && mOftenHistoryAdapter.getCount() != 0){  //设置一定延迟，当软键盘隐藏和清除按钮显示同时发生时，有一定的闪动
                ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        cleanTv.setVisibility(View.VISIBLE);
                    }
                },10);
            }else{
                cleanTv.setVisibility(View.GONE);
            }
            mQuickInputIsShown = false;
        }
    }

    private IOftenHistoryItemClik mIOftenHistoryItemClik = new IOftenHistoryItemClik() {

        @Override
        public void onItemClick(String url) {
            if (!TextUtils.isEmpty(url)) {
                mUrlEdit.setText(url);
                mUrlEdit.setSelection(url.length());
            }
            Statistics.sendOnceStatistics(GoogleConfigDefine.SEARCH,GoogleConfigDefine.SEARCH_CLICK_FILL);
        }
    };

    public void onSetSearchEngin(final boolean isNeedReload) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.SWITCH_SEARCH_ENGINE,GoogleConfigDefine.SWITCH_SEARCH_ENGINE_BUTTON_CLICK_COUNT);
        selectDialog = new ListIconDialog(getContext());
        final int selectItem = ConfigManager.getInstance().getSearchEngine();
        selectDialog.setItems(createItems(), selectItem);
        selectDialog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.findViewById(R.id.iv_check)
                        .setVisibility(ConfigManager.getInstance().getSearchEngine()
                                == position ? VISIBLE : GONE);
                ConfigManager.getInstance().setSearchEngine(position,true);
                ConfigManager.getInstance().setDefaultSearchEngineModified();

                sendStatistics(position);


                checkIsNeedReload(isNeedReload);

                if (selectDialog.isShowing())
                    selectDialog.dismiss();

            }
        });
        selectDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showIM();
            }
        });
        selectDialog.show();

    }

    private void checkIsNeedReload(boolean isNeedReload) {

        if (isNeedReload && !TextUtils.isEmpty(mSearchKey)) {
            try {
                String newUrl = SearchUtils.buildSearchUrl(URLDecoder.decode(mSearchKey, "UTF-8"), getContext());
                openUrl(newUrl);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendStatistics(int position) {

        if(engineList!=null&&!ListUtils.isEmpty(engineList.getDataList())){
            try {
                String name = engineList.getDataList().get(position).getEngineName();
                if(name != null && !name.isEmpty()){
                    SimpleLog.d(TAG,"sendStatistics setSearchEngine = "+GoogleConfigDefine.SWITCH_SEARCH_ENGINE_TO + name.toLowerCase());
                    Statistics.sendOnceStatistics(GoogleConfigDefine.SWITCH_SEARCH_ENGINE,
                            GoogleConfigDefine.SWITCH_SEARCH_ENGINE_TO + name.toLowerCase());
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }

/*        switch (position) {
            case ConfigDefine.SEARCH_ENGINE_GOOGLE:
                value = GoogleConfigDefine.SWITCH_TO_GOOGLE;

                break;
            case ConfigDefine.SEARCH_ENGINE_BING:
                value = GoogleConfigDefine.SWITCH_TO_BING;
                break;
            case ConfigDefine.SEARCH_ENGINE_YAHOO:
                value = GoogleConfigDefine.SWITCH_TO_YAHOO;

                break;
            case ConfigDefine.SEARCH_ENGINE_YANDEX:
                value = GoogleConfigDefine.SWITCH_TO_YANDEX;
                break;
            case ConfigDefine.SEARCH_ENGINE_DUCKGO:
                value = GoogleConfigDefine.SWITCH_TO_DUCK_GO;
                break;
            case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
                value = GoogleConfigDefine.SWITCH_TO_YOUTUBE;
                break;
            case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
                value = GoogleConfigDefine.SWITCH_TO_GOOGLE_QUICK;
                break;
        }*/

    }

    private ArrayList<ListIconDialog.Item> createItems() {

        ArrayList<ListIconDialog.Item> list = new ArrayList<>();
        if (engineList != null && !ListUtils.isEmpty(engineList.getDataList())) {

            Collections.sort(engineList.getDataList());
            for (SearchEngineList.DataListBean dataListBean : engineList.getDataList()) {
                if(dataListBean == null || dataListBean.getEngineName() == null ) continue;
                list.add(selectDialog.new Item(SearchUtils.getMultiLanByEn(dataListBean.getEngineName()),dataListBean.getEnginePic()));
            }
        }else{
            return createDefaultItems();
        }
        return list;
    }

    private ArrayList<ListIconDialog.Item> createDefaultItems() {
        ArrayList<ListIconDialog.Item> list = new ArrayList<>();
        for(Map.Entry<String,int[]> entry : ConfigDefine.SEARCH_ENGINE_NAME_MAP.entrySet()){
            if(entry == null || entry.getValue() == null || entry.getValue().length < 2) continue;
            list.add(selectDialog.new Item(getResources().getString(entry.getValue()[1]),entry.getValue()[0]));
        }
        return list;
    }

    public void refreshSearchEngineUI( int searchEngine) {
        initEngineData();
        if(engineList!=null&&!ListUtils.isEmpty(engineList.getDataList())){
            SimpleLog.d(TAG,"SearchFrame refreshSearchEngineUI engineList!=null --- ");
            if(searchEngine>=engineList.getDataList().size()){
                searchEngine = engineList.getDataList().size()-1;
                ConfigManager.getInstance().setSearchEngine(searchEngine,false);
            }
            try {
                int defaultRes = SearchUtils.getDefaultEngineIconByName(engineList.getDataList().get(searchEngine).getEngineName());
                ImageLoadUtils.loadImage(getContext(), engineList.getDataList().get(searchEngine).getEnginePic(), mSearchEngineImg,R.drawable.engin_default_bg,defaultRes);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        /*else{
            SimpleLog.d(TAG,"SearchFrame refreshSearchEngineUI engineList==null --- ");
            switch (searchEngine) {
                case ConfigDefine.SEARCH_ENGINE_GOOGLE:

                    mSearchEngineImg.setImageResource(R.drawable.google_icon);
                    break;
                case ConfigDefine.SEARCH_ENGINE_BING:
                    mSearchEngineImg.setImageResource(R.drawable.bing);
                    break;
                case ConfigDefine.SEARCH_ENGINE_YAHOO:
                    mSearchEngineImg.setImageResource(R.drawable.yahoo);

                    break;
                case ConfigDefine.SEARCH_ENGINE_YANDEX:
                    mSearchEngineImg.setImageResource(R.drawable.yandex);
                    break;
                case ConfigDefine.SEARCH_ENGINE_DUCKGO:
                    mSearchEngineImg.setImageResource(R.drawable.duck_duck_go);
                    break;
                case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
                    mSearchEngineImg.setImageResource(R.drawable.youtube);
                    break;
                case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
                    mSearchEngineImg.setImageResource(R.drawable.google_quick_search);
                    break;
                default:
                    break;
            }
        }*/

    }

    public void setSearchKey(String mSearchKey) {
        this.mSearchKey = mSearchKey;
    }

    @Override
    public void onOpen(String url) {
        if (!url.contains(UrlUtils.PROTOCOL_MARK)) {
            url = UrlUtils.HTTP_PREFIX + url;
        }
        openUrl(url,false);
        if (url.endsWith(UrlUtils.PROTOCOL_MARK_G)) {
            saveSearchRecord(url,SearchRecord.GO);
        } else {
            saveSearchRecord(url+UrlUtils.PROTOCOL_MARK_G,SearchRecord.GO);
        }
    }


    public void destroy(){
        mSuggestionTopView.destroy();
    }

    public void saveSk() {
        if (mSearchKeyList.size() > 0) {
            FileUtils.saveToJsonFile(FileUtils.getFileWithName(Constants.SK_FILE_NAME), Constants.SK_UPLOAD_KEY, mSearchKeyList);
            mSearchKeyList.clear();
        }
    }
}
