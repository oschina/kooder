package com.gitee.search.index;

/**
 * Lucene Index Exception
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexException extends RuntimeException {

    public IndexException(String message, Throwable cause) {
        super(message, cause);
    }

}
