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
package com.gitee.kooder.gitea;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.core.KooderConfig;
import com.gitee.kooder.indexer.GitlabIndexThread;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.models.Searchable;
import com.gitee.kooder.query.QueryFactory;
import com.gitee.kooder.queue.QueueTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
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
        this.gsearch_url = KooderConfig.getProperty("http.url");
        this.system_hook_url = gsearch_url + "/gitea";
        this.secret_token = KooderConfig.getProperty("gitea.secret_token", "gsearch");
    }

    @Override
    public void run() {
        try {
            long ct = System.currentTimeMillis();
            checkAndIndexIssues(checkAndIndexProjects());
            log.info("Gitea data initialize finished in {} ms.", System.currentTimeMillis() - ct);
        } catch (GiteaException e) {
            log.error("Failed to initialize gitea data.", e);
        }
    }

    /**
     * check and index projects data
     *
     * @throws GiteaException get projects data error
     */
    private List<com.gitee.kooder.gitea.Repository> checkAndIndexProjects() throws GiteaException {
        long ct = System.currentTimeMillis();
        int pc = 0;

        com.gitee.kooder.models.Repository lastRepository = (Repository) QueryFactory.REPO().getLastestObject();
        int maxId = lastRepository == null ? 0 : Math.toIntExact(lastRepository.getId());
        List<com.gitee.kooder.gitea.Repository> repositoryList = GiteaApi.getInstance().listUserRepository(maxId);
        for (com.gitee.kooder.gitea.Repository repository : repositoryList) {
            Repository repo = repository.toKooderRepository();
            QueueTask.add(Constants.TYPE_REPOSITORY, repo);
            CodeRepository codes = new CodeRepository();
            codes.setId(repository.getId());
            codes.setEnterprise(repository.getOwner().getId());
            codes.setScm(CodeRepository.SCM_GIT);
            codes.setName(repository.getName());
            codes.setUrl(repository.getHtmlUrl());
            QueueTask.add(Constants.TYPE_CODE, codes);
            pc++;
        }

        log.info("{} repositories indexed (with id > {}), using {} ms", pc, maxId, System.currentTimeMillis() - ct);
        return repositoryList;
    }

    /**
     * check and index issues data
     *
     * @param repositoryList
     * @throws GiteaException get issues data error
     */
    private void checkAndIndexIssues(List<com.gitee.kooder.gitea.Repository> repositoryList) throws GiteaException {
        long ct = System.currentTimeMillis();

        com.gitee.kooder.models.Issue lastIssue = (Issue) QueryFactory.ISSUE().getLastestObject();
        int maxId = lastIssue == null ? 0 : Math.toIntExact(lastIssue.getId());
        List<Issue> issueList = new ArrayList<>();
        for (com.gitee.kooder.gitea.Repository repository : repositoryList) {
            for (com.gitee.kooder.gitea.Issue issue : GiteaApi.getInstance().listRepositoryIssue(repository, maxId)) {
                issueList.add(issue.toKooderIssue(repository));
            }
        }

        issueList.stream()
                .sorted(Comparator.comparingLong(Searchable::getId))
                .forEach(issue -> QueueTask.add(Constants.TYPE_ISSUE, issue));

        log.info("{} issues indexed (with id > {}), using {} ms", issueList.size(), maxId, System.currentTimeMillis() - ct);
    }

}
