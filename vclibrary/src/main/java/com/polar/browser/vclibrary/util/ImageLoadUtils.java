package com.polar.browser.vclibrary.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.polar.browser.vclibrary.R;

/**
 * Created by FKQ on 2016/9/28.
 */

public class ImageLoadUtils {

    /**
     * 加载图片
     *
     * @param context
     * @param url
     * @param imageView
     * @dec 现在默认图片设为灰色背景图
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        if (imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }
        if(!isActivityValidate(context))return;

        commonBuilder(context,url)
                .placeholder(R.drawable.card_news_default_bg)
                .error(R.drawable.card_news_default_bg)
                .into(imageView);
//        Picasso.with(context).load(url).placeholder(R.drawable.card_news_default_bg).error(R.drawable.card_news_default_bg)
//                .into(imageView);
    }

    /**
     * 加载图片
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
//        if (url.startsWith("http://") || url.startsWith("http://") || url.startsWith("file://")) {
//            Picasso.with(context).load(url).error(error).placeholder(placeholder).into(imageView);
//        } else {
//            Picasso.with(context).load(new File(url)).error(error).placeholder(placeholder).into(imageView);
//        }
        if(!isActivityValidate(context))return;

        getDrawableTypeRequest(context,url)
                .thumbnail(0.5f)
                .placeholder(placeholder)
                .error(error)
                .crossFade()
                .into(imageView);


    }

    /**
     * 加载图片
     *
     * @param context
     * @param url
     * @param imageView
     * @param placeholder
     * @param error
     */
    public static void loadImage(Context context, int url, ImageView imageView, int placeholder, int error) {
        if (imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }
        if(!isActivityValidate(context))return;

//        Picasso.with(context).load(url).error(error).placeholder(placeholder).into(imageView);
        getDrawableTypeRequest(context,url).error(error).placeholder(placeholder).into(imageView);
    }

    public static void loadRoundImage(Context context, String url, ImageView imageView) {
        if (imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }

        if(!isActivityValidate(context))return;

        commonBuilder(context, url)
                .placeholder(R.drawable.card_news_default_bg)
                .error(R.drawable.card_news_default_bg)
                .transform(new GlideCircleTransform(context))
                .into(imageView);

    }

    public static void loadEllipseImage(Context context, String url, ImageView imageView, int stackDrawable, int placeholder, int error) {
        if(imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }
        Context appContext = context.getApplicationContext();
        Glide.with(appContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeholder)
                .error(error)
                .crossFade()
                .transform(new GlideStackTransform(appContext, stackDrawable))
                .into(imageView);

    }

    public static void loadEllipseImage(Context context, int url, ImageView imageView, int stackDrawable, int placeholder, int error) {
        if(imageView == null) {
            throw new IllegalArgumentException("loadImage argument error");
        }
        Context appContext = context.getApplicationContext();
        Glide.with(appContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeholder)
                .error(error)
                .crossFade()
                .transform(new GlideStackTransform(appContext, stackDrawable))
                .into(imageView);

    }

    private static boolean isActivityValidate(Context context) {
        return context != null && !(context instanceof Activity)
                || context != null && !((Activity) context).isFinishing();
    }

    private static DrawableTypeRequest getDrawableTypeRequest(Context context, Object url) {
        return Glide.with(context).load(url);
    }


    private static DrawableRequestBuilder commonBuilder(Context context, String url) {
        return getDrawableTypeRequest(context, url).diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade();
    }

    public static void loadImageWithListener(Context context, String url, @NonNull SimpleTarget<Bitmap> target) {
        if (!isActivityValidate(context)) return;
        getDrawableTypeRequest(context, url).asBitmap().into(target);
    }


}
