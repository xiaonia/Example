package com.library.common.database;

/**
 * Created by xuqingqi on 2016/1/15.
 */
public interface DbListener {

    void onDbOpStart();

    void onDbOpSuccess(Object extra);

    void onDbOpFailed(String message);

}
