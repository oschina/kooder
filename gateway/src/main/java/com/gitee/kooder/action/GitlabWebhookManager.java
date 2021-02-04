package com.gitee.kooder.action;

import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.utils.JacksonJson;
import org.gitlab4j.api.webhook.*;
import org.slf4j.LoggerFactory;


/**
 * 重写 WebHookManager 替换成 vert.x 的 RoutingContext
 * This class provides a handler for processing GitLab Web Hook callouts.
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitlabWebhookManager {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(GitlabWebhookManager.class);

    private final JacksonJson jacksonJson = new JacksonJson();

    private String secretToken;

    /**
     * Create a HookManager to handle GitLab webhook events which will be verified
     * against the specified secretToken.
     *
     * @param secretToken the secret token to verify against
     */
    public GitlabWebhookManager(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Parses and verifies an Event instance from the HTTP request and
     * fires it off to the registered listeners.
     *
     * @param context the HttpServletRequest to read the Event instance from
     * @throws GitLabApiException if the parsed event is not supported
     */
    public void handleEvent(RoutingContext context) throws GitLabApiException {
        handleRequest(context);
    }

    /**
     * Parses and verifies an Event instance from the HTTP request and
     * fires it off to the registered listeners.
     *
     * @param context the HttpServletRequest to read the Event instance from
     * @return the Event instance that was read from the request body, null if the request
     * not contain a webhook event
     * @throws GitLabApiException if the parsed event is not supported
     */
    public Event handleRequest(RoutingContext context) throws GitLabApiException {

        String eventName = context.request().getHeader("X-Gitlab-Event");
        if (eventName == null || eventName.trim().isEmpty()) {
            log.warn("X-Gitlab-Event header is missing!");
            return (null);
        }

        //Check secure token
        String secret_token = context.request().getHeader("X-Gitlab-Token");
        if (StringUtils.isNotBlank(this.secretToken) && !StringUtils.equals(this.secretToken, secret_token))
            throw new GitLabApiException("X-Gitlab-Token mismatch!");

        switch (eventName) {

            case IssueEvent.X_GITLAB_EVENT:
            case JobEvent.JOB_HOOK_X_GITLAB_EVENT:
            case MergeRequestEvent.X_GITLAB_EVENT:
            case NoteEvent.X_GITLAB_EVENT:
            case PipelineEvent.X_GITLAB_EVENT:
            case PushEvent.X_GITLAB_EVENT:
            case TagPushEvent.X_GITLAB_EVENT:
            case WikiPageEvent.X_GITLAB_EVENT:
                break;

            default:
                String message = "Unsupported X-Gitlab-Event, event Name=" + eventName;
                log.warn(message);
                throw new GitLabApiException(message);
        }

        Event event;
        try {
            event = jacksonJson.unmarshal(Event.class, context.getBodyAsString());
        } catch (Exception e) {
            log.warn(String.format("Error processing JSON data, exception=%s, error=%s",
                    e.getClass().getSimpleName(), e.getMessage()));
            throw new GitLabApiException(e);
        }

        try {
            event.setRequestUrl(context.request().uri());
            event.setRequestQueryString(context.request().query());
            event.setRequestSecretToken(secretToken);
            fireEvent(event);
            return (event);
        } catch (Exception e) {
            log.warn(String.format("Error processing event, exception=%s, error=%s",
                    e.getClass().getSimpleName(), e.getMessage()));
            throw new GitLabApiException(e);
        }
    }

    /**
     * Verifies the provided Event and fires it off to the registered listeners.
     *
     * @param event the Event instance to handle
     * @throws GitLabApiException if the event is not supported
     */
    public void handleEvent(Event event) throws GitLabApiException {
        switch (event.getObjectKind()) {
            case BuildEvent.OBJECT_KIND:
            case IssueEvent.OBJECT_KIND:
            case JobEvent.OBJECT_KIND:
            case MergeRequestEvent.OBJECT_KIND:
            case NoteEvent.OBJECT_KIND:
            case PipelineEvent.OBJECT_KIND:
            case PushEvent.OBJECT_KIND:
            case TagPushEvent.OBJECT_KIND:
            case WikiPageEvent.OBJECT_KIND:
                fireEvent(event);
                break;

            default:
                String message = "Unsupported event object_kind, object_kind=" + event.getObjectKind();
                log.warn(message);
                throw new GitLabApiException(message);
        }
    }

    /**
     * Fire the event to the registered listeners.
     *
     * @param event the Event instance to fire to the registered event listeners
     * @throws GitLabApiException if the event is not supported
     */
    public void fireEvent(Event event) throws GitLabApiException {

        switch (event.getObjectKind()) {
            case BuildEvent.OBJECT_KIND:
                fireBuildEvent((BuildEvent) event);
                break;

            case IssueEvent.OBJECT_KIND:
                fireIssueEvent((IssueEvent) event);
                break;

            case JobEvent.OBJECT_KIND:
                fireJobEvent((JobEvent) event);
                break;

            case MergeRequestEvent.OBJECT_KIND:
                fireMergeRequestEvent((MergeRequestEvent) event);
                break;

            case NoteEvent.OBJECT_KIND:
                fireNoteEvent((NoteEvent) event);
                break;

            case PipelineEvent.OBJECT_KIND:
                firePipelineEvent((PipelineEvent) event);
                break;

            case PushEvent.OBJECT_KIND:
                firePushEvent((PushEvent) event);
                break;

            case TagPushEvent.OBJECT_KIND:
                fireTagPushEvent((TagPushEvent) event);
                break;

            case WikiPageEvent.OBJECT_KIND:
                fireWikiPageEvent((WikiPageEvent) event);
                break;

            default:
                String message = "Unsupported event object_kind, object_kind=" + event.getObjectKind();
                log.warn(message);
                throw new GitLabApiException(message);
        }
    }

    protected void fireBuildEvent(BuildEvent buildEvent) {
    }

    protected void fireIssueEvent(IssueEvent issueEvent) {
    }

    protected void fireJobEvent(JobEvent jobEvent) {
    }

    protected void fireMergeRequestEvent(MergeRequestEvent mergeRequestEvent) {
    }

    protected void fireNoteEvent(NoteEvent noteEvent) {
    }

    protected void firePipelineEvent(PipelineEvent pipelineEvent) {
    }

    protected void firePushEvent(PushEvent pushEvent) {
    }

    protected void fireTagPushEvent(TagPushEvent tagPushEvent) {
    }

    protected void fireWikiPageEvent(WikiPageEvent wikiPageEvent) {
    }
}
