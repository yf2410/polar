package com.polar.browser.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.download.view.HackyViewPager;
import com.polar.browser.download_refactor.util.FileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SysUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;

public class ImageGalleryActivity extends LemonBaseActivity implements View.OnClickListener {

    private static final String TAG = ImageGalleryActivity.class.getSimpleName();

    private HackyViewPager viewpager;
    private ImageAdapter imageAdapter;
    private TextView image_gallery_title;
    private View title_bar;
    private View bottom_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_gallery);
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
        SysUtils.setFullScreen(this,true);
        initData();
    }

    private void initViews() {
        image_gallery_title = (TextView) findViewById(R.id.image_gallery_title);
        this.title_bar = findViewById(R.id.image_gallery_title_bar);
        bottom_layout= findViewById(R.id.bottom_layout);
        findViewById(R.id.image_gallery_back).setOnClickListener(this);
        findViewById(R.id.image_gallery_share).setOnClickListener(this);
        findViewById(R.id.image_gallery_set_wallpaper).setOnClickListener(this);
        findViewById(R.id.image_gallery_delete).setOnClickListener(this);

        viewpager = (HackyViewPager) findViewById(R.id.viewpager);
    }

    public void initData(){
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        List<String> imagePaths = intent.getStringArrayListExtra("imagePaths");
        imageAdapter = new ImageAdapter(getApplicationContext(), imagePaths);
        viewpager.setAdapter(imageAdapter);
        viewpager.addOnPageChangeListener(new PageChangeListener());
        viewpager.setCurrentItem(position);
        updateTitle(viewpager.getCurrentItem());
    }

    private void updateTitle(int position) {
        if (imageAdapter.getCount() == 0) {
            image_gallery_title.setText("0/0");
        } else {
            image_gallery_title.setText((position + 1) + "/" + imageAdapter.getCount());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_gallery_back:
                finish();
                break;
            case R.id.image_gallery_share:
                share();
                break;
            case R.id.image_gallery_set_wallpaper:
                setImageWallpaper();
                break;
            case R.id.image_gallery_delete:
                showDeleteDialog();
                break;
        }
    }

    private void sendGoogleStatistics(String currentBtn) {
//        Statistics.sendOnceStatistics(GoogleConfigDefine.FILE_MANAGER_STATISTICS,GoogleConfigDefine.PIC,currentBtn);
    }

    private void showDeleteDialog() {
        final CommonDialog dialog = new CommonDialog(this, "", getString(R.string.file_delete_hint));
        dialog.hideTitle();
        dialog.setBtnCancel(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setBtnOk(getString(R.string.delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                delete();
//                sendGoogleStatistics(GoogleConfigDefine.PIC_DELETE_CONFIRM);
            }
        });
        dialog.show();
    }

    private void setImageWallpaper() {
        if (imageAdapter.getCount() == 0) {
            return;
        }
        final ImageAdapter.CurrentItem item = imageAdapter.getCurrentItem();
        final String imagePath = imageAdapter.getImagePath(item.currentPosition);
        Intent intent = new Intent(this, SetWallpaperActivity.class);
        intent.putExtra("image_path", imagePath);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    private void share() {
        if (imageAdapter.getCount() == 0) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        final ImageAdapter.CurrentItem item = imageAdapter.getCurrentItem();
        final String imagePath = imageAdapter.getImagePath(item.currentPosition);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imagePath)));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void delete() {
        if (imageAdapter.getCount() == 0) {
            return;
        }

        final ImageAdapter.CurrentItem item = imageAdapter.getCurrentItem();
        final String imagePath = imageAdapter.getImagePath(item.currentPosition);
        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + "=?",
                new String[] {imagePath});
        FileUtils.delete(imagePath);
        viewpager.removeView(item.currentView);
        imageAdapter.removeItem(item.currentPosition);
        QueryUtils.notifyFileCountChanged(TYPE_IMAGE);

        updateTitle(item.currentPosition);

        if(imageAdapter.getCount() == 0){
            finish();
        }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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
        private List<String> data;
        private LayoutInflater inflater;
        private CurrentItem currentItem;

        ImageAdapter(Context context, List<String> data) {
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
                    title_bar.setVisibility(title_bar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
                    bottom_layout.setVisibility(bottom_layout.getVisibility() == View.VISIBLE?View.GONE:View.VISIBLE);
                }
            });
            File file = new File(data.get(position));
            //使用StringSignature在Key中混入文件修改时间，当文件改变时缓存失效
            Glide.with(context).load(file).signature(new StringSignature(String.valueOf(file.lastModified()))).fitCenter().into(imageView);
            view.addView(imageLayout, 0);
            return imageLayout;
        }

        String getImagePath(int position) {
            if (data != null && !data.isEmpty()) {
                return data.get(position);
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

    public static void start(Context context,int position,ArrayList<String> imagePaths) {
        Intent starter = new Intent(context, ImageGalleryActivity.class);
        starter.putExtra("position", position);
        starter.putStringArrayListExtra("imagePaths", imagePaths);
        context.startActivity(starter);
    }
}
