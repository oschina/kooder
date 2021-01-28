package com.gitee.search.action;

import com.gitee.search.code.CodeRepository;
import com.gitee.search.core.Constants;
import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.indexer.Gitlab;
import com.gitee.search.models.GitlabProjectEvent;
import com.gitee.search.models.Issue;
import com.gitee.search.models.Repository;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.server.Action;
import com.gitee.search.server.GitlabSystemHookManager;
import com.gitee.search.server.GitlabWebhookManager;
import io.vertx.ext.web.RoutingContext;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.systemhooks.*;
import org.gitlab4j.api.webhook.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle gitlab webhook
 * http://localhost:8080/gitlab/system   System hook  (new project/project updated etc)
 * http://localhost:8080/gitlab/project  Project hook (issue\push\pr etc)
 *
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitlabAction implements Action {

    private final static Logger log = LoggerFactory.getLogger(GitlabAction.class);

    private String secret_token = GiteeSearchConfig.getProperty("gitlab.secret_token", "gsearch");

    /**
     * handle system webhook from gitlab
     * @param context
     * @throws GitLabApiException
     */
    public void system(RoutingContext context) throws GitLabApiException {
        GitlabSystemHookManager hookMgr = new GitlabSystemHookManager(secret_token){
            @Override
            protected void fireProjectEvent(ProjectSystemHookEvent event) {
                switch(event.getEventName()) {
                    case GitlabProjectEvent.E_PROJECT_CREATE:
                        //get project object
                        Project p = getProject(event.getProjectId());
                        if(p != null) {
                            Repository repo = new Repository(p);
                            QueueTask.add(Constants.TYPE_REPOSITORY, repo);

                            CodeRepository coder = new CodeRepository(repo);
                            coder.setScm(CodeRepository.SCM_GIT);
                            QueueTask.add(Constants.TYPE_CODE, coder);
                        }
                        break;
                    case GitlabProjectEvent.E_PROJECT_DESTROY:
                        //delete all source codes of this repository
                        CodeRepository coder = new CodeRepository();
                        coder.setId(event.getProjectId());
                        QueueTask.delete(Constants.TYPE_CODE, coder);
                        //delete repository
                        Repository repo = new Repository();
                        repo.setId(event.getProjectId());
                        QueueTask.delete(Constants.TYPE_REPOSITORY, repo);
                        break;
                    default:
                        p = getProject(event.getProjectId());
                        if(p != null) {
                            repo = new Repository(p);
                            QueueTask.update(Constants.TYPE_REPOSITORY, repo);
                        }
                }
            }

            @Override
            protected void firePushEvent(PushSystemHookEvent event) {
                this.fireCodeUpdate(event.getProjectId());
            }

            private void fireCodeUpdate(long pid) {
                CodeRepository repo = new CodeRepository(pid);
                QueueTask.update(Constants.TYPE_CODE, repo); //update source code indexes
            }
        };
        hookMgr.handleEvent(context);
    }

    /**
     * handle project webhook from gitlab
     * @param context
     * @throws GitLabApiException
     */
    public void project(RoutingContext context) throws GitLabApiException {
        GitlabWebhookManager hookMgr = new GitlabWebhookManager(this.secret_token){
            @Override
            protected void fireIssueEvent(IssueEvent e) {
                Issue issue = new Issue(e);
                QueueTask.update(Constants.TYPE_ISSUE, issue);
            }
        };
        hookMgr.handleEvent(context);
    }

    /**
     * Read project detail via gitlab api
     * @param id
     * @return
     */
    private Project getProject(int id) {
        try {
            return Gitlab.INSTANCE.getProjectApi().getProject(id);
        } catch (GitLabApiException e) {
            log.error("Failed to read project object, id = " + id, e);
        }
        return null;
    }

}

