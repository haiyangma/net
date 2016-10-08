package com.mhy.netty.net

/**
  * Created by root on 16-8-8.
  */
trait Handler {
  def doGet( map :collection.mutable.Map[String,String]):Object;
}
