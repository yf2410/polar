package com.polar.browser.download.uncompress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.download.ICallback;
import com.polar.browser.utils.FormatUtils;
import com.polar.browser.utils.OpenFileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yd_lzk on 2016/11/25.
 */

public class UncompressFolderAdapter extends BaseAdapter {

    private static final String TAG = UncompressFolderAdapter.class.getSimpleName();

    private Context context;
    private List<UncompressInfo> data;
    private LayoutInflater inflater;
    /**正常模式 false， 编辑模式 true*/
    private boolean editMode;
    /**已选择的条目容器*/
    private Map<String, UncompressInfo> map;
    /**是否enable 底部按钮的callback*/
    private ICallback callback;

    public UncompressFolderAdapter(Context context, List<UncompressInfo> data, ICallback callback) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(context);
        map = new HashMap<String, UncompressInfo>();
        this.callback = callback;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public Map<String, UncompressInfo> getSelectedData() {
        return map;
    }

    public int getCheckedCount(){
        return map != null ? map.size() : 0;
    }

    public List<String> getAllImagePaths() {
        ArrayList<String> list = new ArrayList<>();
        if (list != null) {
            for (UncompressInfo info: data) {
                if(info.getType() == UncompressInfo.TYPE_FILE &&
                        OpenFileUtils.checkEndsWithInStringArray(info.getPath(), context.getResources()
                                .getStringArray(R.array.fileEndingImage))){
                    list.add(info.getPath());
                }
            }
        }
        return list;
    }

    public void changeData(List<UncompressInfo> list) {
        if(data == null) {
            data = new ArrayList<>();
        }
        data.clear();
        if(list != null){
            data.addAll(list);
        }
        this.notifyDataSetChanged();
    }

    /**改变模式*/
    public void changeEditState(boolean state) {
        editMode = state;
        if (!state) {
            map.clear();
        }
        notifyDataSetChanged();
    }

    /**全选*/
    public void selectAll() {
        if (getCount() != 0) {
            map.clear();
            for (UncompressInfo info : data) {
                map.put(info.getPath(), info);
            }
            notifyDataSetChanged();
        }
    }

    /**取消全选*/
    public void deSelectAll() {
        if (getCount() != 0) {
            map.clear();
            notifyDataSetChanged();
            callback.onShow(false, false);
        }
    }

    /**删除勾选的条目*/
    public void deleteItem(List<ImageFolderInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            data.remove(list.get(i));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (data != null && !data.isEmpty()) {
            return data.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (data != null && !data.isEmpty()) {
            return data.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (data != null && !data.isEmpty()) {
            return data.get(position).getType();
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            switch (getItemViewType(position)){
                case UncompressInfo.TYPE_ROOT:
                    convertView = inflater.inflate(R.layout.file_uncompress_list_rootdir_item, null);
                    holder.uncompress_checkbox = (CheckBox) convertView.findViewById(R.id.uncompress_checkbox);
                    holder.uncompress_icon = (ImageView) convertView.findViewById(R.id.uncompress_icon);
                    holder.uncompress_title = (TextView) convertView.findViewById(R.id.uncompress_title);
                    holder.uncompress_path = (TextView) convertView.findViewById(R.id.uncompress_path);
                    break;
                case UncompressInfo.TYPE_DIR:
                case UncompressInfo.TYPE_FILE:
                    convertView = inflater.inflate(R.layout.file_uncompress_list_item, null);
                    holder.uncompress_checkbox = (CheckBox) convertView.findViewById(R.id.uncompress_checkbox);
                    holder.uncompress_icon = (ImageView) convertView.findViewById(R.id.uncompress_icon);
                    holder.uncompress_title = (TextView) convertView.findViewById(R.id.uncompress_title);
                    holder.uncompress_size = (TextView) convertView.findViewById(R.id.uncompress_size);
                    holder.uncompress_date = (TextView) convertView.findViewById(R.id.uncompress_time);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.uncompress_checkbox.setVisibility(editMode ? View.VISIBLE : View.GONE);
        final UncompressInfo info = data.get(position);
        switch (getItemViewType(position)){
            case UncompressInfo.TYPE_ROOT:
                holder.uncompress_path.setText(info.getPath());
                break;
            case UncompressInfo.TYPE_DIR:
                holder.uncompress_icon.setImageResource(R.drawable.icon_folder);
                holder.uncompress_size.setText(context.getResources().getString(R.string.file_child_count,info.getChildren()));
                holder.uncompress_date.setText(FormatUtils.CalenderformatDate(info.getDate()));
                break;
            case UncompressInfo.TYPE_FILE:
                holder.uncompress_icon.setImageResource(OpenFileUtils
                        .getFileIconByFileName(info.getName()));
                holder.uncompress_size.setText(FormatUtils.formatFileSize(info.getSize()));
                holder.uncompress_date.setText(FormatUtils.CalenderformatDate(info.getDate()));
                break;
        }
        holder.uncompress_title.setText(info.getName().trim());
        holder.uncompress_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!map.containsKey(info.getPath())) {
                        map.put(info.getPath(), info);
                    }

                    if (getCount() != 0 && getCount() == map.size()) {
                        callback.onShow(true, true);
                    } else {
                        callback.onShow(true, false);
                    }
                } else {
                    if (map.containsKey(info.getPath())) {
                        map.remove(info.getPath());
                    }

                    if (getCount() != 0 && getCount() != map.size()) {
                        callback.onShow(true, false);
                    }

                    if (map.isEmpty()) {
                        callback.onShow(false, false);
                    }
                }
            }
        });

        if (map.containsKey(info.getPath())) {
            holder.uncompress_checkbox.setChecked(true);
        } else {
            holder.uncompress_checkbox.setChecked(false);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int type = UncompressInfo.TYPE_DIR;
        UncompressInfo info = data.get(position);
        type = info.getType();
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return UncompressInfo.TYPE_COUNT;
    }

    static class ViewHolder {
        CheckBox uncompress_checkbox;
        ImageView uncompress_icon;
        TextView uncompress_title;
        TextView uncompress_path;
        TextView uncompress_size;
        TextView uncompress_date;
    }
}