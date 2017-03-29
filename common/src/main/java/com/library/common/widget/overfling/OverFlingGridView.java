package com.library.common.widget.overfling;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.GridView;

import java.lang.reflect.Field;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/3/27
 */

public class OverFlingGridView extends GridView {

    private static final int OVER_FLING_DISTANCE_DIP = 200;

    public OverFlingGridView(Context context) {
        super(context);
        init();
    }

    public OverFlingGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverFlingGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverFlingGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        try {
            setOverScrollMode(AbsListView.OVER_SCROLL_ALWAYS);
            int overFlingDistancePx = (int) (getContext().getResources().getDisplayMetrics().density
                    * OVER_FLING_DISTANCE_DIP + 0.5);
            Field overFling = AbsListView.class.getDeclaredField("mOverflingDistance");
            overFling.setAccessible(true);
            overFling.set(this, overFlingDistancePx);

            Field overScroll = AbsListView.class.getDeclaredField("mOverscrollDistance");
            overScroll.setAccessible(true);
            overScroll.set(this, overFlingDistancePx);

            Field topEdge = AbsListView.class.getDeclaredField("mEdgeGlowTop");
            topEdge.setAccessible(true);
            topEdge.set(this, new EmptyEdgeEffect(getContext()));

            Field bottomEdge = AbsListView.class.getDeclaredField("mEdgeGlowBottom");
            bottomEdge.setAccessible(true);
            bottomEdge.set(this, new EmptyEdgeEffect(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
