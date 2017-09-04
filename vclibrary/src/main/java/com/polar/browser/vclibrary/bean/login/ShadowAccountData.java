package com.polar.browser.vclibrary.bean.login;

/**
 * Created by FKQ on 2017/3/30.
 */

public class ShadowAccountData {

//    {
//        "errorcode": 0,       返回的服务端错误编号，0正常处理，其他为异常信息
//        "msg": "Successful",  处理结果信息
//        "success": true,      成功标志（true/false）
//        "lsid": "",           业务扩展保留字段，现在未启用，下发为( “”)
//        "ltoken": "b76cb4acc4d03e18fdcaa69093c876fd5aaf463de65c9f2732030e72cfe53c371bb49aa227fffe7239ea3a6aefa99687a7b603ae523917c223f578be22c28eb8d0c3990e08cfd96d"
//          服务端给客户端的影子token，以后请求都需要带着该token信息与服务端进行交互
//    }

    public int errorcode;
    public String msg;
    public boolean success;
    public String lsid;
    public String ltoken;

    public ShadowAccountData() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ShadowAccountData{");
        sb.append("errorcode=").append(errorcode);
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", success=").append(success);
        sb.append(", lsid='").append(lsid).append('\'');
        sb.append(", ltoken='").append(ltoken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
