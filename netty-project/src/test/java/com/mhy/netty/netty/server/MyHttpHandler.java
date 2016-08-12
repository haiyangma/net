package com.mhy.netty.netty.server;

import com.mhy.netty.server.HttpHandler;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

/**
 * Created by root on 16-8-11.
 */
public class MyHttpHandler extends HttpHandler {

    @Override
    public Object receive(Map map) {
        return map.toString();
    }

}
