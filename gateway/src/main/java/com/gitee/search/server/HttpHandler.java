package com.gitee.search.server;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.gitee.search.action.ActionException;
import com.gitee.search.action.ActionExecutor;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;

/**
 * Http requeset handler
 * @author Winter Lau<javayou@gmail.com>
 */
@Deprecated
public class HttpHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger log = LoggerFactory.getLogger(HttpHandler.class);

    //private HttpRequest request;
    private StringBuilder responseData = new StringBuilder();
    private AccessLogger logger;

    public HttpHandler(AccessLogger logger) {
        this.logger = logger;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {

        long ct = System.currentTimeMillis();

        if (msg instanceof HttpRequest) {
            if (HttpUtil.is100ContinueExpected((HttpRequest) msg))
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER));
            responseData.setLength(0);
        }

        if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                Request httpRequest = Request.fromNettyHttpRequest((HttpRequest) msg);
                Response httpResponse;
                try {
                    httpResponse = ActionExecutor.execute(httpRequest);
                } catch (ActionException e) {
                    if (e.getErrorCode() == INTERNAL_SERVER_ERROR)
                        log.error("Failed to call action with '{}'", httpRequest.getUri(), e);
                    httpResponse = Response.error(e.getErrorCode());
                } catch (Exception e) {
                    log.error("Failed to call action with '{}'", httpRequest.getUri(), e);
                    httpResponse = Response.error(INTERNAL_SERVER_ERROR);
                }

                int len = this.writeResponse(ctx, httpRequest, httpResponse);

                this.writeAccessLog(ctx, httpRequest, httpResponse, len, System.currentTimeMillis() - ct);
            }
        }
    }

    /**
     * write result to http response
     * @param ctx
     * @param req
     * @param resp
     * @return
     */
    private int writeResponse(ChannelHandlerContext ctx, Request req, Response resp) {
        boolean keepAlive = req.isKeepAlive();
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, resp.getStatus(), resp.getBody());
        httpResponse.headers().set(HttpHeaderNames.SERVER, "Gitee Search Gateway/1.0");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, resp.getContentType());
        httpResponse.headers().set(HttpHeaderNames.DATE, new Date());

        int len = httpResponse.content().readableBytes();

        if (keepAlive) {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, len);
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        httpResponse.headers().add(resp.getHeaders());

        ctx.write(httpResponse);

        if (!keepAlive)
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

        return len;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unexpected exception occurred.", cause);
        ctx.close();
    }

    /**
     * 显示 access log
     * //61.150.12.23 - - [25/Nov/2020:18:06:08 +0800] "GET /robots.txt HTTP/1.1" 404 34 "-" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36" "-"
     * @param ctx
     * @param req
     * @param resp
     * @param len
     * @param time
     */
    private void writeAccessLog(ChannelHandlerContext ctx, Request req, Response resp, int len, long time) {
        String ua = req.getHeader("User-Agent");
        if(ua == null)
            ua = "-";
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = insocket.getAddress().getHostAddress();
        logger.writeAccessLog(req.getPath(),
                String.format("%s - \"%s %s\" %d %d - %dms - \"%s\"",
                        ip, req.getMethod().name(), req.getUri(), resp.getStatus().code(), len, time, ua));
    }

}
