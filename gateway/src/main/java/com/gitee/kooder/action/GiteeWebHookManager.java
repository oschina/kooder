package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.gitee.*;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Relation;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.queue.QueueTask;
import com.gitee.kooder.utils.JsonUtils;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

/**
 * This class provides a handler for processing gitee Web Hook callouts.
 *
 * @author zhanggx
 */
class GiteeWebHookManager {

    /**
     * handle gitee web hook
     *
     * @param secretToken
     * @param context
     * @throws GiteeException
     */
    public static void handleEvent(String secretToken, RoutingContext context) throws GiteeException {
        checkSecretToken(secretToken, context);
        GiteeWebHookEvent giteeWebHookEvent = parseGiteeWebHookEvent(context);
        Enterprise enterprise = GiteeApi.getInstance().getEnterprise();

        switch (giteeWebHookEvent) {
            case REPO_HOOK:
                handleRepoHookEvent(context, enterprise);
                break;
            case PUSH_HOOK:
                handlePushHookEvent(context, enterprise);
                break;
            case ISSUE_HOOK:
                handleIssueHookEvent(context, enterprise);
                break;
            default:
                throw new GiteeException("Web hook event unsupported!");
        }
    }

    /**
     * handle repo hook
     *
     * @param context
     * @param enterprise
     * @throws GiteeException action unsupported!
     */
    private static void handleRepoHookEvent(RoutingContext context, Enterprise enterprise) throws GiteeException {
        RepoWebHook repoWebHook = JsonUtils.readValue(context.getBodyAsString(), RepoWebHook.class);
        if (repoWebHook == null) {
            throw new GiteeException("Cannot resolve repo web hook!");
        }

        if (RepoWebHook.ACTION_CREATE.equals(repoWebHook.getAction())) {
            // index repository data
            Repository repository = repoWebHook.getRepository().toKooderRepository();
            repository.setEnterprise(new Relation(enterprise.getId(), enterprise.getName(), enterprise.getUrl()));
            QueueTask.add(Constants.TYPE_REPOSITORY, repository);

        } else if (RepoWebHook.ACTION_DESTROY.equals(repoWebHook.getAction())) {
            // delete repository data
            Repository repository = new Repository();
            repository.setId(repoWebHook.getRepository().getId());
            QueueTask.delete(Constants.TYPE_REPOSITORY, repository);

        } else {
            throw new GiteeException("Repo web hook action unsupported!");
        }
    }

    /**
     * handle push hook
     *
     * @param context
     * @param enterprise
     * @throws GiteeException cannot resolve push web hook
     */
    private static void handlePushHookEvent(RoutingContext context, Enterprise enterprise) throws GiteeException {
        PushWebHook pushWebHook = JsonUtils.readValue(context.getBodyAsString(), PushWebHook.class);
        if (pushWebHook == null) {
            throw new GiteeException("Cannot resolve push web hook!");
        }
        Repository repository = pushWebHook.getRepository().toKooderRepository();
        repository.setEnterprise(new Relation(enterprise.getId(), enterprise.getName(), enterprise.getUrl()));
        CodeRepository codeRepository = new CodeRepository(repository);
        codeRepository.setScm(CodeRepository.SCM_GIT);
        QueueTask.add(Constants.TYPE_CODE, codeRepository);
    }

    /**
     * handle issue hook
     *
     * @param context
     * @param enterprise
     * @throws GiteeException cannot resolve issue web hook
     */
    private static void handleIssueHookEvent(RoutingContext context, Enterprise enterprise) throws GiteeException {
        IssueWebHook issueWebHook = JsonUtils.readValue(context.getBodyAsString(), IssueWebHook.class);
        if (issueWebHook == null) {
            throw new GiteeException("Cannot resolve issue web hook!");
        }
        issueWebHook.getIssue().setRepository(issueWebHook.getRepository());
        Issue issue = issueWebHook.getIssue().toKooderIssue();
        issue.setEnterprise(new Relation(enterprise.getId(), enterprise.getName(), enterprise.getUrl()));
        QueueTask.update(Constants.TYPE_ISSUE, issue);
    }

    /**
     * check web hook secret token
     *
     * @param secretToken
     * @param context
     * @throws GiteeException secret token missing or mismatch
     */
    private static void checkSecretToken(String secretToken, RoutingContext context) throws GiteeException {
        String giteeToken = context.request().getHeader("X-Gitee-Token");
        if (Objects.isNull(giteeToken)) {
            throw new GiteeException("X-Gitee-Token header is missing!");
        } else if (!secretToken.equals(giteeToken)) {
            throw new GiteeException("X-Gitee-Token mismatch!");
        }
    }

    /**
     * parse web hook event
     *
     * @param context
     * @return
     * @throws GiteeException event missing or unknown
     */
    private static GiteeWebHookEvent parseGiteeWebHookEvent(RoutingContext context) throws GiteeException {
        String event = context.request().getHeader("X-Gitee-Event");
        if (event == null) {
            throw new GiteeException("X-Gitee-Event header is missing!");
        } else {
            GiteeWebHookEvent giteeWebHookEvent = GiteeWebHookEvent.getEvent(event);
            if (Objects.nonNull(giteeWebHookEvent)) {
                return giteeWebHookEvent;
            }
            throw new GiteeException("Unknown X-Gitee-Event: " + event + " !");
        }
    }

}
