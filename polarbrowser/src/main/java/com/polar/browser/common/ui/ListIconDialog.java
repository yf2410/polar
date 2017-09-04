package com.polar.browser.common.ui;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本图标列表对话框
 */
public class ListIconDialog extends CommonBaseDialog implements OnItemClickListener {

    private static final String TAG = ListIconDialog.class.getSimpleName();

    protected List<Item> mItems;

    protected ListView mListView;

    protected DialogListAdatper mAdapter;

    protected OnItemClickListener mItemClicklistener;

    protected boolean mSingleLine = false;

    protected TruncateAt mEllipsize = null;

    protected int mSelectedIndex;

    public ListIconDialog(Context context) {
        super(context, R.style.common_dialog);
        initCenterView();
    }

    public void setSingleLine(boolean singleLine) {
        mSingleLine = singleLine;
    }

    public void setEllipsize(TruncateAt where) {
        mEllipsize = where;
    }

    protected void initCenterView() {

        View view = View.inflate(getContext(), R.layout.search_engine_popup, null);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setDivider(null);

        mListView.setSelector(R.drawable.empty_selector);
        mAdapter = new DialogListAdatper();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        setContentView(view);
    }

    public ListView getListView() {
        return mListView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mItemClicklistener != null) {
            mItemClicklistener.onItemClick(parent, view, position, id);
        }
        mSelectedIndex = position;
        dismiss();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClicklistener = listener;
    }

    public void setItems(ArrayList<Item> items, int selectedIndex) {
        mItems = items;
        mSelectedIndex = selectedIndex;
        mAdapter.notifyDataSetChanged();
    }

    protected View getListRowView(int position, View convertView, ViewGroup parent) {
        convertView = getLayoutInflater().inflate(R.layout.select_engin_item_layout, null);
        // 设置背景色，根据mItems.length
        View root = convertView.findViewById(R.id.root);
        View line = convertView.findViewById(R.id.line);
        if (mItems.size() == 1) {
            // case 1 ,只有1条目
            root.setBackgroundResource(R.drawable.list_row_one_selector);
            line.setVisibility(View.GONE);
        } else if (mItems.size() == 2) {
            // case 2 ,只有2条目
            if (position == 0) {
                root.setBackgroundResource(R.drawable.list_row_top_selector);
                line.setVisibility(View.VISIBLE);
            } else if (position == 1) {
                root.setBackgroundResource(R.drawable.list_row_bottom_selector);
                line.setVisibility(View.GONE);
            }
        } else {
            // 条目>=3
            if (position == 0) {
                root.setBackgroundResource(R.drawable.list_row_top_selector);
                line.setVisibility(View.VISIBLE);
            } else if (position == mItems.size() - 1) {
                root.setBackgroundResource(R.drawable.list_row_bottom_selector);
                line.setVisibility(View.INVISIBLE);
            } else {
                root.setBackgroundResource(R.drawable.common_list_row1);
                line.setVisibility(View.VISIBLE);
            }
        }



//        if(position == 0){
//            ((LinearLayout.LayoutParams)root.getLayoutParams()).setMargins(0,DensityUtil.dip2px(getContext(),10),0,0);
//        }else if(position == mItems.size()-1){
//            ((LinearLayout.LayoutParams)root.getLayoutParams()).setMargins(0,0,0,DensityUtil.dip2px(getContext(),10));
//        }else{
//            ((LinearLayout.LayoutParams)root.getLayoutParams()).setMargins(0,0,0,0);
//        }

        TextView title = (TextView) convertView.findViewById(R.id.tv_title);
        ImageView icon = (ImageView) convertView.findViewById(R.id.search_engine_icon);
        Item item = mItems.get(position);
        title.setText(item.itemTitle);
        if(item.imgId !=Item.EMPTY_ID)
            icon.setImageResource(item.imgId);
        else{
            int defaultIconId = SearchUtils.getDefaultEngineIconByName(item.itemTitle);
            ImageLoadUtils.loadImage(getContext(),item.imgUrl,icon,R.drawable.engin_default_bg,defaultIconId);
        }
        if (mSelectedIndex == position) {
            convertView.findViewById(R.id.iv_check).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.iv_check).setVisibility(View.INVISIBLE);
        }
        ((ImageView) convertView.findViewById(R.id.iv_check)).setImageResource(R.drawable.list_check);
        title.setTextColor(convertView.getResources().getColor(R.color.common_font_color_selector_2));
        return convertView;
    }

    class DialogListAdatper extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems == null ? null : mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getListRowView(position, convertView, parent);
        }
    }

    public class Item {
        private static final int EMPTY_ID = -1;
        public Item(String itemTitle, int imgId) {
            this.itemTitle = itemTitle;
            this.imgId = imgId;
        }

        public Item(String itemTitle, String imgUrl) {
            this.itemTitle = itemTitle;
            this.imgUrl = imgUrl;
        }

        public String itemTitle;
        public int imgId = EMPTY_ID;
        public String imgUrl;
    }

}
