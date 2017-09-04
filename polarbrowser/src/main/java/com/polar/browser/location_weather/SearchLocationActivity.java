package com.polar.browser.location_weather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.ILocationClickCallback;
import com.polar.browser.i.IshowEmpty;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.LastWeatherInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchLocationActivity extends LemonBaseActivity implements ILocationClickCallback, View.OnClickListener, IshowEmpty {

    private EditText mSearchContent;
    private View mClearText;
    private View mCancel;
    private RecyclerView mRecyclerView;
    private LocationsAdapter mAdapter;
    private LastWeatherInfo mCurrentWeatherInfo;
    private TextView mEmptyView;
    private TextView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        mCurrentWeatherInfo = (LastWeatherInfo) getIntent().getSerializableExtra(SwitchLocationActivity.KEY_CURRENT_WEATHER_INFO);
        initViews();
        initListeners();
    }

    private void initListeners() {
        mClearText.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                showClearText(s);
                ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        requestForLocations(s);
                    }
                }, 800);
            }
        });
    }

    private void showClearText(Editable s) {
        if (TextUtils.isEmpty(s)) {
            mClearText.setVisibility(View.GONE);
        } else {
            mClearText.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        mSearchContent = (EditText) findViewById(R.id.et_search_content);
        mClearText = findViewById(R.id.iv_clear);
        mCancel = findViewById(R.id.tv_cancel);
        mEmptyView = (TextView) findViewById(R.id.tv_empty);
        mLoadingView = (TextView) findViewById(R.id.tv_loading);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_city_location);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDec = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        itemDec.setDrawable(getResources().getDrawable(R.drawable.activity_switch_location_list_divider));
        mRecyclerView.addItemDecoration(itemDec);
        mAdapter = new SearchLocationActivity.LocationsAdapter(this, null, this, this);
        mRecyclerView.setAdapter(mAdapter);

        mClearText.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
    }

    private void showEmptyView(boolean show) {
        if (show) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void showLoadingView(boolean show) {
        if (show) {
            mLoadingView.setVisibility(View.VISIBLE);
        } else {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 根绝输入的关键字，查询匹配城市。
     *
     * @param inputs
     */
    private void requestForLocations(Editable inputs) {
        if (TextUtils.isEmpty(inputs)) {
            mAdapter.clearData();
            return;
        }
        showEmptyView(false);
        showLoadingView(true);
        WeatherManager.requestRecCities(inputs).enqueue(new Callback<List<LastWeatherInfo>>() {

            @Override
            public void onResponse(Call<List<LastWeatherInfo>> call, Response<List<LastWeatherInfo>> response) {
                List<LastWeatherInfo> list = response.body();
                mAdapter.changeData(list);
                showLoadingView(false);
            }

            @Override
            public void onFailure(Call<List<LastWeatherInfo>> call, Throwable t) {
                showLoadingView(false);
            }
        });
    }


    public static void startSearchLocationActivity(Context context, Bundle extras) {
        Intent intent = new Intent(context, SearchLocationActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_clear:
                mSearchContent.setText("");
                break;
            case R.id.tv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocationClick(LastWeatherInfo info) {
        if (mCurrentWeatherInfo == null) {
            Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.SELECT_NOT_CUR);
        } else {
            if (!info.getCity().equals(mCurrentWeatherInfo.getCity())
                    || !info.getCountry().equals(mCurrentWeatherInfo.getCountry())) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.SELECT_NOT_CUR);
            }
        }
        startActivity(new Intent(this, BrowserActivity.class));
    }

    @Override
    public void showEmpty(boolean show) {
        showEmptyView(show);
    }

    static class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationViewHolder> {

        private List<LastWeatherInfo> locations;
        private Context context;
        private LayoutInflater inflater;
        private ILocationClickCallback callback;
        private IshowEmpty emptyCallback;

        public LocationsAdapter(Context context, List<LastWeatherInfo> locations, ILocationClickCallback callback, IshowEmpty emptyCallback) {
            this.context = context;
            this.locations = locations;
            inflater = LayoutInflater.from(context);
            this.callback = callback;
            this.emptyCallback = emptyCallback;
        }

        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.activity_switch_loaction_list_item, parent, false);
            return new LocationsAdapter.LocationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(LocationsAdapter.LocationViewHolder holder, final int position) {
            holder.locationContent.setText(locations.get(position).getQualifiedName());
            if (holder.itemView != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final LastWeatherInfo bean = locations.get(position);
                        WeatherLocationEvent event = new WeatherLocationEvent();
                        event.lat = bean.getLat();
                        event.lon = bean.getLon();
                        event.city = bean.getCity();
                        event.country = bean.getCountry();
                        EventBus.getDefault().post(event);
                        callback.onLocationClick(bean);
                        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.SELECT_SEARCH_RESULT);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (locations == null) {
                return 0;
            }
            return locations.size();
        }

        public void changeData(List<LastWeatherInfo> data) {
            locations = data;
            notifyDataSetChanged();
            if (locations == null || locations.isEmpty()) {
                emptyCallback.showEmpty(true);
            } else {
                emptyCallback.showEmpty(false);
            }
        }

        public void clearData() {
            if (locations != null) {
                locations.clear();
                notifyDataSetChanged();
                emptyCallback.showEmpty(true);
            }
        }

        public static class LocationViewHolder extends RecyclerView.ViewHolder {

            public TextView locationContent;

            public LocationViewHolder(View v) {
                super(v);
                locationContent = (TextView) v.findViewById(R.id.tv_location_content);
            }
        }
    }
}
