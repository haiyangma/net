package com.mhy.netty.net

import java.io.IOException

import com.esotericsoftware.reflectasm.MethodAccess
import com.mhy.netty.util.PropertyUtil
import com.typesafe.scalalogging.LazyLogging


/**
  * Created by root on 16-8-8.
  */
class HttpServerHandler extends ChannelInboundHandlerAdapter  with LazyLogging{
  val propUtil:PropertyUtil = new PropertyUtil("app.properties")


  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    super.channelActive(ctx)
  }


  private var CONTENT_TYPE_V = "text/plain; charset=UTF-8";
  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    if(msg.isInstanceOf[HttpRequest] ){
      val request = msg.asInstanceOf[HttpRequest];
      if (request == null) return;
      val map:Map[String,String] = packRequest(request);
      val uri=request.getUri;
      map.put("uri", uri);
      map.put("HttpMethod", request.getMethod.name());
      processOneMessage(ctx, request, map);
    }
  }

  def processOneMessage( ctx:ChannelHandlerContext, request:HttpRequest ,  map:Map[String, String]):Unit= {
    // Decide whether to close the connection or not.
    val keepAlive = HttpHeaders.isKeepAlive(request);

    var handlerResult = "";
    try {
      handlerResult = callMethod("doGet", map).toString;
    } catch  {
      case rpc_e:Exception =>
        logger.error("process request err !",rpc_e);
      return;
    }

    val buf = Unpooled.copiedBuffer(handlerResult, CharsetUtil.UTF_8);
    val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,buf);
    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
    response.headers().set("Access-Control-Allow-Origin", "*");
    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
    if (keepAlive) {
      response.headers().set(HttpHeaders.Names.CONNECTION, "keep-alive");
    }
    val future = ctx.writeAndFlush(response);
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @throws[Exception]
  def callMethod( method:String,  map:Map[String, String]):Object = {
    return HttpServerHandler.access.invoke(HttpServerHandler.handler,HttpServerHandler.methodMap(method),map);
  }

  /**
    *
    * @param request
    * @return
    */
  def parseGetQueryString(request :HttpRequest) :Map[String, String]={
    val decoder = new QueryStringDecoder(request.getUri);
    return traversalGetDecoder(decoder, new HashMap[String,String]());
  }

  def parsePostQueryString( request : HttpRequest):Map[String, String]= {
    val httpPostRequestDecoder = new HttpPostRequestDecoder(request);
    return traversalPostDecoder(httpPostRequestDecoder, new HashMap[String,String]());
  }

  /**
    *
    * @param decoder
    * @param parameters
    * @return
    */
  def traversalGetDecoder( decoder:QueryStringDecoder,  parameters:Map[String, String]):Map[String, String]={
    val iterator = decoder.parameters().entrySet().iterator();
    while (iterator.hasNext()) {
      val entry = iterator.next();
      parameters.put(entry.getKey(),entry.getValue().get(0));
    }
    return parameters;
  }

  def traversalPostDecoder(decoder:HttpPostRequestDecoder, parameters:Map[String, String]):Map[String, String]={
    val params = decoder.getBodyHttpDatas();
    val iter = params.iterator();
    while(iter.hasNext){
      val param = iter.next()
      if (param.getHttpDataType == InterfaceHttpData.HttpDataType.Attribute) {
        try {
          val attr = param.asInstanceOf[Attribute]
          parameters.put(attr.getName ,attr.getValue);
        } catch  {
          case e:IOException => e.printStackTrace();
        }
      }
    }
    return parameters;
  }

  /**
    *
    * @param request
    * @return
    * @throws Exception
    */
  @throws[Exception]
  def packRequest(request:HttpRequest) : Map[String,String]={
    var map = Map[String, String]();
    if(request.getMethod.equals(HttpMethod.GET)){
      map = parseGetQueryString(request);
    }else{
      map = parsePostQueryString(request);
    }

    //        if (!request.method().equals(HttpMethod.GET)) {
    //            throw new IOException("Not support http method except GET!");
    //        }
    return map;
  }

}

object HttpServerHandler{
  private var access:MethodAccess = null
  protected var methodMap = Map[String,Int]();
  private var handler:Object=null
}
