package com.gitee.search.query;

/**
 * Lucene Query Exception
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueryException extends RuntimeException {

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

}
