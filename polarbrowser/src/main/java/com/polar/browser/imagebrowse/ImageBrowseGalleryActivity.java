package com.polar.browser.imagebrowse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.view.HackyViewPager;
import com.polar.browser.i.IDownloadCallBack;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.GlideUtils;
import com.polar.browser.video.share.CustomShareDialog;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageBrowseGalleryActivity extends LemonBaseActivity implements View.OnClickListener {

    private HackyViewPager mViewpager;
    private ImageAdapter mImageAdapter;

    private ImageView mIvHead;
    private TextView mTvUsername;

    private List<ImageInfo> mData;
    private int currentPosi = -1; //记录当前显示位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browse_gallery);
        initViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initViews() {
        findViewById(R.id.btn_share).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        mViewpager = (HackyViewPager) findViewById(R.id.viewpager);
        mIvHead = (ImageView) findViewById(R.id.iv_head);
        mTvUsername = (TextView) findViewById(R.id.tv_username);
    }

    public void initData(){
        Intent intent = getIntent();
        if(intent != null){
            int position = intent.getIntExtra("position", 0);
            if(currentPosi == -1){  //第一次初始化
                currentPosi = position;
            }
            mData = ((ImageListData) intent.getSerializableExtra(ImageBrowseActivity.IMAGE_LIST)).imgs;
        }
        mImageAdapter = new ImageAdapter(getApplicationContext(), mData);
        mViewpager.setAdapter(mImageAdapter);
        mViewpager.addOnPageChangeListener(new PageChangeListener());
        mViewpager.setCurrentItem(currentPosi<0 ? 0 : currentPosi);
        updateTitle(mViewpager.getCurrentItem());
    }

    private void updateTitle(int position) {
        if (mImageAdapter.getCount() == 0) {
        } else {
            mTvUsername.setText(mData.get(position).getUsername());
           // Glide.with(this).load(mData.get(position).getProfPic()).fitCenter().into(mIvHead);
            GlideUtils.loadCircleImage(this , mData.get(position).getProfPic() ,mIvHead );
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.alpha_scale_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_share:
                share();
                break;
            case R.id.btn_save:
                save();
                break;
            default:
                break;
        }
    }

    private void share() {
        if (mImageAdapter.getCount() == 0) {
            return;
        }
        //statistics click count
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.WEBPAGE_IMAGE_SHARE);
        //share image
        try{
            ImageAdapter.CurrentItem item = mImageAdapter.getCurrentItem();
            String imageUrl = mImageAdapter.getImagePath(item.currentPosition);
            CustomShareDialog dialog = new CustomShareDialog(this,imageUrl,CustomShareDialog.CHANNEL_INSTAGRAM_SHARE);
            dialog.show();
        }catch (Exception e){
            CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
        }
    }

    private void save(){
        if (mImageAdapter.getCount() == 0) {
            return;
        }
        //statistics click count
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.WEBPAGE_IMAGE_SAVE);
        //save image
        try{
            ImageAdapter.CurrentItem item = mImageAdapter.getCurrentItem();
            String imageUrl = mImageAdapter.getImagePath(item.currentPosition);
            String imgName = FileUtils.getFileNameByPath(imageUrl);
            if(imgName == null){
                imgName = "images_" + System.currentTimeMillis();
            }
            String path = VCStoragerManager.getInstance().getImageDirPath()+imgName;
            //Context使用长生命周期，即使退出当前Activity同样完成下载任务。
            GlideUtils.loadImageToFile(this.getApplicationContext(), imageUrl,0, path, new IDownloadCallBack<String>() {
                @Override
                public void onDownloadSuccess(String source) {
                    CustomToastUtils.getInstance().showTextToast(R.string.picture_saved);
                }

                @Override
                public void onDownloadFailed(String error) {
                    CustomToastUtils.getInstance().showTextToast(R.string.download_error);
                }
            });
        }catch (Exception e){
            CustomToastUtils.getInstance().showTextToast(R.string.download_error);
        }

    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosi = position;
        }

        @Override
        public void onPageSelected(int position) {
            updateTitle(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private  class ImageAdapter extends PagerAdapter {
        private Context context;
        private List<ImageInfo> data;
        private LayoutInflater inflater;
        private CurrentItem currentItem;

        ImageAdapter(Context context, List<ImageInfo> data) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public Object instantiateItem(ViewGroup view, final int position) {
            View imageLayout = inflater.inflate(R.layout.image_gallery_item, view, false);
            PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image);
            imageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                }
            });
            Glide.with(context).load(data.get(position).getUrl()).fitCenter().into(imageView);
            view.addView(imageLayout, 0);
            return imageLayout;
        }

        String getImagePath(int position) {
            if (data != null && !data.isEmpty()) {
                return data.get(position).getUrl();
            }
            return null;
        }

        CurrentItem getCurrentItem() {
            return currentItem;
        }

        @Override
        public int getCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

        void removeItem(int position) {
            if (data != null) {
                data.remove(position);
            }
            notifyDataSetChanged();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            setCurrentItem(new CurrentItem((View)object, position));
        }

        private void setCurrentItem(CurrentItem currentItem) {
            this.currentItem = currentItem;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        class CurrentItem {
            /**当前显示的view*/
            View currentView;
            /**当前显示的position*/
            int currentPosition;

            CurrentItem(View currentView, int currentPosition) {
                this.currentView = currentView;
                this.currentPosition = currentPosition;
            }
        }
    }

}
