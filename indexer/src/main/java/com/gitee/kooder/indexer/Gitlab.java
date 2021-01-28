package com.gitee.kooder.indexer;

import com.gitee.kooder.core.GiteeSearchConfig;
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
        gitlab_url = GiteeSearchConfig.getProperty("gitlab.url");
        access_token = GiteeSearchConfig.getProperty("gitlab.personal_access_token");
        version = NumberUtils.toInt(GiteeSearchConfig.getProperty("gitlab.version"), 4);
        gsearch_url = GiteeSearchConfig.getProperty("http.url");
        system_hook_url = gsearch_url + "/gitlab/system";
        project_hook_url = gsearch_url + "/gitlab/project";
        secret_token = GiteeSearchConfig.getProperty("gitlab.secret_token", "gsearch");

        INSTANCE = new GitLabApi((version != 3) ? GitLabApi.ApiVersion.V4 : GitLabApi.ApiVersion.V3, gitlab_url, access_token);
        // Set the connect timeout to 1 second and the read timeout to 5 seconds
        INSTANCE.setRequestTimeout(1000, 5000);
    }

}
