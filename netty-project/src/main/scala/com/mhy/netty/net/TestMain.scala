package com.mhy.netty.net

import grizzled.slf4j.Logger

/**
  * Created by root on 16-8-8.
  */
class TestMain {

}
object TestMain{
  val logger = Logger(classOf[TestMain])
  def main(args: Array[String]) {
//    val server = new HttpServer(55555,new HandlerProxy);
//    println("start the server at 55555")
//    server.start();
    var n = 10
    while(n>0){
      n-=1;
      logger.warn(s"testset ${n} !")
    }
  }
}