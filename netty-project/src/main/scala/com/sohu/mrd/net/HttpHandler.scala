package com.sohu.mrd.net

import java.io.IOException
import java.nio.ByteBuffer

import com.esotericsoftware.reflectasm.MethodAccess
import com.mhy.netty.client.{RpcResponseCallback, TransportClient}
import com.mhy.netty.server.{OneForOneStreamManager, RpcHandler, StreamManager}
import com.sohu.mrd.util.PropertyUtil
import com.typesafe.scalalogging.LazyLogging
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext}
import io.netty.handler.codec.http.multipart.{Attribute, HttpPostRequestDecoder, InterfaceHttpData}
import io.netty.handler.codec.http.{HttpMethod, QueryStringDecoder, _}
import io.netty.util.CharsetUtil

import scala.collection.mutable.{HashMap, Map}



object HttpHandler{
  private var access:MethodAccess = null
  protected var methodMap = Map[String,Int]();
  private var handler:Object=null
}
