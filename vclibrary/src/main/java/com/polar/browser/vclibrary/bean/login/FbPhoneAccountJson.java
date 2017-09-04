package com.polar.browser.vclibrary.bean.login;

/**
 * Created by FKQ on 2017/3/30.
 */

public class FbPhoneAccountJson {

    /*{
        "input_token": "EAAK1KJJDKS0BAFJzMPi3NWOeZCb1tFKBZC9jOmIfPQ2uwqZApSTCn8WJaNjhOMw2J0v4VWc3oA3ykAtDwMftWRDiE8AEbta88M4of3Gn8ZC9MwzJD22w784ewZCqGQepZAKFNaUUTQbI6wf0rTYHc7ZAanf2cifiVtEHGNHUplmN0l1gTxHeezuva9i2Nsr9k0ZD",
            "token": "6d7cc24888ed6c4f0bd457dfa85ec4955783989ad3fe31c009d25c3d5de42225edff1205ec8bae0bd4e62c1ee4874fd4ff4931dd118fa3563c1bc67852599e8a",
            "type": "ios",
            "appName": "vc"
    }*/

    private String input_token;
    private String token;
    private String type;
    private String appName;

    public FbPhoneAccountJson() {
    }

    public String getInput_token() {
        return input_token;
    }

    public void setInput_token(String input_token) {
        this.input_token = input_token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return "FbPhoneAccountJson{" +
                "input_token='" + input_token + '\'' +
                ", token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", appName='" + appName + '\'' +
                '}';
    }
}
