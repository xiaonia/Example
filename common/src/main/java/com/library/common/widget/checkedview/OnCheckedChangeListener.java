package com.library.common.widget.checkedview;

import android.widget.Checkable;

/**
 * Interface definition for a callback to be invoked when the checked state
 * of a compound button changed.
 */
public interface OnCheckedChangeListener {
    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param checkable The compound button view whose state has changed.
     * @param isChecked The new checked state of buttonView.
     */
    void onCheckedChanged(Checkable checkable, boolean isChecked);
}