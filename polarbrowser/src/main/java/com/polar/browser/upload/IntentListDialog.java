package com.polar.browser.upload;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by duan on 16/9/9.
 */
public class IntentListDialog extends CommonBaseDialog implements AdapterView.OnItemClickListener {

    protected String[] mItems;

    protected ListView mListView;

    protected DialogListAdapter mAdapter;

    protected AdapterView.OnItemClickListener mItemClickListener;

    protected boolean mSingleLine = false;

    protected TextUtils.TruncateAt mEllipsize = null;

    protected int mSelectedIndex;

    private boolean isDismissByBackPress;

    protected Context mContext;

    public IntentListDialog(Context context) {
        super(context, R.style.common_dialog);
        mContext = context;
        initCenterView();
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialog_anim_show_from_bottom);
    }

    public void setSingleLine(boolean singleLine) {
        mSingleLine = singleLine;
    }

    public void setEllipsize(TextUtils.TruncateAt where) {
        mEllipsize = where;
    }

    protected void initCenterView() {
        mListView = new ListView(getContext());
//        mListView.setDivider(getContext().getResources().getDrawable(R.color.common_grey_color1));
//        mListView.setDividerHeight((int) getContext().getResources().getDimension(R.dimen.common_divider_width));
        // 12-05; fix bug 1834
        mListView.setDivider(null);
        mListView.setSelector(R.drawable.empty_selector);
        mAdapter = new DialogListAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        setContentView(mListView);
    }

    public ListView getListView() {
        return mListView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(parent, view, position, id);
        }
        mSelectedIndex = position;
        dismiss();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setItems(String[] items, int selectedIndex) {
        mItems = items;
        mSelectedIndex = selectedIndex;
        mAdapter.notifyDataSetChanged();
    }

    protected View getListRowView(int position, View convertView, ViewGroup parent) {
        convertView = getLayoutInflater().inflate(R.layout.common_dialog_list_row2, null);
        // 设置背景色，根据mItems.length
        View root = convertView.findViewById(R.id.root);
        View line = convertView.findViewById(R.id.line);
        View blank = convertView.findViewById(R.id.blank);
        if (mItems.length == 1) {
            // case 1 ,只有1条目
//            root.setBackgroundResource(R.drawable.list_row_one_selector);
            root.setBackgroundResource(R.drawable.common_list_row1);
            line.setVisibility(View.GONE);
        } else if (mItems.length == 2) {
            // case 2 ,只有2条目
            if (position == 0) {
//                root.setBackgroundResource(R.drawable.list_row_top_selector);
                root.setBackgroundResource(R.drawable.common_list_row1);
                line.setVisibility(View.VISIBLE);
            } else if (position == 1) {
//                root.setBackgroundResource(R.drawable.list_row_bottom_selector);
                root.setBackgroundResource(R.drawable.common_list_row1);
                line.setVisibility(View.GONE);
            }
        } else {
            // 条目>=3
            if (position == 0) {
//                root.setBackgroundResource(R.drawable.list_row_top_selector);
                root.setBackgroundResource(R.drawable.common_list_row1);
                blank.setVisibility(View.GONE);
                line.setVisibility(View.VISIBLE);
            } else if (position == mItems.length - 1) {
//                root.setBackgroundResource(R.drawable.list_row_bottom_selector);
                root.setBackgroundResource(R.drawable.common_list_row1);
                blank.setVisibility(View.GONE);
                line.setVisibility(View.INVISIBLE);
            } else if (position == mItems.length - 2) {
//                root.setBackgroundResource(R.drawable.list_row_bottom_selector);
                root.setBackgroundResource(R.drawable.common_list_row1);
                blank.setVisibility(View.VISIBLE);
                line.setVisibility(View.INVISIBLE);
            } else {
                root.setBackgroundResource(R.drawable.common_list_row1);
                blank.setVisibility(View.GONE);
                line.setVisibility(View.VISIBLE);
            }
        }
        TextView title = (TextView) convertView.findViewById(R.id.tv_title);
        title.setText(mItems[position]);
        title.setTextColor(convertView.getResources().getColor(R.color.common_font_color_selector_2));
        return convertView;
    }

    public void setItems(String[] items) {
       setItems(items, -1);
    }

    @Override
    public void dismiss() {
        if (mSelectedIndex == -1) {
            // 点击返回,消失
            isDismissByBackPress = true;
        }
        super.dismiss();
    }

    public boolean isDismissByBackPress() {
        return isDismissByBackPress;
    }

    @Override
    public void show() {
        super.show();
        WindowManager windowManager = ((Activity)mContext).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int)display.getWidth(); //设置宽度
        getWindow().setAttributes(lp);
    }


    class DialogListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems == null ? null : mItems[position];
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

}
