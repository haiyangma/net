package com.mhy.netty.http;


public abstract class Action {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected abstract String action(HttpRequestKit httpRequestKit, HttpResponseKit httpResponse)
            throws Exception;


}
