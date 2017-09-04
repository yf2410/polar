package com.polar.browser.vclibrary.bean.login;

/**
 * Created by FKQ on 2017/3/30.
 */

public class ShadowAccountJson {

//    {
//        "p": "ios",   客户端平台信息（ios ， android）
//        "a": "vc",    应用名称：目前定义的有 （vc  insta  download ）如果有新的需要核对后在使用
//        "b": "b",     保留字段未使用
//        "c": "c"      保留字段未使用（客户端的IMEI）
//    }

    private String a;
    private String b;
    private String c;
    private String p;

    public ShadowAccountJson() {
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ShadowAccountJson{");
        sb.append("a='").append(a).append('\'');
        sb.append(", b='").append(b).append('\'');
        sb.append(", c='").append(c).append('\'');
        sb.append(", p='").append(p).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
