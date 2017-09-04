package com.polar.browser.vclibrary.bean.login;

/**
 * Created by FKQ on 2017/3/30.
 */

public class FbAccountData {
/*
    {
        "success": true,        成功标志（true/false）
            "errorcode": 0,     返回的服务端错误编号，0正常处理，其他为异常信息
            "sid": "",          保留字端未启用，目前下发为（“”）
            "lsid": "",         保留字端未启用，目前下发为（“”）
            用户登录后得到的token
            "token": "a57686fe02e57cbf209195d0f93f7081674e7c7a54df86f672eb1afd26cbb4f90b3bc014082c47088690098e4b2895996c2ff700f7189ce792459f558b0862188cce831981132b6d",
            "ltoken": "",       用户本地的影子token，（客户端保存好此字段，第一次绑定时会下发此字段，第二次用facebook登录时，服务端不会在返回此字段）
            "msg": "Successful.",   处理结果信息
            "email": "",            Facebook 返回的email
            "gender": "M",          Facebook 返回的gender
            "fbname": "Zhi Tang",   Facebook 返回的fbname
            "ageRange": {
        "min": 21   Facebook 返回的年龄范围，有min 和 max 对应的数据类型都为int
    },
        "locale": "zh_CN",  Facebook  返回的语言种类
        Facebook 返回的头像地址
            "picture": "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/11046793_121848288188775_3354262791706675539_n.jpg?oh=f3a7399d0abea78d3ba9a168b1033983&oe=59452EFD",
            "timeZone": 8       Facebook 返回的时区
    }*/


    public boolean success;
    public int errorcode;
    public String sid;
    public String lsid;
    public String token;
    public String ltoken;
    public String msg;
    public String email;
    public String gender;
    public String fbname;
    public String locale;
    public String picture;
    public int timeZone;
    public AgeRange ageRange;
    public String birthday;
    public String name;
    public FbAccountData() {
    }

    public static class AgeRange {
        public int min;
        public int max;

        @Override
        public String toString() {
            return "AgeRange{" +
                    "min=" + min +
                    ", max=" + max +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FbAccountData{" +
                "success=" + success +
                ", errorcode=" + errorcode +
                ", sid='" + sid + '\'' +
                ", lsid='" + lsid + '\'' +
                ", token='" + token + '\'' +
                ", msg='" + msg + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", fbname='" + fbname + '\'' +
                ", locale='" + locale + '\'' +
                ", picture='" + picture + '\'' +
                ", timeZone=" + timeZone +
                ", ageRange=" + (ageRange != null ? ageRange.toString() : "null" )+
                '}';
    }
}
