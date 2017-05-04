package com.library.common.database;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DbController {

    private static DbController INSTANCE = null;
    //private Application mApplication;
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private DbWrapper mDatabase = null;
    private Handler mMainThread;

    private DbController(Application application, SQLiteOpenHelper dbHelper) {
        //this.mApplication = application;
        this.mDatabase = new DbWrapper(application, dbHelper);
        this.mMainThread = new Handler(Looper.getMainLooper());
    }

    public static DbController init(Application application, SQLiteOpenHelper dbHelper) {
        return new DbController(application, dbHelper);
    }

    public static DbController instance() {
        return INSTANCE;
    }

    private ExecutorService threadPool() {
        return mThreadPool;
    }

    private Handler mainThread() {
        return mMainThread;
    }

    private DbWrapper database() {
        return mDatabase;
    }

    @SuppressWarnings("unused")
    public void insert(final Object object, final DbListener dbListener){
        execute(dbListener, false, true, new DbWrapper.DbCallback<Void>() {
            @Override
            public Void doDbWork(SQLiteDatabase db) throws Exception {
                dispatchStart(dbListener);
                long id = db.insert(DbUtils.getTableName(object.getClass()), null, DbUtils.contentValuesFromObject(object));
                if (id > -1) {
                    dispatchSuccess(dbListener, id);
                    return null;
                }
                dispatchFailed(dbListener, "insert failed");
                return null;
            }
        });
    }

    @SuppressWarnings("unused")
    public void delete(final String table, final Map<String, Object> valueMap, final DbListener dbListener){
        execute(dbListener, false, true, new DbWrapper.DbCallback<Void>() {
            @Override
            public Void doDbWork(SQLiteDatabase db) throws Exception  {
                dispatchStart(dbListener);
                Pair<String, String[]> pair = DbUtils.whereFromMap(valueMap);
                if (pair != null) {
                    final int count = db.delete(table, pair.first, pair.second);
                    if (count > 0) {
                        dispatchSuccess(dbListener, count);
                        return null;
                    }
                }
                dispatchFailed(dbListener, "delete failed");
                return null;
            }
        });
    }

    @SuppressWarnings("unused")
    public void query(final Class classOfType, final Map<String, Object> valueMap, final DbListener dbListener){
        Pair<String, String[]> pair = DbUtils.whereFromMap(valueMap);
        if (pair != null) {
            query(classOfType, pair.first, pair.second, null, null, null, null, dbListener);
        } else {
            query(classOfType, null, null, null, null, null, null, dbListener);
        }
    }

    @SuppressWarnings("unused")
    public void query(final Class classOfType, final DbListener dbListener){
        query(classOfType, null, null, null, null, null, null, dbListener);
    }

    @SuppressWarnings("unused")
    public void query(final Class classOfType, final String orderBy, final String limit, final DbListener dbListener){
        query(classOfType, null, null, null, null, orderBy, limit, dbListener);
    }

    @SuppressWarnings("unused")
    public void query(final Class classOfType, final String selection, final String[] selectionArgs, final String groupBy,
            final String having, final String orderBy, final String limit, final DbListener dbListener){

        execute(dbListener, true, true, new DbWrapper.DbCallback<Void>() {
            @Override
            public Void doDbWork(SQLiteDatabase db) throws Exception {
                dispatchStart(dbListener);
                Cursor cursor = null;
                try {
                    cursor = db.query(DbUtils.getTableName(classOfType), null, selection, selectionArgs, groupBy, having, orderBy, limit);
                    if (cursor != null ) {
                        List<Object> result = new ArrayList<>();
                        while (cursor.moveToNext()){
                            try {
                                Object object = DbUtils.parseResultObject(classOfType, cursor);
                                result.add(object);
                            }catch (Exception ignore) {
                                ignore.printStackTrace();
                            }
                        }
                        if (result.size() > 0) {
                            dispatchSuccess(dbListener, result);
                            return null;
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                dispatchFailed(dbListener, "query failed");
                return null;
            }
        });
    }

    @SuppressWarnings("unused")
    public void update(final Class classOfType, final Map<String, Object> valueMap, final Map<String, Object> whereMap, final DbListener dbListener){
        execute(dbListener, false, true, new DbWrapper.DbCallback() {
            @Override
            public Object doDbWork(SQLiteDatabase db) throws Exception {
                dispatchStart(dbListener);
                String table = DbUtils.getTableName(classOfType);
                Pair<String, String[]> wherePair = DbUtils.whereFromMap(whereMap);
                ContentValues contentValues = DbUtils.contentValuesFromMap(valueMap);
                int id = db.update(table, contentValues, wherePair == null? null : wherePair.first, wherePair == null? null : wherePair.second);
                if (id >= 0) {
                    dispatchSuccess(dbListener, id);
                    return null;
                }
                dispatchFailed(dbListener, "update failed");
                return null;
            }
        });
    }

    private void execute(final DbListener listener, final boolean isRead, final boolean transactional, final DbWrapper.DbCallback callback) {
        threadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database().execute(isRead, transactional, callback);
                } catch (Exception allEx) {
                    allEx.printStackTrace();
                    dispatchFailed(listener, allEx.getMessage());
                }
            }
        });
    }

    private void dispatchStart(final DbListener dbListener) {
        mainThread().post(new Runnable() {
            @Override
            public void run() {
                if (dbListener == null) {
                    return;
                }
                dbListener.onDbOpStart();
            }
        });
    }

    private void dispatchSuccess(final DbListener dbListener, final Object object) {
        mainThread().post(new Runnable() {
            @Override
            public void run() {
                if (dbListener == null) {
                    return;
                }
                dbListener.onDbOpSuccess(object);
            }
        });
    }

    private void dispatchFailed(final DbListener dbListener, final String message) {
        mainThread().post(new Runnable() {
            @Override
            public void run() {
                if (dbListener == null) {
                    return;
                }
                dbListener.onDbOpFailed(message);
            }
        });
    }

}
