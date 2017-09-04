package com.polar.browser.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.polar.browser.R;

public class RangeBar extends View {

    private static final String TAG = "RangeBar";

    private RectF mBackgroundPaddingRect;
    private boolean mFirstDraw = true;
    private RangeBarAdapter mAdapter;
    private int[][] mAnchor;
    private boolean mModIsHorizontal = true;
    private int mCurrentX, mCurrentY, mPivotX, mPivotY;
    private boolean mSlide = false;

    private static final int[] STATE_NORMAL = new int[]{};
    private static final int[] STATE_SELECTED = new int[]{android.R.attr.state_selected};
    private static final int[] STATE_PRESS = new int[]{android.R.attr.state_pressed};
    private int[] mState = STATE_SELECTED;
    private int mCurrentItem;

    private int mAnchorWidth, mAnchorHeight;

    private int mPlaceHolderWidth, mPlaceHolderHeight;
    private int mTextMargin;
    private int mType;

    private Paint mTextPaint;
    private Paint mPaint;
    private int mTextSize;
    private int mTextColor;//文本颜色
    private int mRightColor;//右进度条颜色
    private int mLeftColor;//左进度条颜色

    private int mLastX;
    private int mSlideX, mSlideY;

    private int mAbsoluteY;

    private boolean mIsStartAnimation = false, mIsEndAnimation = false;
    private ValueAnimator mStartAnim, mEndAnim;
    private boolean mIsFirstSelect = true, mCanSelect = true;

    private RangeBarListener gbSlideBarListener;
    private RectF rect;//整个进度条
    private RectF leftRect;//左边的进度条
    private RectF rightRect;

    private float rightRectLeft = 0;//由边进度条左坐标
    private Drawable itemDefault;//图标
    private StateListDrawable stateListDrawable;


    public RangeBar(Context context) {
        super(context);
        init(null, 0);
    }

