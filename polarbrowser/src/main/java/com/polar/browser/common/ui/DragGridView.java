package com.polar.browser.common.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.polar.browser.R;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.homepage.customlogo.DragAdapter;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;

import java.util.LinkedList;
import java.util.List;

public class DragGridView extends GridView {
    /**
     * 一行的ITEM数量
     */
    private static final int COLUMNS = 5;
    private static final String TAG = "DragGridView";
    /**
     * 点击时候的X位置
     */
    public int downX;
    /**
     * 点击时候的Y位置
     */
    public int downY;
    /**
     * 点击时候对应整个界面的X位置
     */
    public int windowX;
    /**
     * 点击时候对应整个界面的Y位置
     */
    public int windowY;
    /**
     * 长按时候对应postion
     */
    public int dragPosition;
    /**
     * 拖动的里x的距离
     */
    int dragOffsetX;
    /**
     * 拖动的里Y的距离
     */
    int dragOffsetY;
    /**
     * 屏幕上的X
     */
    private int win_view_x;
    /**
     * 屏幕上的Y
     */
    private int win_view_y;
    /**
     * Up后对应的ITEM的Position
     */
    private int dropPosition;
    /**
     * 开始拖动的ITEM的Position
     */
    private int startPosition;
    /**
     * item高
     */
    private int itemHeight;
    /**
     * item宽
     */
    private int itemWidth;
    /**
     * 拖动的时候对应ITEM的VIEW
     */
    private View dragImageView = null;
    /**
     * WindowManager管理器
     */
    private WindowManager windowManager = null;
    /** */
    private WindowManager.LayoutParams windowParams = null;
    /**
     * item总量
     */
    private int itemTotalCount;
    /**
     * 是否在移动
     */
    private boolean isMoving = false;
    /** */
    private int holdPosition;
    /**
     * 拖动的时候放大的倍数
     */
    private double dragScale = 1.5D;
    /**
     * 每个ITEM之间的水平间距
     */
    private int mHorizontalSpacing = 15;
    /**
     * 每个ITEM之间的竖直间距
     */
    private int mVerticalSpacing = 15;
    /* 移动时候最后个动画的ID */
    private String LastAnimationID;

