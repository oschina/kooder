/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * http util by okhttp3
 *
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

    /**
     * http get
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static Response get(String url, Map<String, String> params) throws IOException {
        Request request = getRequestBuilder(url, params).build();
        return getResponse(request);
    }

    /**
     * http post application/json
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
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
