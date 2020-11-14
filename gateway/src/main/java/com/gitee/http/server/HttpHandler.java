package com.gitee.http.server;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.gitee.http.action.ActionExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Http requeset handler
 * @author Winter Lau<javayou@gmail.com>
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger log = LoggerFactory.getLogger(HttpHandler.class);
    private HttpRequest request;
    StringBuilder responseData = new StringBuilder();

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
                LastHttpContent trailer = (LastHttpContent) msg;
                QueryStringDecoder uri_decoder = new QueryStringDecoder(request.uri());
                try {
                    StringBuilder resp = ActionExecutor.execute(uri_decoder.path(), uri_decoder.parameters(), formatBody(trailer));
                    responseData.append(resp);
                    writeResponse(ctx, trailer, responseData);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);
                } catch (IllegalAccessException e) {
                    writeErrorResponse(ctx, HttpResponseStatus.FORBIDDEN);
                } catch (Exception e) {
                    log.error("Failed to call action with '" + uri_decoder.path() + "'", e);
                    writeErrorResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private StringBuilder formatBody(HttpContent httpContent) {
        StringBuilder responseData = new StringBuilder();
        ByteBuf content = httpContent.content();
        if (content.isReadable()) {
            responseData.append(content.toString(CharsetUtil.UTF_8).toUpperCase());
            responseData.append("\r\n");
        }
        return responseData;
    }

    private void writeResponse(ChannelHandlerContext ctx, LastHttpContent trailer, StringBuilder responseData) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HTTP_1_1,
                ((HttpObject) trailer).decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8)
        );
        writeGlobalHeaders(ctx, httpResponse);
    }

    private void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus errorCode) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, errorCode, Unpooled.EMPTY_BUFFER);
        writeGlobalHeaders(ctx, httpResponse);
    }

    private void writeGlobalHeaders(ChannelHandlerContext ctx, FullHttpResponse httpResponse) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        httpResponse.headers().set(HttpHeaderNames.SERVER, "Gitee Search Gateway 1.0");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
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
