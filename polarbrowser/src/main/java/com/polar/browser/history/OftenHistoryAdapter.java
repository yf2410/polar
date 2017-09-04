package com.polar.browser.history;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.polar.browser.R;
import com.polar.browser.vclibrary.bean.db.SearchRecord;
import java.util.List;

public class OftenHistoryAdapter extends BaseAdapter {

	private List<SearchRecord> objects;
	private LayoutInflater layoutInflater;
	private IOftenHistoryItemClik mIOftenHistoryItemClik;
	private Context mContext;

	public OftenHistoryAdapter(Context context, List<SearchRecord> objects, IOftenHistoryItemClik oftenHistoryItemClik) {
		this.layoutInflater = LayoutInflater.from(context);
		this.objects = objects;
		this.mIOftenHistoryItemClik = oftenHistoryItemClik;
		this.mContext = context;
	}

	public void setList(List<SearchRecord> objects) {
		if (this.objects != null) {
			this.objects.clear();
		}
		this.objects = objects;
	}

	public void remove(int position) {
		if(objects != null){
			this.objects.remove(position);
		}
	}

	public void removeAll(List<SearchRecord> data) {
		if(objects != null){
			this.objects.removeAll(data);
		}
	}

	@Override
	public int getCount() {
		return objects != null ? objects.size() : 0;
	}

	@Override
	public SearchRecord getItem(int position) {
		return objects != null ? objects.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		ImgOnClick imgOnClick;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.item_search_record, null);
			viewHolder.tvUrl = (TextView) convertView.findViewById(R.id.search_record_content);
			viewHolder.ivImg = (ImageView) convertView.findViewById(R.id.search_record_img_right);
			viewHolder.ivImgIcon = (ImageView) convertView.findViewById(R.id.search_record_img_left);
			imgOnClick = new ImgOnClick();
			viewHolder.ivImg.setOnClickListener(imgOnClick);
			imgOnClick.setPosition(position);
			convertView.setTag(viewHolder.ivImg.getId(),imgOnClick);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			imgOnClick = (ImgOnClick) convertView.getTag(viewHolder.ivImg.getId());
		}
		imgOnClick.setPosition(position);
		SearchRecord historyOftenInfo = objects.get(position);
		if (!TextUtils.isEmpty(historyOftenInfo.getSearchAddr())) {
			viewHolder.tvUrl.setText(historyOftenInfo.getSearchAddr());
		}
		if (historyOftenInfo.getType() == SearchRecord.GO) {
			viewHolder.ivImgIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_default));
		} else if (historyOftenInfo.getType() == SearchRecord.SEARCH) {
			viewHolder.ivImgIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.search_icon));
		}
		return convertView;
	}

	protected class ViewHolder {
		private TextView tvUrl;
		private ImageView ivImg;
		private ImageView ivImgIcon;
	}

	class ImgOnClick implements View.OnClickListener{
		private int position;

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if(getItem(position) != null){
				mIOftenHistoryItemClik.onItemClick(getItem(position).getSearchAddr());
			}
		}
	}
}
