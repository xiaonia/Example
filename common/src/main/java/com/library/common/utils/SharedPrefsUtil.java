package com.library.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

public class SharedPrefsUtil{

    public static boolean putValue(Context context, String file, String key, Object value) {
        if (context == null || TextUtils.isEmpty(file)) {
            return false;
        }

        SharedPreferences preferences = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return putValue(preferences, key, value);
    }

    public static boolean putValue(SharedPreferences sharedPreferences, String key, Object value) {
        if (sharedPreferences == null || TextUtils.isEmpty(key)) {
            return false;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            if (value == null) {
                editor.remove(key);
            } if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Set) {
                Set set = (Set) value;
                if (set.size() != 0
                        && set.toArray()[0] instanceof String) {
                    editor.putStringSet(key, set);
                }
            } else {
                return false;
            }
        } catch (ClassCastException cce) {
            cce.printStackTrace();
            return false;
        }

        editor.commit();
        return true;
    }

    public static<T> T getValue(Context context, String file, String key, T defValue){
        if (context == null || TextUtils.isEmpty(file)) {
            return defValue;
        }

        SharedPreferences sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return getValue(sp, key, defValue);
    }

    @SuppressWarnings("unchecked")
    public static<T> T getValue(SharedPreferences preferences, String key, T defValue) {
        if (preferences == null || TextUtils.isEmpty(key)) {
            return defValue;
        }

        try {
            if (defValue instanceof Integer) {
                return (T) Integer.valueOf(preferences.getInt(key, (Integer) defValue));
            } else if (defValue instanceof Long) {
                return (T) Long.valueOf(preferences.getLong(key, (Long) defValue));
            } else if (defValue instanceof Float) {
                return (T) Float.valueOf(preferences.getFloat(key, (Float) defValue));
            } else if (defValue instanceof Boolean) {
                return (T) Boolean.valueOf(preferences.getBoolean(key, (Boolean) defValue));
            } else if (defValue instanceof String) {
                return (T) preferences.getString(key, (String) defValue);
            } else if (defValue instanceof Set) {
                Set set = (Set) defValue;
                return (T) preferences.getStringSet(key, set);
            }
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        }

        return defValue;
    }

}
