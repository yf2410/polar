package com.polar.browser.vclibrary.rx;

import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.network.exception.NoResultException;
import com.polar.browser.vclibrary.network.exception.ResultErrorException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by saifei on 17/4/5.
 */

public abstract class ResultObserver<T> implements Observer<Result<T>> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(Result<T> result) {
        try {
            if (result != null) {
                int code = result.getCode();
                /**
                 * 服务器可以正常处理该请求业务
                 */
                if (code == Result.SUCCESS) {
                    /**
                     * CV校验通过
                     * 暂时不支持cv校验
                     */
//                        String responseCV = result.getCv();
//                        String requestCV = call.request().url().queryParameter(ApiConstants.PARAM_CV);
//                        if (Util.equals(responseCV, requestCV)) {
//                        } else {
//                            throw new CVNotConsistentException(requestCV, responseCV);
//                        }
                    if (result.getData() != null) {
                        success(result.getData());
                    } else {
                        throw new NoResultException("data is null!");
                    }
                } else {
                    throw new ResultErrorException(code, result.getMessage());
                }
            } else {
                throw new NoResultException("Result is null!");
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void onComplete() {

    }
    public abstract void success(T data) throws Exception;

}
