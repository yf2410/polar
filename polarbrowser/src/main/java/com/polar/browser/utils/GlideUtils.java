package com.polar.browser.utils;

/**
 * Created by Administrator on 2017/3/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.polar.browser.database.MediaDBRefreshHelper;
import com.polar.browser.i.IDownloadCallBack;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.vclibrary.R;
import com.polar.browser.vclibrary.util.GlideCircleTransform;

import java.io.File;


/**
 * Created by FKQ on 2016/9/28.
 */

public class GlideUtils {

    /**
     * Gilde加载图片
     *
     * @param context
     * @param url
     * @param imageView
     * @dec 现在默认图片设为灰色背景图
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        loadImage(context,url,imageView,R.drawable.card_news_default_bg,R.drawable.card_news_default_bg);
}

    public static void loadCircleImage(Context context, String url, ImageView imageView) {
        if (imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }
        commonBuilder(context, url).placeholder(R.drawable.card_news_default_bg)

                .transform(new GlideCircleTransform(context))
                .into(imageView);
    }


    /**
     * Gilde加载图片
     *
     * @param context
     * @param url
     * @param imageView
     * @param placeholder
     * @param error
     */
    public static void loadImage(Context context, String url, ImageView imageView, int placeholder, int error) {
        if (imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }
        commonBuilder(context, url)
                .placeholder(placeholder)
                .error(error)
                .into(imageView);
    }

//    public static void downloadOnly(Context context,String url){
//
//        try {
//            Glide.with(context)
//                    .load(url)
//                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .get();
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 加载圆形图片
     * @param context
     * @param url
     * @param lastModified  url最后编辑时间，防止缓存错误（避免相同url不同图片的情况）
     * @param imageView
     * @param defaultRes
     */
    public static void loadCircleImage(Context context, String url, long lastModified, ImageView imageView, Drawable placeholderDraw, int defaultRes){
        if(url != null) {
            if(placeholderDraw == null){
                placeholderDraw = context.getResources().getDrawable(defaultRes);
            }
            Glide.with(context).load(url).signature(new StringSignature(String.valueOf(lastModified)))
//                    .bitmapTransform(new GlideCircleTransform(context.getApplicationContext()))
                    .dontAnimate()
                    .placeholder(placeholderDraw)
                    .error(defaultRes)
                    .into(imageView);
        }else {
            imageView.setImageResource(defaultRes);
        }
    }

    public static void loadImageWithListener(Context context, String url, @NonNull SimpleTarget<Bitmap> target) {
        getDrawableTypeRequest(context, url).asBitmap().into(target);
    }

    private static DrawableTypeRequest getDrawableTypeRequest(Context context, String url) {
        return Glide.with(context).load(url);
    }

//    public static void loadRoundImageWithListener(Context context, String url, ImageView imageView,
//                                                  RoundedCornersTransformation.CornerType cornerType,
//                                                  @NonNull SimpleTarget target) {
//        if (imageView == null) {
//            throw new IllegalArgumentException("loadImage argument error");
//        }
//        getDrawableTypeRequest(context, url)
//                .bitmapTransform(new RoundedCornersTransformation(context, 5, 0, cornerType))
//                .into(target);
//    }

    private static DrawableRequestBuilder commonBuilder(Context context, String url) {
        return getDrawableTypeRequest(context, url).diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade();
    }

    public static void loadImageToFile(final Context context, final String url, final long lastModified, final String path, final IDownloadCallBack<String> callBack){

        GlideUtils.getBitmapByLoadUrl(context, url, lastModified, new IDownloadCallBack<Bitmap>() {
            @Override
            public void onDownloadSuccess(final Bitmap source) {

                //当获取到Bitmap，提前通知保存结果，提高响应速度
                if (source != null) {
                    callBack.onDownloadSuccess("");
                } else {
                    callBack.onDownloadFailed("");
                }

                //switch Thread to IO
                ThreadManager.postTaskToIOHandler(new Runnable() {
                    @Override
                    public void run() {

                        String dir = null;
                        String imgName = null;
                        if(path != null){
                            File file = new File(path);
                            dir = file.getParent();
                            imgName = file.getName();
                        }
                        if(dir == null){ dir = VCStoragerManager.getInstance().getImageDirPath(); }
                        if(imgName == null){ imgName = "images_" + System.currentTimeMillis(); }

                        FileUtils.saveBitmapToFile(source, dir, imgName);

                        //将下载图片插入媒体数据库
                        MediaDBRefreshHelper.getInstance(context).insertFile( dir + imgName);

/*                      final File file = new File(VCStoragerManager.getInstance().getImageDirPath(),imgName);
                        //switch Thread to UI
                        ThreadManager.postTaskToUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                if (file.exists()) {
                                    callBack.onDownloadSuccess("");
                                } else {
                                    callBack.onDownloadFailed("");
                                }
                            }
                        });*/
                    }
                });
            }

            @Override
            public void onDownloadFailed(String error) {
                callBack.onDownloadFailed("");
            }

        });
    }

    /**
     *
     * @param context
     * @param url
     * @param lastModified
     * @param callBack
     * @return
     */
    public static Bitmap getBitmapByLoadUrl(Context context, String url,long lastModified, final IDownloadCallBack<Bitmap> callBack){
        Bitmap bitmap = null;
        if(url == null) return bitmap;
        try {
            RequestListener<String, Bitmap> requestListener = new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    callBack.onDownloadFailed("");
                    return false;
                }
                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    callBack.onDownloadSuccess(resource);
                    return false;
                }
            };

            if(lastModified > 0){
                Glide.with(context)
                        .load(url)
                        .asBitmap() //必须
                        .signature(new StringSignature(String.valueOf(lastModified)))  //TODO
                        .listener(requestListener)
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }else{
                Glide.with(context)
                        .load(url)
                        .asBitmap() //必须
                        .listener(requestListener)
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

