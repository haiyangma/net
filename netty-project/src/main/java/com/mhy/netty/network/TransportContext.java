/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mhy.netty.network;

import com.google.common.collect.Lists;
import com.mhy.netty.http.Controller;
import com.mhy.netty.server.HttpHandler;
import com.mhy.netty.server.HttpServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import com.mhy.netty.client.TransportClient;
import com.mhy.netty.client.TransportClientBootstrap;
import com.mhy.netty.client.TransportClientFactory;
import com.mhy.netty.client.TransportResponseHandler;
import com.mhy.netty.protocol.MessageDecoder;
import com.mhy.netty.protocol.MessageEncoder;
import com.mhy.netty.server.*;
import com.mhy.netty.util.NettyUtils;
import com.mhy.netty.util.TransportConf;
import com.mhy.netty.util.TransportFrameDecoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Contains the context to create a {@link TransportServer}, {@link TransportClientFactory}, and to
 * setup Netty Channel pipelines with a {@link org.apache.spark.network.server.TransportChannelHandler}.
 *
 * There are two communication protocols that the TransportClient provides, control-plane RPCs and
 * data-plane "chunk fetching". The handling of the RPCs is performed outside of the scope of the
 * TransportContext (i.e., by a user-provided handler), and it is responsible for setting up streams
 * which can be streamed through the data plane in chunks using zero-copy IO.
 *
 * The TransportServer and TransportClientFactory both create a TransportChannelHandler for each
 * channel. As each TransportChannelHandler contains a TransportClient, this enables server
 * processes to send messages back to the client on an existing channel.
 */
public class TransportContext {
  private final Logger logger = LoggerFactory.getLogger(TransportContext.class);

  private final TransportConf conf;
  private RpcHandler rpcHandler = null;
  private final boolean closeIdleConnections;

  private MessageEncoder encoder = null;
  private MessageDecoder decoder = null;

  public TransportContext(TransportConf conf, RpcHandler rpcHandler) {
    this(conf, rpcHandler, false);
  }

  public TransportContext(
      TransportConf conf,
      RpcHandler rpcHandler,
      boolean closeIdleConnections) {
    this.conf = conf;
    this.rpcHandler = rpcHandler;
    this.encoder = new MessageEncoder();
    this.decoder = new MessageDecoder();
    this.closeIdleConnections = closeIdleConnections;
  }

    /**
     * create http server
     * @param conf
     * @param closeIdleConnections
     */
    public TransportContext(
            TransportConf conf,
            boolean closeIdleConnections) {
        this.conf = conf;
        this.closeIdleConnections = closeIdleConnections;
    }

  /**
   * Initializes a ClientFactory which runs the given TransportClientBootstraps prior to returning
   * a new Client. Bootstraps will be executed synchronously, and must run successfully in order
   * to create a Client.
   */
  public TransportClientFactory createClientFactory(List<TransportClientBootstrap> bootstraps) {
    return new TransportClientFactory(this, bootstraps);
  }

  public TransportClientFactory createClientFactory() {
    return createClientFactory(Lists.<TransportClientBootstrap>newArrayList());
  }

  /** Create a server which will attempt to bind to a specific port. */
  public TransportServer createServer(int port, List<TransportServerBootstrap> bootstraps) {
    return new TransportServer(this, null, port, rpcHandler, bootstraps,TransportConf.ServerType.SOCKETCHANNELSERVER);
  }
  public TransportServer createServer(int port, List<TransportServerBootstrap> bootstraps, TransportConf.ServerType type) {
    return new TransportServer(this, null, port, rpcHandler, bootstraps,type);
  }

    public TransportServer createHttpServer(int port,
                                        List<TransportServerBootstrap> bootstraps,
                                        TransportConf.ServerType type
    ) {
        String packageStr = conf.actionpath();
        String springXml = conf.springXml();
        if(StringUtils.isBlank(packageStr) || StringUtils.isBlank(springXml)){
            logger.error("you must special the config in the conf provider by key 'actionPath:(value splited by comma)'  and 'springXml'");
            System.exit(1);
        }
        List<String> actionPaths = Lists.newArrayList(packageStr.split(","));
        return new TransportServer(this, null, port, rpcHandler, bootstraps,type,springXml,actionPaths);
    }
  /** Create a server which will attempt to bind to a specific host and port. */
  public TransportServer createServer(
      String host, int port, List<TransportServerBootstrap> bootstraps) {
    return new TransportServer(this, host, port, rpcHandler, bootstraps, TransportConf.ServerType.SOCKETCHANNELSERVER);
  }

  /** Creates a new server, binding to any available ephemeral port. */
  public TransportServer createServer(List<TransportServerBootstrap> bootstraps) {
    return createServer(0, bootstraps);
  }
    public TransportServer createRpcServer(int port) {
        return createServer(port, Lists.<TransportServerBootstrap>newArrayList(), TransportConf.ServerType.SOCKETCHANNELSERVER);
    }

