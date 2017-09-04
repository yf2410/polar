package com.polar.browser.library.rx;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class RetryWhenProcess implements Function<Observable<? extends Throwable>, Observable<?>> {

    /**
     * 多久重试一次，单位秒
     */
    private long mInterval;

    public RetryWhenProcess(long interval) {

        mInterval = interval;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> observable) {
//        return observable.flatMap(new Function<Throwable, ObservableSource<?>>() {
//            @Override
//            public Observable<?> apply(Throwable throwable) {
//
//        });}
        return observable.flatMap(new Function<Throwable, ObservableSource<?>>() {
            @Override
            public Observable<?> apply(Throwable throwable) {
                if (throwable instanceof UnknownHostException) {
                    return Observable.error(throwable);
                }
                return Observable.just(throwable).zipWith(Observable.range(1, 3), new BiFunction<Throwable, Integer, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable, Integer retryCount) {

                        return retryCount;
                    }
                }).flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer retryCount) throws Exception {
                        return Observable.timer((long) Math.pow(mInterval, retryCount), TimeUnit.SECONDS);
                    }
                });
            }
        });
    }
}