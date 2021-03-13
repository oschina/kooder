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
package com.gitee.kooder.indexer;

import com.gitee.kooder.core.KooderConfig;
import org.apache.commons.lang3.math.NumberUtils;
import org.gitlab4j.api.GitLabApi;

/**
 * Gitlab access instance
 * @author Winter Lau<javayou@gmail.com>
 */
public class Gitlab {

    private static String gitlab_url;
    private static String access_token;
    private static int version;
    private static String gsearch_url;
    private static String system_hook_url;
    private static String project_hook_url;
    private static String secret_token;

    public final static GitLabApi INSTANCE;

    static {
        gitlab_url = KooderConfig.getProperty("gitlab.url");
        access_token = KooderConfig.getProperty("gitlab.personal_access_token");
        version = NumberUtils.toInt(KooderConfig.getProperty("gitlab.version"), 4);
        gsearch_url = KooderConfig.getProperty("http.url");
        system_hook_url = gsearch_url + "/gitlab/system";
        project_hook_url = gsearch_url + "/gitlab/project";
        secret_token = KooderConfig.getProperty("gitlab.secret_token", "gsearch");

        INSTANCE = new GitLabApi((version != 3) ? GitLabApi.ApiVersion.V4 : GitLabApi.ApiVersion.V3, gitlab_url, access_token);
        // Set the connect timeout to 1 second and the read timeout to 5 seconds
        int connectTimeout = NumberUtils.toInt(KooderConfig.getProperty("gitlab.connect_timeout"), 2000);
        int readTimeout = NumberUtils.toInt(KooderConfig.getProperty("gitlab.read_timeout"), 10000);
        INSTANCE.setRequestTimeout(connectTimeout, readTimeout);
    }

}
