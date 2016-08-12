package com.mhy.netty.server;



import com.esotericsoftware.reflectasm.MethodAccess;
import com.mhy.netty.client.RpcResponseCallback;
import com.mhy.netty.client.TransportClient;
import com.mhy.netty.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by root on 16-8-10.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerHandler.class);
    protected String CONTENT_TYPE_V = null;
    protected Map<String, Integer> methodMap;
    private HttpHandler rpcHandler;

    public HttpServerHandler(HttpHandler rpcHandler){
        this.rpcHandler = rpcHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            HttpRequest request = (HttpRequest) msg;
            if (request == null) return;
            Map<String,String> map = null;
            try {
                map = packRequest(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String uri=request.getUri();
            map.put("uri", uri);
            map.put("HttpMethod", request.getMethod().name().toString());
            processOneMessage(ctx,request, map);
        }
    }

    protected void processOneMessage(ChannelHandlerContext ctx, HttpRequest request, Map<String, String> map)  {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        Object handlerResult = null;

        try {
            handlerResult = rpcHandler.receive(map);
        } catch (Exception rpc_e) {
            LOG.error("process request err !",rpc_e);
            return;
        }
        byte[] ret = null;
        try {
            ret = handlerResult.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ByteBuf buf = Unpooled.copiedBuffer(handlerResult.toString(), CharsetUtil.UTF_8);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,buf);

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONNECTION, "keep-alive");
        }
        ChannelFuture future = ctx.writeAndFlush(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     *
     * @param request
     * @return
     */
    public Map<String, String> parseGetQueryString(HttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        return traversalGetDecoder(decoder, null);
    }

    public Map<String, String> parsePostQueryString(HttpRequest request) {
        HttpPostRequestDecoder httpPostRequestDecoder = new HttpPostRequestDecoder(request);
        return traversalPostDecoder(httpPostRequestDecoder, null);
    }

    /**
     *
     * @param decoder
     * @param parameters
     * @return
     */
    private Map<String, String> traversalGetDecoder(QueryStringDecoder decoder, Map<String, String> parameters){
        if (null == parameters) {
            parameters = new HashMap<String, String>();
        }
        Iterator<Map.Entry<String, List<String>>> iterator = decoder.parameters().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            parameters.put(entry.getKey(), entry.getValue().get(0));
        }
        return parameters;
    }

    private Map<String, String> traversalPostDecoder(HttpPostRequestDecoder decoder, Map<String, String> parameters){
        if (null == parameters) {
            parameters = new HashMap<String, String>();
        }
        List<InterfaceHttpData> params = decoder.getBodyHttpDatas();
        for(InterfaceHttpData param : params){
            if (param.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute)param;
                try {
                    parameters.put(attribute.getName(),attribute.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return parameters;
    }

    /**
     *
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, String> packRequest(HttpRequest request) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        if(request.getMethod().equals(HttpMethod.GET)){
            map = parseGetQueryString(request);
        }else{
            map = parsePostQueryString(request);
        }

//        if (!request.method().equals(HttpMethod.GET)) {
//            throw new IOException("Not support http method except GET!");
//        }
        return map;
    }
}
