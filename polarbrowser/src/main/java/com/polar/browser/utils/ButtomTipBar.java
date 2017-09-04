package com.polar.browser.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.polar.browser.R;

/**
 * Created by FKQ on 2016/10/8.
 */

public class ButtomTipBar {

    public static void showButtomTipBar(Context context, final RelativeLayout buttomTipLayout , final onTipBtnListener listener, String tipResId, String settingResId) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View tipLayout = inflater.inflate(R.layout.view_bottom_tip, null);
        tipLayout.setLayoutParams(lp);
        View spaceView = tipLayout.findViewById(R.id.space_view);
        TextView textViewTip = (TextView) tipLayout.findViewById(R.id.tv_bottom_tip);
        textViewTip.setText(tipResId);
        Button textViewSetting = (Button) tipLayout.findViewById(R.id.tv_bottom_tip_setting);
        textViewSetting.setText(settingResId);
        Button imageViewClose = (Button) tipLayout.findViewById(R.id.tv_bottom_tip_close);
        textViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttomTipLayout.removeView(tipLayout);
                listener.onClickSetting(v);
            }
        });
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttomTipLayout.removeView(tipLayout);
                listener.onClickClose(v);
            }
        });

        spaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttomTipLayout.removeView(tipLayout);
                listener.onClickClose(v);
            }
        });

        buttomTipLayout.addView(tipLayout);
        buttomTipLayout.setVisibility(View.VISIBLE);
    }

    public interface onTipBtnListener{
        void onClickSetting(View v);
        void onClickClose(View v);
    }

}
