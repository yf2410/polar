package com.polar.browser.utils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yxx on 2017/3/2.
 */

public class StringUtils {

    //高亮
    public static SpannableStringBuilder highlight(String text, String target, int color) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        CharacterStyle span = null;
        target = target.trim();
        Pattern p = Pattern.compile(target,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        while (m.find()) {
            span = new ForegroundColorSpan(color);// 需要重复！
            spannable.setSpan(span, m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    //生成json对象
    public static JSONObject generateJson(String key, String url, String language, String area) {
        //判断为null的变量不参加json生成。
        String jsonresult = "";//定义返回字符串
        JSONObject object = new JSONObject();//创建一个总的对象，这个对象对整个json串
        try {
            if (url != null){
                object.put(key, url);
            }
//            object.put("language", language);
//            object.put("area", area);
            return object;//生成返回字符串
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getRandom(int max, int min){
        return (int)(Math.random()*max+1);
    }

    public  static String getFirstChar(String str) {
        if (str != null) {
            return str.substring(0,1).toUpperCase();
        }
        return "";
    }

    /**
     * 传入一个字符串，返回出现的第一个汉字。
     * 但是过滤了某些字，如  手 机  大 小  电 新  这些字。
     * @param str
     * @return  ch
     */
    public static String getFirstChineseChar(String str) {
        String ch = "";
        // 如果是字母和数字或者纯字母数字的，取第一个就ok，如果取到的是字符，要转成大写。
        if (RegularUtils.isAllLettersAndNum(str)) {
            ch = RegularUtils.getUpperLetter(str.substring(0, 1));
        } else {
            for (int i = 0; i < str.length(); i++) {
                ch = str.substring(i, i + 1);
                if (RegularUtils.isCheneseChar(ch)) {
                    if ((TextUtils.equals(ch, "手") ||TextUtils.equals(ch, "机"))&& str.startsWith("手机")) {
                        continue;
                    }else if ((TextUtils.equals(ch, "小")) || (TextUtils.equals(ch, "大"))||(TextUtils.equals(ch, "电"))||(TextUtils.equals(ch, "新"))) {
                        continue;
                    }
                    return ch;
                }
            }
        }
        return ch;
    }

    /**
     * 判断url是不是以汉字开头的，做个简单的url限制。
     * @param url
     * @return
     */
    public static boolean isStartWithChinese(String url) {
        return RegularUtils.isCheneseChar(url.substring(0, 1));
    }
}
