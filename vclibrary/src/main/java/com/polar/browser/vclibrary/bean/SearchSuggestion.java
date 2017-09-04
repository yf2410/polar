package com.polar.browser.vclibrary.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by saifei on 16/11/29.
 */

public class SearchSuggestion implements Serializable {

    /**
     * _meta : {"status":"SUCCESS","rid":"2dd1c59c-300f-4e6f-ce76-281e03734e77","count":2,"ttl":3600}
     * records : [{"suggestion":{"id":1727374,"queryFragment":"google","suggRank":0,"suggestion":"Google.com","cardCount":6,"cards":[{"id":"manualFDF5D407AA88643AD15B8D63629AA9EB","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://www.google.com/"}],"card_format":"inline","cardRank":1},{"id":"manual06116C107BBA85283BF699F86CE100B7","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://www.yahoo.com/"}],"card_format":"inline","cardRank":2},{"id":"manual9CAAA2B811E4358BDE36A8FC29693207","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"http://www.bing.com/"}],"card_format":"inline","cardRank":3},{"id":"manual9F54AB1A64D41AB4A930DDDB23DA034F","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://duckduckgo.com/"}],"card_format":"inline","cardRank":4},{"id":"manual6F8156E975F26D8F791832A9568F3A72","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://startpage.com/"}],"card_format":"inline","cardRank":5},{"id":"manualAF54C2DE3A97F0B4650493DA6CD7A934","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"http://www.ask.com/"}],"card_format":"inline","cardRank":6}]}},{"suggestion":{"id":3884940,"queryFragment":"google","suggRank":1,"suggestion":"Google","cardCount":1,"cards":[{"id":"42mattersandroidcom.google.android.googlequicksearchbox","x1":"http://d1jh24layu79ld.cloudfront.net/androidapp/151116/c/62cc_inline_android_app_42mattersandroidcom.google.android.googlequicksearchbox_1479196542545_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/androidapp/151116/c/62cc_inline_android_app_42mattersandroidcom.google.android.googlequicksearchbox_1479196542545_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/androidapp/151116/c/62cc_inline_android_app_42mattersandroidcom.google.android.googlequicksearchbox_1479196542545_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/androidapp/151116/c/62cc_inline_android_app_42mattersandroidcom.google.android.googlequicksearchbox_1479196542545_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url_market","action_target":"https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox&referrer=utm_source%3D42matters.com%26utm_medium%3Dapi"},{"action_type":"web_url","action_target":"https://www.google.com/search/about/"},{"action_type":"deep_link","action_target":"market://details?id=com.google.android.googlequicksearchbox"},{"action_type":"app_preview","action_target":"https://www.youtube.com/embed/pPqliPzHYyc?ps=play&vq=large&rel=0&autohide=1&showinfo=0&autoplay=1"}],"card_format":"inline","cardRank":1}]}}]
     */

    private MetaBean _meta;
    private List<RecordsBean> records;

    public MetaBean get_meta() {
        return _meta;
    }

    public void set_meta(MetaBean _meta) {
        this._meta = _meta;
    }

    public List<RecordsBean> getRecords() {
        return records;
    }

    public void setRecords(List<RecordsBean> records) {
        this.records = records;
    }

    public static class MetaBean {
        /**
         * status : SUCCESS
         * rid : 2dd1c59c-300f-4e6f-ce76-281e03734e77
         * count : 2
         * ttl : 3600
         */

        private String status;
        private String rid;
        private int count;
        private int ttl;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRid() {
            return rid;
        }

        public void setRid(String rid) {
            this.rid = rid;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }
    }

    public static class RecordsBean {
        /**
         * suggestion : {"id":1727374,"queryFragment":"google","suggRank":0,"suggestion":"Google.com","cardCount":6,"cards":[{"id":"manualFDF5D407AA88643AD15B8D63629AA9EB","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://www.google.com/"}],"card_format":"inline","cardRank":1},{"id":"manual06116C107BBA85283BF699F86CE100B7","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://www.yahoo.com/"}],"card_format":"inline","cardRank":2},{"id":"manual9CAAA2B811E4358BDE36A8FC29693207","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"http://www.bing.com/"}],"card_format":"inline","cardRank":3},{"id":"manual9F54AB1A64D41AB4A930DDDB23DA034F","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://duckduckgo.com/"}],"card_format":"inline","cardRank":4},{"id":"manual6F8156E975F26D8F791832A9568F3A72","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://startpage.com/"}],"card_format":"inline","cardRank":5},{"id":"manualAF54C2DE3A97F0B4650493DA6CD7A934","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"http://www.ask.com/"}],"card_format":"inline","cardRank":6}]}
         */

        private SuggestionBean suggestion;

        public SuggestionBean getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(SuggestionBean suggestion) {
            this.suggestion = suggestion;
        }

