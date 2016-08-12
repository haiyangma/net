package com.mhy.netty.server;

import com.mhy.netty.client.RpcResponseCallback;
import com.mhy.netty.client.TransportClient;
import io.netty.handler.codec.http.HttpResponse;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by root on 16-8-11.
 */
public abstract class HttpHandler extends RpcHandler {
    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {

    }

    @Override
    public StreamManager getStreamManager() {
        return null;
    }

    public abstract Object receive(Map map);

}
