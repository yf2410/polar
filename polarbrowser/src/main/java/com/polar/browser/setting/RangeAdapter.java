package com.polar.browser.setting;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.polar.browser.view.RangeBar;


public class RangeAdapter implements RangeBar.RangeBarAdapter {
    protected StateListDrawable[] mItems;
    protected String[] content;
    protected int[] textColor;

    public RangeAdapter(Resources resources, int[] items) {
        int size = items.length;
        mItems = new StateListDrawable[size];
        Drawable drawable;
        for (int i = 0; i < size; i++) {
            drawable = resources.getDrawable(items[i]);
            if (drawable instanceof StateListDrawable) {
                mItems[i] = (StateListDrawable) drawable;
            } else {
                mItems[i] = new StateListDrawable();
                mItems[i].addState(new int[]{}, drawable);
            }
        }
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public String getText(int position) {
        return content[position];
    }

    @Override
    public StateListDrawable getItem(int position) {
        return mItems[position];
    }

    @Override
    public int getTextColor(int position) {
        return textColor[position];
    }

    public void setTextColor(int[] color) {
        textColor = color;
    }

    public void setContent(String[] content) {
        this.content = content;
    }
}
