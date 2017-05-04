package com.library.common.database;

public interface DbListener {

    void onDbOpStart();

    void onDbOpSuccess(Object extra);

    void onDbOpFailed(String message);

}
