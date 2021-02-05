package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.gitee.GiteeException;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.gitee.GiteeWebHookEvent;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.gitee.IssueWebHook;
import com.gitee.kooder.gitee.RepoWebHook;
import com.gitee.kooder.queue.QueueTask;
import com.gitee.kooder.utils.JsonUtils;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.Optional;

/**
 * This class provides a handler for processing gitee Web Hook callouts.
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

        switch (giteeWebHookEvent) {
            case REPO_HOO:
                RepoWebHook repoWebHook = JsonUtils.readValue(context.getBodyAsString(), RepoWebHook.class);
                if (repoWebHook == null) {
                    throw new GiteeException("Cannot resolve repo web hook!");
                }

                if (RepoWebHook.ACTION_CREATE.equals(repoWebHook.getAction())) {
                    // index repository data
                    Repository repository = new Repository(repoWebHook.getRepository());
                    QueueTask.add(Constants.TYPE_REPOSITORY, repository);
                    // index code data
                    CodeRepository coder = new CodeRepository(repository);
                    coder.setScm(CodeRepository.SCM_GIT);
                    QueueTask.add(Constants.TYPE_CODE, coder);

                } else if (RepoWebHook.ACTION_DESTROY.equals(repoWebHook.getAction())) {
                    // delete repository data
                    Repository repository = new Repository();
                    repository.setId(repoWebHook.getRepository().getId());
                    QueueTask.delete(Constants.TYPE_REPOSITORY, repository);

                } else {
                    throw new GiteeException("Repo web hook action unsupported!");
                }

                break;
            case PUSH_HOOK:
                Integer projectId = Optional.ofNullable(context.getBodyAsJson())
                        .map(jsonObject -> jsonObject.getJsonObject("project"))
                        .map(jsonObject -> jsonObject.getInteger("id"))
                        .orElseThrow(() -> new GiteeException("Cannot resolve project id!"));
                QueueTask.update(Constants.TYPE_CODE, new CodeRepository(projectId));
                break;
            case ISSUE_HOOK:
                IssueWebHook issueWebHook = JsonUtils.readValue(context.getBodyAsString(), IssueWebHook.class);
                if (issueWebHook == null) {
                    throw new GiteeException("Cannot resolve issue web hook!");
                }
                issueWebHook.getIssue().setRepository(issueWebHook.getRepository());
                Issue issue = new Issue(issueWebHook.getIssue());
                QueueTask.update(Constants.TYPE_ISSUE, issue);
                break;
            default:
                throw new GiteeException("Web hook event unsupported!");
        }
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