    public DragGridView(Context context) {
        super(context);
        init(context);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        //将布局文件中设置的间距dip转为px
        mHorizontalSpacing = DensityUtil.dip2px(context, mHorizontalSpacing);
        mVerticalSpacing = DensityUtil.dip2px(context, mVerticalSpacing);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        SimpleLog.d(TAG, "onInterceptTouchEvent");
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) ev.getX();
            downY = (int) ev.getY();
            windowX = (int) ev.getX();
            windowY = (int) ev.getY();
            setOnItemClickListener(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        SimpleLog.d(TAG, "onTouchEvent");
        if (dragImageView != null && dragPosition != AdapterView.INVALID_POSITION) {
            // 移动时候的对应x,y位置
            super.onTouchEvent(ev);
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) ev.getX();
                    windowX = (int) ev.getX();
                    downY = (int) ev.getY();
                    windowY = (int) ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    onDrag(x, y, (int) ev.getRawX(), (int) ev.getRawY());
                    if (!isMoving) {
                        onMove(x, y);
                    }
                    if (pointToPosition(x, y) != AdapterView.INVALID_POSITION) {
                        break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    stopDrag();
                    onDrop(x, y);
                    requestDisallowInterceptTouchEvent(false);
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        SimpleLog.d(TAG, "dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 在拖动的情况
     */
    private void onDrag(int x, int y, int rawx, int rawy) {
        if (dragImageView != null) {
            windowParams.alpha = 0.6f;
//			windowParams.x = rawx - itemWidth / 2;
//			windowParams.y = rawy - itemHeight / 2;
            windowParams.x = rawx - win_view_x;
            windowParams.y = rawy - win_view_y;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
    }

    /**
     * 在松手下放的情况
     */
    private void onDrop(int x, int y) {
        // 根据拖动到的x,y坐标获取拖动位置下方的ITEM对应的POSTION
        int tempPostion = pointToPosition(x, y);
        dropPosition = tempPostion;
        DragAdapter mDragAdapter = (DragAdapter) getAdapter();
        //显示刚拖动的ITEM
        mDragAdapter.setShowDropItem(true);
        mDragAdapter.setDrop();
        //刷新适配器，让对应的ITEM显示
        mDragAdapter.notifyDataSetChanged();
    }

    /**
     * 长按点击监听
     *
     * @param ev
     */
    public void setOnItemClickListener(final MotionEvent ev) {
        setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                int x = (int) ev.getX();// 长安事件的X位置
                int y = (int) ev.getY();// 长安事件的y位置
                itemTotalCount = DragGridView.this.getCount();
                startPosition = position;// 第一次点击的postion
                dragPosition = position;
                DragAdapter mDragAdapter = (DragAdapter) getAdapter();
                if (mDragAdapter.getItem(startPosition).getSiteName().equals(getResources().getString(R.string.url_add)) || dragPosition - getFirstVisiblePosition() < 0) {
                    return false;
                }
                ViewGroup dragViewGroup = (ViewGroup) getChildAt(dragPosition - getFirstVisiblePosition());
                if (dragViewGroup == null) {
                    return false;
                }
                ImageView dragTextView = (ImageView) dragViewGroup.findViewById(R.id.site_pic);
                dragTextView.setSelected(true);
                dragTextView.setEnabled(false);
                itemHeight = dragViewGroup.getHeight();
                itemWidth = dragViewGroup.getWidth();
                // 如果特殊的这个不等于拖动的那个,并且不等于-1
                if (dragPosition != AdapterView.INVALID_POSITION) {
                    // 释放的资源使用的绘图缓存。如果你调用buildDrawingCache()手动没有调用setDrawingCacheEnabled(真正的),你应该清理缓存使用这种方法。
                    win_view_x = windowX - dragViewGroup.getLeft();//VIEW相对自己的X，半斤
                    win_view_y = windowY - dragViewGroup.getTop();//VIEW相对自己的y，半斤
                    dragOffsetX = (int) (ev.getRawX() - x);//手指在屏幕的上X位置-手指在控件中的位置就是距离最左边的距离
                    dragOffsetY = (int) (ev.getRawY() - y);//手指在屏幕的上y位置-手指在控件中的位置就是距离最上边的距离
                    dragViewGroup.destroyDrawingCache();
                    dragViewGroup.setDrawingCacheEnabled(true);
                    Bitmap dragBitmap = Bitmap.createBitmap(dragViewGroup.getDrawingCache());
                    startDrag(dragBitmap, (int) ev.getRawX(), (int) ev.getRawY());
                    hideDropItem();
                    dragViewGroup.setVisibility(View.INVISIBLE);
                    isMoving = false;
                    requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }
        });
    }


    public void startDrag(Bitmap dragBitmap, int x, int y) {
        stopDrag();
        windowParams = new WindowManager.LayoutParams();// 获取WINDOW界面的
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        //得到preview左上角相对于屏幕的坐标
        windowParams.x = x - win_view_x;
        windowParams.y = y - win_view_y;
        //设置拖拽item的宽和高
        windowParams.width = (int) (dragScale * dragBitmap.getWidth());// 放大dragScale倍，可以设置拖动后的倍数
        windowParams.height = (int) (dragScale * dragBitmap.getHeight());// 放大dragScale倍，可以设置拖动后的倍数
        this.windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        this.windowParams.format = PixelFormat.TRANSLUCENT;
        this.windowParams.windowAnimations = 0;
        ImageView iv = new ImageView(getContext());
        iv.setImageBitmap(dragBitmap);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);// "window"
        windowManager.addView(iv, windowParams);
        dragImageView = iv;
    }

    /**
     * 停止拖动 ，释放并初始化
     */
    private void stopDrag() {
        if (dragImageView != null) {
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }
    /** 在ScrollView内，所以要进行计算高度 *
     * ！！！如果计算了，没法出滚动条 */
//	@Override
//	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
//		super.onMeasure(widthMeasureSpec, expandSpec);
//	}

    /**
     * 隐藏 放下 的ITEM
     */
    private void hideDropItem() {
        ((DragAdapter) getAdapter()).setShowDropItem(false);
    }

    /**
     * 获取移动动画
     */
    public Animation getMoveAnimation(float toXValue, float toYValue) {
        TranslateAnimation mTranslateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0F,
                Animation.RELATIVE_TO_SELF, toXValue,
                Animation.RELATIVE_TO_SELF, 0.0F,
                Animation.RELATIVE_TO_SELF, toYValue);// 当前位置移动到指定位置
        mTranslateAnimation.setFillAfter(true);// 设置一个动画效果执行完毕后，View对象保留在终止的位置。
        mTranslateAnimation.setDuration(300L);
        return mTranslateAnimation;
    }


    /**
     * item的交换动画效果
     *
     * @param oldPosition
     * @param newPosition
     */
    public void animateReorder(final int oldPosition, final int newPosition) {
        boolean isForward = newPosition > oldPosition;
        List<Animator> resultList = new LinkedList<Animator>();
        if (isForward) {
            for (int pos = oldPosition; pos < newPosition; pos++) {
                View view = getChildAt(pos - getFirstVisiblePosition());
                if ((pos + 1) % COLUMNS == 0) {
                    resultList.add(createTranslationAnimations(view,
                            -(view.getWidth() + mHorizontalSpacing) * (COLUMNS - 1), 0,
                            view.getHeight() + mVerticalSpacing, 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth() + mHorizontalSpacing, 0, 0, 0));
                }
            }
        } else {
            for (int pos = oldPosition; pos > newPosition; pos--) {
                View view = getChildAt(pos - getFirstVisiblePosition());
                if ((pos) % COLUMNS == 0) {
                    resultList.add(createTranslationAnimations(view,
                            (view.getWidth() + mHorizontalSpacing) * (COLUMNS - 1), 0,
                            -view.getHeight() - mVerticalSpacing, 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            -view.getWidth() - mHorizontalSpacing, 0, 0, 0));
                }
            }
        }
        AnimatorSet resultSet = new AnimatorSet();
        resultSet.playTogether(resultList);
        resultSet.setDuration(300);
        resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
        resultSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        resultSet.start();
    }

    /**
     * 创建移动动画
     *
     * @param view
     * @param startX
     * @param endX
     * @param startY
     * @param endY
     * @return
     */
    private AnimatorSet createTranslationAnimations(View view, float startX,
                                                    float endX, float startY, float endY) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
                startY, endY);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        return animSetXY;
    }


    /**
     * 删除item的动画效果
     *
     * @param position
     */
    public void removeItemAnimation(final int position) {
        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                animateReorder(position, getLastVisiblePosition() + 1);
                return true;
            }
        });
    }

    /**
     * 移动的时候触发
     */
    public void onMove(int x, int y) {
        // 拖动的VIEW下方的POSTION
        int dPosition = pointToPosition(x, y);
        DragAdapter mDragAdapter = (DragAdapter) getAdapter();
        if (dPosition < itemTotalCount) {
            if ((dPosition == -1) || (dPosition == dragPosition)) {
                return;
            }
            if (TextUtils.equals(HomeSiteUtil.SITE_ID_ADD,mDragAdapter.getItem(dPosition).getSiteId())) {
                return;
            }
            dropPosition = dPosition;
            if (dragPosition != startPosition) {
                dragPosition = startPosition;
            }
            int movecount;
            //拖动的=开始拖的，并且 拖动的 不等于放下的
            if ((dragPosition == startPosition) || (dragPosition != dropPosition)) {
                //移需要移动的动ITEM数量
                movecount = dropPosition - dragPosition;
            } else {
                //移需要移动的动ITEM数量为0
                movecount = 0;
            }
            if (movecount == 0) {
                return;
            }
            SimpleLog.d(TAG, "dragPos:" + dragPosition);
            int movecount_abs = Math.abs(movecount);
            if (dPosition != dragPosition) {
                //dragGroup设置为不可见
                ViewGroup dragGroup = (ViewGroup) getChildAt(dragPosition);
                if (dragGroup == null) {
                    return;
                }
                dragGroup.setVisibility(View.INVISIBLE);
                float to_x = 1;// 当前下方positon
                float to_y;// 当前下方右边positon
                //x_vlaue移动的距离百分比（相对于自己长度的百分比）
                float x_vlaue = ((float) mHorizontalSpacing / (float) itemWidth) + 1.0f;
                //y_vlaue移动的距离百分比（相对于自己宽度的百分比）
                float y_vlaue = ((float) mVerticalSpacing / (float) itemHeight) + 1.0f;
                for (int i = 0; i < movecount_abs; i++) {
                    to_x = x_vlaue;
                    to_y = y_vlaue;
                    //像左
                    if (movecount > 0) {
                        // 判断是不是同一行的
                        holdPosition = dragPosition + i + 1;
                        if (dragPosition / COLUMNS == holdPosition / COLUMNS) {
                            to_x = -x_vlaue;
                            to_y = 0;
                        } else if (holdPosition % COLUMNS == 0) {
                            to_x = (COLUMNS - 1) * x_vlaue;
                            to_y = -y_vlaue;
                        } else {
                            to_x = -x_vlaue;
                            to_y = 0;
                        }
                    } else {
                        //向右,下移到上，右移到左
                        holdPosition = dragPosition - i - 1;
                        if (dragPosition / COLUMNS == holdPosition / COLUMNS) {
                            to_x = x_vlaue;
                            to_y = 0;
                        } else if ((holdPosition + 1) % COLUMNS == 0) {
                            to_x = -(COLUMNS - 1) * x_vlaue;
                            to_y = y_vlaue;
                        } else {
                            to_x = x_vlaue;
                            to_y = 0;
                        }
                    }
                    ViewGroup moveViewGroup = (ViewGroup) getChildAt(holdPosition);
                    if (moveViewGroup == null) {
                        return;
                    }
                    Animation moveAnimation = getMoveAnimation(to_x, to_y);
                    moveViewGroup.startAnimation(moveAnimation);
                    //如果是最后一个移动的，那么设置他的最后个动画ID为LastAnimationID
                    if (holdPosition == dropPosition) {
                        LastAnimationID = moveAnimation.toString();
                    }
                    moveAnimation.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                            // TODO Auto-generated method stub
                            isMoving = true;
                            //检查首页logo是否有改动
                            ConfigManager.getInstance().setCheckModifiedHomeSite(true);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // TODO Auto-generated method stub
                            // 如果为最后个动画结束，那执行下面的方法
                            if (animation.toString().equalsIgnoreCase(LastAnimationID)) {
                                DragAdapter mDragAdapter = (DragAdapter) getAdapter();
                                mDragAdapter.changePosition(startPosition, dropPosition);
                                SimpleLog.d(TAG, "changePosition");
                                startPosition = dropPosition;
                                dragPosition = dropPosition;
                                isMoving = false;
                            }
                        }
                    });
                }
            }
        }
    }
}