package com.mhy.netty.net

import com.esotericsoftware.reflectasm.ConstructorAccess

import scala.collection.mutable.Map

/**
  * Created by root on 16-8-8.
  */
class MyHandler  extends Handler{
  def doGet(map: Map[String, String]):Object= {
    return map
  }
}

object MyHandler{
  var instance:MyHandler = null
  def getInstance:MyHandler = {
    if(instance == null){
      instance = new MyHandler;
    }
    return instance
  }
}
