package com.gitee.kooder.action;

import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.systemhooks.*;
import org.gitlab4j.api.utils.JacksonJson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重写 SystemHookManager 替换成 vert.x 的 RoutingContext
 * This class provides a handler for processing GitLab System Hook callouts.
 * @author Winter Lau<javayou@gmail.com>
 */
class GitlabSystemHookManager {

    private final static Logger log = LoggerFactory.getLogger(GitlabSystemHookManager.class);

    private final JacksonJson jacksonJson = new JacksonJson();

    private String secretToken;

    /**
     * Create a HookManager to handle GitLab system hook events which will be verified
     * against the specified secretToken.
     *
     * @param secretToken the secret token to verify against
     */
    public GitlabSystemHookManager(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Parses and verifies an SystemHookEvent instance from the HTTP request and
     * fires it off to the registered listeners.
     *
     * @param context the HttpServerRequest to read the Event instance from
     * @throws GitLabApiException if the parsed event is not supported
     */
    public void handleEvent(RoutingContext context) throws GitLabApiException {
        handleRequest(context);
    }

    /**
     * Parses and verifies an SystemHookEvent instance from the HTTP request and
     * fires it off to the registered listeners.
     *
     * @param context the HttpServletRequest to read the Event instance from
     * @return the processed SystemHookEvent instance read from the request,null if the request
     * not contain a system hook event
     * @throws GitLabApiException if the parsed event is not supported
     */
    public SystemHookEvent handleRequest(RoutingContext context) throws GitLabApiException {

        String eventName = context.request().getHeader("X-Gitlab-Event");
        if (eventName == null || eventName.trim().isEmpty()) {
            String message = "X-Gitlab-Event header is missing!";
            log.warn(message);
            return (null);
        }

        //Check secure token
        String secret_token = context.request().getHeader("X-Gitlab-Token");
        if (StringUtils.isNotBlank(this.secretToken) && !StringUtils.equals(this.secretToken, secret_token))
            throw new GitLabApiException("X-Gitlab-Token mismatch!");

        // Get the JSON as a JsonNode tree.  We do not directly unmarshal the input as special handling must
        // be done for "merge_request" events.
        JsonNode tree;
        try {
            tree = jacksonJson.readTree(context.getBodyAsString());
        } catch (Exception e) {
            log.warn("Failed to read JSON data, exception=" + e.getClass().getSimpleName() + ", error=" + e.getMessage());
            throw new GitLabApiException(e);
        }

        // NOTE: This is a hack based on the GitLab documentation and actual content of the "merge_request" event
        // showing that the "event_name" property is missing from the merge_request system hook event.  The hack is
        // to inject the "event_name" node so that the polymorphic deserialization of a SystemHookEvent works correctly
        // when the system hook event is a "merge_request" event.
        if (!tree.has("event_name") && tree.has("object_kind")) {
            String objectKind = tree.get("object_kind").asText();
            if (MergeRequestSystemHookEvent.MERGE_REQUEST_EVENT.equals(objectKind)) {
                ObjectNode node = (ObjectNode)tree;
                node.put("event_name", MergeRequestSystemHookEvent.MERGE_REQUEST_EVENT);
            } else {
                String message = "Unsupported object_kind for system hook event, object_kind=" + objectKind;
                log.warn(message);
                throw new GitLabApiException(message);
            }
        }

        // Unmarshal the tree to a concrete instance of a SystemHookEvent and fire the event to any listeners
        SystemHookEvent event;
        try {
            event = jacksonJson.unmarshal(SystemHookEvent.class, tree);
            event.setRequestUrl(context.request().uri());
            event.setRequestQueryString(context.request().query());
            String secretToken = context.request().getHeader("X-Gitlab-Token");
            event.setRequestSecretToken(secretToken);
        } catch (Exception e) {
            log.warn(String.format("Error processing JSON data, exception=%s, error=%s",
                    e.getClass().getSimpleName(), e.getMessage()));
            throw new GitLabApiException(e);
        }

        try {
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
    public void handleEvent(SystemHookEvent event) throws GitLabApiException {
        if (event != null) {
            log.info("handleEvent:" + event.getClass().getSimpleName() + ", eventName=" + event.getEventName());
            fireEvent(event);
        } else {
            log.warn("handleEvent: provided event cannot be null!");
        }
    }

    /**
     * Fire the event to the registered listeners.
     *
     * @param event the SystemHookEvent instance to fire to the registered event listeners
     * @throws GitLabApiException if the event is not supported
     */
    public void fireEvent(SystemHookEvent event) throws GitLabApiException {

        if (event instanceof ProjectSystemHookEvent) {
            fireProjectEvent((ProjectSystemHookEvent) event);
        } else if (event instanceof TeamMemberSystemHookEvent) {
            fireTeamMemberEvent((TeamMemberSystemHookEvent) event);
        } else if (event instanceof UserSystemHookEvent) {
            fireUserEvent((UserSystemHookEvent) event);
        } else if (event instanceof KeySystemHookEvent) {
            fireKeyEvent((KeySystemHookEvent) event);
        } else if (event instanceof GroupSystemHookEvent) {
            fireGroupEvent((GroupSystemHookEvent) event);
        } else if (event instanceof GroupMemberSystemHookEvent) {
            fireGroupMemberEvent((GroupMemberSystemHookEvent) event);
        } else if (event instanceof PushSystemHookEvent) {
            firePushEvent((PushSystemHookEvent) event);
        } else if (event instanceof TagPushSystemHookEvent) {
            fireTagPushEvent((TagPushSystemHookEvent) event);
        } else if (event instanceof RepositorySystemHookEvent) {
            fireRepositoryEvent((RepositorySystemHookEvent) event);
        } else if (event instanceof MergeRequestSystemHookEvent) {
            fireMergeRequestEvent((MergeRequestSystemHookEvent) event);
        } else {
            String message = "Unsupported event, event_named=" + event.getEventName();
            log.warn(message);
            throw new GitLabApiException(message);
        }
    }

    protected void fireProjectEvent(ProjectSystemHookEvent event) {}

    protected void fireTeamMemberEvent(TeamMemberSystemHookEvent event) {}

    protected void fireUserEvent(UserSystemHookEvent event) {}

    protected void fireKeyEvent(KeySystemHookEvent event) {}

    protected void fireGroupEvent(GroupSystemHookEvent event) {}

    protected void fireGroupMemberEvent(GroupMemberSystemHookEvent event) {}

    protected void firePushEvent(PushSystemHookEvent event) {}

    protected void fireTagPushEvent(TagPushSystemHookEvent event) {}

    protected void fireRepositoryEvent(RepositorySystemHookEvent event) {}

    protected void fireMergeRequestEvent(MergeRequestSystemHookEvent event) {}
}
