package com.polar.browser.service;

interface IAdBlockService {
    boolean shouldBlockUrlSync(String mainUrl, String url);
    String getCssAndLocalStorageScript(String host);
    void updateRule(String filename);
    void exit();
}