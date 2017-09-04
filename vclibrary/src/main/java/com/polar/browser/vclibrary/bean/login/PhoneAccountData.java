package com.polar.browser.vclibrary.bean.login;

/**
 * Created by FKQ on 2017/3/30.
 */

public class PhoneAccountData {

    /*{
        "success": true,
            "errorcode": 0,
            "token": "a2da19bbee1e74ee126b01d3caed7103f264ae99031e55eb32030e72cfe53c3799e1958ea9b56f79a7bf6f0a6c5e0a6b24473a5c85051b896f65b9c281605302b048ab7c2559878f",
            "ltoken": "883b7bc50570aa30fdcaa69093c876fd5aaf463de65c9f2732030e72cfe53c374d273a64b30910788c60fc49db982014acc0ed62c40a0995f03077bdbf1dc15590f6020efde87acc",
            "msg": "Successful.",
            "pn": "+8615311458441",
            "sid": "",
            "lsid": ""
    }*/

    public boolean success;
    public int errorcode;
    public String token;
    public String ltoken;
    public String msg;
    public String pn;
    public String picture;
    public String sid;
    public String lsid;

    public PhoneAccountData() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PhoneAccountData{");
        sb.append("success=").append(success);
        sb.append(", errorcode=").append(errorcode);
        sb.append(", token='").append(token).append('\'');
        sb.append(", ltoken='").append(ltoken).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", pn='").append(pn).append('\'');
        sb.append(", picture='").append(picture).append('\'');
        sb.append(", sid='").append(sid).append('\'');
        sb.append(", lsid='").append(lsid).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
