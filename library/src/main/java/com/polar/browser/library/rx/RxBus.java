package com.polar.browser.library.rx;

import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by saifei on 17/4/6.
 * 用来实现EventBus功能
 */

public class RxBus {

    private final FlowableProcessor<Object> mBus;

    private RxBus() {
        // toSerialized method made bus thread safe
        mBus = PublishProcessor.create().toSerialized();
    }

    public static RxBus get() {
        return Holder.BUS;
    }

    public void post(Object obj) {
        mBus.onNext(obj);
    }

    public <T> Flowable<T> toFlowable(Class<T> tClass) {
        return mBus.ofType(tClass);
    }

    public <T> Flowable<T> safetySubscribe(Class<T> tClass, RxFragmentActivity activity) {
        return toFlowable(tClass).compose(activity.<T>bindToLifecycle()).observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<Object> toFlowable() {
        return mBus;
    }

    public boolean hasSubscribers() {
        return mBus.hasSubscribers();
    }

    private static class Holder {
        private static final RxBus BUS = new RxBus();
    }


}
