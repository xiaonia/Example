package com.fdu.xuqingqi.example;

import com.fdu.xuqingqi.example.helpers.TestDbHelper;
import com.library.common.database.DbController;

import android.app.Application;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/5/4
 */

public class XApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init() {
        DbController.init(this, new TestDbHelper(getApplicationContext()));
    }

}
