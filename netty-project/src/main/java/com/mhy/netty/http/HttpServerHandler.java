package com.mhy.netty.http;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class.getName());

    private boolean readingChunks;

    private final StringBuilder responseContent = new StringBuilder();



    private final Controller controller;
   // private AccessLogService logService;
    //private final HttpServerLogClient logClient  ;


    public HttpServerHandler(Controller controller) {
        this.controller = controller;
      //logClient = HttpServerLogClient.getInstance();
      //  logService=AccessLogService.getInstance();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("[HttpServerHandler] [channelInactive] [close]");
        //super.channelInactive(ctx);
        ctx.channel().close();
        //ctx.close();
    }

    public void messageReceived(final ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("Cache-Control", "must-revalidate");
        response.headers().add("Cache-Control", "no-cache");
        response.headers().add("Cache-Control", "no-store");
        response.headers().add("Expires", 0);



        final HttpRequestKit httpRequestKit = new HttpRequestKit(ctx.channel(), msg);
        String uri = httpRequestKit.getHttpRequest().getUri();
        String url=uri;
        uri = StringUtils.replace(uri, "//", "/");
        if (uri.indexOf('?') > 0) uri = uri.substring(0, uri.indexOf('?'));

        uri=uri.replaceAll("//","_");
        if(!uri.equals("/favicon.ico")) {
//            controller.doAction(httpRequestKit, response);
//            writeResponse(ctx.channel(), httpRequestKit.getHttpRequest(), response);

            String timer_uri=uri;
            if( uri.indexOf("?")!=-1 ){
                timer_uri=uri.substring(0,uri.indexOf("?"));
            }

            try {
                controller.doAction(httpRequestKit, response);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                writeResponse(ctx.channel(), httpRequestKit.getHttpRequest(), response);
                //logClient.send(uri,start,end);
            }
        }else{
            try {
                controller.doAction(httpRequestKit, response);
                writeResponse(ctx.channel(), httpRequestKit.getHttpRequest(), response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void reset() {

    }

    private void writeHttpData(InterfaceHttpData data) {

        /**
         * HttpDataType有三种类型
         * Attribute, FileUpload, InternalAttribute
         */
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                e1.printStackTrace();
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ":"
                        + attribute.getName() + " Error while reading value: " + e1.getMessage() + "\r\n");
                return;
            }
            if (value.length() > 100) {
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ":"
                        + attribute.getName() + " data too long\r\n");
            } else {
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ":"
                        + attribute.toString() + "\r\n");
            }
        }
    }


    /**
     * http返回响应数据
     *
     * @param channel
     */
    private void writeResponse(Channel channel, HttpRequest fullHttpRequest, FullHttpResponse response) {

        // Decide whether to close the connection or not.
        boolean close = fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || fullHttpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

        // Build the response object.
        //response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        }

//        Set<Cookie> cookies;
//        String value = fullHttpRequest.headers().get(COOKIE);
//        if (value == null) {
//            cookies = Collections.emptySet();
//        } else {
//            cookies = CookieDecoder.decode(value);
//        }
//        if (!cookies.isEmpty()) {
//            // Reset the cookies if necessary.
//            for (Cookie cookie : cookies) {
//
//
//              //  response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
//            }
//        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn(responseContent.toString(), cause);
        ctx.channel().close();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if(!channelHandlerContext.channel().isActive()) {
            logger.error("[HttpServerHandler] [channelRead0] [isNotActive] [close]");
            channelHandlerContext.channel().close();
            return;
        }
        messageReceived(channelHandlerContext, fullHttpRequest);
    }
}
