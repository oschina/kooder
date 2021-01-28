package com.gitee.kooder.indexer;

import com.gitee.kooder.core.GiteeSearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Check and Index all of Gitea data for first time
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteaIndexThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger("[gitea]");

    private final static int itemsPerPage = 20;

    private String gsearch_url;
    private String system_hook_url;
    private String secret_token;

    public static void main(String[] args) {
        new GitlabIndexThread().start();
    }

    public GiteaIndexThread() {
        this.gsearch_url = GiteeSearchConfig.getProperty("http.url");
        this.system_hook_url = gsearch_url + "/gitea";
        this.secret_token = GiteeSearchConfig.getProperty("gitea.secret_token", "gsearch");
    }

    @Override
    public void run() {
        long ct = System.currentTimeMillis();

        log.info("Gitea data initialize finished in {} ms.", System.currentTimeMillis() - ct);
    }

}
