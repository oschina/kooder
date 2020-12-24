package com.gitee.search.server;

/**
 * HTTP access log 接口
 * @author Winter Lau<javayou@gmail.com>
 */
@Deprecated
public interface AccessLogger {

    void writeAccessLog(String uri, String msg);

}