        public static class SuggestionBean {
            /**
             * id : 1727374
             * queryFragment : google
             * suggRank : 0
             * suggestion : Google.com
             * cardCount : 6
             * cards : [{"id":"manualFDF5D407AA88643AD15B8D63629AA9EB","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://www.google.com/"}],"card_format":"inline","cardRank":1},{"id":"manual06116C107BBA85283BF699F86CE100B7","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/0/4aA0_inline_manual_06116C107BBA85283BF699F86CE100B7_1478577050521_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://www.yahoo.com/"}],"card_format":"inline","cardRank":2},{"id":"manual9CAAA2B811E4358BDE36A8FC29693207","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/b/4CE1_inline_manual_9CAAA2B811E4358BDE36A8FC29693207_1478577492558_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"http://www.bing.com/"}],"card_format":"inline","cardRank":3},{"id":"manual9F54AB1A64D41AB4A930DDDB23DA034F","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/7/cec4_inline_manual_9F54AB1A64D41AB4A930DDDB23DA034F_1478577498007_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://duckduckgo.com/"}],"card_format":"inline","cardRank":4},{"id":"manual6F8156E975F26D8F791832A9568F3A72","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/d/a171_inline_manual_6F8156E975F26D8F791832A9568F3A72_1478577366344_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"https://startpage.com/"}],"card_format":"inline","cardRank":5},{"id":"manualAF54C2DE3A97F0B4650493DA6CD7A934","x1":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x1.jpeg","x1_h":40,"x1_w":170,"x2":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x2.jpeg","x2_h":80,"x2_w":340,"x3":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x3.jpeg","x3_h":106,"x3_w":453,"x4":"http://d1jh24layu79ld.cloudfront.net/manual/081116/a/CeeD_inline_manual_AF54C2DE3A97F0B4650493DA6CD7A934_1478577537841_x4.jpeg","x4_h":160,"x4_w":680,"actions":[{"action_type":"web_url","action_target":"http://www.ask.com/"}],"card_format":"inline","cardRank":6}]
             */

            private int id;
            private String queryFragment;
            private int suggRank;
            private String suggestion;
            private int cardCount;
            private List<CardsBean> cards;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getQueryFragment() {
                return queryFragment;
            }

            public void setQueryFragment(String queryFragment) {
                this.queryFragment = queryFragment;
            }

            public int getSuggRank() {
                return suggRank;
            }

            public void setSuggRank(int suggRank) {
                this.suggRank = suggRank;
            }

            public String getSuggestion() {
                return suggestion;
            }

            public void setSuggestion(String suggestion) {
                this.suggestion = suggestion;
            }

            public int getCardCount() {
                return cardCount;
            }

            public void setCardCount(int cardCount) {
                this.cardCount = cardCount;
            }

            public List<CardsBean> getCards() {
                return cards;
            }

            public void setCards(List<CardsBean> cards) {
                this.cards = cards;
            }

            public static class CardsBean {
                /**
                 * id : manualFDF5D407AA88643AD15B8D63629AA9EB
                 * x1 : http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x1.jpeg
                 * x1_h : 40
                 * x1_w : 170
                 * x2 : http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x2.jpeg
                 * x2_h : 80
                 * x2_w : 340
                 * x3 : http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x3.jpeg
                 * x3_h : 106
                 * x3_w : 453
                 * x4 : http://d1jh24layu79ld.cloudfront.net/manual/081116/c/BA0A_inline_manual_FDF5D407AA88643AD15B8D63629AA9EB_1478577796517_x4.jpeg
                 * x4_h : 160
                 * x4_w : 680
                 * actions : [{"action_type":"web_url","action_target":"https://www.google.com/"}]
                 * card_format : inline
                 * cardRank : 1
                 */

                private String id;
                private String x1;
                private int x1_h;
                private int x1_w;
                private String x2;
                private int x2_h;
                private int x2_w;
                private String x3;
                private int x3_h;
                private int x3_w;
                private String x4;
                private int x4_h;
                private int x4_w;
                private String card_format;
                private int cardRank;
                private List<ActionsBean> actions;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getX1() {
                    return x1;
                }

                public void setX1(String x1) {
                    this.x1 = x1;
                }

                public int getX1_h() {
                    return x1_h;
                }

                public void setX1_h(int x1_h) {
                    this.x1_h = x1_h;
                }

                public int getX1_w() {
                    return x1_w;
                }

                public void setX1_w(int x1_w) {
                    this.x1_w = x1_w;
                }

                public String getX2() {
                    return x2;
                }

                public void setX2(String x2) {
                    this.x2 = x2;
                }

                public int getX2_h() {
                    return x2_h;
                }

                public void setX2_h(int x2_h) {
                    this.x2_h = x2_h;
                }

                public int getX2_w() {
                    return x2_w;
                }

                public void setX2_w(int x2_w) {
                    this.x2_w = x2_w;
                }

                public String getX3() {
                    return x3;
                }

                public void setX3(String x3) {
                    this.x3 = x3;
                }

                public int getX3_h() {
                    return x3_h;
                }

                public void setX3_h(int x3_h) {
                    this.x3_h = x3_h;
                }

                public int getX3_w() {
                    return x3_w;
                }

                public void setX3_w(int x3_w) {
                    this.x3_w = x3_w;
                }

                public String getX4() {
                    return x4;
                }

                public void setX4(String x4) {
                    this.x4 = x4;
                }

                public int getX4_h() {
                    return x4_h;
                }

                public void setX4_h(int x4_h) {
                    this.x4_h = x4_h;
                }

                public int getX4_w() {
                    return x4_w;
                }

                public void setX4_w(int x4_w) {
                    this.x4_w = x4_w;
                }

                public String getCard_format() {
                    return card_format;
                }

                public void setCard_format(String card_format) {
                    this.card_format = card_format;
                }

                public int getCardRank() {
                    return cardRank;
                }

                public void setCardRank(int cardRank) {
                    this.cardRank = cardRank;
                }

                public List<ActionsBean> getActions() {
                    return actions;
                }

                public void setActions(List<ActionsBean> actions) {
                    this.actions = actions;
                }

                public static class ActionsBean {
                    /**
                     * action_type : web_url
                     * action_target : https://www.google.com/
                     */

                    private String action_type;
                    private String action_target;

                    public String getAction_type() {
                        return action_type;
                    }

                    public void setAction_type(String action_type) {
                        this.action_type = action_type;
                    }

                    public String getAction_target() {
                        return action_target;
                    }

                    public void setAction_target(String action_target) {
                        this.action_target = action_target;
                    }
                }
            }
        }
    }
}
