package com.polar.browser.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.service.SuggestionEventService;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.vclibrary.bean.SearchSuggestion;
import com.polar.browser.vclibrary.bean.SearchSuggestion.RecordsBean;
import com.polar.browser.vclibrary.bean.SuggestionEvent;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.GooglePlayUtil;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by saifei on 16/11/29.
 * 搜索建议 View
 */

public class SearchSuggestionView extends LinearLayout implements IConfigObserver {
    private static final String TAG = SearchSuggestionView.class.getSimpleName();

    private IOpenDelegate mOpenUrlDelegate;
    private String currKeyWords;
    private SuggestionEvent event;

    public SearchSuggestionView(Context context) {
        super(context);
        init();
    }

    public SearchSuggestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ConfigManager.getInstance().registerObserver(this);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, 0, 0, DensityUtil.dip2px(getContext(), 10));
        setBackgroundColor(Color.argb(255, 244, 244, 244));
        event = new SuggestionEvent();

    }

    private void addCard(RecordsBean.SuggestionBean suggestionBean, SearchSuggestion suggestionInfo) { //TODO
        View view = inflate(getContext(), R.layout.search_suggestion_layout, null);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.suggestion_layout);
        TextView suggestionNameTV = (TextView) view.findViewById(R.id.suggestion_name_tv);
        suggestionNameTV.setText(suggestionBean.getSuggestion());

        for (int i = 0; i < suggestionBean.getCardCount(); i++) {
            RecordsBean.SuggestionBean.CardsBean card = suggestionBean.getCards().get(i);
            inflateCard(card, linearLayout, i == suggestionBean.getCardCount() - 1, suggestionBean.getId(), suggestionInfo.get_meta().getRid());
//            addShowEvent(card, recordsBean, suggestionInfo);
        }

        addView(view);

    }

    private void inflateCard(final RecordsBean.SuggestionBean.CardsBean card, LinearLayout linearLayout, boolean isLast, final int suggestionId, final String rid) {
        View view = inflate(getContext(), R.layout.search_suggestion_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginRight = isLast ? DensityUtil.dip2px(getContext(), 10) : 0;
        lp.setMargins(DensityUtil.dip2px(getContext(), 10), 0, marginRight, 0);
        linearLayout.addView(view, lp);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.ZOWDOW_SUGGESTION, GoogleConfigDefine.SUGGESTION_ITEM_CLICK);
                handleClickForAction(card);
                addClickEvent(card, suggestionId, rid);
            }
        });

        ImageLoadUtils.loadImage(getContext(), getImageUrlByDensity(card), imageView);
    }

    private void addClickEvent(RecordsBean.SuggestionBean.CardsBean card, int suggestionId, String rid) {
        event.getEvents().add(new SuggestionEvent.EventsBean("card_clicked", System.currentTimeMillis(),
                new SuggestionEvent.EventsBean.PayloadBean(card.getId(), suggestionId, rid)));
//        checkSendEvent();
    }

    private void handleClickForAction(RecordsBean.SuggestionBean.CardsBean card) {
        RecordsBean.SuggestionBean.CardsBean.ActionsBean deepLinkActionBean = queryAction("deep_link", card);
        RecordsBean.SuggestionBean.CardsBean.ActionsBean webUrlMarketActionBean = queryAction("web_url_market", card);
        RecordsBean.SuggestionBean.CardsBean.ActionsBean webUrlActionBean = queryAction("web_url", card);


        if (deepLinkActionBean != null) {
            if (deepLinkActionBean.getAction_target().startsWith("market:")) {
                try {
                    GooglePlayUtil.launchAppDetail(getContext(),deepLinkActionBean.getAction_target());
                }catch (Exception e){
                    if(webUrlMarketActionBean!=null)
                        mOpenUrlDelegate.onOpen(webUrlMarketActionBean.getAction_target());
                    else if (webUrlActionBean != null) {
                        mOpenUrlDelegate.onOpen(webUrlActionBean.getAction_target());
                    }
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkActionBean.getAction_target()));
                if (GooglePlayUtil.isActivityExist(intent,getContext())) {
                    getContext().startActivity(intent);
                } else {
                    mOpenUrlDelegate.onOpen(webUrlActionBean.getAction_target());
                }
            }

        } else if (webUrlActionBean != null) {
            mOpenUrlDelegate.onOpen(webUrlActionBean.getAction_target());
        }
    }

    private RecordsBean.SuggestionBean.CardsBean.ActionsBean queryAction(String actionName, RecordsBean.SuggestionBean.CardsBean card) {
        RecordsBean.SuggestionBean.CardsBean.ActionsBean result = null;
        for (RecordsBean.SuggestionBean.CardsBean.ActionsBean actionsBean : card.getActions()) {
            if (actionName.equals(actionsBean.getAction_type())) {
                result = actionsBean;
            }
        }
        return result;
    }

    private String getImageUrlByDensity(SearchSuggestion.RecordsBean.SuggestionBean.CardsBean card) {
        float density = getContext().getResources().getDisplayMetrics().density;//TODO 根据系统分辨率加载不同尺寸图片


        return card.getX2();
    }

    public SearchSuggestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void onSearch(final String keyWords) {
        setCurrKeyWords(keyWords);
        removeAllViews();
        Api.getInstance().searchSuggestion(keyWords).enqueue(
                new Callback<SearchSuggestion>() {
                    @Override
                    public void onResponse(Call<SearchSuggestion> call, Response<SearchSuggestion> response) {

                        SearchSuggestion suggestionInfo = response.body();
                        if (suggestionInfo == null || suggestionInfo.getRecords() == null || suggestionInfo.getRecords().isEmpty())
                            return;
                        RecordsBean suggestionBean = suggestionInfo.getRecords().get(0);
                        if (!currKeyWords.equalsIgnoreCase(suggestionBean.getSuggestion().getQueryFragment()))
                            return;
                        removeAllViews();
                        for (SearchSuggestion.RecordsBean recordsBean : suggestionInfo.getRecords()) {
                            SearchSuggestion.RecordsBean.SuggestionBean suggestBean = recordsBean.getSuggestion();
                            addCard(suggestBean, suggestionInfo);
                        }
//                        Statistics.sendOnceStatistics(GoogleConfigDefine.ZOWDOW_SUGGESTION, GoogleConfigDefine.SUGGESTION_SHOW_COUNT);
//                        checkSendEvent();


                    }

                    @Override
                    public void onFailure(Call<SearchSuggestion> call, Throwable t) {

                    }
                }
        );
    }

    private void addShowEvent(RecordsBean.SuggestionBean.CardsBean cardsBean, RecordsBean recordsBean, SearchSuggestion suggestionInfo) {
        SuggestionEvent.EventsBean eventsBean = new SuggestionEvent.EventsBean("card_shown", System.currentTimeMillis(),
                new SuggestionEvent.EventsBean.PayloadBean(cardsBean.getId(),
                        recordsBean.getSuggestion().getId(), suggestionInfo.get_meta().getRid()));
        event.getEvents().add(eventsBean);

    }

    private void checkSendEvent() {
        Gson gson = new Gson();
        ConfigManager.getInstance().saveSuggestionEvent(gson.toJson(event),false);

        if (ConfigManager.getInstance().isNeedSendSuggesitonEvent()) {
            JuziApp.getInstance().startService(new Intent(JuziApp.getAppContext(), SuggestionEventService.class));
            ConfigManager.getInstance().setNeedSendSuggesitonEvent(false);
        }
    }

    public void setOpenUrlDelegate(IOpenDelegate mOpenUrlDelegate) {
        this.mOpenUrlDelegate = mOpenUrlDelegate;
    }

    public void setCurrKeyWords(String currKeyWords) {
        this.currKeyWords = currKeyWords;
    }

    @Override
    public void notifyChanged(String key, boolean value) {

    }

    @Override
    public void notifyChanged(String key, String value) {
        if (ConfigDefine.SUGGESTION_EVENT.equals(key) && TextUtils.isEmpty(value)) {
            event.getEvents().clear();
        }
    }

    @Override
    public void notifyChanged(String key, int value) {

    }


    interface IOpenDelegate {
        void onOpen(String url);
    }

    public void destroy() {
        ConfigManager.getInstance().unregisterObserver(this);
    }


}
