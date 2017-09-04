package com.polar.browser.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.utils.ApkUtils;
import com.polar.browser.utils.BitmapUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.cache.memory.impl.LRULimitedMemoryCache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import libcore.io.DiskLruCache;

/**
 * Created by yd_lzk on 2016/11/4.
 */

public class ApkIconLoader {

    private static final String TAG = ApkIconLoader.class.getSimpleName();

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    /**
     * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
     */
    private LRULimitedMemoryCache mMemoryCache;
    /**
     * 硬盘缓存
     */
    private DiskLruCache mDiskLruCache;
    /**
     * 线程池
     */
    private ExecutorService threadPool;
    private Context mContext;
    private static ApkIconLoader instance;

    /***
     * 用来标记页面是否已经被finish了
     */
    private boolean mFinished;


    public static ApkIconLoader getInstance() {
        if (instance == null) {
            synchronized (ApkIconLoader.class) {
                if (instance == null) {
                    instance = new ApkIconLoader();
                }
            }
        }
//        SimpleLog.d(TAG,"mMemoryCache ："+instance.mMemoryCache.toString()+" size = "+instance.mMemoryCache.size());
        return instance;
    }

    private ApkIconLoader() {
        mContext = JuziApp.getAppContext();
        //获取系统分配给每个应用程序的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;
        SimpleLog.d(TAG, "init allocated Memory size = " + mCacheSize/1024/1024+" M");
        //给LruCache分配1/8 4M
/*        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize) {
            //必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
//                super.entryRemoved(evicted, key, oldValue, newValue);
                SimpleLog.d(TAG,"entryRemoved -- evicted = "+evicted +" key = "+key);
                if(evicted){
                    if(oldValue != null){
                        oldValue.recycle();
                        oldValue = null;
                        SimpleLog.d(TAG,"entryRemoved --- oldValue.recycle ");
                    }
                }
            }
        };*/
        mMemoryCache = new LRULimitedMemoryCache(mCacheSize);
        // 创建线程数
        threadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        // 硬盘缓存
        try {
            File cacheDir = DiskCacheUtils.getDiskCacheDir(JuziApp.getAppContext(), "bitmap");
            if(cacheDir != null){
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                mDiskLruCache = DiskLruCache.open(cacheDir, DiskCacheUtils.getAppVersion(JuziApp.getAppContext()), 1, 10 * 1024 * 1024);  // 设置10M 磁盘缓存
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        SimpleLog.d(TAG,"close -------------- ");
        try{
            //取消所有异步任务
            setFinished(true);
            //关闭线程池
            if(threadPool != null){
                threadPool.shutdownNow();
                threadPool = null;
            }
            //关闭内存缓存
            if(mMemoryCache != null){
                mMemoryCache.clear();
                mMemoryCache = null;
            }
            //关闭硬盘缓存
            if(mDiskLruCache != null){
                mDiskLruCache.close();
                mDiskLruCache = null;
            }
            instance = null;
        }catch (Exception ioe){
            ioe.printStackTrace();
        }
    }

    public void release(){
        //取消所有异步任务
        setFinished(true);
        if(mMemoryCache != null){
            mMemoryCache.clear();
        }
    }

    private Bitmap getCacheFromMemory(final String imgPath) {
        return mMemoryCache != null ? mMemoryCache.get(imgPath) : null;
    }

    private void addCache(String key, Bitmap bitmap) {
        if (getCacheFromMemory(key) == null && bitmap != null) {
            if(mMemoryCache != null){
                mMemoryCache.put(key,bitmap);
            }
            if(mDiskLruCache != null){
                addCacheToDisk(key,bitmap);
            }
        }
    }

    /**
     *  Note: 耗时操作，需要在子线程中处理
     * @param key
     * @param bitmap
     */
    private void addCacheToDisk(String key,Bitmap bitmap){
        try {
            if(!isExistInDisk(key) && bitmap != null){ //如果硬盘中没有数据，则持久化数据
                String fileName = DiskCacheUtils.hashKeyForDisk(key);
                DiskLruCache.Editor editor = mDiskLruCache.edit(fileName);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (saveBitmapToDisk(bitmap, outputStream)) {
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                }
                mDiskLruCache.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件是否保存于磁盘缓存中
     * @param imgPath
     * @return
     */
    private boolean isExistInDisk(String imgPath){
        try {
            if(mDiskLruCache == null) return false;
            //得到文件名
            String key = DiskCacheUtils.hashKeyForDisk(imgPath);
            if(null != mDiskLruCache.get(key)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private Bitmap getCacheFromDisk(String imgPath, int ivWidth, int ivHeight){
        try {
            if(mDiskLruCache == null) return null;
            String key = DiskCacheUtils.hashKeyForDisk(imgPath);
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                Bitmap bm = BitmapUtils.decodeSampledBitmapFromInputStream(snapShot.getInputStream(0),ivWidth,ivHeight);
                return bm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean saveBitmapToDisk(Bitmap bitmap,OutputStream outputStream){
        boolean result = false;
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    /***
     * 用来标记页面是否已经被关闭了
     *
     * @param hasFinished
     */
    public void setFinished(boolean hasFinished) {
        mFinished = true;
    }


    public void loadLocalImage(final String imgPath, final ImageView imageView, final int defaultImageId, boolean isScrolling) {
        if (imageView == null || imgPath == null) return;
        try{
            //为ImageView设置Tag，避免多线程引起赋值错误
            imageView.setTag(imgPath);
            if (getCacheFromMemory(imgPath) != null) {
                if (imageView.getTag() != null && imageView.getTag().equals(imgPath)) {
                    imageView.setImageBitmap(getCacheFromMemory(imgPath));
                }
            } else {
                if(isScrolling){  //滚动时，不开启加载任务
                    if (imageView.getTag() != null && imageView.getTag().equals(imgPath)) {
                        imageView.setImageResource(defaultImageId);
                    }
                    return;
                }
                /***建立新任务时，将mFinished标志位还原***/
                mFinished = false;
                LoadLocalImageTask loadLocalImage = new LoadLocalImageTask(JuziApp.getInstance(), imageView, imgPath, defaultImageId);
                threadPool.execute(loadLocalImage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadLocalVideoThumbnails(final long id, final String path, final ImageView imageView, final int defaultImageId, boolean isScrolling) {
        if (imageView == null || path == null) return;
        //为ImageView设置Tag，避免多线程引起赋值错误
        imageView.setTag(path);

        if (getCacheFromMemory(path) != null) {
            if (imageView.getTag() != null && imageView.getTag().equals(path)) {
                imageView.setImageBitmap(getCacheFromMemory(path));
            }
        } else {
            if(isScrolling){  //滚动时，不开启加载任务
                if (imageView.getTag() != null && imageView.getTag().equals(path)) {
                    imageView.setImageResource(defaultImageId);
                }
                return;
            }
            /***建立新任务时，将mFinished标志位还原***/
            mFinished = false;
            LoadLocalVideoThumbnailsTask loadLocalImage = new LoadLocalVideoThumbnailsTask(imageView, id, path, defaultImageId);
            threadPool.execute(loadLocalImage);
        }
    }


    /****
     * 新建图片加载任务，避免内存泄漏
     */
    private class LoadLocalImageTask implements Runnable {

        private WeakReference<ImageView> mImageViewWeakReference;
        private Context mcontext;
        private String mImagePath;
        private int mDefaultImageId;
        private int ivWidth;
        private int ivHeight;

        public LoadLocalImageTask(Context context, ImageView imageView, String imagePath, int defaultImageId) {
            this.mcontext = context;
            this.mImagePath = imagePath;
            this.mDefaultImageId = defaultImageId;
            mImageViewWeakReference = new WeakReference<ImageView>(imageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if(params != null){
                ivWidth = params.width;
                ivHeight = params.height;
            }else{
                ivWidth = ivHeight =
                        (int)JuziApp.getAppContext().getResources().getDimension(R.dimen.file_list_item_iv_width);
            }
        }

        @Override
        public void run() {
            if (mFinished || Thread.currentThread().isInterrupted()) {
                return;
            }
            final ImageView _imageView = mImageViewWeakReference.get();
            if (_imageView != null) {
                //先检查硬盘缓存
                Bitmap bm = null;
                bm = getCacheFromDisk(mImagePath,ivWidth,ivHeight);
                if(bm == null){ //硬盘缓存 无
                    bm = BitmapUtils.drawableToBitmap(ApkUtils.getPackageIcon(mcontext, mImagePath));
                }
                if (bm != null) {  //获取成功
                    //对Bitmap进行压缩
                    bm = BitmapUtils.compressBitmap(bm,ivWidth,ivHeight,50);
                    if(bm != null){
                        addCache(mImagePath, bm);
                        if (_imageView.getTag() != null && _imageView.getTag().equals(mImagePath)) {
                            _imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView imageView = mImageViewWeakReference.get();
                                    if (imageView != null) {
                                        imageView.setImageBitmap(getCacheFromMemory(mImagePath));
                                    }
                                }
                            });
                        }
                    }
                    return;
                }

                if (_imageView.getTag() != null && _imageView.getTag().equals(mImagePath)) {
                    _imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            ImageView imageView = mImageViewWeakReference.get();
                            if (imageView != null) {
                                imageView.setImageResource(mDefaultImageId);
                            }
                        }
                    });
                }

            }
        }
    }

    /****
     * 新建视频缩略图加载任务，避免内存泄漏
     */
    private class LoadLocalVideoThumbnailsTask implements Runnable {

        private WeakReference<ImageView> mImageViewWeakReference;
        private long thumbnailId;
        private String mImagePath;
        private int mDefaultImageId;
        private int ivWidth;
        private int ivHeight;

        public LoadLocalVideoThumbnailsTask(ImageView imageView, long id, String imagePath, int defaultImageId) {
            this.mImagePath = imagePath;
            this.thumbnailId = id;
            this.mDefaultImageId = defaultImageId;
            mImageViewWeakReference = new WeakReference<ImageView>(imageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if(params != null){
                ivWidth = params.width;
                ivHeight = params.height;
            }else{
                ivWidth = ivHeight =
                        (int)JuziApp.getAppContext().getResources().getDimension(R.dimen.file_list_item_iv_width);
            }
        }

        @Override
        public void run() {
            if (mFinished) {
                return;
            }
            final ImageView imageView = mImageViewWeakReference.get();
            if (imageView != null) {
                //先检查硬盘缓存
                Bitmap bm = null;
                bm = getCacheFromDisk(mImagePath,ivWidth,ivHeight);
                if(bm == null){  //硬盘缓存 无
                    String thumbnailPath = QueryUtils.getVideoThumbnailPath(mContext, thumbnailId);
                    if (thumbnailPath != null) {
                        bm = BitmapUtils.decodeSampledBitmapFromFile(thumbnailPath,ivWidth,ivHeight,false);
                        if (bm == null) {
                            bm = ThumbnailUtils.createVideoThumbnail(mImagePath, MediaStore.Images.Thumbnails.MINI_KIND);
                        }
                    } else {
                        bm = ThumbnailUtils.createVideoThumbnail(mImagePath, MediaStore.Images.Thumbnails.MINI_KIND);
                    }
                }
                if (bm != null) {
                    //对Bitmap进行压缩
                    bm = BitmapUtils.compressBitmap(bm,ivWidth,ivHeight,50);
                    if(bm != null){
                        addCache(mImagePath, bm);
                        if (imageView.getTag() != null && imageView.getTag().equals(mImagePath)) {
                            imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView _imageView = mImageViewWeakReference.get();
                                    if (_imageView != null) {
                                        _imageView.setImageBitmap(getCacheFromMemory(mImagePath));
                                    }
                                }
                            });
                        }
                        return;
                    }
                }

                if (imageView.getTag() != null && imageView.getTag().equals(mImagePath)) {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            ImageView _imageView = mImageViewWeakReference.get();
                            if (_imageView != null) {
                                _imageView.setImageResource(mDefaultImageId);
                            }
                        }
                    });
                }

            }
        }
    }

}
