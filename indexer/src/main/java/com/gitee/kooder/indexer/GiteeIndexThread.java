package com.gitee.kooder.indexer;

import com.gitee.kooder.gitee.GiteeApi;
import com.gitee.kooder.core.Constants;
import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.gitee.GiteeException;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.gitee.EnterpriseHook;
import com.gitee.kooder.query.QueryFactory;
import com.gitee.kooder.queue.QueueTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Check and Index all of Gitee data for first time
 *
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteeIndexThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger("[gitee]");

    /**
     * local address
     */
    private String gsearchUrl;
    /**
     * web hook url
     */
    private String systemHookUrl;
    /**
     * web hook password
     */
    private String secretToken;

    public static void main(String[] args) {
        new GitlabIndexThread().start();
    }

    public GiteeIndexThread() {
        this.gsearchUrl = GiteeSearchConfig.getProperty("http.url");
        this.systemHookUrl = gsearchUrl + "/gitee";
        this.secretToken = GiteeSearchConfig.getProperty("gitee.secret_token", Constants.DEFAULT_SECRET_TOKEN);
    }

    @Override
    public void run() {
        try {
            long ct = System.currentTimeMillis();
            checkAndInstallEnterpriseHook();
            checkAndIndexProjects();
            checkAndIndexIssues();
            log.info("Gitee data initialize finished in {} ms.", System.currentTimeMillis() - ct);
        } catch (GiteeException e) {
            log.error("Failed to initialize gitlab data.", e);
        }
    }

    /**
     * check and install enterprise hook
     *
     * @throws GiteeException install enterprise hook error
     */
    private void checkAndInstallEnterpriseHook() throws GiteeException {
        List<EnterpriseHook> enterpriseHookList = GiteeApi.getInstance().getEnterpriseHooks();
        for (EnterpriseHook enterpriseHook : enterpriseHookList) {
            if (systemHookUrl.equals(enterpriseHook.getUrl())) {
                return;
            }
        }
        GiteeApi.getInstance().createEnterpriseHooks(systemHookUrl, secretToken, true, true, false, true, false, false);
        log.info("Gitee enterprise hook : {} installed.", systemHookUrl);
    }

    /**
     * check and index projects data
     *
     * @throws GiteeException get projects data error
     */
    private void checkAndIndexProjects() throws GiteeException {
        long ct = System.currentTimeMillis();
        int pc = 0;

        Repository lastRepository = (Repository) QueryFactory.REPO().getLastestObject();
        int maxId = lastRepository == null ? 0 : Math.toIntExact(lastRepository.getId());
        List<com.gitee.kooder.gitee.Repository> repositoryList = GiteeApi.getInstance().getRepos(maxId);
        for (com.gitee.kooder.gitee.Repository repository : repositoryList) {
            Repository repo = new Repository(repository);
            QueueTask.add(Constants.TYPE_REPOSITORY, repo);
            CodeRepository codes = new CodeRepository();
            codes.setId(repository.getId());
            codes.setScm(CodeRepository.SCM_GIT);
            codes.setName(repository.getName());
            codes.setUrl(repository.getHtmlUrl());
            QueueTask.add(Constants.TYPE_CODE, codes);
            pc++;
        }

        log.info("{} repositories indexed (with id > {}), using {} ms", pc, maxId, System.currentTimeMillis() - ct);
    }

    /**
     * check and index issues data
     *
     * @throws GiteeException get issues data error
     */
    private void checkAndIndexIssues() throws GiteeException {
        long ct = System.currentTimeMillis();
        int pc = 0;

        Issue lastIssue = (Issue) QueryFactory.ISSUE().getLastestObject();
        int maxId = lastIssue == null ? 0 : Math.toIntExact(lastIssue.getId());
        List<com.gitee.kooder.gitee.Issue> issueList = GiteeApi.getInstance().getIssues(maxId);
        for (com.gitee.kooder.gitee.Issue issue : issueList) {
            QueueTask.add(Constants.TYPE_ISSUE, new Issue(issue));
            pc++;
        }

        log.info("{} issues indexed (with id > {}), using {} ms", pc, maxId, System.currentTimeMillis() - ct);
    }

}
