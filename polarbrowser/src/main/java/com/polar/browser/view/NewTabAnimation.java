package com.polar.browser.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.polar.browser.R;
import com.polar.browser.utils.DensityUtil;

/**
 * Created by lzk-pc on 2017/3/29.
 */

public class NewTabAnimation extends View {
    private static final String TAG = NewTabAnimation.class.getSimpleName();

    private Paint mPaint;
    private Paint textPaint;

    private int backCircleCor = Color.rgb(221,221,221); //背景圆的颜色
    private int foreCircleCor = Color.rgb(13,126,246); //前景圆的颜色

    private int totalWidth;  //控件总宽带
    private int totalHeight;
    private float plusRadius;  //加号的一半宽度
    private float padding;


    private int centerX;   //中心点x
    private int centerY;  //中心点y
    private float circleRadius; //圆圈半径

    //动画
    private boolean animationRunning = false;
    private int currentProgress = 0;  //当前加载进度
    private int cancelProgress; //当取消创建时，保存加载进度，用于取消动画
    private int mLoadingTime = 1500;  //加载动画时间

    private ValueAnimator valueAnimator;  //加载动画
    private ValueAnimator cancelAnimator;  //取消动画，用户在加载完成之前抬起手指

    private OnLoadListener mLoadAnimListener;

    public NewTabAnimation(Context context) {
        super(context);
        init(context,null);
    }

    public NewTabAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){

        for(int i =0 ;i < attrs.getAttributeCount();i++) {
            if ("layout_height".equals(attrs.getAttributeName(i))) {
                totalWidth = DensityUtil.getPxByAttributeValue(context,attrs.getAttributeValue(i));
            } else if ("layout_width".equals(attrs.getAttributeName(i))) {
                totalHeight = DensityUtil.getPxByAttributeValue(context,attrs.getAttributeValue(i));
            } else if("padding".equals(attrs.getAttributeName(i))){
                padding = DensityUtil.getPxByAttributeValue(context,attrs.getAttributeValue(i));
            }
        }

        int centerCircleDiameter = Math.max(totalWidth,totalHeight);
        if(centerCircleDiameter <= 0){
            centerCircleDiameter = DensityUtil.dip2px(getContext(),60);
        }

        float plusLineWidth = getResources().getDimension(R.dimen.new_tab_plus_width);
        plusRadius = (plusLineWidth/2.0f);  //加号的一半

        centerX = centerY = centerCircleDiameter/2;  //NOTE:控件的实际宽度增加了2*padding，可见宽度为centerCircleDiameter
        circleRadius = (int)(centerCircleDiameter - 2*padding)/2;

        initPaint();
        initValueAnimation();

        invalidate();
        startLoadingAnimation();  //开启加载动画
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);  //填充，否则drawCircle绘制的是圆环

        float plusLineWid = getResources().getDimension(R.dimen.new_tab_plus_line_width);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(plusLineWid);  //线宽
        textPaint.setColor(Color.WHITE);
    }

    public void setOnLoadAnimationListener(NewTabAnimation.OnLoadListener loadListener){
        this.mLoadAnimListener = loadListener;
    }

    /**
     * 加载动画
     */
    private void initValueAnimation(){
        valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setTarget(this);
        valueAnimator.setDuration(mLoadingTime);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentProgress = (Integer) animation.getAnimatedValue();
                invalidate();
                if(currentProgress >= 100 && animationRunning){
                    loadFinishAnimation();
                }
            }
        });
    }

    /**
     * 加载动画完成后，开启消失动画
     */
    private void loadFinishAnimation()
    {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.5f,1f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.5f,1f);
        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.8f,1.5f);
        ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.8f,1.5f);
        ObjectAnimator objectAnimator5 = ObjectAnimator.ofFloat(this, "alpha", 1f, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.setDuration(200);
        animatorSet2.playTogether(objectAnimator3,objectAnimator4,objectAnimator5);
        animatorSet.play(objectAnimator1).with(objectAnimator2).before(animatorSet2);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mLoadAnimListener != null){
                    mLoadAnimListener.onLoadFinish();  //加载完成
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    /**
     * 加载动画取消后，开启取消动画
     */
    private void loadCancelAnimation(){

        cancelAnimator = ValueAnimator.ofInt(cancelProgress, 0);
        valueAnimator.setTarget(this);
        cancelAnimator.setDuration(300);
        cancelAnimator.setInterpolator(new AccelerateInterpolator());
        cancelAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                cancelProgress = (Integer) animation.getAnimatedValue();
                invalidate();
                if(cancelProgress <= 0){
                    if(mLoadAnimListener != null){
                        mLoadAnimListener.onLoadCancel();  //加载取消
                    }
                }
            }
        });
        cancelAnimator.start();
    }

    public void startLoadingAnimation()
    {
        if (!animationRunning)
        {
            valueAnimator.start();
            animationRunning = true;
        }
    }

    public void stopLoadingAnimation()
    {
        if (animationRunning)
        {
            cancelProgress = currentProgress;
            animationRunning = false;
            valueAnimator.end();
            loadCancelAnimation();
        }
    }

    /**
     * 用户抬起手指，若进度条未加载完成，则取消新建动作
     */
    public void onActionUpCheckProgress(){
        if(currentProgress<100){
            stopLoadingAnimation();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景圆&加号
        mPaint.setColor(backCircleCor);
        canvas.drawCircle(centerX,centerY,circleRadius,mPaint);
        //加号
//        canvas.drawLine(centerX-plusRadius,centerY,centerX+plusRadius,centerY,textPaint); //横线
//        canvas.drawLine(centerX,centerY-plusRadius,centerX,centerY+plusRadius,textPaint); //竖线

        int progress = 0;
        float percentPlusRadius = 0;
        if(animationRunning){
            progress = currentProgress;
            percentPlusRadius = plusRadius * Math.max(progress,20) / 100;   //前景圆在20%开始扩大
        }else{
            progress = cancelProgress;
            percentPlusRadius = plusRadius * progress / 100;
        }

        //绘制前景圆&加号
        mPaint.setColor(foreCircleCor);
        canvas.drawCircle(centerX,centerY,circleRadius * progress / 100, mPaint);
        //加号
        canvas.drawLine(centerX-percentPlusRadius,centerY,centerX+percentPlusRadius,centerY,textPaint); //横线
        canvas.drawLine(centerX,centerY-percentPlusRadius,centerX,centerY+percentPlusRadius,textPaint); //竖线
    }

    public interface OnLoadListener{
        void onLoadFinish();
        void onLoadCancel();
    }

}