  public TransportServer createHttpServer(int port) {
    return createServer(port, Lists.<TransportServerBootstrap>newArrayList(), TransportConf.ServerType.HTTPSERVER);
  }

    public TransportServer createNewHttpServer(int port) {
        return createHttpServer(port, Lists.<TransportServerBootstrap>newArrayList(), TransportConf.ServerType.HTTPSERVER);
    }


  public TransportServer createServer() {
    return createServer(0, Lists.<TransportServerBootstrap>newArrayList());
  }

 public TransportChannelHandler initializePipeline(SocketChannel channel) {
     return initializePipeline(channel, rpcHandler);
 }
  public void initializePipeline(SocketChannel channel, TransportConf.ServerType type) {
    if(type == TransportConf.ServerType.SOCKETCHANNELSERVER){
        initializePipeline(channel, rpcHandler);
    }else if(type == TransportConf.ServerType.HTTPSERVER){
        initializeHttpPipeline(channel,rpcHandler);
    }

  }

  /**
   * Initializes a client or server Netty Channel Pipeline which encodes/decodes messages and
   * has a {@link org.apache.spark.network.server.TransportChannelHandler} to handle request or
   * response messages.
   *
   * @param channel The channel to initialize.
   * @param channelRpcHandler The RPC handler to use for the channel.
   *
   * @return Returns the created TransportChannelHandler, which includes a TransportClient that can
   * be used to communicate on this channel. The TransportClient is directly associated with a
   * ChannelHandler to ensure all users of the same channel get the same TransportClient object.
   */
  public TransportChannelHandler initializePipeline(
      SocketChannel channel,
      RpcHandler channelRpcHandler) {
    try {
      TransportChannelHandler channelHandler = createChannelHandler(channel, channelRpcHandler);
      channel.pipeline()
        .addLast("encoder", encoder)
        .addLast(TransportFrameDecoder.HANDLER_NAME, NettyUtils.createFrameDecoder())
        .addLast("decoder", decoder)
        .addLast("idleStateHandler", new IdleStateHandler(0, 0, conf.connectionTimeoutMs() / 1000))
        // NOTE: Chunks are currently guaranteed to be returned in the order of request, but this
        // would require more logic to guarantee if this were not part of the same event loop.
        .addLast("handler", channelHandler);
      return channelHandler;
    } catch (RuntimeException e) {
      logger.error("Error while initializing Netty pipeline", e);
      throw e;
    }
  }

    public HttpServerHandler initializeHttpPipeline(
            SocketChannel channel,
            RpcHandler rpcHandler) {
        try {
            HttpServerHandler serverHandler = new HttpServerHandler((HttpHandler) rpcHandler);
            channel.pipeline()
                    .addLast("http-decoder",new HttpRequestDecoder())
                    .addLast("aggregator", new HttpObjectAggregator(1048576))
                    .addLast("encoder", new HttpResponseEncoder())
                    .addLast("idleStateHandler", new IdleStateHandler(0, 0, conf.connectionTimeoutMs() / 1000))
                    .addLast("handler", serverHandler);
            return serverHandler;
        } catch (RuntimeException e) {
            logger.error("Error while initializing Netty pipeline", e);
            throw e;
        }
    }

    public com.mhy.netty.http.HttpServerHandler initializeNewHttpPipeline(
            SocketChannel channel,
            Controller controller) {
        try {
            com.mhy.netty.http.HttpServerHandler serverHandler = new com.mhy.netty.http.HttpServerHandler(controller);
            channel.pipeline()
                    .addLast("http-decoder",new HttpRequestDecoder())
                    .addLast("aggregator", new HttpObjectAggregator(1048576))
                    .addLast("encoder", new HttpResponseEncoder())
                    .addLast("handler", serverHandler);
            return serverHandler;
        } catch (RuntimeException e) {
            logger.error("Error while initializing Netty pipeline", e);
            throw e;
        }
    }

  /**
   * Creates the server- and client-side handler which is used to handle both RequestMessages and
   * ResponseMessages. The channel is expected to have been successfully created, though certain
   * properties (such as the remoteAddress()) may not be available yet.
   */
  private TransportChannelHandler createChannelHandler(Channel channel, RpcHandler rpcHandler) {
    TransportResponseHandler responseHandler = new TransportResponseHandler(channel);
    TransportClient client = new TransportClient(channel, responseHandler);
    TransportRequestHandler requestHandler = new TransportRequestHandler(channel, client,
      rpcHandler);
    return new TransportChannelHandler(client, responseHandler, requestHandler,
      conf.connectionTimeoutMs(), closeIdleConnections);
  }

  public TransportConf getConf() { return conf; }
}
