package com.polar.browser.imagebrowse;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by duan on 17/3/24.
 */

public class RectImageView extends ImageView {
    public RectImageView(Context context) {
        super(context);
    }

    public RectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
