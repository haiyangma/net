package com.mhy.netty.net

import com.esotericsoftware.reflectasm.ConstructorAccess
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelInitializer, ChannelOption, ChannelPipeline}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}

/**
  * Created by root on 16-8-8.
  */
class HttpServer(port:Int,proxy:HandlerProxy) extends Server{
  protected var ch:Channel = null;
  protected var bossGroup:NioEventLoopGroup = null;
  protected var workGroup:NioEventLoopGroup = null;
  protected var bootstrap:ServerBootstrap = new ServerBootstrap()
  bossGroup = new NioEventLoopGroup(100);
  workGroup = new NioEventLoopGroup(700);
  try {
    bootstrap.group(bossGroup, workGroup);
    bootstrap.channel(classOf[NioServerSocketChannel]);
    bootstrap.childOption[java.lang.Boolean](ChannelOption.SO_REUSEADDR, true);
    bootstrap.childOption[java.lang.Boolean](ChannelOption.TCP_NODELAY, true);
    bootstrap.childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true);
    bootstrap.childHandler(new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        val pipeline = ch.pipeline();
        pipeline.addLast("http-decoder",new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler",proxy.newInstance);
      }
    })
  } catch {
    case e:Exception => e.printStackTrace();
  }



  override  def start(): Unit = synchronized{
    ch = bootstrap.bind(port).sync().channel();
    ch.closeFuture().sync();
  }

  override def stop(): Unit = synchronized{
    if (ch != null)
      bossGroup.shutdownGracefully();
    workGroup.shutdownGracefully();
  }

}
