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
package com.gitee.kooder.gitee;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.core.KooderConfig;
import com.gitee.kooder.indexer.GitlabIndexThread;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Relation;
import com.gitee.kooder.models.Repository;
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
        this.gsearchUrl = KooderConfig.getProperty("http.url");
        this.systemHookUrl = gsearchUrl + "/gitee";
        this.secretToken = KooderConfig.getProperty("gitee.secret_token", Constants.DEFAULT_SECRET_TOKEN);
    }

    @Override
    public void run() {
        try {
            long ct = System.currentTimeMillis();
            Enterprise enterprise = GiteeApi.getInstance().getEnterprise();
            checkAndInstallEnterpriseHook();
            checkAndIndexProjects(enterprise);
            checkAndIndexIssues(enterprise);
            log.info("Gitee data initialize finished in {} ms.", System.currentTimeMillis() - ct);
        } catch (GiteeException e) {
            log.error("Failed to initialize gitee data.", e);
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
     * @param enterprise
     */
    private void checkAndIndexProjects(Enterprise enterprise) throws GiteeException {
        long ct = System.currentTimeMillis();
        int pc = 0;

        Repository lastRepository = (Repository) QueryFactory.REPO().getLastestObject();
        int maxId = lastRepository == null ? 0 : Math.toIntExact(lastRepository.getId());
        List<com.gitee.kooder.gitee.Repository> repositoryList = GiteeApi.getInstance().getRepos(maxId);
        for (com.gitee.kooder.gitee.Repository repository : repositoryList) {
            Repository repo = repository.toKooderRepository();
            repo.setEnterprise(new Relation(enterprise.getId(), enterprise.getName(), enterprise.getUrl()));
            QueueTask.add(Constants.TYPE_REPOSITORY, repo);
            CodeRepository codes = new CodeRepository();
            codes.setId(repository.getId());
            codes.setEnterprise(enterprise.getId());
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
     * @param enterprise
     * @throws GiteeException get issues data error
     */
    private void checkAndIndexIssues(Enterprise enterprise) throws GiteeException {
        long ct = System.currentTimeMillis();
        int pc = 0;

        Issue lastIssue = (Issue) QueryFactory.ISSUE().getLastestObject();
        int maxId = lastIssue == null ? 0 : Math.toIntExact(lastIssue.getId());
        List<com.gitee.kooder.gitee.Issue> issueList = GiteeApi.getInstance().getIssues(maxId);
        for (com.gitee.kooder.gitee.Issue issue : issueList) {
            Issue kooderIssue = issue.toKooderIssue();
            kooderIssue.setEnterprise(new Relation(enterprise.getId(), enterprise.getName(), enterprise.getUrl()));
            QueueTask.add(Constants.TYPE_ISSUE, kooderIssue);
            pc++;
        }

        log.info("{} issues indexed (with id > {}), using {} ms", pc, maxId, System.currentTimeMillis() - ct);
    }

}
