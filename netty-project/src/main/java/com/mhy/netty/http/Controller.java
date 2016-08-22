package com.mhy.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public final class Controller {
    protected final Map<String, Action> actions = new HashMap<String, Action>();
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    public Controller() {

    }

    public final void doAction(HttpRequestKit httpRequestKit, FullHttpResponse httpResponse) throws Exception {
        String firstPath = getEndPoint(httpRequestKit.getHttpRequest(), httpResponse);

//        if (firstPath.equals("favicon.ico") || firstPath.equals("s")) {
//
//        }

        String uri = httpRequestKit.getHttpRequest().getUri();
        uri=java.net.URLDecoder.decode(uri,"utf-8");

        if (uri.indexOf('?') > 0) uri = uri.substring(0, uri.indexOf('?'));

//        String[] uriSplits = uri.split("/");
//        if (uriSplits.length < 3) {
//            invalidRequestFormat(httpRequestKit.getHttpRequest(), httpResponse);
//        }
//
        uri = StringUtils.replace(uri, "//", "/");
        String actionStr = uri;

        if(actionStr.equals("/ping")){
            httpResponse.content().writeBytes(Unpooled.copiedBuffer("OK", Charset.forName("utf-8")));
            return;
        }
//add ping
        Action action = actions.get(actionStr);

        if (action == null) {
            unknownAction(actionStr, httpResponse);
        } else {
            String content = "";
            try {
                content = action.action(httpRequestKit, new HttpResponseKit( httpResponse));
            } catch (Throwable e) {
                log.error(e.getMessage(), e);

            }
            if(content==null){
                content="";
                log.warn(uri+" returned null!");
            }

            httpResponse.content().writeBytes(Unpooled.copiedBuffer(content, Charset.forName("utf-8")));
        }
    }

    /**
     * Returns the "first path segment" in the URI.
     * <p/>
     * Examples:
     * <pre>
     *   URI request | Value returned
     *   ------------+---------------
     *   /           | ""
     *   /foo        | "foo"
     *   /foo/bar    | "foo"
     *   /foo?quux   | "foo"
     * </pre>
     */
    private String getEndPoint(HttpRequest httpRequest, FullHttpResponse httpResponse) throws Exception {
        final String uri = httpRequest.getUri();
        if (uri.length() < 1) {
            invalidRequestFormat(httpRequest, httpResponse);
            return "";
        }
        if (uri.charAt(0) != '/') {

        }
        final int questionmark = uri.indexOf('?', 1);
        final int slash = uri.indexOf('/', 1);
        int pos;  // Will be set to where the first path segment ends.
        if (questionmark > 0) {
            if (slash > 0) {
                pos = (questionmark < slash
                        ? questionmark       // Request: /foo?bar/quux
                        : slash);            // Request: /foo/bar?quux
            } else {
                pos = questionmark;         // Request: /foo?bar
            }
        } else {
            pos = (slash > 0
                    ? slash                // Request: /foo/bar
                    : uri.length());       // Request: /foo
        }
        return uri.substring(1, pos);
    }

    private final void invalidRequestFormat(HttpRequest httpRequest, FullHttpResponse httpResponse) throws IOException {
        httpResponse.setStatus(HttpResponseStatus.valueOf(404));
        httpResponse.content().writeBytes(Unpooled.copiedBuffer("Incorrectly formatted request: " + httpRequest.getUri(), Charset.defaultCharset()));

    }

    private final void unknownAction(String action, FullHttpResponse httpResponse) throws IOException {
        httpResponse.setStatus(HttpResponseStatus.valueOf(404));
        httpResponse.content().writeBytes(Unpooled.copiedBuffer("Unknown action: " + action, Charset.defaultCharset()));
    }


    public void addAction(String path, Action act) {
        actions.put(path, act);
    }
}
