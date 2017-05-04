package com.library.common.database;

/**
 * Created by xuqingqi on 2016/1/15.
 */
public interface DataListener {

    void onOperationStart();

    void onOperationSuccess(Object extra);

    void onOperationFailed(String message);

}
