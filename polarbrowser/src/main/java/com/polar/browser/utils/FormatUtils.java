package com.polar.browser.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yd_lp on 2016/10/21.
 */

final public class FormatUtils {
    private static final String DEFAULT_DATE_FORMAT = "yyyy.M.d";
    private FormatUtils() {

    }


    /**
     * 格式化日期
     * */
    public static String formatDate(long timeMillis) {
        return CalenderformatDate(timeMillis);
    }

    /**
     * 格式化日期
     * */

    public static String CalenderformatDate(long time) {
        //使用默认时区和语言环境获得一个日历
        Calendar cale = Calendar.getInstance();
        if((time+"").length() == 10){  //10位，毫秒应该为13位
            time = time * 1000L;
        }
        cale.setTimeInMillis(time);
        //将Calendar类型转换成Date类型
        Date tasktime = cale.getTime();
        //设置日期输出的格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //格式化输出
        return df.format(tasktime);
    }

    /**
     * 格式化文件大小
     * */
    public static String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String result;
        String wrongSize="0B";
        if(size==0){
            return wrongSize;
        }
        if (size < 1024){
            result = df.format((double) size) + "B";
        }
        else if (size < 1048576){
            result = df.format((double) size / 1024) + "KB";
        }
        else if (size < 1073741824){
            result = df.format((double) size / 1048576) + "MB";
        }
        else{
            result = df.format((double) size / 1073741824) + "GB";
        }
        return result;
    }

}
