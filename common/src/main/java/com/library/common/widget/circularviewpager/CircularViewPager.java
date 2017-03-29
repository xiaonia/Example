package com.library.common.widget.circularviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/3/27
 */

public class CircularViewPager extends ViewPager {

    private OnCircularPageChangeListeners mOnCircularPageChangeListeners = new OnCircularPageChangeListeners();

    public CircularViewPager(Context context) {
        super(context);
        init();
    }

    public CircularViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        super.addOnPageChangeListener(mOnCircularPageChangeListeners);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(new CircularPagerAdapter(this, adapter));
    }

    @Override
    public PagerAdapter getAdapter() {
        if (super.getAdapter() instanceof CircularPagerAdapter) {
            return ((CircularPagerAdapter) super.getAdapter()).getAdapter();
        }

        return super.getAdapter();
    }

    public CircularPagerAdapter getCircularAdapter() {
        if (super.getAdapter() instanceof CircularPagerAdapter) {
            return (CircularPagerAdapter) super.getAdapter();
        }

        return null;
    }

    @Override
    public void setCurrentItem(int item) {
        CircularPagerAdapter adapter = getCircularAdapter();
        if (adapter != null && adapter.isCircularEnable()) {
            super.setCurrentItem(item + 1);
            return;
        }

        super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        CircularPagerAdapter adapter = getCircularAdapter();
        if (adapter != null && adapter.isCircularEnable()) {
            super.setCurrentItem(item + 1, smoothScroll);
            return;
        }

        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnCircularPageChangeListeners.addOnPageChangeListener(listener);
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        mOnCircularPageChangeListeners.addOnPageChangeListener(listener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        mOnCircularPageChangeListeners.removeOnPageChangeListener(listener);
    }

    public void clearOnPageChangeListeners() {
        mOnCircularPageChangeListeners.clearOnPageChangeListeners();
    }

    class OnCircularPageChangeListeners implements OnPageChangeListener {

        private List<OnPageChangeListener> mOnPageChangeListeners;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            CircularPagerAdapter adapter = getCircularAdapter();
            if (adapter != null && adapter.isCircularEnable()) {
                if (position == 0) {
                    position = adapter.getItemCount() - 1;
                    if (positionOffset == 0) {
                        setCurrentItem(adapter.getCount() - 2 - 1, false);
                    }
                } else if (position == adapter.getCount() - 1) {
                    position = 0;
                    if (positionOffset == 0) {
                        setCurrentItem(1 - 1, false);
                    }
                } else {
                    position -= 1;
                }
            }
            dispatchOnPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            CircularPagerAdapter adapter = getCircularAdapter();
            if (adapter != null && adapter.isCircularEnable()) {
                if (position == 0) {
                    position = adapter.getItemCount() - 1;
                    //setCurrentItem(adapter.getCount() - 2 - 1, false);
                } else if (position == adapter.getCount() - 1) {
                    position = 0;
                    //setCurrentItem(1 - 1, false);
                } else {
                    position -= 1;
                }
            }

            dispatchOnPageSelected(position - 1);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            dispatchOnScrollStateChanged(state);
        }

        public void addOnPageChangeListener(OnPageChangeListener listener) {
            if (mOnPageChangeListeners == null) {
                mOnPageChangeListeners = new ArrayList<>();
            }
            mOnPageChangeListeners.add(listener);
        }

        public void removeOnPageChangeListener(OnPageChangeListener listener) {
            if (mOnPageChangeListeners != null) {
                mOnPageChangeListeners.remove(listener);
            }
        }

        public void clearOnPageChangeListeners() {
            if (mOnPageChangeListeners != null) {
                mOnPageChangeListeners.clear();
            }
        }

        private void dispatchOnPageScrolled(int position, float offset, int offsetPixels) {
            if (mOnPageChangeListeners != null) {
                for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrolled(position, offset, offsetPixels);
                    }
                }
            }
        }

        private void dispatchOnPageSelected(int position) {
            if (mOnPageChangeListeners != null) {
                for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageSelected(position);
                    }
                }
            }
        }

        private void dispatchOnScrollStateChanged(int state) {
            if (mOnPageChangeListeners != null) {
                for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrollStateChanged(state);
                    }
                }
            }
        }
    }
}
