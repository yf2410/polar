package com.polar.browser.vclibrary.bean.login;

/**
 * Created by FKQ on 2017/4/1.
 */

public class ServerUserAccountData {

    /*      "hPortrait": "http://vc-file-bucket.s3-accelerate.amazonaws.com/201611290226344048.jpg",  头像地址
            "age": 0,
            "gender": "男",
            "nickname": "克勤勤",
            "name": "克勤",
            "hometown": "中国",
            "phoneNo": "15522233333",
            "other1": "other1",
            "other2": "其他",
            "other3": "其他",
            "other4": "其他",
            "other5": "其他",
            "other6": "其他",
            "other7": "其他",
            "other8": "其他",
            "msg": "success",
            "errorcode": 0,
            "success": true,
            "token":"2fed658fb24717b34fd8e115b56db61557a36726f20d73a619fd584a94c18e16ede2ab9c02de4fc2c014cbe9e3dc3de04e249bcccf28cf44f11c7640cd508e60"
*/

    public int errorcode;
    public boolean success;
    public String msg;
    public String hPortrait;
    public String token;
    public int age;
    public String gender;
    public String nickname;
    public String name;
    public String birthday;
    public String hometown;
    public String phoneNo;
    public String other1;
    public String other2;
    public String other3;
    public String other4;
    public String other5;
    public String other6;
    public String other7;
    public String other8;

    public ServerUserAccountData() {
    }
}
