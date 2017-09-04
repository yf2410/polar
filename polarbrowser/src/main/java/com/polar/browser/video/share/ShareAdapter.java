package com.polar.browser.video.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;

import java.util.List;

/**
 * Created by yd_lzk on 2017/1/9.
 */

public class ShareAdapter extends BaseAdapter{

    private List<Integer> iconList;
    private List<Integer> nameList;
    private Context context;
    private LayoutInflater inflater;

    public ShareAdapter(Context context, List<Integer> iconList, List<Integer> nameList) {
        this.context = context;
        this.iconList = iconList;
        this.nameList = nameList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return nameList != null ? nameList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.view_custom_share_dialog_item, null);
            holder.iconIV = (ImageView) convertView.findViewById(R.id.share_iv_icon);
            holder.nameTV = (TextView) convertView.findViewById(R.id.share_tv_name);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.iconIV.setImageResource(iconList.get(position));
        holder.nameTV.setText(nameList.get(position));
        return convertView;
    }

    static class ViewHolder {
        ImageView iconIV;
        TextView nameTV;
    }
}
