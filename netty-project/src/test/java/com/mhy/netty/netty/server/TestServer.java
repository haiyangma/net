package com.mhy.netty.netty.server;

import com.mhy.netty.client.RpcResponseCallback;
import com.mhy.netty.client.TransportClient;
import com.mhy.netty.client.TransportClientFactory;
import com.mhy.netty.network.TransportContext;
import com.mhy.netty.server.TransportServer;
import com.mhy.netty.util.MapConfigProvider;
import com.mhy.netty.util.TransportConf;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 16-8-11.
 */
public class TestServer {
    @Test
    public void createHttpServer() throws Exception{
        TransportConf conf = new TransportConf("test",new MapConfigProvider(new HashMap()));
        TransportContext context = new TransportContext(conf,new MyHttpHandler(),true);
        TransportServer server =  context.createHttpServer(56666);
        server.sync();
    }

    @Test
    public void createNewHttpServer() throws Exception{
        Map map = new HashMap();
        map.put("springXml","spring.xml");
        map.put("actionPath","com.mhy.netty.action");
        TransportConf conf = new TransportConf("test",new MapConfigProvider(map));
        TransportContext context = new TransportContext(conf,true);
        TransportServer server =  context.createNewHttpServer(56666);
        server.sync();
    }

    @Test
    public void createRpcServer() throws Exception{
        TransportConf conf = new TransportConf("test",new MapConfigProvider(new HashMap()));
        TransportContext context = new TransportContext(conf,new MyRpcHandler(),true);
        TransportServer server =  context.createRpcServer(56666);
        server.sync();
    }


    @Test
    public void createRpcClient() throws Exception{
        TransportConf conf = new TransportConf("test",new MapConfigProvider(new HashMap()));
        TransportContext context = new TransportContext(conf,new MyRpcHandler(),true);
        TransportClientFactory clientFactory = context.createClientFactory();
        TransportClient client = clientFactory.createClient("10.13.89.31",56666);
        client.sendRpc(ByteBuffer.wrap("nihao".getBytes()), new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
                System.out.println(new String(response.array()));
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println(e.getMessage());
            }
        });
    }
}
