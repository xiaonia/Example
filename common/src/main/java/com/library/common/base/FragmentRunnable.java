package com.library.common.base;

import com.library.common.log.LogUtils;
import com.library.common.utils.References;

import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;

/**
 * Small Runnable-like wrapper that first checks that the Fragment is in a good state before
 * doing any work. Ideal for use with a {@link android.os.Handler}.
 */
public abstract class FragmentRunnable implements Runnable {

    public static final String TAG = "FragmentRunnable";
    private final String mOpName;
    private final WeakReference<Fragment> mData;

    public FragmentRunnable(String opName, Fragment fragment) {
        mOpName = opName;
        mData = References.newWeakReference(fragment);
    }

    @Override
    public final void run() {
        final Fragment fragment = mData.get();
        if (fragment == null) {
            LogUtils.i(TAG, "Unable to run op='%s' b/c fragment has been recycled", mOpName);
            return;
        }
        if (!fragment.isAdded()) {
            LogUtils.i(TAG, "Unable to run op='%s' b/c fragment is not attached: %s", mOpName, fragment);
            return;
        }
        go();
    }

    public abstract void go();

}
