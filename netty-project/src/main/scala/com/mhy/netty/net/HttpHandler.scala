package com.mhy.netty.net

import java.io.IOException
import java.nio.ByteBuffer

import com.esotericsoftware.reflectasm.MethodAccess
import com.mhy.netty.client.{RpcResponseCallback, TransportClient}
import com.mhy.netty.server.{OneForOneStreamManager, RpcHandler, StreamManager}
import com.mhy.netty.util.PropertyUtil



object HttpHandler{
  private var access:MethodAccess = null
  protected var methodMap = Map[String,Int]();
  private var handler:Object=null
}
