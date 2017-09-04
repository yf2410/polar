package com.polar.browser.vclibrary.bean;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by saifei on 16/12/6.
 */
public class SearchEngineList {


    /**
     * version : 1481081988290
     * dataList : [{"id":5,"engineName":"GOOGLE","enginePosition":"1","enginePic":"http://vc-file-bucket.s3-accelerate.amazonaws.com/bb9abf1f44aa4ee799edf95a9a2e5fa8.jpg","engineKeyWord":"P","url":"www.google.com","isDefault":"1","paramMap":[{"name":"test3243","value":"terw12jkkkdfsf"}]},{"id":7,"engineName":"搜狗","enginePosition":"2","enginePic":"http://vc-file-bucket.s3-accelerate.amazonaws.com/8056b89cd14749dfb42486a2fac9f0c2.jpg","engineKeyWord":"a","url":"www.sougou.com","isDefault":"1"},{"id":8,"engineName":"360","enginePosition":"4","enginePic":"http://vc-file-bucket.s3-accelerate.amazonaws.com/02f64c8335024ed2a75ede1ab581ebba.jpg","engineKeyWord":"b","url":"www.260.com","isDefault":"0"},{"id":9,"engineName":"必应","enginePosition":"6","enginePic":"http://vc-file-bucket.s3-accelerate.amazonaws.com/5d53cfc3e9564dac8545f616acecbdef.jpg","engineKeyWord":"l","url":"www.bing.com","isDefault":"0","paramMap":[{"name":"cid","value":"dfs4546"}]},{"id":10,"engineName":"雅虎","enginePosition":"8","enginePic":"http://vc-file-bucket.s3-accelerate.amazonaws.com/80a5fd74845744089ff60b04ffc50a59.jpg","engineKeyWord":"y","url":"www.yahu.com","isDefault":"0"},{"id":11,"engineName":"百度","enginePosition":"9","enginePic":"http://vc-file-bucket.s3-accelerate.amazonaws.com/bafd20a2510c49d1b9fd6c565ca31556.jpg","engineKeyWord":"b","url":"www.baidu.com","isDefault":"0"}]
     */
    private String version;
    private List<DataListBean> dataList;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<DataListBean> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataListBean> dataList) {
        this.dataList = dataList;
    }

    public static class DataListBean implements Comparable<DataListBean> {
        /**
         * id : 5
         * engineName : GOOGLE
         * enginePosition : 1
         * enginePic : http://vc-file-bucket.s3-accelerate.amazonaws.com/bb9abf1f44aa4ee799edf95a9a2e5fa8.jpg
         * engineKeyWord : P
         * url : www.google.com
         * isDefault : 1
         * paramMap : [{"name":"test3243","value":"terw12jkkkdfsf"}]
         */

        private int id;
        private String engineName;
        private String enginePosition;
        private String enginePic;
        private String engineKeyWord;
        private String url;
        private String redirectUrl;
        private String isDefault;
        private List<ParamMapBean> paramMap;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEngineName() {
            return engineName;
        }

        public void setEngineName(String engineName) {
            this.engineName = engineName;
        }

        public String getEnginePosition() {
            return enginePosition;
        }

        public void setEnginePosition(String enginePosition) {
            this.enginePosition = enginePosition;
        }

        public String getEnginePic() {
            return enginePic;
        }

        public void setEnginePic(String enginePic) {
            this.enginePic = enginePic;
        }

        public String getEngineKeyWord() {
            return engineKeyWord;
        }

        public void setEngineKeyWord(String engineKeyWord) {
            this.engineKeyWord = engineKeyWord;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(String isDefault) {
            this.isDefault = isDefault;
        }

        public List<ParamMapBean> getParamMap() {
            return paramMap;
        }

        public void setParamMap(List<ParamMapBean> paramMap) {
            this.paramMap = paramMap;
        }

        @Override
        public int compareTo(@NonNull DataListBean another) {
            int anotherPosition = Integer.parseInt(another.enginePosition);
            int thisPosition = Integer.parseInt(this.enginePosition);
            if(thisPosition>anotherPosition)
                return 1;
            else
                return -1;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public static class ParamMapBean {
            /**
             * name : test3243
             * value : terw12jkkkdfsf
             */

            private String name;
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
