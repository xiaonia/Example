package com.library.common.widget.pullview;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class FlingLayout extends FrameLayout {

    private static final String TAG = FlingLayout.class.getSimpleName();

    private static final int DEFAULT_TARGET_SCROLL_PULL_UP = 64;
    private static final int DEFAULT_TARGET_SCROLL_PULL_DOWN = 64;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int SCROLL_DURATION = 200;
    private static final int INVALID_POINTER = -1;
    protected static final float DRAG_RATE = 0.5f;
    private static final float TENSION_FACTOR = 2.0f;//over scroll 0.5

    protected View mTarget;
    protected int mStartDistance = 0;//默认的位置
    protected int mTotalScrollY = 0;//总共滚动的距离
    protected int mMoveDistance = 0;//单次滑动的距离

    protected int mTouchSlop;
    protected float mDensity = 1;
    private Scroller mScroller;

    private boolean mPullUpEnabled = true;//是否可以下拉
    private boolean mPullDownEnabled = true;//是否可以上拉
    protected OnScrollListener mOnScrollListener;

    protected boolean mInterceptEnabled = true;//是否允许拦截TouchEvent
    private float mInitialMotionY;
    private float mInitialDownY;
    private int mActivePointerId = INVALID_POINTER;

    protected boolean mIsBeingDragged;//是否处于滚动状态

    public FlingLayout(Context context) {
        this(context, null);
    }

    public FlingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context, new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR));
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                postInvalidate();
            }
        }
        super.computeScroll();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mInterceptEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (!isEnabled()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                stopScroll();
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                mMoveDistance = 0;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;

        if (!isEnabled()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                mMoveDistance = 0;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final int distance = (int) ((y - mInitialMotionY) * DRAG_RATE);
                    if (canPullDown() && distance > 0) {//下拉
                        startMoveChildBy(-(distance - mMoveDistance));
                        mMoveDistance = distance;
                    }
                    else if (canPullUp() && distance < 0) {//上拉
                        startMoveChildBy(-(distance - mMoveDistance));
                        mMoveDistance = distance;
                    } else {
                        return false;
                    }
                }

                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(TAG,
                            "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final int distance = (int) ((y - mInitialMotionY) * DRAG_RATE);
                    mIsBeingDragged = false;
                    finishMoveChild(mTotalScrollY -(distance - mMoveDistance));
                    mMoveDistance = distance;
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (!mIsBeingDragged) {
            if (yDiff > mTouchSlop && canPullDown()) {//下拉
                mInitialMotionY = mInitialDownY + mTouchSlop;
                mIsBeingDragged = true;
            }
            else if (yDiff < -mTouchSlop && canPullUp()) {//上拉
                mInitialMotionY = mInitialDownY - mTouchSlop;
                mIsBeingDragged = true;
            }
        }
    }

    private boolean stopScroll() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            return true;
        }
        return false;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    protected boolean canPullUp() {
        if (mTarget != null) {
            return mPullUpEnabled && !FlingHelper.canChildPullUp(mTarget);
        }
        return mPullUpEnabled;
    }

    protected boolean canPullDown() {
        if (mTarget != null) {
            return mPullDownEnabled && !FlingHelper.canChildPullDown(mTarget);
        }
        return mPullDownEnabled;
    }

    protected void dispatchScroll(int y) {

    }

    public int startScrollBy(int startY, int dy) {
        int duration = Math.abs(dy);
        int time = duration > SCROLL_DURATION ? SCROLL_DURATION : duration;
        mScroller.startScroll(0, startY, 0, dy, time);
        invalidate();
        return time;
    }

    public int startScrollTo(int startY, int endY) {
        return startScrollBy(startY, endY - startY);
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        onAddView(child, index, params);
        super.addView(child, index, params);
    }

    /**
     *  called before {@link #addView(View child, int index, ViewGroup.LayoutParams params)}
     */
    protected void onAddView(View child, int index, ViewGroup.LayoutParams params) {
        if (mTarget == null) {
            mTarget = child;
        } else {
            throw new IllegalArgumentException("can only hold one content view");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void scrollTo(int x, int y) {
        //int targetY = y > 0? targetScrollPullUp() : targetScrollPullDown();
        int totalY = y > 0? totalScrollPullUp() : totalScrollPullDown();
        int scrollY = FlingHelper.getTargetByScroll(getTensionFactor(x, y), y, totalY);
        super.scrollTo(x, scrollY);

        this.mTotalScrollY = y;
        dispatchScroll(getScrollY());
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, getScrollY());
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        stopScroll();
        scrollTo(getScrollX() + x, mTotalScrollY + y);
    }

    public void setOnScrollListener(OnScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }

    /** 是否可以下拉*/
    public void setPullDownEnabled(boolean pullDownEnabled) {
        this.mPullDownEnabled = pullDownEnabled;
        if (!pullDownEnabled && mTotalScrollY < 0) {
            moveChildTo(0, false);
        }
    }

    /** 是否可以下拉*/
   public void setPullUpEnabled(boolean pullUpEnabled) {
        this.mPullUpEnabled = pullUpEnabled;
        if (!pullUpEnabled && mTotalScrollY > 0) {
            moveChildTo(0, false);
        }
    }

    /** 下拉的最大距离*/
    protected int totalScrollPullDown() {
        return -dp2px(DEFAULT_TARGET_SCROLL_PULL_DOWN);
    }

    /** 上拉的最大距离*/
    protected int totalScrollPullUp() {
        return dp2px(DEFAULT_TARGET_SCROLL_PULL_UP);
    }

    /** 下拉刷新的距离*/
    protected int targetScrollPullDown() {
        return -dp2px(DEFAULT_TARGET_SCROLL_PULL_DOWN);
    }

    /** 上拉加载的距离 */
    protected int targetScrollPullUp() {
        return dp2px(DEFAULT_TARGET_SCROLL_PULL_UP);
    }

    protected int dp2px(int value) {
        return (int) (mDensity * value + 0.5);
    }

    protected void startMoveChildTo(int distance) {
        startMoveChildTo(distance, false);
    }

    protected void startMoveChildTo(int distance, boolean anim) {
        moveChildTo(distance, anim);
    }

    protected void startMoveChildBy(int move) {
        startMoveChildBy(move, false);
    }

    protected void startMoveChildBy(int move, boolean anim) {
        moveChildBy(move, anim);
    }

    protected void finishMoveChild(int distance) {
        moveToStart();
    }

    protected void reset() {
        moveChildTo(mStartDistance, false);
    }

    protected void moveToStart() {
        moveChildTo(mStartDistance, true);
    }

    protected void moveChildTo(int target, boolean anim) {
        stopScroll();

        if (anim) {
            startScrollTo(mTotalScrollY, target);
            return;
        }
        scrollTo(0, target);
    }

    protected void moveChildBy(int move, boolean anim) {
        stopScroll();
        int end = mTotalScrollY + move;

        if (anim) {
            startScrollTo(mTotalScrollY, end);
            return;
        }
        scrollTo(0, end);
    }

    /**
     *  当上拉或者下拉的距离超过目标距离之后的弹簧系数
     *  最终拉伸长度等于{ 1 + getTensionFactor() * 0.25 }
     *  默认拉伸距离为 1.5倍
     */
    protected float getTensionFactor(int x, int y) {
        return TENSION_FACTOR;
    }

    public interface OnScrollListener<T extends FlingLayout> {

        void onScroll(T flingLayout, int y);

    }

}