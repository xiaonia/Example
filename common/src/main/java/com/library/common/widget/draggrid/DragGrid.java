package com.library.common.widget.draggrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

public class DragGrid extends GridView implements AdapterView.OnItemLongClickListener{

    private static final String TAG = DragGrid.class.getSimpleName();
    private static final double DRAG_SCALE = 1.2d;

    //手指按下的位置
    private int downX;
    private int downY;
    private int windowX;
    private int windowY;
    //手指按下的位置距离dragView左边和上边的距离
    private int distanceX;
    private int distanceY;
    //拖动的View的位置
    public int dragPosition;
    //放下的View的位置
    private int dropPosition;

    private Vibrator mVibrator;
    private View dragView = null;
    private ImageView dragImageView = null;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams windowParams = null;

    //是否正在拖动
    private boolean isDrag = false;
    //是否正在变换位置
    private boolean isMoving = false;

    private Rect mDragFrame;

    public DragGrid(Context context) {
        super(context);
        init(context);
    }

    public DragGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DragGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        super.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) ev.getX();
            downY = (int) ev.getY();
            windowX = (int) ev.getRawX();
            windowY = (int) ev.getRawY();
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDrag) {
                    onDrag(x, y ,(int) ev.getRawX() , (int) ev.getRawY());
                    OnMove(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDrag) {
                    stopDrag();
                    isDrag = false;
                    onDrop(x, y);
                }
                requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onTouchEvent(ev);
    }

    private void startDrag(Bitmap dragBitmap, int x, int y) {
        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowParams.x = x - distanceX;
        windowParams.y = y  - distanceY;
        windowParams.width = (int) (DRAG_SCALE * dragBitmap.getWidth());
        windowParams.height = (int) (DRAG_SCALE * dragBitmap.getHeight());
        this.windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        this.windowParams.format = PixelFormat.TRANSLUCENT;
        this.windowParams.windowAnimations = 0;
        dragImageView = new ImageView(getContext());
        dragImageView.setImageBitmap(dragBitmap);
        windowManager.addView(dragImageView, windowParams);
    }

    private void onDrag(int x, int y , int rawx , int rawy) {
        if (dragImageView != null) {
            windowParams.alpha = 0.6f;
            windowParams.x = rawx - distanceX;
            windowParams.y = rawy - distanceY;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
    }

    private void stopDrag() {
        if (dragImageView != null) {
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }

    private void onDrop(int x, int y) {
        if (!isMoving) {
            DragAdapter adapter = (DragAdapter) getAdapter();
            adapter.setDragPosition(INVALID_POSITION);
            adapter.notifyDataSetChanged();
            /*if (dragView != null) {
                dragView.setVisibility(VISIBLE);
                dragView = null;
            }*/
        }
    }

    private Animation getMoveAnimation(float toXValue, float toYValue) {
        TranslateAnimation mTranslateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0F,
                Animation.RELATIVE_TO_SELF,toXValue,
                Animation.RELATIVE_TO_SELF, 0.0F,
                Animation.RELATIVE_TO_SELF, toYValue);
        mTranslateAnimation.setFillAfter(true);
        mTranslateAnimation.setDuration(300L);
        return mTranslateAnimation;
    }

    private void OnMove(int x, int y) {
        if (isMoving) {
            return;
        }

        DragAdapter adapter = (DragAdapter) getAdapter();
        int dPosition = pointToRecentPosition(x, y);
        if (dPosition == dragPosition
                || !adapter.isPositionValid(dPosition)) {

            return;
        }

        dropPosition = dPosition;
        View fromView;
        View toView;
        float to_x;
        float to_y;
        Animation moveAnimation;

        int direction = dropPosition > dragPosition? +1 : -1;
        int to = dragPosition;
        int from = dragPosition + direction;
        while (to != dropPosition) {
            fromView = getChildAt(from - getFirstVisiblePosition());
            if (fromView == null) {
                continue;
            }
            toView = getChildAt(to - getFirstVisiblePosition());
            if (toView == null) {
                continue;
            }
            to_x = 1.f * (toView.getLeft() - fromView.getLeft()) / fromView.getWidth();
            to_y = 1.f * (toView.getTop() - fromView.getTop()) / fromView.getWidth();
            moveAnimation = getMoveAnimation(to_x, to_y);
            if (from == dropPosition) {
                moveAnimation.setAnimationListener(animationListener);
            }
            fromView.startAnimation(moveAnimation);

            to = from;
            from += direction;
        }
    }

    public int pointToRecentPosition(int x, int y) {
        Rect frame = mDragFrame;
        if (frame == null) {
            mDragFrame = new Rect();
            frame = mDragFrame;
        }

        int minDistanceY = -1;
        int distanceY = -1;
        int position = INVALID_POSITION;
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (x >= frame.left && x <= frame.right) {
                    distanceY = Math.min(Math.abs(y - frame.top), Math.abs(y - frame.bottom));
                    if (minDistanceY == -1 || minDistanceY > distanceY) {
                        minDistanceY = distanceY;
                        position = getFirstVisiblePosition() + i;
                    }
                }
            }
        }

        return position;
    }

    /**
     * 动画监听器
     */
    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isMoving = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // 在动画完成时将adapter里的数据交换位置
            DragAdapter adapter = (DragAdapter) getAdapter();
            if (isDrag) {
                adapter.setDragPosition(dropPosition);
            } else {
                adapter.setDragPosition(INVALID_POSITION);
            }
            /*if (dragView != null) {
                dragView.setVisibility(isDrag? INVISIBLE : VISIBLE);
                dragView = null;
            }*/
            adapter.exchange(dragPosition, dropPosition);
            dragPosition = dropPosition;
            isMoving = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (isDrag || isMoving) {
            return false;
        }

        DragAdapter adapter = (DragAdapter) getAdapter();
        if (position >= adapter.getItemCount()) {
            return false;
        }

        dragView = view;
        distanceX = downX - dragView.getLeft();
        distanceY = downY - dragView.getTop();
        dragPosition = position;
        adapter.setDragPosition(dragPosition);
        dragView.destroyDrawingCache();
        dragView.setDrawingCacheEnabled(true);
        startDrag(Bitmap.createBitmap(dragView.getDrawingCache()), windowX,  windowY);
        dragView.setVisibility(View.INVISIBLE);
        mVibrator.vibrate(50);
        isDrag = true;
        isMoving = false;
        requestDisallowInterceptTouchEvent(true);
        return true;
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        throw new IllegalArgumentException("Can not set OnItemLongClickListener to DragGrid");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof DragAdapter) {
            super.setAdapter(adapter);
            return;
        }

        throw new IllegalArgumentException("The adapter must extend DragAdapter");
    }

}