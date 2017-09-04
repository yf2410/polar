package com.polar.browser.vclibrary.bean;

/**
 * Created by yxx on 2017/4/13.
 */

public class WeatherResult {
    /**
     * query : {"count":1,"created":"2017-04-13T11:25:01Z","lang":"zh-CN","results":{"channel":{"location":{"city":"Beijing","country":"China","region":" Beijing"},"item":{"condition":{"code":"28","date":"Thu, 13 Apr 2017 06:00 PM CST","temp":"17","text":"Mostly Cloudy"}}}}}
     */

    private QueryBean query;

    public QueryBean getQuery() {
        return query;
    }

    public void setQuery(QueryBean query) {
        this.query = query;
    }

    public static class QueryBean {
        /**
         * count : 1
         * created : 2017-04-13T11:25:01Z
         * lang : zh-CN
         * results : {"channel":{"location":{"city":"Beijing","country":"China","region":" Beijing"},"item":{"condition":{"code":"28","date":"Thu, 13 Apr 2017 06:00 PM CST","temp":"17","text":"Mostly Cloudy"}}}}
         */

        private int count;
        private String created;
        private String lang;
        private ResultsBean results;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public ResultsBean getResults() {
            return results;
        }

        public void setResults(ResultsBean results) {
            this.results = results;
        }

        public static class ResultsBean {
            /**
             * channel : {"location":{"city":"Beijing","country":"China","region":" Beijing"},"item":{"condition":{"code":"28","date":"Thu, 13 Apr 2017 06:00 PM CST","temp":"17","text":"Mostly Cloudy"}}}
             */

            private ChannelBean channel;

            public ChannelBean getChannel() {
                return channel;
            }

            public void setChannel(ChannelBean channel) {
                this.channel = channel;
            }

            public static class ChannelBean {
                /**
                 * location : {"city":"Beijing","country":"China","region":" Beijing"}
                 * item : {"condition":{"code":"28","date":"Thu, 13 Apr 2017 06:00 PM CST","temp":"17","text":"Mostly Cloudy"}}
                 */

                private LocationBean location;
                private ItemBean item;

                public LocationBean getLocation() {
                    return location;
                }

                public void setLocation(LocationBean location) {
                    this.location = location;
                }

                public ItemBean getItem() {
                    return item;
                }

                public void setItem(ItemBean item) {
                    this.item = item;
                }

                public static class LocationBean {
                    /**
                     * city : Beijing
                     * country : China
                     * region :  Beijing
                     */

                    private String city;
                    private String country;
                    private String region;

                    public String getCity() {
                        return city;
                    }

                    public void setCity(String city) {
                        this.city = city;
                    }

                    public String getCountry() {
                        return country;
                    }

                    public void setCountry(String country) {
                        this.country = country;
                    }

                    public String getRegion() {
                        return region;
                    }

                    public void setRegion(String region) {
                        this.region = region;
                    }
                }

                public static class ItemBean {
                    /**
                     * condition : {"code":"28","date":"Thu, 13 Apr 2017 06:00 PM CST","temp":"17","text":"Mostly Cloudy"}
                     */

                    private ConditionBean condition;

                    public ConditionBean getCondition() {
                        return condition;
                    }

                    public void setCondition(ConditionBean condition) {
                        this.condition = condition;
                    }

                    public static class ConditionBean {
                        /**
                         * code : 28
                         * date : Thu, 13 Apr 2017 06:00 PM CST
                         * temp : 17
                         * text : Mostly Cloudy
                         */

                        private String code;
                        private String date;
                        private String temp;
                        private String text;

                        public String getCode() {
                            return code;
                        }

                        public void setCode(String code) {
                            this.code = code;
                        }

                        public String getDate() {
                            return date;
                        }

                        public void setDate(String date) {
                            this.date = date;
                        }

                        public String getTemp() {
                            return temp;
                        }

                        public void setTemp(String temp) {
                            this.temp = temp;
                        }

                        public String getText() {
                            return text;
                        }

                        public void setText(String text) {
                            this.text = text;
                        }
                    }
                }
            }
        }
    }

//    {
//        "query": {
//        "count": 1,
//                "created": "2017-04-13T11:25:01Z",
//                "lang": "zh-CN",
//                "results": {
//            "channel": {
//                "location": {
//                    "city": "Beijing",
//                            "country": "China",
//                            "region": " Beijing"
//                },
//                "item": {
//                    "condition": {
//                        "code": "28",
//                                "date": "Thu, 13 Apr 2017 06:00 PM CST",
//                                "temp": "17",
//                                "text": "Mostly Cloudy"
//                    }
//                }
//            }
//        }
//    }
//    }


}
