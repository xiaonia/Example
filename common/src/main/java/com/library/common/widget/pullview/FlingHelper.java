package com.library.common.widget.pullview;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.AbsListView;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/4/20
 */

public class FlingHelper {

    private static final String TAG = FlingHelper.class.getSimpleName();

    public static int getTargetByScroll(float tensionFactor, float scroll, int totalDistance) {
        if (tensionFactor < 1.0f) {
            tensionFactor = 1.0f;
        }
        float dragPercent = Math.min(Math.abs(scroll / totalDistance), tensionFactor);
        float tensionPercent = (float) ((-Math.pow((dragPercent / tensionFactor), 2) + 2 * dragPercent / tensionFactor));
        return (int) (totalDistance * tensionPercent);
    }

    public static  int getScrollByTarget(float tensionFactor, float target, int totalDistance) {
        float tensionPercent = Math.min(1.0f, Math.abs(1.f * target / totalDistance));
        if (tensionFactor < 1.0f) {
            tensionFactor = 1.0f;
        }
        float dragPercent = (float) ((1.f - Math.sqrt(1.f - tensionPercent)) * tensionFactor);
        return (int) (totalDistance * dragPercent + 0.5);
    }

    public static  int calculateScrollDistance(float tensionFactor, float scroll, int totalDistance, int targetDistance) {
        float originalDragPercent = scroll / totalDistance;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        float extraOS = Math.abs(scroll) - Math.abs(totalDistance);
        float slingshotDist = Math.abs(targetDistance);
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2)
                / slingshotDist);
        /** tensionSlingshotPercent 0.0 - 2.0 */
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;
        /** tensionPercent 0 - 0.25 */
        float extraMove = (targetDistance) * tensionPercent * tensionFactor;

        return (int) ((targetDistance * dragPercent) + extraMove);
    }

    /**
     * 是否可以下拉
     */
    public static  boolean canChildPullDown(View target) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(target, -1) || target.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, -1);
        }
    }

    /**
     *  是否可以上拉
     */
    public static boolean canChildPullUp(View target) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                if (absListView.getChildCount() > 0) {
                    if (absListView.getLastVisiblePosition() < absListView.getCount() - 1) {
                        return true;
                    }
                    View lastChild = absListView.getChildAt(
                            absListView.getLastVisiblePosition() - absListView.getFirstVisiblePosition());
                    if (lastChild.getBottom() > absListView.getHeight() - absListView.getPaddingBottom()) {
                        return true;
                    }
                }
                return false;
            } else {
                return ViewCompat.canScrollVertically(target, 1) || target.getScrollY() < 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, 1);
        }
    }

}
