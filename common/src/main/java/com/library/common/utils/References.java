package com.library.common.utils;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class References {
    public static <E> SoftReference<E> newSoftReference(E value) {
        return new SoftReference<E>(value);
    }

    public static <E> WeakReference<E> newWeakReference(E value) {
        return new WeakReference<E>(value);
    }
}
