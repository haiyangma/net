package com.mhy.netty.http;



import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.nio.charset.Charset;


/**
 * wrapper Response
 */
public final class HttpResponseKit {

    public static final String ENCODING = "UTF-8";
    public static final Charset CHARSET = Charset.forName(ENCODING);

    public static final String HTML = "text/html; charset=" + ENCODING;
    public static final String JAVASCRIPT = "application/javascript; charset="
            + ENCODING;
    public static final String JSON = "application/json; charset=" + ENCODING;
    public static final String TEXT_PLAIN = "text/plain; charset=" + ENCODING;
    public static final String XML = "application/xml; charset=" + ENCODING;
    private final FullHttpResponse httpResponse;


    public HttpResponseKit(FullHttpResponse httpResponse) {
        this.httpResponse = httpResponse;

    }

    public void setHeader(String name, String value) {
        this.httpResponse.headers().set(name, value);
    }

    public HttpHeaders getHeaders() {
        return this.httpResponse.headers();
    }

    public void setContentType(String contentType) {
        this.httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);

    }
    public  void setCookie(String name,String value){
        setCookie(new DefaultCookie(name, value));

    }
    public  void setCookie(Cookie cookie){
        String  cookieEncoder =  ClientCookieEncoder.LAX.encode(cookie);

        httpResponse.headers().add (HttpHeaders.Names.SET_COOKIE, cookieEncoder);
    }

    public FullHttpResponse getHttpResponse() {
        return httpResponse;
    }
}
