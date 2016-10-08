package com.mhy.netty.action;

import com.mhy.netty.annotation.RequestMapping;
import com.mhy.netty.http.HttpRequestKit;
import com.mhy.netty.http.HttpResponseKit;
import org.springframework.stereotype.Component;

/**
 * Created by root on 16-8-22.
 */
@Component
@RequestMapping("mhy/get")
public class Test {
    @RequestMapping()
    public String testDemo(HttpRequestKit request, HttpResponseKit response){
        return request.getParams().toString();
    }
}
