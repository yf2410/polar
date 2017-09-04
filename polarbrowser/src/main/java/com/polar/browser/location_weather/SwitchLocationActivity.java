package com.polar.browser.location_weather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.LastWeatherInfo;
import com.polar.browser.vclibrary.bean.WeatherResult;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 选择位置，请求雅虎api
 * Created by yxx on 2017/4/14.
 */

public class SwitchLocationActivity extends LemonBaseActivity implements View.OnClickListener {

    private static final String TAG = "SwitchLocationActivity";
    private TextView mLocationInput;
    private LastWeatherInfo mLastWeatherInfo;
    private ImageView mRefreshLoaction;
    private TextView mCurrentLocation;
    private LastWeatherInfo mCurrentWeatherInfo;
    private TextView mLocatePermission;
    public static final String KEY_CURRENT_WEATHER_INFO = "current_weather_info";

    private long mRotateStartTime;
    private View mGroupRefreshLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_location);
        Intent intent = this.getIntent();
        mLastWeatherInfo = (LastWeatherInfo) intent.getSerializableExtra("lastWeatherInfo");
        mCurrentWeatherInfo = mLastWeatherInfo;
        initView();
        initListener();
        initData();
    }

    private void initData() {
        if (isLocateGranted()) {
            mLocatePermission.setVisibility(View.GONE);
            getCurrentLocation();
        } else {
            mLocatePermission.setVisibility(View.VISIBLE);
            mCurrentLocation.setText(getApplicationContext().getString(R.string.not_available));
        }
    }

    private void initView() {
        mLocationInput = (TextView) findViewById(R.id.tv_location_input);
        mRefreshLoaction = (ImageView) findViewById(R.id.iv_location_refresh);
        mCurrentLocation = (TextView) findViewById(R.id.tv_weather_current_location);
        mLocatePermission = (TextView) findViewById(R.id.tv_permission_deny);
        mGroupRefreshLocation = findViewById(R.id.group_location_refresh);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void initListener() {
        mLocationInput.setOnClickListener(this);
        mCurrentLocation.setOnClickListener(this);
        mLocatePermission.setOnClickListener(this);
        mGroupRefreshLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_location_input:
                startSearchActivity();
                Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.SEATCH_BOX_CLICK);
                break;
            case R.id.group_location_refresh:
                if (isLocateGranted()) {
                    getCurrentLocation();
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.REFRESH_CUR_LOCATION);
                } else {
                    requestLocatePermission();
                }

                break;
            case R.id.tv_weather_current_location:
                if (!isCurrentLocationEnabled()) {
                    return;
                }
                changeWeatherLocation();
                Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.CUR_LOCATION_SELECTED);
                break;
            case R.id.tv_permission_deny:
                requestLocatePermission();
                Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.REQ_LOCATION_PER_CLICK);
                break;
            default:
                break;
        }
    }

    private boolean isCurrentLocationEnabled() {
        final String currentText = mCurrentLocation.getText().toString();
        if (!isLocateGranted() || getString(R.string.no_locate).equals(currentText)
                || getString(R.string.unavailable).equals(currentText)) {
            return false;
        }
        return true;
    }

    private void rotateAnim(View view, boolean start) {
        Animation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        anim.setFillAfter(true); // 设置保持动画最后的状态
        anim.setDuration(1000); // 设置动画时间
        anim.setRepeatCount(100);
//        anim.setInterpolator(new AccelerateInterpolator()); // 设置插入器
        if (start) {
            view.startAnimation(anim);
        }else {
            view.clearAnimation();
        }
    }

    private void startSearchActivity() {
        Bundle extras = new Bundle();
        extras.putSerializable(KEY_CURRENT_WEATHER_INFO, mCurrentWeatherInfo);
        SearchLocationActivity.startSearchLocationActivity(this, extras);
    }

    private void requestLocatePermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults != null) {
                boolean granted = true;
                for (int result: grantResults) {
                    granted &= (result == PackageManager.PERMISSION_GRANTED);
                }
                notifyLocateGranted(granted);
            }
        }
    }

    private void notifyLocateGranted(boolean granted) {
        if (granted) {
            mLocatePermission.setVisibility(View.GONE);
            getCurrentLocation();
        } else {
            mLocatePermission.setVisibility(View.VISIBLE);
            mCurrentLocation.setText(getApplicationContext().getString(R.string.not_available));
        }
    }

    private void changeWeatherLocation() {
        EventBus.getDefault().post(new WeatherLocationEvent(mCurrentWeatherInfo.getLat(),
                mCurrentWeatherInfo.getLon()));
        finish();
    }

    private String buildCurrenLocation(String city, String country) {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(city) || TextUtils.isEmpty(country)) {
            return getString(this, R.string.unavailable);
        }
        return sb.append(city).append(",").append(" ").append(country).toString();
    }

    private void getCurrentLocation() {
        rotateAnim(mRefreshLoaction, true);
        mCurrentLocation.setText(R.string.loading);
        mRotateStartTime = System.currentTimeMillis();

        AlxLocationManager.getInstance().init(new ILocationCallBack() {
            @Override
            public void callback(double lat, double lng) {
                getLocation(lat, lng);
                AlxLocationManager.pauseGPS();
            }

            @Override
            public void onFailed(int code, String msg) {
                mRefreshLoaction.clearAnimation();
                mCurrentLocation.setText(getApplicationContext().getString(R.string.no_locate));
            }
        });
        AlxLocationManager.pauseGPS();
        AlxLocationManager.onCreateGPS(getApplicationContext());
    }

    private void getLocation(final double lat, final double lng) {
        WeatherManager.requestWeather(lat, lng)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<WeatherResult>() {
                    private WeatherResult.QueryBean.ResultsBean.ChannelBean.LocationBean location;

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(WeatherResult value) {
                        if (value != null && value.getQuery() != null && value.getQuery().getResults() != null
                                && value.getQuery().getResults().getChannel() != null ) {
                            location = value.getQuery().getResults().getChannel().getLocation();
                        }

                        if (location != null) {
                            final String city = location.getCity();
                            final String country = location.getCountry();
                            mCurrentLocation.setText(buildCurrenLocation(city, country));
                            resetCurrentWeatherInfo(city, country, lat, lng);
                        } else {
                            mCurrentLocation.setText(getApplicationContext().getString(R.string.no_locate));
                        }

                        if ((System.currentTimeMillis()- mRotateStartTime) < 1000) {
                            ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                                @Override
                                public void run() {
                                    mRefreshLoaction.clearAnimation();
                                }
                            }, (System.currentTimeMillis() - mRotateStartTime));
                        } else {
                            mRefreshLoaction.clearAnimation();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        mCurrentLocation.setText(getApplicationContext().getString(R.string.no_locate));
                        mRefreshLoaction.clearAnimation();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void resetCurrentWeatherInfo(String city, String country, double lat, double lon) {
        LastWeatherInfo info = new LastWeatherInfo();
        info.setCity(city);
        info.setCity(country);
        info.setLat(lat);
        info.setLon(lon);
        mCurrentWeatherInfo = info;
    }

    private boolean isLocateGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

}
