package com.gitee.search.server;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.gitee.search.action.ActionException;
import com.gitee.search.action.ActionExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Http requeset handler
 * @author Winter Lau<javayou@gmail.com>
 */
class HttpHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger log = LoggerFactory.getLogger(HttpHandler.class);

    private HttpRequest request;
    private StringBuilder responseData = new StringBuilder();
    private AccessLogger logger;

    HttpHandler(AccessLogger logger) {
        this.logger = logger;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(request))
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER));
            responseData.setLength(0);
        }

        if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                long len = 0;
                int errcode = OK.code();
                LastHttpContent trailer = (LastHttpContent) msg;
                Request httpRequest = Request.fromNettyHttpRequest(request);
                try {
                    Response resp = ActionExecutor.execute(httpRequest);
                    if(resp.getBody() != null)
                        responseData.append(resp.getBody());
                    writeResponse(ctx, trailer, responseData, resp.getContentType());
                    len = responseData.length();
                } catch (ActionException e) {
                    log.error("Failed to call action with '{}'", httpRequest.getUri(), e);
                    writeErrorResponse(ctx, e.getErrorCode());
                    errcode = e.getErrorCode().code();
                } catch (Exception e) {
                    log.error("Failed to call action with '{}'", httpRequest.getUri(), e);
                    writeErrorResponse(ctx, INTERNAL_SERVER_ERROR);
                    errcode = INTERNAL_SERVER_ERROR.code();
                }
                this.showAccessLog(ctx, errcode, len);
            }
        }
    }

    /**
     * 显示 access log
     * //61.150.12.23 - - [25/Nov/2020:18:06:08 +0800] "GET /robots.txt HTTP/1.1" 404 34 "-" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36" "-"
     * @param ctx
     * @param errcode
     * @param len
     */
    private void showAccessLog(ChannelHandlerContext ctx, int errcode, long len) {
        String ua = request.headers().get("user-agent");
        if(ua == null)
            ua = "-";
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = insocket.getAddress().getHostAddress();
        logger.writeAccessLog(request.uri(),
                String.format("%s - \"%s %s\" %d %d - \"%s\"",
                        ip, request.method().name(), request.uri(), errcode, len, ua));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 提取 HTTP 请求中的 Body 内容
     * @param httpContent
     * @return
     */
    private StringBuilder formatBody(HttpContent httpContent) {
        StringBuilder responseData = new StringBuilder();
        ByteBuf content = httpContent.content();
        if (content.isReadable()) {
            responseData.append(content.toString(CharsetUtil.UTF_8));
            responseData.append("\r\n");
        }
        return responseData;
    }

    private void writeResponse(ChannelHandlerContext ctx, LastHttpContent trailer, StringBuilder responseData, String contentType) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HTTP_1_1,
                ((HttpObject) trailer).decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8)
        );
        writeGlobalHeaders(ctx, httpResponse, contentType);
    }

    private void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus errorCode) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, errorCode, Unpooled.EMPTY_BUFFER);
        writeGlobalHeaders(ctx, httpResponse, Response.CONTENT_TYPE_HTML);
    }

    private void writeGlobalHeaders(ChannelHandlerContext ctx, FullHttpResponse httpResponse, String contentType) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        httpResponse.headers().set(HttpHeaderNames.SERVER, "GSearch Gateway 1.0");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        if (keepAlive) {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(httpResponse);

        if (!keepAlive)
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unexpected exception occurred.", cause);
        ctx.close();
    }
}
