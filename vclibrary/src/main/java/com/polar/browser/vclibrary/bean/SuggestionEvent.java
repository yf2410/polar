package com.polar.browser.vclibrary.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by saifei on 16/12/21.
 *
 * 搜索建议统计用的 bean
 */

public class SuggestionEvent implements Serializable {


    /**
     * app_id : searchmaster.test.app
     * device_id : AKHKJJhdkjk4j2
     * events : [{"name":"card_shown","time":1475540698146,"payload":{"card_id":12342,"suggestion_id":12,"rid":12342}},{"name":"card_clicked","time":1475540998146,"payload":{"card_id":12342,"suggestion_id":12,"rid":12342}}]
     */

    private String app_id;
    private String device_id;
    private List<EventsBean> events;

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public List<EventsBean> getEvents() {
        if(events == null) events = new ArrayList<>();
        return events;
    }

    public void setEvents(List<EventsBean> events) {
        this.events = events;
    }

    public static class EventsBean implements Serializable {

        public EventsBean(String name, long time, PayloadBean payload) {
            this.name = name;
            this.time = time;
            this.payload = payload;
        }

        /**
         * name : card_shown
         * time : 1475540698146
         * payload : {"card_id":12342,"suggestion_id":12,"rid":12342}
         */

        private String name;
        private long time;
        private PayloadBean payload;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public PayloadBean getPayload() {
            return payload;
        }

        public void setPayload(PayloadBean payload) {
            this.payload = payload;
        }

        public static class PayloadBean implements Serializable {
            /**
             * card_id : 12342
             * suggestion_id : 12
             * rid : 12342
             */

            private String card_id;
            private int suggestion_id;
            private String rid;

            public PayloadBean(String card_id, int suggestion_id, String rid) {
                this.card_id = card_id;
                this.suggestion_id = suggestion_id;
                this.rid = rid;
            }

            public String getCard_id() {
                return card_id;
            }

            public void setCard_id(String card_id) {
                this.card_id = card_id;
            }

            public int getSuggestion_id() {
                return suggestion_id;
            }

            public void setSuggestion_id(int suggestion_id) {
                this.suggestion_id = suggestion_id;
            }

            public String getRid() {
                return rid;
            }

            public void setRid(String rid) {
                this.rid = rid;
            }
        }
    }
}
