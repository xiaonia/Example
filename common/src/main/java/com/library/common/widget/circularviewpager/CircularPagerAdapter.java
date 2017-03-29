package com.library.common.widget.circularviewpager;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/3/27
 */

public class CircularPagerAdapter extends PagerAdapter {

    public static final int CACHE_COUNT = 2;
    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private boolean mCircularEnable = false;

    public CircularPagerAdapter (ViewPager viewPager, PagerAdapter adapter) {
        if (viewPager == null || adapter == null) {
            throw new IllegalArgumentException("ViewPager or PagerAdapter is NULL !");
        }
        this.mViewPager = viewPager;
        this.mAdapter = adapter;
    }

    public PagerAdapter getAdapter() {
        return this.mAdapter;
    }

    @Override
    public int getCount() {
        if (mAdapter.getCount() > 1) {
            mCircularEnable = true;
            return getItemCount() + CACHE_COUNT;
        }

        mCircularEnable = false;
        return getItemCount();
    }

    public int getItemCount() {
        return mAdapter.getCount();
    }

    public boolean isCircularEnable() {
        return this.mCircularEnable;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (isCircularEnable()) {
            if (position == 0) {
                position = getItemCount() - 1;
            } else if (position == getCount() - 1) {
                position = 0;
            } else {
                position -= 1;
            }
        }

        return this.mAdapter.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (isCircularEnable()) {
            if (position == 0) {
                position = getItemCount() - 1;
            } else if (position == getCount() - 1) {
                position = 0;
            } else {
                position -= 1;
            }
        }

        this.mAdapter.destroyItem(container, position, object);
    }

}
