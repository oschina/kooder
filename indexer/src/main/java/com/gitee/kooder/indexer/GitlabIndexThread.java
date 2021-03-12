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

import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.core.Constants;
import com.gitee.kooder.core.KooderConfig;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.query.QueryFactory;
import com.gitee.kooder.queue.QueueTask;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * Check and Index all of Gitlab data for first time
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitlabIndexThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger("[gitlab]");

    private final static int itemsPerPage = 20;

    private String gsearch_url;
    private String system_hook_url;
    private String project_hook_url;
    private String secret_token;

    public static void main(String[] args) {
        new GitlabIndexThread().start();
    }

    public GitlabIndexThread() {
        this.gsearch_url = KooderConfig.getProperty("http.url");
        this.system_hook_url = gsearch_url + "/gitlab/system";
        this.project_hook_url = gsearch_url + "/gitlab/project";
        this.secret_token = KooderConfig.getProperty("gitlab.secret_token", "gsearch");
    }

    /**
     * 检查索引库种 repo\issue 的最大值
     * 从最大值+1开始读取gitlab数据库种的新数据并进行索引
     */
    @Override
    public void run() {
        long ct = System.currentTimeMillis();
        try {
            GitLabApi gitlab = this.connect();
            this.checkAndInstallSystemHook(gitlab);
            this.checkAndIndexProjects(gitlab);
            this.checkAndIndexIssues(gitlab);
            log.info("Gitlab data initialize finished in {} ms.", System.currentTimeMillis() - ct);
        } catch (GitLabApiException e) {
            log.error("Failed to initialize gitlab data.", e);
        }
    }

    /**
     * Connect to gitlab
     * @return
     * @throws GitLabApiException
     */
    private GitLabApi connect() throws GitLabApiException {
        GitLabApi gitlab = Gitlab.INSTANCE;
        log.info("Connected to GitLab {} at {}" , gitlab.getVersion().getVersion(), gitlab.getGitLabServerUrl());
        return gitlab;
    }


    /**
     * check and install system hook
     * @param gitlab
     * @throws GitLabApiException
     */
    private void checkAndInstallSystemHook(GitLabApi gitlab) throws GitLabApiException {
        try {
            SystemHooksApi api = gitlab.getSystemHooksApi();
            for (SystemHook hook : api.getSystemHooks()) {
                if (hook.getUrl().equals(system_hook_url))
                    return;
            }
            gitlab.getSystemHooksApi().addSystemHook(system_hook_url, secret_token, true, true, false);
            log.info("Gitlab system hook : {} installed.", system_hook_url);
        } catch (Exception e) {
            log.error("Failed to install gitlab system hook: {}", system_hook_url );
        }
    }

    /**
     * Check and install project hook
     * @param gitlab
     * @param p
     * @throws GitLabApiException
     */
    private void checkAndInstallProjectHook(GitLabApi gitlab, Project p) throws GitLabApiException {
        try {
            for (ProjectHook hook : gitlab.getProjectApi().getHooks(p.getId())) {
                if (hook.getUrl().equals(this.project_hook_url))
                    return;
            }
            ProjectHook hook = new ProjectHook(); //just accept issue event, other event via system hook trigger
            hook.setIssuesEvents(true);
            hook.setPushEvents(false);
            hook.setMergeRequestsEvents(false);
            hook.setRepositoryUpdateEvents(false);
            hook.setTagPushEvents(false);
            hook.setWikiPageEvents(false);
            gitlab.getProjectApi().addHook(p.getId(), this.project_hook_url, hook, true, secret_token);
        } catch (Exception e) {
            log.error("Failed to install gitlab project hook: {}", this.project_hook_url);
        }
    }

    /**
     * check and index projects
     * @param gitlab
     * @throws GitLabApiException
     */
    private void checkAndIndexProjects(GitLabApi gitlab) throws GitLabApiException {
        long ct = System.currentTimeMillis();
        int pc = 0;
        Repository lastRepository = (Repository)QueryFactory.REPO().getLastestObject();
        int maxId = (lastRepository == null) ? 0 : (int)lastRepository.getId();
        //list all repositories with id bigger than {maxId}
        ProjectApi api = gitlab.getProjectApi();
        Pager<Project> projects = api.getProjects(new ProjectFilter().withIdAfter(maxId), itemsPerPage);
        while(projects.hasNext()) {
            for(Project p : projects.next()) {
                //Check and install webhook to this project (Repository hook only handle issue event)
                this.checkAndInstallProjectHook(gitlab, p);
                //index project info
                Repository repo = new Repository(p);
                Map<String, Float> langs = gitlab.getProjectApi().getProjectLanguages(p.getId());
                repo.setLicense(this.selectLang(langs));
                //write to lucene index
                QueueTask.add(Constants.TYPE_REPOSITORY, repo);
                //index code
                CodeRepository codes = new CodeRepository();
                codes.setEnterprise(0); //Gitlab doesn't support enterprise
                codes.setId(p.getId());
                codes.setScm(CodeRepository.SCM_GIT);
                codes.setName(p.getName());
                codes.setUrl(p.getWebUrl());
                //write to lucene index
                QueueTask.add(Constants.TYPE_CODE, codes);
                pc ++;
            }
        }

        log.info("{} repositories indexed (with id > {}), using {} ms", pc, maxId, System.currentTimeMillis() - ct);
    }

    /**
     * Select the largest proportion language as the main programming language for the project
     * @param langs
     * @return
     */
    private String selectLang(Map<String, Float> langs) {
        String result = null;
        float percent = -1f;
        for(String lang : langs.keySet()) {
            if(langs.get(lang) > percent) {
                result = lang;
                percent = langs.get(lang);
            }
        }
        return result;
    }

    /**
     * check and index issues
     * @param gitlab
     * @throws GitLabApiException
     */
    private void checkAndIndexIssues(GitLabApi gitlab) throws GitLabApiException {
        long ct = System.currentTimeMillis();
        int pc = 0;
        Issue lastIssue = (Issue)QueryFactory.ISSUE().getLastestObject();
        long last = (lastIssue == null) ? 0 : lastIssue.getCreatedAt();
        Date lastDate = new Date(last);
        //list all issues with id bigger than {maxId}
        IssuesApi api = gitlab.getIssuesApi();
        Pager<org.gitlab4j.api.models.Issue> issues = api.getIssues(new IssueFilter().withCreatedAfter(lastDate), itemsPerPage);
        while(issues.hasNext()) {
            for(org.gitlab4j.api.models. Issue gitlab_issue : issues.next()) {
                if(gitlab_issue.getCreatedAt().after(lastDate)) {
                    //index issue info
                    Issue issue = new Issue(gitlab_issue);
                    //write to lucene index
                    QueueTask.add(Constants.TYPE_ISSUE, issue);
                    pc++;
                }
            }
        }

        log.info("{} issues indexed (with created_at > {}), using {} ms", pc, last, System.currentTimeMillis() - ct);
    }

}
