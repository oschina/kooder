package com.gitee.search.action;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Action 异常
 * @author Winter Lau<javayou@gmail.com>
 * @Deprecated 基于 vertx 的 http 不再需要这个
 */
@Deprecated
public class ActionException extends RuntimeException {

    private HttpResponseStatus errorCode;

    public ActionException() {}

    public ActionException(HttpResponseStatus code) {
        this.errorCode = code;
    }
    public ActionException(HttpResponseStatus code, String message) {
        super("code:"+code.code()+", msg:"+message);
        this.errorCode = code;
    }

    public ActionException(HttpResponseStatus code, String message, Throwable t) {
        super("code:"+code.code()+", msg:"+message, t);
        this.errorCode = code;
    }

    public HttpResponseStatus getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(HttpResponseStatus errorCode) {
        this.errorCode = errorCode;
    }

}
