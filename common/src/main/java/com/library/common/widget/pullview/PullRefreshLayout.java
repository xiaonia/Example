package com.library.common.widget.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PullRefreshLayout extends NestedFlingLayout {

    private static final String TAG = PullRefreshLayout.class.getSimpleName();

    protected AbsFooterView mFooter;
    protected AbsHeaderView mHeader;

    protected boolean mRefreshing = false;
    protected boolean mLoading = false;
    protected boolean mReturningToStart = false;

    protected OnLoadListener mOnLoadListener;
    protected OnRefreshListener mOnRefreshListener;

    protected Runnable mRefreshRunnable;
    protected Runnable mLoadRunnable;

    public PullRefreshLayout(Context context) {
        this(context, null);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void dispatchScroll(int y) {
        if (mFooter != null && y >= 0) {
            mFooter.onScroll(this, y);
        }
        if (mHeader != null && y <= 0) {
            mHeader.onScroll(this, y);
        }
    }

    @Override
    protected void onAddView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof AbsHeaderView && mHeader == null) {
            mHeader = (AbsHeaderView) child;
        } else if (child instanceof AbsFooterView && mFooter == null) {
            mFooter = (AbsFooterView) child;
        } else {
            super.onAddView(child, index, params);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int height = getHeight();
        if (mHeader != null) {
            mHeader.layout(mHeader.getLeft(), -mHeader.getMeasuredHeight(), mHeader.getRight(), 0);
        }
        if (mFooter != null) {
            mFooter.layout(mFooter.getLeft(), height, mFooter.getRight(), height + mFooter.getMeasuredHeight());
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes)
                //&& !mReturningToStart && !mRefreshing && !mLoading
                ;
    }

    @Override
    protected int totalScrollPullDown() {
        if (mHeader != null) {
            return -mHeader.getMeasuredHeight();
        }
        return super.totalScrollPullDown();
    }

    @Override
    protected int totalScrollPullUp() {
        if (mFooter != null) {
            return mFooter.getMeasuredHeight();
        }
        return super.totalScrollPullUp();
    }

    @Override
    protected int targetScrollPullDown() {
        if (mHeader != null) {
            return -mHeader.getTargetHeight();
        }
        return super.targetScrollPullDown();
    }

    @Override
    protected int targetScrollPullUp() {
        if (mFooter != null) {
            return mFooter.getTargetHeight();
        }
        return super.targetScrollPullUp();
    }

    @Override
    protected void finishMoveChild(int distance) {
        if (distance > 0) {
            if (distance > 0) {//上拉位置大于零就可以加载
                if (mLoading) {
                    return;
                }
                if (!mRefreshing
                        && (mOnLoadListener != null && mOnLoadListener.beforeLoad())) {
                    setLoading(true, 0);
                    mOnLoadListener.onLoad();
                    return;
                }
            }
        }
        else if (distance < 0) {
            if (distance <= getRefreshScroll()) {//下拉必须超过刷新距离才可以刷新
                if (mRefreshing) {
                    startMoveChildTo(getRefreshScroll(), true);
                    return;
                }
                if (!mLoading
                        && (mOnRefreshListener != null && mOnRefreshListener.beforeRefresh())) {
                    setRefreshing(true, 0);
                    mOnRefreshListener.onRefresh();
                    return;
                }
            }
        }

        super.finishMoveChild(distance);
    }

    public PullRefreshLayout setHeaderState(int resId, String title) {
        if (mHeader != null) {
            mHeader.setStateDrawable(resId)
                    .setStateString(title);
        }
        return this;
    }

    public PullRefreshLayout setRefreshing(final boolean isRefresh, long show) {
        if (mRefreshRunnable != null) {
            removeCallbacks(mRefreshRunnable);
        }
        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (mRefreshing == isRefresh) {
                    return;
                }
                mRefreshing = isRefresh;
                if (mHeader != null) {
                    mHeader.setRefreshing(isRefresh);
                }
                if (isRefresh) {
                    startMoveChildTo(getRefreshScroll(), true);
                } else {
                    reset();
                }
            }
        };
        postDelayed(mRefreshRunnable, show);
        return this;
    }

    public PullRefreshLayout setFooterState(int resId, String title) {
        if (mFooter != null) {
            mFooter.setStateDrawable(resId)
                    .setStateString(title);
        }
        return this;
    }

    public PullRefreshLayout setLoading(final boolean isLoading, long show) {
        if (mLoadRunnable != null) {
            removeCallbacks(mLoadRunnable);
        }
        mLoadRunnable = new Runnable() {
            @Override
            public void run() {
                if (mLoading == isLoading) {
                    return;
                }
                mLoading = isLoading;
                if (mFooter != null) {
                    mFooter.setLoading(isLoading);
                }
                if (!isLoading) {
                    reset();
                }
            }
        };
        postDelayed(mLoadRunnable, show);
        return this;
    }

    public int getRefreshScroll() {
        return FlingHelper.getScrollByTarget(getTensionFactor(0, -1), targetScrollPullDown(), totalScrollPullDown());
    }

    public int getLoadScroll() {
        return FlingHelper.getTargetByScroll(getTensionFactor(0, 1), targetScrollPullUp(), totalScrollPullUp());
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }

    public interface OnLoadListener {

        boolean beforeLoad();/** 加载更多的监听，如果返回false则不会加载更多 */

        void onLoad();
    }

    public interface OnRefreshListener {

        boolean beforeRefresh(); /** 下拉刷新的监听，如果返回false则不会刷新 */

        void onRefresh();
    }

    public static class SimpleOnLoadListener implements OnLoadListener{

        @Override
        public boolean beforeLoad() {
            return false;
        }

        @Override
        public void onLoad() {

        }
    }

    public static class SimpleOnRefreshListener implements OnRefreshListener {

        @Override
        public boolean beforeRefresh() {
            return true;
        }

        @Override
        public void onRefresh() {

        }
    }

}