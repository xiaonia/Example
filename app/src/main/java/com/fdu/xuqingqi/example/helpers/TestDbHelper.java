package com.fdu.xuqingqi.example.helpers;

import com.fdu.xuqingqi.example.domain.User;
import com.library.common.database.DbHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/5/4
 */

public class TestDbHelper extends DbHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "data.db";

    public TestDbHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        try {
            createTable(db, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        try {
            upgradeTable(db, oldVersion, newVersion, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
