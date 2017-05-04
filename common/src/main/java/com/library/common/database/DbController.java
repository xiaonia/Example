package com.library.common.database;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class DbController {


    private static DbController INSTANCE = null;
    private Application mApplication;
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private DatabaseWrapper mDatabase = null;
    private Handler mMainThread;

    private DbController(Application application, SQLiteOpenHelper dbHelper) {
        this.mApplication = application;
        this.mDatabase = new DatabaseWrapper(application, dbHelper);
        this.mMainThread = new Handler(Looper.getMainLooper());
    }

    public static DbController init(Application application, SQLiteOpenHelper dbHelper) {
        return new DbController(application, dbHelper);
    }

    public static DbController getInstance() {
        return INSTANCE;
    }

    private ExecutorService getThreadPool() {
        return mThreadPool;
    }

    private Handler getMainThread() {
        return mMainThread;
    }

    private DatabaseWrapper getDatabase() {
        return mDatabase;
    }

    @SuppressWarnings("unused")
    public void insert(final Object object, final DataListener dataListener){
        getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                getDatabase().execute(false, true, new DatabaseWrapper.DbCallback<Void>() {
                    @Override
                    public Void doDbWork(SQLiteDatabase db) {
                        dispatchStart(dataListener);

                        try {
                            final long id = insertObject(db, object);
                            if (id > -1) {
                                dispatchSuccess(dataListener, id);
                                return null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        dispatchFailed(dataListener, "insert failed");
                        return null;
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    public void insert(final String table, final Map<String, Object> valueMap, final DataListener dataListener){
        getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                getDatabase().execute(false, true, new DatabaseWrapper.DbCallback<Void>() {
                    @Override
                    public Void doDbWork(SQLiteDatabase db) {
                        dispatchStart(dataListener);

                        ContentValues values = buildContentValues(valueMap);
                        if (values != null) {
                            final long id = db.insert(table, null, values);
                            if (id > -1) {
                                dispatchSuccess(dataListener, id);
                                return null;
                            }
                        }

                        dispatchFailed(dataListener, "insert failed");
                        return null;
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    public void delete(final String table, final Map<String, Object> valueMap, final DataListener dataListener){
        getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                getDatabase().execute(false, true, new DatabaseWrapper.DbCallback<Void>() {
                    @Override
                    public Void doDbWork(SQLiteDatabase db) {
                        dispatchStart(dataListener);

                        Pair<String, String[]> pair = buildWhereClause(valueMap);
                        if (pair != null) {
                            final int count = db.delete(table, pair.first, pair.second);
                            if (count > 0) {
                                dispatchSuccess(dataListener, count);
                                return null;
                            }
                        }

                        dispatchFailed(dataListener, "delete failed");
                        return null;
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    public void query(final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having,
            final String orderBy, final String limit, final DataListener dataListener, final Class classOfType){

        getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                getDatabase().execute(false, true, new DatabaseWrapper.DbCallback<Void>() {
                    @Override
                    public Void doDbWork(SQLiteDatabase db) {
                        dispatchStart(dataListener);

                        Cursor cursor = null;
                        try {
                            final List<Object> result = new ArrayList<>();
                            cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
                            if (cursor != null ) {
                                while (cursor.moveToNext()){
                                    try {
                                        Object object = parseResultObject(classOfType, cursor);
                                        result.add(object);
                                    }catch (Exception ignore) {
                                        ignore.printStackTrace();
                                    }
                                }

                                if (result.size() > 0) {
                                    dispatchSuccess(dataListener, result);
                                    return null;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }

                        dispatchFailed(dataListener, "query failed");
                        return null;
                    }
                });
            }
        });
    }



    private void dispatchStart(final DataListener dataListener) {
        getMainThread().post(new Runnable() {
            @Override
            public void run() {
                if (dataListener == null) {
                    return;
                }
                dataListener.onOperationStart();
            }
        });
    }

    private void dispatchSuccess(final DataListener dataListener, final Object object) {
        getMainThread().post(new Runnable() {
            @Override
            public void run() {
                if (dataListener == null) {
                    return;
                }
                dataListener.onOperationSuccess(object);
            }
        });
    }

    private void dispatchFailed(final DataListener dataListener, final String message) {
        getMainThread().post(new Runnable() {
            @Override
            public void run() {
                if (dataListener == null) {
                    return;
                }
                dataListener.onOperationFailed(message);
            }
        });
    }

    private static Pair<String, String[]> buildWhereClause(Map<String, Object> paramMap) {
        if (paramMap == null || paramMap.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        List<String> argList = new ArrayList<>();
        boolean firstParam = true;
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!TextUtils.isEmpty(key)) {
                if (!firstParam) {
                    sb.append(" AND");
                } else {
                    firstParam = false;
                    //sb.append(" WHERE");
                }

                if (value instanceof Collection) {
                    Collection childValues = (Collection) value;
                    boolean firstChild = true;
                    for (Object childValue : childValues) {
                        if (!firstChild) {
                            sb.append(" OR");
                            sb.append(" ");
                        } else {
                            firstChild = false;
                            sb.append(" (");
                        }
                        sb.append(key);
                        sb.append("=");
                        sb.append("?");
                        argList.add(String.valueOf(childValue));
                    }
                    if (firstChild) {
                        sb.append(")");
                    }
                } else {
                    sb.append(" ");
                    sb.append(key);
                    sb.append("=");
                    sb.append("?");
                    argList.add(String.valueOf(value));
                }
            }
        }
        String[] args = new String[argList.size()];
        argList.toArray(args);
        return new Pair<>(sb.toString(), args);
    }

    private static ContentValues buildContentValues(Map<String, Object> valueMap) {
        if (valueMap != null) {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value == null) {
                    values.putNull(key);
                } else if (value instanceof String) {
                    values.put(key, (String) value);
                } else if (value instanceof Byte) {
                    values.put(key, (Byte) value);
                } else if (value instanceof Boolean) {
                    values.put(key, (Boolean) value? 1 : 0);
                } else if (value instanceof Double) {
                    values.put(key, (Double) value);
                } else if (value instanceof Float) {
                    values.put(key, (Float) value);
                } else if (value instanceof Integer) {
                    values.put(key, (Integer) value);
                } else if (value instanceof Long) {
                    values.put(key, (Long) value);
                } else if (value instanceof Short) {
                    values.put(key, (Short) value);
                } else {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutput out = null;
                        try {
                            out = new ObjectOutputStream(bos);
                            out.writeObject(value);
                            out.flush();
                            values.put(key, bos.toByteArray());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } finally {
                            try {
                                bos.close();
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException ex) {
                                // ignore close exception
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return values;
        }
        return null;
    }

    /** insert an object to database */
    private long insertObject(SQLiteDatabase db, Object object) throws Exception {
        String table = "";
        Class cls = object.getClass();
        Annotation annotation = cls.getAnnotation(DatabaseTable.class);
        if (annotation instanceof DatabaseTable) {
            DatabaseTable databaseTable = (DatabaseTable) annotation;
            table = databaseTable.table();
        } else {
            throw new Exception("can not find table name for " + cls.getSimpleName());
        }

        ContentValues values = new ContentValues();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DatabaseColumn.class);
            if (columnAnnotation != null) {
                DatabaseColumn databaseColumn = (DatabaseColumn) columnAnnotation;
                String column = databaseColumn.column();
                boolean nullable = databaseColumn.nullable();
                field.setAccessible(true);
                Class type = field.getType();
                Object value = field.get(object);

                if (value == null) {
                    values.putNull(column);
                } else if (value instanceof String) {
                    values.put(column, (String) value);
                } else if (value instanceof Boolean) {
                    values.put(column, (Boolean) value? 1 : 0);
                } else if (value instanceof Double) {
                    values.put(column, (Double) value);
                } else if (value instanceof Float) {
                    values.put(column, (Float) value);
                } else if (value instanceof Integer) {
                    values.put(column, (Integer) value);
                } else if (value instanceof Long) {
                    values.put(column, (Long) value);
                } else if (value instanceof Short) {
                    values.put(column, (Short) value);
                }  else if (value instanceof Byte) {
                    values.put(column, (Byte) value);
                } else {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutput out = null;
                        try {
                            out = new ObjectOutputStream(bos);
                            out.writeObject(value);
                            out.flush();
                            values.put(column, bos.toByteArray());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } finally {
                            try {
                                bos.close();
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException ex) {
                                // ignore close exception
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return db.insert(table, null, values);
    }

    /** parse result of query to object list */
    @SuppressWarnings("unchecked")
    private Object parseResultObject(Class cls, Cursor cursor) throws IllegalAccessException, InstantiationException {
        Object instance = cls.newInstance();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DatabaseColumn.class);
            if (columnAnnotation != null) {
                DatabaseColumn databaseColumn = (DatabaseColumn) columnAnnotation;
                String column = databaseColumn.column();
                int version = databaseColumn.version();
                field.setAccessible(true);

                int columnIndex = cursor.getColumnIndex(column);
                if (cursor.isNull(columnIndex)) {
                    continue;
                }

                Class type = field.getType();
                Object value = null;
                if (type.isAssignableFrom(String.class)) {
                    value = cursor.getString(columnIndex);
                } else if (type.isAssignableFrom(Long.class)
                        || type.isAssignableFrom(long.class)) {
                    value = cursor.getLong(columnIndex);
                } else if (type.isAssignableFrom(Short.class)
                        || type.isAssignableFrom(short.class)) {
                    value = cursor.getShort(columnIndex);
                } else if (type.isAssignableFrom(Integer.class)
                        || type.isAssignableFrom(int.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (type.isAssignableFrom(Float.class)
                        || type.isAssignableFrom(float.class)) {
                    value = cursor.getFloat(columnIndex);
                } else if (type.isAssignableFrom(Double.class)
                        || type.isAssignableFrom(double.class)) {
                    value = cursor.getDouble(columnIndex);
                } else if (type.isAssignableFrom(Boolean.class)
                        || type.isAssignableFrom(boolean.class)) {
                    value = cursor.getInt(columnIndex) != 0;
                }  else if (type.isAssignableFrom(Byte.class)
                        || type.isAssignableFrom(byte.class)) {
                    value = cursor.getInt(columnIndex);
                } else {
                    try {
                        ByteArrayInputStream bis = new ByteArrayInputStream(cursor.getBlob(columnIndex));
                        ObjectInput in = null;
                        try {
                            in = new ObjectInputStream(bis);
                            value = in.readObject();
                        } catch (IOException ioe) {
                          ioe.printStackTrace();
                        } finally {
                            try {
                                if (in != null) {
                                    in.close();
                                }
                            } catch (IOException ex) {
                                // ignore close exception
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (value != null) {
                    try {
                        field.set(instance, value);
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                    }
                }
            }
        }

        return instance;
    }

}
