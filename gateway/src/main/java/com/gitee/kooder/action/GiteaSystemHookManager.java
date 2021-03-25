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
package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.gitea.*;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.queue.QueueTask;
import com.gitee.kooder.utils.JsonUtils;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

/**
 * This class provides a handler for processing Gitea System Web Hook callouts.
 *
 * @author zhanggx
 */
public class GiteaSystemHookManager {

    public static void handleEvent(String secretToken, RoutingContext context) throws GiteaException {
        checkSecretToken(secretToken, context);
        GiteaWebHookEvent giteaWebHookEvent = parseGiteaWebHookEvent(context);

        switch (giteaWebHookEvent) {
            case REPOSITORY_HOOK:
                handlerRepositoryHookEvent(context);
                break;
            case PUSH_HOOK:
                handlePushHookEvent(context);
                break;
            case ISSUES_HOOK:
                handleIssueHookEvent(context);
                break;
            default:
                throw new GiteaException("Web hook event unsupported!");
        }

    }

    private static void handlerRepositoryHookEvent(RoutingContext context) throws GiteaException {
        RepositoryWebHook repositoryWebHook = JsonUtils.readValue(context.getBodyAsString(), RepositoryWebHook.class);
        if (repositoryWebHook == null) {
            throw new GiteaException("Cannot resolve repository web hook!");
        }

        if (RepositoryWebHook.ACTION_CREATED.equals(repositoryWebHook.getAction())) {
            // index repository data
            Repository repository = repositoryWebHook.getRepository().toKooderRepository();
            QueueTask.add(Constants.TYPE_REPOSITORY, repository);
        } else if (RepositoryWebHook.ACTION_DELETED.equals(repositoryWebHook.getAction())) {
            Repository repository = new Repository();
            repository.setId(repositoryWebHook.getRepository().getId());
            QueueTask.delete(Constants.TYPE_REPOSITORY, repository);
        } else {
            throw new GiteaException("Repository web hook action unsupported!");
        }
    }

    private static void handlePushHookEvent(RoutingContext context) throws GiteaException {
        PushWebHook pushWebHook = JsonUtils.readValue(context.getBodyAsString(), PushWebHook.class);
        if (pushWebHook == null) {
            throw new GiteaException("Cannot resolve push web hook!");
        }
        Repository repository = pushWebHook.getRepository().toKooderRepository();
        CodeRepository codeRepository = new CodeRepository(repository);
        codeRepository.setScm(CodeRepository.SCM_GIT);
        QueueTask.add(Constants.TYPE_CODE, codeRepository);
    }

    private static void handleIssueHookEvent(RoutingContext context) throws GiteaException {
        IssueWebHook issueWebHook = JsonUtils.readValue(context.getBodyAsString(), IssueWebHook.class);
        if (issueWebHook == null) {
            throw new GiteaException("Cannot resolve issue web hook!");
        }
        Issue issue = issueWebHook.getIssue().toKooderIssue(issueWebHook.getRepository());
        QueueTask.update(Constants.TYPE_ISSUE, issue);
    }

    /**
     * check web hook secret token
     *
     * @param secretToken
     * @param context
     * @throws GiteaException secret token missing or mismatch
     */
    private static void checkSecretToken(String secretToken, RoutingContext context) throws GiteaException {
        String secret = context.getBodyAsJson().getString("secret");
        if (Objects.isNull(secret)) {
            throw new GiteaException("Gitea secret is missing!");
        }
        if (!secret.equals(secretToken)) {
            throw new GiteaException("Gitea secret mismatch!");
        }
    }

    /**
     * parse web hook event
     *
     * @param context
     * @return
     * @throws GiteaException event missing or unknown
     */
    private static GiteaWebHookEvent parseGiteaWebHookEvent(RoutingContext context) throws GiteaException {
        String event = context.request().getHeader("X-Gitea-Event");
        if (event == null) {
            throw new GiteaException("X-Gitea-Event header is missing!");
        } else {
            GiteaWebHookEvent giteaWebHookEvent = GiteaWebHookEvent.getEvent(event);
            if (Objects.nonNull(giteaWebHookEvent)) {
                return giteaWebHookEvent;
            }
            throw new GiteaException("Unknown X-Gitea-Event: " + event + " !");
        }
    }

}
