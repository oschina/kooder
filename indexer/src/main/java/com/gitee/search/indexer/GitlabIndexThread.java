package com.gitee.search.indexer;

import com.gitee.search.code.CodeRepository;
import com.gitee.search.core.Constants;
import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.models.Issue;
import com.gitee.search.models.Repository;
import com.gitee.search.query.QueryFactory;
import com.gitee.search.queue.QueueTask;
import org.apache.commons.lang3.math.NumberUtils;
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

    private final static Logger log = LoggerFactory.getLogger(GitlabIndexThread.class);

    private final static int itemsPerPage = 20;

    private String gitlab_url;
    private String access_token;
    private int version;
    private String gsearch_url;
    private String system_hook_url;
    private String project_hook_url;

    public static void main(String[] args) {
        new GitlabIndexThread().start();
    }

    public GitlabIndexThread() {
        this.gitlab_url = GiteeSearchConfig.getProperty("gitlab.url");
        this.access_token = GiteeSearchConfig.getProperty("gitlab.personal_access_token");
        this.version = NumberUtils.toInt(GiteeSearchConfig.getProperty("gitlab.version"), 4);
        this.gsearch_url = GiteeSearchConfig.getProperty("http.url");
        this.system_hook_url = gsearch_url + "/gitlab/system";
        this.project_hook_url = gsearch_url + "/gitlab/project";
    }

    /**
     * 检查索引库种 repo\issue 的最大值
     * 从最大值+1开始读取gitlab数据库种的新数据并进行索引
     */
    @Override
    public void run() {
        long ct = System.currentTimeMillis();
        try (GitLabApi gitlab = this.connect()){
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
        GitLabApi gitlab = new GitLabApi((this.version != 3) ? GitLabApi.ApiVersion.V4 : GitLabApi.ApiVersion.V3, gitlab_url, access_token);
        // Set the connect timeout to 1 second and the read timeout to 5 seconds
        gitlab.setRequestTimeout(1000, 5000);
        log.info("Connected to GitLab {} at {}" , gitlab.getVersion().getVersion(), gitlab_url);
        return gitlab;
    }


    /**
     * check and install system hook
     * @param gitlab
     * @throws GitLabApiException
     */
    private void checkAndInstallSystemHook(GitLabApi gitlab) throws GitLabApiException {
        SystemHooksApi api = gitlab.getSystemHooksApi();
        for(SystemHook hook : api.getSystemHooks()){
            if(hook.getUrl().equals(system_hook_url))
                return ;
        }
        gitlab.getSystemHooksApi().addSystemHook(system_hook_url, Constants.GITLAB_SECRET_TOKEN, true, true, false);
        log.info("Gitlab system hook : {} installed.", system_hook_url);
    }

    /**
     * Check and install project hook
     * @param gitlab
     * @param p
     * @throws GitLabApiException
     */
    private void checkAndInstallProjectHook(GitLabApi gitlab, Project p) throws GitLabApiException {
        for(ProjectHook hook : gitlab.getProjectApi().getHooks(p.getId())){
            if(hook.getUrl().equals(this.project_hook_url))
                return ;
        }
        ProjectHook hook = new ProjectHook();
        hook.setIssuesEvents(true);
        hook.setPushEvents(true);
        hook.setMergeRequestsEvents(true);
        hook.setRepositoryUpdateEvents(true);
        hook.setTagPushEvents(true);
        hook.setWikiPageEvents(true);
        gitlab.getProjectApi().addHook(p.getId(), this.project_hook_url, hook, true, Constants.GITLAB_SECRET_TOKEN);
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
                //check and install webhook to this project
                this.checkAndInstallProjectHook(gitlab, p);
                //index project info
                Repository repo = new Repository(p);
                Map<String, Float> langs = gitlab.getProjectApi().getProjectLanguages(p.getId());
                repo.setLicense(this.selectLang(langs));
                //write to lucene index
                QueueTask.add(Constants.TYPE_REPOSITORY, repo);
                //index code
                CodeRepository codes = new CodeRepository();
                codes.setId(p.getId());
                codes.setScm(CodeRepository.SCM_GIT);
                codes.setName(p.getName());
                codes.setUrl(p.getHttpUrlToRepo());
                //write to lucene index
                QueueTask.add(Constants.TYPE_CODE, codes);
                pc ++;
            }
        }

        log.info("{} repositories indexed (with id > {}), using {} ms", pc, maxId, System.currentTimeMillis() - ct);
    }

    /**
     * 选择占比最大的作为项目的编程语言
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
        long last = (lastIssue == null) ? 0 : (int)lastIssue.getCreatedAt();
        Date lastDate = new Date(last);
        //list all issues with id bigger than {maxId}
        IssuesApi api = gitlab.getIssuesApi();
        Pager<org.gitlab4j.api.models.Issue> issues = api.getIssues(new IssueFilter().withCreatedAfter(lastDate), itemsPerPage);
        while(issues.hasNext()) {
            for(org.gitlab4j.api.models. Issue gitlab_issue : issues.next()) {
                //index issue info
                Issue issue = new Issue(gitlab_issue);
                //write to lucene index
                QueueTask.add(Constants.TYPE_ISSUE, issue);
                pc ++;
            }
        }

        log.info("{} issues indexed (with created_at > {}), using {} ms", pc, last, System.currentTimeMillis() - ct);
    }

}
