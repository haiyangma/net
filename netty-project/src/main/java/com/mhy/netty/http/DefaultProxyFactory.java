package com.mhy.netty.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mahaiyang
 * Date: 16-1-17
 * Time: 下午3:17
 */
public class DefaultProxyFactory implements ProxyFactory {
    private static final Logger log = LoggerFactory.getLogger(DefaultProxyFactory.class);

    @Override
    public <T> T getObject(Class<T> tClass) {
        try {
            return tClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