    public RangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RangeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attributeSet, int defStyleAttr) {
        mBackgroundPaddingRect = new RectF();
        TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.RangeBar, defStyleAttr, 0);
        mBackgroundPaddingRect.left = a.getDimension(R.styleable.RangeBar_gbs_paddingLeft, 0.0f);
        mBackgroundPaddingRect.top = a.getDimension(R.styleable.RangeBar_gbs_paddingTop, 0.0f);
        mBackgroundPaddingRect.right = a.getDimension(R.styleable.RangeBar_gbs_paddingRight, 0.0f);
        mBackgroundPaddingRect.bottom = a.getDimension(R.styleable.RangeBar_gbs_paddingBottom, 0.0f);

        mAnchorWidth = (int) a.getDimension(R.styleable.RangeBar_gbs_anchor_width, 50.0f);
        mAnchorHeight = (int) a.getDimension(R.styleable.RangeBar_gbs_anchor_height, 50.0f);

        mPlaceHolderWidth = (int) a.getDimension(R.styleable.RangeBar_gbs_placeholder_width, 20.0f);
        mPlaceHolderHeight = (int) a.getDimension(R.styleable.RangeBar_gbs_placeholder_height, 20.0f);

        mType = a.getInt(R.styleable.RangeBar_gbs_type, 1);

        mTextSize = a.getDimensionPixelSize(R.styleable.RangeBar_gbs_textSize, 28);
        mTextColor = a.getColor(R.styleable.RangeBar_gbs_textColor, Color.BLACK);

        mRightColor = a.getColor(R.styleable.RangeBar_gbs_rightColor, Color.BLACK);
        mLeftColor = a.getColor(R.styleable.RangeBar_gbs_leftColor, Color.BLACK);

        mTextMargin = (int) a.getDimension(R.styleable.RangeBar_gbs_text_margin, 0.0f);
        a.recycle();

        //文字画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        //背景画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * 控件长度
     */
    private void measure() {
        rect = new RectF((int) mBackgroundPaddingRect.left + mAnchorWidth,
                (int) mBackgroundPaddingRect.top,
                (int) (getWidth() - mBackgroundPaddingRect.right - mAnchorWidth),
                (int) (getHeight() - mBackgroundPaddingRect.bottom));

        mAbsoluteY = (int) (mBackgroundPaddingRect.top - mBackgroundPaddingRect.bottom);
        mCurrentX = mPivotX = getWidth() / 2;
        mCurrentY = mPivotY = getHeight() / 2;

        int widthBase = (int) ((rect.width()) / (getCount()-1));
        int widthHalf = widthBase / 2;
        int heightBase = (int) (rect.height() / (getCount()-1));
        int heightHalf = heightBase / 2;
        mAnchor = new int[getCount()][2];
        for (int i = 0, j = 1; i < getCount(); i++, j++) {
            if (i == 0) {
                mAnchor[i][0] = mModIsHorizontal ? (int) rect.left : mPivotX;
            } else if (i == getCount() - 1) {
                mAnchor[i][0] = mModIsHorizontal ? (int) rect.right : mPivotX;
            } else {
//                mAnchor[i][0] = mModIsHorizontal ? (int) (widthBase * j - widthHalf - rect.left) : mPivotX;
                mAnchor[i][0] = mModIsHorizontal ? (int) (widthBase * i + rect.left) : mPivotX;
            }
            mAnchor[i][1] = !mModIsHorizontal ? (int) (heightBase * j - heightHalf + rect.top) : mPivotY + mAbsoluteY / 2;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) return;
        if (mFirstDraw) measure();

        if (!mSlide) {
            int distance, minIndex = 0, minDistance = Integer.MAX_VALUE;
            for (int i = 0; i < getCount(); i++) {
                distance = Math.abs(mModIsHorizontal ? mAnchor[i][0] - mCurrentX : mAnchor[i][1] - mCurrentY);
                if (minDistance > distance) {
                    minIndex = i;
                    minDistance = distance;
                }
            }
            setCurrentItem(minIndex);
            stateListDrawable = mAdapter.getItem(minIndex);
        } else {
            mSlide = false;
            mCurrentX = mAnchor[mCurrentItem][0];
            mCurrentY = mAnchor[mCurrentItem][1];
            if (mFirstDraw) {
                mSlideX = mLastX = mCurrentX;
            }
            stateListDrawable = mAdapter.getItem(mCurrentItem);
            mIsFirstSelect = true;

        }
        stateListDrawable.setState(mState);
        itemDefault = stateListDrawable.getCurrent();
        //画文字
        for (int i = 0; i < getCount(); i++) {
            if (i == mCurrentItem) {
                mTextPaint.setColor(mAdapter.getTextColor(mCurrentItem));
                canvas.drawText(mAdapter.getText(i), mAnchor[i][0], mAnchor[i][1] + mAnchorHeight * 3 / 2 + mTextMargin, mTextPaint);
            }else {
                mTextPaint.setColor(mTextColor);
                canvas.drawText(mAdapter.getText(i), mAnchor[i][0], mAnchor[i][1] + mAnchorHeight * 3 / 2 + mTextMargin, mTextPaint);
            }
            stateListDrawable = mAdapter.getItem(i);
            stateListDrawable.setState(STATE_NORMAL);

        }
        itemDefault.setBounds(
                mSlideX - mAnchorWidth,
                mPivotY + mAbsoluteY / 2 - mAnchorHeight,
                mSlideX + mAnchorWidth,
                mPivotY + mAbsoluteY / 2 + mAnchorHeight
        );
        leftRect = new RectF((int) rect.left,
                (int) mBackgroundPaddingRect.top,
                mSlideX,
                (int) (getHeight() - mBackgroundPaddingRect.bottom));

        if(mSlideX-rect.left <= 0) {
            rightRectLeft = rect.left;
        }else {
            rightRectLeft = mSlideX;
        }

        rightRect = new RectF(rightRectLeft,
                (int) mBackgroundPaddingRect.top,
                rect.right,
                (int) (getHeight() - mBackgroundPaddingRect.bottom));
        //画右边进度条
        mPaint.setColor(mRightColor);
        canvas.drawRoundRect(rightRect, rect.height() / 2, rect.height() / 2, mPaint);
        //画左边进度条
        mPaint.setColor(mLeftColor);
        canvas.drawRoundRect(leftRect, rect.height() / 2, rect.height() / 2, mPaint);
        // 画刻度
        for (int i = 0; i < getCount(); i++) {
            canvas.drawRect(new RectF(mAnchor[i][0], (int) mBackgroundPaddingRect.top - 4, mAnchor[i][0] + 4, (int) (getHeight() - mBackgroundPaddingRect.bottom) + 4), mPaint);
        }
        //画图标
        itemDefault.draw(canvas);
        setFirstDraw(false);
    }


    private void endSlide() {
        if (mIsEndAnimation == false && mSlide) {
            mIsEndAnimation = true;
            mEndAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
            mEndAnim.setDuration(200);
            mEndAnim.setInterpolator(new LinearInterpolator());
            mEndAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSlideX = (int) ((mCurrentX - mLastX) * animation.getAnimatedFraction() + mLastX);
                    mSlideY = (int) (mCurrentY * animation.getAnimatedFraction());
                    invalidate();
                }
            });
            mEndAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsStartAnimation = false;
                    mLastX = mCurrentX;
                    mIsEndAnimation = false;
                    mCanSelect = true;
                    invalidate();
                }
            });
            mEndAnim.start();
        } else {
            mLastX = mCurrentX;
            mSlideX = mCurrentX;
            invalidate();
        }
    }

    private void startSlide() {
        if (mIsStartAnimation == false && !mSlide && mCanSelect) {

            mIsStartAnimation = true;
            mStartAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
            mStartAnim.setDuration(200);
            mStartAnim.setInterpolator(new LinearInterpolator());
            mStartAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSlideX = (int) ((mCurrentX - mLastX) * animation.getAnimatedFraction() + mLastX);
                    mSlideY = (int) (mCurrentY * animation.getAnimatedFraction());

                    invalidate();
                }
            });
            mStartAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    mLastX = mCurrentX;
                    mIsStartAnimation = false;
                    mCanSelect = true;
                    invalidate();
                }
            });
            mStartAnim.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mCanSelect) {
            int action = event.getAction();
            //获取当前坐标
            mCurrentX = mModIsHorizontal ? getNormalizedX(event) : mPivotX;
            mCurrentY = !mModIsHorizontal ? (int) event.getY() : mPivotY;

            mSlide = action == MotionEvent.ACTION_UP;

            if (!mSlide && mIsFirstSelect) {
                startSlide();
                mIsFirstSelect = false;

            } else if (mIsStartAnimation == false && mIsEndAnimation == false) {
                endSlide();
            }


            mState = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL ? STATE_SELECTED : STATE_PRESS;

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    invalidate();
                    return true;
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    mCanSelect = false;
                    invalidate();
                    return true;
            }
        }

        return super.onTouchEvent(event);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    private int getNormalizedX(MotionEvent event) {
        return Math.min(Math.max((int) event.getX(), mAnchorWidth), getWidth() - mAnchorWidth);
    }

    private void setFirstDraw(boolean firstDraw) {
        mFirstDraw = firstDraw;
    }

    private int getCount() {
        return isInEditMode() ? 3 : mAdapter.getCount();
    }

    private void setCurrentItem(int currentItem) {
        if (mCurrentItem != currentItem && gbSlideBarListener != null) {
            gbSlideBarListener.onPositionSelected(currentItem);
        }
        mCurrentItem = currentItem;
    }

    public void setAdapter(RangeBarAdapter adapter) {
        mAdapter = adapter;
    }

    public void setPosition(int position) {
        position = position < 0 ? 0 : position;
        position = position > mAdapter.getCount() ? mAdapter.getCount() - 1 : position;
        mCurrentItem = position;
        mSlide = true;
        invalidate();
    }

    public void setOnGbSlideBarListener(RangeBarListener listener) {
        this.gbSlideBarListener = listener;
    }

    public interface RangeBarListener {
        void onPositionSelected(int position);
    }

    public interface RangeBarAdapter {

        int getCount();
        String getText(int position);
        StateListDrawable getItem(int position);
        int getTextColor(int position);
    }

}
