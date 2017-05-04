/**
 * Copyright 2015 Pengyuan-Jiang
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Author：Ybao on 2015/11/7 ‏‎0:27
 * <p/>
 * QQ: 392579823
 * <p/>
 * Email：392579823@qq.com
 */
package com.library.common.widget.pullview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


public abstract class AbsFooterView extends RelativeLayout implements IFooter {

    private boolean mLoading = false;
    private String mStateString;
    private Drawable mStateDrawable;

    public AbsFooterView(Context context) {
        this(context, null);
    }

    public AbsFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbsFooterView> T setStateString(String title) {
        this.mStateString = title;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbsFooterView> T setStateDrawable(Drawable drawable) {
        this.mStateDrawable = drawable;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbsFooterView> T setStateDrawable(int resId){
        if (resId <= 0) {
            return (T) this;
        }
        this.mStateDrawable = ContextCompat.getDrawable(getContext(), resId);
        return (T) this;
    }

    public String getStateString() {
        if (mStateString == null) {
            return "";
        }
        return mStateString;
    }

    public Drawable getStateDrawable() {
        return mStateDrawable;
    }
}

