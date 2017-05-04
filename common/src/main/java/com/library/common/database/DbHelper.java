package com.library.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/4/15
 *
 * TEXT : String
 * INTEGER : Long Short Integer Byte Boolean
 * REAL : Double Float
 * BLOB : the rest classes
 */

public class DbHelper extends SQLiteOpenHelper {

    // Any changes to the database format *must* include update-in-place code.
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "data.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    private void createTable(SQLiteDatabase db, Class cls) throws Exception {
        StringBuffer sb = new StringBuffer("");
        sb.append("CREATE TABLE");
        sb.append(" ");
        Annotation annotation = cls.getAnnotation(DatabaseTable.class);
        if (annotation instanceof DatabaseTable) {
            DatabaseTable databaseTable = (DatabaseTable) annotation;
            sb.append(databaseTable.table());
            sb.append(" ");
        } else {
            throw new Exception("can not find table name for " + cls.getSimpleName());
        }

        sb.append("(");
        boolean firstColumn = true;
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DatabaseColumn.class);
            if (columnAnnotation != null) {
                DatabaseColumn databaseColumn = (DatabaseColumn) columnAnnotation;
                String column = databaseColumn.column();
                int version = databaseColumn.version();
                boolean deprecated = databaseColumn.deprecated();
                boolean primary = databaseColumn.primary();
                boolean nullable = databaseColumn.nullable();
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
                if (sqlType == null) {
                    throw new Exception("can not find SQLite Type for " + type.getSimpleName());
                } else {
                    sb.append(" ");
                    sb.append(sqlType);
                }

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
    private void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion, Class cls) throws Exception {
        String table = "";
        Annotation annotation = cls.getAnnotation(DatabaseTable.class);
        if (annotation instanceof DatabaseTable) {
            DatabaseTable databaseTable = (DatabaseTable) annotation;
            table = databaseTable.table();
        } else {
            throw new Exception("can not find table name for " + cls.getSimpleName());
        }

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation columnAnnotation = field.getAnnotation(DatabaseColumn.class);
            if (columnAnnotation != null) {
                DatabaseColumn databaseColumn = (DatabaseColumn) columnAnnotation;
                String column = databaseColumn.column();
                int version = databaseColumn.version();
                boolean deprecated = databaseColumn.deprecated();
                boolean primary = databaseColumn.primary();
                boolean nullable = databaseColumn.nullable();
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
                if (sqlType == null) {
                    throw new Exception("can not find SQLite Type for " + type.getSimpleName());
                } else {
                    sb.append(" ");
                    sb.append(sqlType);
                }

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
                        Log.e(DATABASE_NAME, "Unable to add " + column + " column to " + table);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String getSQLiteType(Class type) {
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
