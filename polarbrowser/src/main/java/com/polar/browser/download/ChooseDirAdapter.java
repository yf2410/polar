package com.polar.browser.download;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polar.browser.R;


/**
 * Created by yd_lzk on 2016/11/14.
 */

public class ChooseDirAdapter extends RecyclerView.Adapter<ChooseDirAdapter.VH>{

    private String[] data;
    private Context mContext;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        public void onItemClick(String selectedPath);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public ChooseDirAdapter(String[] data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    public void setData(String[] data){
        this.data = data;
        this.notifyDataSetChanged();
    }


    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(View.inflate(mContext, R.layout.choose_dir_item,null));
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        if(data[position]!=null){
            holder.mTextView.setText(data[position]);
            if(position == getItemCount()-1){
                holder.mTextView.setTextColor(mContext.getResources().getColor(R.color.base_txt));
            }else{
                holder.mTextView.setTextColor(mContext.getResources().getColor(R.color.black));
            }
            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StringBuilder currentPath = new StringBuilder();
                    final int index = position;
                    for(int i=0; i<=index; i++){
                        currentPath.append(data[i]);
                        if(i != index){
                            currentPath.append("/");
                        }
                    }
                    listener.onItemClick(currentPath.toString());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data!=null ? data.length : 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView mTextView;
        public VH(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.cdi_tv_directory);
        }
    }


}
