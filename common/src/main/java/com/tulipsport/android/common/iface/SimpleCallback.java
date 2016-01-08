package com.tulipsport.android.common.iface;
/*
 * @description
 *   Please write the SimpleCallback module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(11/17/2015)
 */

public interface SimpleCallback<T> {

    /**
     * 回调方法
     * @param result 传入回调的处理结果
     */
    void invoke(T result);
}
