package com.library.common.database;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseWrapper {

    private final Lock mReadLock;
    private final Lock mWriteLock;
    {
        final ReadWriteLock lock = new ReentrantReadWriteLock(true);
        mReadLock = lock.readLock();
        mWriteLock = lock.writeLock();
    }

    private Application mApp;
    private SQLiteOpenHelper mDbHelper;

    private SQLiteDatabase mDb;
    private ThreadLocal<Boolean> inTransaction = new ThreadLocal<Boolean>();

    public static interface DbCallback<T>{

        T doDbWork(SQLiteDatabase db);
    }

    public DatabaseWrapper(Application application, SQLiteOpenHelper dbHelper){
        this.mApp = application;
        this.mDbHelper = dbHelper;
    }

    public <T> T execute(final boolean isRead, final boolean transactional, final DbCallback<T> callback){
        if (isRead) {
            mReadLock.lock();
        } else {
            mWriteLock.lock();
        }
        final boolean doTransaction = transactional && inTransaction.get() == null;
        try{
            if (isRead) {
                mDb = mDbHelper.getReadableDatabase();
            } else {
                mDb = mDbHelper.getWritableDatabase();
            }
            if (mDb == null) {
                throw new Exception("open or create database failed");
            }

            if (doTransaction) {
                inTransaction.set(true);
                mDb.beginTransaction();
            }
            final T result = callback.doDbWork(mDb);
            if (doTransaction) {
                mDb.setTransactionSuccessful();
            }
            return result;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (doTransaction) {
                mDb.endTransaction();
                inTransaction.set(null);
            }
            if (mDb.isOpen()) {
                mDb.close();
            }
            if (isRead) {
                mReadLock.unlock();
            } else {
                mWriteLock.unlock();
            }
        }
        return null;
    }

}
