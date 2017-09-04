package com.polar.browser.imagebrowse;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by duan on 17/3/14.
 */

public class ImageBrowseAdapter extends BaseAdapter<ImageInfo, ImageBrowseAdapter.ViewHolder>  {

    private Context mContext;

    public ImageBrowseAdapter(Context context, List<ImageInfo> data) {
        super(context, data);
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new ImageItem(mContext));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bindData(data.get(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bindData(ImageInfo data) {
            ((ImageItem)itemView).bind(data);
        }

    }

}
