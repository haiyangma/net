package com.mhy.netty.net

import com.esotericsoftware.reflectasm.ConstructorAccess
import io.netty.channel.ChannelHandler

/**
  * Created by root on 16-8-8.
  */
class HandlerProxy extends ConstructorAccess[ChannelHandler]{
  override def newInstance(): ChannelHandler = {
    return new HttpServerHandler
  }

  override def newInstance(enclosingInstance: scala.Any): ChannelHandler = {
    return new HttpServerHandler
  }
}

object HandlerProxy{

}
