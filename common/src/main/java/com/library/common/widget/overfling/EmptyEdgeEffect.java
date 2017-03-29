package com.library.common.widget.overfling;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.EdgeEffect;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/3/27
 */

public class EmptyEdgeEffect extends EdgeEffect {
    /**
     * Construct a new EdgeEffect with a theme appropriate for the provided context.
     *
     * @param context Context used to provide theming and resource information for the EdgeEffect
     */
    public EmptyEdgeEffect(Context context) {
        super(context);
    }

    @Override
    public boolean draw(Canvas canvas) {
        //return super.draw(canvas);
        return false;
    }

}
