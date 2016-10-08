package com.mhy.netty.net

/**
  * Created by root on 16-8-8.
  */
class TestMain {

}
object TestMain{
  def main(args: Array[String]) {
    val server = new HttpServer(55555,new HandlerProxy);
    println("start the server at 55555")
    server.start();
  }
}