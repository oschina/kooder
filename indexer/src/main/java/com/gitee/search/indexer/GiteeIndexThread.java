package com.gitee.search.indexer;

import com.gitee.search.core.GiteeSearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Check and Index all of Gitee data for first time
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteeIndexThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger("[gitee]");

    private final static int itemsPerPage = 20;

    private String gsearch_url;
    private String system_hook_url;
    private String secret_token;

    public static void main(String[] args) {
        new GitlabIndexThread().start();
    }

    public GiteeIndexThread() {
        this.gsearch_url = GiteeSearchConfig.getProperty("http.url");
        this.system_hook_url = gsearch_url + "/gitee";
        this.secret_token = GiteeSearchConfig.getProperty("gitee.secret_token", "gsearch");
    }

    @Override
    public void run() {
        long ct = System.currentTimeMillis();

        log.info("Gitee data initialize finished in {} ms.", System.currentTimeMillis() - ct);
    }

}
