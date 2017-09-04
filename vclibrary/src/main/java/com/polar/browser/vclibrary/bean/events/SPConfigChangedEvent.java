package com.polar.browser.vclibrary.bean.events;

/**
 * Created by FKQ on 2016/12/16.
 */

public class SPConfigChangedEvent {

    private boolean value;
    private String ConfigDefineValue;

    public SPConfigChangedEvent(boolean value, String configDefineValue) {
        this.value = value;
        this.ConfigDefineValue = configDefineValue;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String getConfigDefineValue() {
        return ConfigDefineValue;
    }

    public void setConfigDefineValue(String configDefineValue) {
        ConfigDefineValue = configDefineValue;
    }
}
