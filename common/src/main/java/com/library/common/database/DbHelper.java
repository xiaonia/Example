package com.library.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * TEXT : String
 * INTEGER : Long Short Integer Byte Boolean
 * REAL : Double Float
 * BLOB : the rest classes
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = DbHelper.class.getSimpleName();

    // Any changes to the database format *must* include update-in-place code.

    public DbHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            //createTable(db, OAuthBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            //upgradeTable(db, oldVersion, newVersion, OAuthBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked, unused")
    protected void createTable(SQLiteDatabase db, Class cls) throws Exception {
        StringBuilder sb = new StringBuilder("");
        sb.append("CREATE TABLE");
        sb.append(" ");
        Annotation annotation = cls.getAnnotation(DbTable.class);
        if (annotation instanceof DbTable) {
            DbTable dbTable = (DbTable) annotation;
            sb.append(dbTable.table());
            sb.append(" ");
        } else {
            throw new Exception("can not find table name for " + cls.getSimpleName());
        }

        sb.append("(");
        boolean firstColumn = true;
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DbColumn.class);
            if (columnAnnotation != null) {
                DbColumn dbColumn = (DbColumn) columnAnnotation;
                String column = dbColumn.column();
                int version = dbColumn.version();
                boolean deprecated = dbColumn.deprecated();
                boolean primary = dbColumn.primary();
                boolean nullable = dbColumn.nullable();
                if (deprecated) {
                    continue;
                }

                if (firstColumn) {
                    firstColumn = false;
                } else {
                    sb.append(",");
                    sb.append(" ");
                }
                sb.append(column);

                Class type = field.getType();
                String sqlType = getSQLiteType(type);
                sb.append(" ");
                sb.append(sqlType);

                if (!nullable) {
                    sb.append(" ");
                    sb.append("NOT NULL");
                }

                if (primary) {
                    sb.append(" ");
                    sb.append("PRIMARY KEY");
                }
            }
        }
        sb.append(")");

        db.execSQL(sb.toString());
    }

    @SuppressWarnings("unchecked, unused")
    protected void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion, Class cls) throws Exception {
        String table = "";
        Annotation annotation = cls.getAnnotation(DbTable.class);
        if (annotation instanceof DbTable) {
            DbTable dbTable = (DbTable) annotation;
            table = dbTable.table();
        } else {
            throw new Exception("can not find table name for " + cls.getSimpleName());
        }

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DbColumn.class);
            if (columnAnnotation != null) {
                DbColumn dbColumn = (DbColumn) columnAnnotation;
                String column = dbColumn.column();
                int version = dbColumn.version();
                boolean deprecated = dbColumn.deprecated();
                boolean primary = dbColumn.primary();
                boolean nullable = dbColumn.nullable();
                if (version != newVersion || deprecated) {
                    continue;
                }

                StringBuilder sb = new StringBuilder("");
                sb.append("ALTER TABLE");
                sb.append(" ");
                sb.append(table);
                sb.append(" ");
                sb.append("ADD");
                sb.append(" ");
                sb.append(column);

                Class type = field.getType();
                String sqlType = getSQLiteType(type);
                sb.append(" ");
                sb.append(sqlType);

                if (!nullable) {
                    sb.append(" ");
                    sb.append("NOT NULL");
                }

                if (primary) {
                    sb.append(" ");
                    sb.append("PRIMARY KEY");
                }

                try {
                    db.execSQL(sb.toString());
                } catch (SQLiteException e) {
                    if (!e.getMessage().startsWith("duplicate column name:")) {
                        Log.e(TAG, "Unable to add " + column + " column to " + table);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected String getSQLiteType(Class type) {
        String sqlType;
        if (type.isAssignableFrom(String.class)) {
            sqlType = "TEXT";
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)
                || type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)
                || type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {

            sqlType = "INTEGER";
        } else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {

            sqlType = "REAL";
        } else {
            sqlType = "BLOB";
        }

        return sqlType;
    }

}
