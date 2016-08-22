package com.mhy.netty.http;

/**
 * User: zhangsuozhu
 * Date: 13-1-17
 * Time: 下午3:15
 */
public interface ProxyFactory {
    <T> T getObject(Class<T> tClass);
}
