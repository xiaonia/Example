package com.library.common.widget.flipview;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by xuqingqi on 2017/3/6.
 */

public class FlipTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position > 0 && position <= 1.f) {
            page.setTranslationX(-page.getWidth() * position);
        } else {
            page.setTranslationX(0.f);
        }
    }

}
