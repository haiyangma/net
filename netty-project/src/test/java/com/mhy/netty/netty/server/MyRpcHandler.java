package com.mhy.netty.netty.server;

import com.mhy.netty.client.RpcResponseCallback;
import com.mhy.netty.client.TransportClient;
import com.mhy.netty.protocol.RpcResponse;
import com.mhy.netty.server.OneForOneStreamManager;
import com.mhy.netty.server.RpcHandler;
import com.mhy.netty.server.StreamManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by root on 16-8-11.
 */
public class MyRpcHandler extends RpcHandler {
    StreamManager streamManager = new OneForOneStreamManager();

    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {
        callback.onSuccess(message);
    }

    @Override
    public StreamManager getStreamManager() {
        return streamManager;
    }
}
