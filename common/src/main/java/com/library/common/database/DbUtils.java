package com.library.common.database;

import android.content.ContentValues;
import android.database.Cursor;
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

public class DbUtils {

    /** build where clause from map */
    public static Pair<String, String[]> whereFromMap(Map<String, Object> paramMap) {
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

    public static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    /** build ContentValues from map */
    public static ContentValues contentValuesFromMap(Map<String, Object> valueMap) {
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
                    values.put(key, writeObject(value));
                }
            }
            return values;
        }
        return null;
    }

    /** build ContentValues from object */
    public static ContentValues contentValuesFromObject(Object object) throws IllegalAccessException{
        Class cls = object.getClass();
        ContentValues values = new ContentValues();
        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DbColumn.class);
            if (columnAnnotation != null) {
                DbColumn dbColumn = (DbColumn) columnAnnotation;
                String column = dbColumn.column();
                boolean nullable = dbColumn.nullable();
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
                    values.put(column, writeObject(value));
                }
            }
        }

        return values;
    }

    /** parse result of query to object list */
    @SuppressWarnings("unchecked")
    public static Object parseResultObject(Class cls, Cursor cursor) throws IllegalAccessException, InstantiationException {
        Object instance = cls.newInstance();
        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DbColumn.class);
            if (columnAnnotation != null) {
                DbColumn dbColumn = (DbColumn) columnAnnotation;
                String column = dbColumn.column();
                int version = dbColumn.version();
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
                    value = readObject(cursor.getBlob(columnIndex));
                }
                if (value != null) {
                    field.set(instance, value);
                }
            }
        }

        return instance;
    }

    /** get database table name from class */
    public static String getTableName(Class cls) throws IllegalArgumentException{
        Annotation annotation = cls.getAnnotation(DbTable.class);
        if (annotation instanceof DbTable) {
            DbTable dbTable = (DbTable) annotation;
            return dbTable.table();
        } else {
            throw new IllegalArgumentException("can not find table name for class " + cls.getSimpleName());
        }
    }

    /** write object to byte array */
    private static byte[] writeObject(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
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
        return null;
    }

    /** read object from byte array */
    private static Object readObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

}
