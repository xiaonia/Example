package com.library.common.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Method;

/**
 * about:
 * author: xuqingqi
 * e-mail: xuqingqi01@gmail.com
 * date: 2017/3/18
 */

public class ResizeImageView extends AppCompatImageView {

    public ResizeImageView(Context context) {
        super(context);
    }

    public ResizeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            Method method = View.class.getDeclaredMethod("setMeasuredDimensionRaw", int.class, int.class);
            if (method != null) {
                method.setAccessible(true);
                method.invoke(this, getMeasuredWidthAndState(), getMeasuredWidthAndState());
            }
        } catch (Exception e) {
            e.printStackTrace();
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
        }
    }

}
