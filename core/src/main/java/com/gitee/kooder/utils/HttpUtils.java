package com.gitee.kooder.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhanggx
 */
public class HttpUtils {

    public static final MediaType APPLICATION_JSON_UTF8 = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient CLIENT;

    private HttpUtils() {
    }

    static {
        CLIENT = new OkHttpClient();
    }

    public static Response get(String url, Map<String, String> params) throws IOException {
        Request request = getRequestBuilder(url, params).build();
        return getResponse(request);
    }

    public static Response postJson(String url, Map<String, String> params) throws IOException {
        Request request = getRequestBuilder(url)
                .post(RequestBody.create(JsonUtils.toJson(params), APPLICATION_JSON_UTF8))
                .build();
        return getResponse(request);
    }

    private static Request.Builder getRequestBuilder(String url, Map<String, String> params) {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
        if (Objects.nonNull(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                httpUrlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        return new Request.Builder()
                .url(httpUrlBuilder.build());
    }

    private static Request.Builder getRequestBuilder(String url) {
        return getRequestBuilder(url, null);
    }


    private static Response getResponse(Request request) throws IOException {
        return CLIENT.newCall(request).execute();
    }

}
