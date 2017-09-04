package com.polar.browser.setting;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by FKQ on 2016/11/9.
 */

public class SubmitFeedBackDialog extends CommonBaseDialog {

    public SubmitFeedBackDialog(Context context, int theme) {
        super(context, R.style.common_dialog);
    }

    public SubmitFeedBackDialog(Context context,int textStr,int image) {
        super(context, R.style.common_dialog);
        setContentView(R.layout.dialog_feed_back);
        TextView dialogDescription = (TextView) findViewById(R.id.description);
        dialogDescription.setText(context.getResources().getString(textStr));

        setCanceledOnTouchOutside(false);
        ImageView dialogIcon = (ImageView) findViewById(R.id.icon_dialog);
        dialogIcon.setImageDrawable(context.getResources().getDrawable(image));
        Animation animation = AnimationUtils.loadAnimation(getContext(),
                R.anim.loading);
        animation.setInterpolator(AnimationUtils.loadInterpolator(
                getContext(), android.R.anim.linear_interpolator));
        dialogIcon.startAnimation(animation);
    }
}
