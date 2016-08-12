package com.mhy.netty.client;

import com.mhy.netty.netty.server.MyRpcHandler;
import com.mhy.netty.network.TransportContext;
import com.mhy.netty.util.MapConfigProvider;
import com.mhy.netty.util.TransportConf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by root on 16-8-11.
 */
public class TestClient {
    @Test
    public void testClient() throws Exception{
        TransportConf conf = new TransportConf("test",new MapConfigProvider(new HashMap()));
        TransportContext context = new TransportContext(conf,new MyRpcHandler(),true);
        TransportClientFactory clientFactory = context.createClientFactory();
        TransportClient client = clientFactory.createClient("10.13.89.31",56666);
        client.sendRpc(ByteBuffer.wrap("nihao".getBytes("utf-8")), new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
//                System.out.println(response);
//                ByteBuf buf = Unpooled.copiedBuffer(response);
//                response.flip();
//                response.slice();
//                System.out.println(new String(buf.nioBuffer().array()));
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println(e.getMessage());
            }
        });
        System.out.println(client.getChannel().closeFuture().get());
    }
}
