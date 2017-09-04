package com.polar.browser.vclibrary.network;

import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.network.exception.NoResultException;
import com.polar.browser.vclibrary.network.exception.ResponseNotSuccessException;
import com.polar.browser.vclibrary.network.exception.ResultErrorException;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by James on 2016/9/18.
 * 处理按约定Result格式响应的Callback
 */

public abstract class ResultCallback<T> implements retrofit2.Callback<Result<T>> {

    @Override
    public void onResponse(Call<Result<T>> call, Response<Result<T>> response) {
        try {
            /**
             * HTTP响应成功
             */
            if (response.isSuccessful()) {
                Result<T> result = response.body();
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
                            success(result.getData(), call, response);
                        } else {
                            throw new NoResultException("data is null!");
                        }
                    } else {
                        throw new ResultErrorException(code, result.getMessage());
                    }
                } else {
                    throw new NoResultException("Result is null!");
                }
            } else {
                throw new ResponseNotSuccessException(response.code(), response.errorBody());
            }
        } catch (Exception e) {
            error(call, e);
        }
    }

    @Override
    public void onFailure(Call<Result<T>> call, Throwable t) {
        error(call, t);
    }

    public abstract void success(T data, Call<Result<T>> call, Response<Result<T>> response) throws Exception;


    public abstract void error(Call<Result<T>> call, Throwable t);
}
