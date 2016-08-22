package com.mhy.netty.http;

import com.google.common.util.concurrent.AbstractService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Starts a HTTP service to listen for and respond to control messages.
 */
public class HttpService extends AbstractService {
    private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);

    private final EventLoopGroup acceptConnectionGroup;
    private final EventLoopGroup ioWorkerGroup;
    private final int modulePort;
  //  private final int adminPort;
    private Channel listenChannel;
    private final Controller controller;
    private final String server_name;
    private String etcdUrl;
    private String serviceInstanceKey;
    private final ScheduledExecutorService etcdPool = Executors.newScheduledThreadPool(1);

    private final  int maxContentLength;
    public HttpService(Controller controller,

                       EventLoopGroup acceptConnectionGroup,
                       EventLoopGroup ioWorkerGroup,
                       int modulePort,
                       String server_name,
                       int maxContentLength,
                       String etcdUrl) {
        this.controller = controller;

        this.acceptConnectionGroup = acceptConnectionGroup;
        this.ioWorkerGroup = ioWorkerGroup;
        this.modulePort = modulePort;
       // this.adminPort = adminPort;
        this.server_name = server_name;
        this.maxContentLength=maxContentLength;
        this.etcdUrl = etcdUrl;
    }

    @Override
    protected void doStart() {
      //  StatsdService.start();
        startHttpRpc();
    }


//    static class ShutDownHook extends Thread {
//
//        public void run() {
//            System.out.println("Bye...");
//            DiscoveryManager.getInstance().shutdownComponent();
//        }
//    }

    public static String getLocalIP() {

        String ip = "";
        ip = System.getProperty("server_ip");//优先取得参数中的ip配置，如果有就直接返回
        if (StringUtils.isNotBlank(ip)) {
            LOG.info("get server_ip :"+ip);
            return StringUtils.trim(ip);
        }
        try {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); ) {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); ) {
                    InetAddress addr = a.nextElement();
                    ip = addr.getHostAddress();
                    if (addr instanceof Inet4Address && !ip.startsWith("127")) {
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return ip;
    }

    private void startHttpRpc() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptConnectionGroup, ioWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
//                             pipeline.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
                            //   pipeline.addLast("http-server", new HttpServerCodec());

                            pipeline.addLast("decoder", new HttpRequestDecoder());

                            pipeline.addLast("encoder", new HttpResponseEncoder());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(maxContentLength));
                            pipeline.addLast("handler", new HttpServerHandler(controller));
                        }
                    });
            ChannelFuture channelFuture=    serverBootstrap.bind(modulePort).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
// yay
                        listenChannel = future.channel();
                        notifyStarted();
                    } else {
                        LOG.error("Unable to bind to port {}", modulePort);
                        notifyFailed(future.cause());
                    }
                }
            });

        } catch (Exception e) {
            notifyFailed(e);
        }
    }
    @Override
    protected void doStop() {
        try {
            listenChannel.close().get();
        } catch (InterruptedException e){
            notifyFailed(e);
        } catch (ExecutionException e) {
            notifyFailed(e);
        }
        notifyStopped();
    }
}
