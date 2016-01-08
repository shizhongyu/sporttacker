package com.tulipsport.android.common.iface;
/*
 * @description
 *   Please write the SimpleVerifyCallback module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(11/17/2015)
 */

public interface SimpleVerifyCallback<T, K> {

    /**
     * 出现错误的回调方法
     * @param error 错误参数
     */
    void onError(K error);

    /**
     * 成功后回调方法
     * @param result 处理结果
     */
    void onSuccess(T result);
}
