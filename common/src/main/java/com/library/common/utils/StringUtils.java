package com.library.common.utils;

import android.text.TextUtils;

public class StringUtils {

    public static boolean equals(String a, String b) {
        return a == b || (a != null && a.equals(b));
    }

    public static boolean isEmpty(String value) {
        return TextUtils.isEmpty(value) || "null".equalsIgnoreCase(value);
    }

    public static int isEmpty(String s, int t, int f) {
        return isEmpty(s) ? t : f;
    }

    public static String isNull(String s, String def) {
        return isNull(s) ? def : s;
    }

    public static boolean isNull(String s) {
        return isEmpty(s) || "\"null\"".equalsIgnoreCase(s);
    }

    /**
     * Do check whether this String s has a specific prefix.
     *
     * @param s      The string to check.
     * @param prefix Specified prefix String.
     * @return true if string s has prefix, otherwise false.
     */
    public static boolean hasPrefix(String s, String prefix) {
        return !isEmpty(s) && s.startsWith(prefix);
    }

}
