package com.polar.browser.homepage.customlogo;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.DragGridView;
import com.polar.browser.custom_logo.DrawBitmapUtils;
import com.polar.browser.custom_logo.LogoBitmapUtils;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.StringUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DragAdapter extends BaseAdapter {
    /**
     * TAG
     */
    private final static String TAG = "DragAdapter";
    /**
     * 可以拖动的列表（即用户选择的频道列表）
     */
    public List<HomeSite> mDataList;

    /**
     * 是否显示底部的ITEM
     */
    private boolean isItemShow = false;
    private Context mContext;
    /**
     * 控制的position
     */
    private int holdPosition = -1;
    /**
     * 是否改变
     */
    private boolean isExchanged = false;
    /**
     * TextView 频道内容
     */


    private DragGridView mDragView;


    public DragAdapter(Context context, List<HomeSite> dataList, DragGridView gridView) {
        this.mContext = context;
        this.mDataList = dataList;
        this.mDragView = gridView;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public HomeSite getItem(int position) {
        if (mDataList != null && mDataList.size() != 0) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_edit_logo, null);
        ImageView sitePicImageView = (ImageView) view.findViewById(R.id.site_pic);
        TextView siteNameTextView = (TextView) view.findViewById(R.id.site_name);
        ImageView deleteImageView = (ImageView) view.findViewById(R.id.delete);
        HomeSite homeSite = getItem(position);
        String siteId = homeSite.getSiteId();
        String siteName = homeSite.getSiteName();
        String siteAddr = homeSite.getSiteAddr();
        String sitePic = homeSite.getSitePic();
        if (!TextUtils.isEmpty(siteName)) {
            siteNameTextView.setText(homeSite.getSiteName());
        }
        if (position == mDataList.size() - 1 && TextUtils.equals(HomeSiteUtil.SITE_ID_ADD,siteId)) {
            siteNameTextView.setText("");
            view.setOnClickListener(null);
            deleteImageView.setVisibility(View.GONE);
            sitePic = homeSite.getSitePic();
            ImageLoadUtils.loadEllipseImage(mContext, sitePic, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_add_new, R.drawable.logo_add_new);
        }  else {
            //移动过程中
            if (isExchanged && (position == holdPosition)) {
                ImageLoadUtils.loadEllipseImage(mContext, R.drawable.logo_position, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
                if(AppEnv.DEBUG) {
                    SimpleLog.d(TAG, "pos:" + position);
                    SimpleLog.d(TAG, "holdPosition:" + holdPosition);
                }
                deleteImageView.setVisibility(View.GONE);
                siteNameTextView.setText("");
            } else {
                if (TextUtils.equals(HomeSiteUtil.SITE_ID_MORE,siteId)) {
                    deleteImageView.setVisibility(View.GONE);
                } else {
                    deleteImageView.setImageResource(R.drawable.edit_logo_delete);
                    deleteImageView.setVisibility(View.VISIBLE);
                }
                siteNameTextView.setTextColor(mContext.getResources().getColor(R.color.black87));
                String format = String.format("%s/%s", JuziApp.getAppContext().getFilesDir().toString(), CommonData.ICON_DIR_NAME);
                if (TextUtils.isEmpty(sitePic)) {
                        processCustomLogo(sitePicImageView, siteName, siteAddr);
//                    ImageLoadUtils.loadEllipseImage(mContext, R.drawable.default_shortcut, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
                } else {
                    //直接加载绿色图片
                    if (sitePic.contains(format)) {
                        processCustomLogo(sitePicImageView, siteName, siteAddr);
                    } else {
                        ImageLoadUtils.loadEllipseImage(mContext, sitePic, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
                    }
                }
            }
            deleteImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    remove(position);
                    //检查首页logo是否有改动
                    ConfigManager.getInstance().setCheckModifiedHomeSite(true);
                }
            });
        }
        return view;
    }

    private void processCustomLogo(final ImageView sitePicImageView, final String siteName, final String url) {
        //创建一个上游 Observable：
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                String name = "";
                Bitmap bitmapLogo = null;
                String firstText = StringUtils.getFirstChar(siteName);
                String host = UrlUtils.getHost(url);
                if (TextUtils.isEmpty(host)) {
                    name = SecurityUtil.getMD5(url)+firstText;
                }else {
                    name = host + firstText;
                }
                bitmapLogo = FileUtils.getBitmapFromFile(LogoBitmapUtils.getLogoPathByName(name));
                emitter.onNext(bitmapLogo);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmapLogo) throws Exception {
                        if (bitmapLogo != null) {
                            sitePicImageView.setImageBitmap(bitmapLogo);
                        }else {
                            ImageLoadUtils.loadEllipseImage(mContext, R.drawable.default_shortcut, sitePicImageView, R.drawable.logo_frame, R.drawable.logo_click, R.drawable.default_shortcut);
                        }
                    }
                });
    }

    /**
     * 拖动变更logo排序
     */
    public void changePosition(final int dragPostion, final int dropPostion) {
        if (dragPostion == dropPostion) {
            return;
        }
        holdPosition = dropPostion;
        HomeSite dragItem = getItem(dragPostion);
        if (dragPostion < dropPostion) {
            mDataList.add(dropPostion + 1, dragItem);
            mDataList.remove(dragPostion);
        } else {
            mDataList.add(dropPostion, dragItem);
            mDataList.remove(dragPostion + 1);
        }
        isExchanged = true;
        notifyDataSetChanged();
    }


    /**
     * 删除频道列表
     */
    public synchronized void remove(final int position) {
        mDataList.remove(position);
        notifyDataSetChanged();
        mDragView.removeItemAnimation(position);
    }

    //TODO 删除logo
    private void deleteCustomDrawLogo(HomeSite homeSite) {
        String url = homeSite.getSiteAddr();
        String title = homeSite.getSiteName();
        String firstChar = StringUtils.getFirstChar(title);
        String name = url + firstChar;
        LogoBitmapUtils.deleteDrawedBitmapByUrl(LogoBitmapUtils.getLogoPathByName(name), url, title);
    }


    /**
     * 设置频道列表
     */
    public void setListData(List<HomeSite> list) {
        mDataList = list;
        notifyDataSetChanged();
    }


    /**
     * 显示放下的ITEM
     */
    public void setShowDropItem(boolean show) {
        isItemShow = show;
    }

    public void setDrop() {
        isExchanged = false;
    }
}