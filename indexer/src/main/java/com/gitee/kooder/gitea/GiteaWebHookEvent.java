package com.gitee.kooder.gitea;

import java.util.Arrays;

/**
 * @author zhanggx
 */
public enum GiteaWebHookEvent {

    REPOSITORY_HOOK("repository"),
    PUSH_HOOK("push"),
    ISSUES_HOOK("issues");

    private String event;

    GiteaWebHookEvent(String event) {
        this.event = event;
    }

    public static GiteaWebHookEvent getEvent(String event) {
        return Arrays.stream(values())
                .filter(giteaWebHookEvent -> giteaWebHookEvent.event.equals(event))
                .findFirst()
                .orElse(null);
    }

}
