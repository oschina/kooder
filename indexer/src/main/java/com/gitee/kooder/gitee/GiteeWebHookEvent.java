package com.gitee.kooder.gitee;

import java.util.Arrays;

/**
 * @author zhanggx
 */
public enum GiteeWebHookEvent {

    REPO_HOO("Repo Hook"),
    PUSH_HOOK("Push Hook"),
    ISSUE_HOOK("Issue Hook");

    private String event;

    GiteeWebHookEvent(String event) {
        this.event = event;
    }

    public static GiteeWebHookEvent getEvent(String event) {
        return Arrays.stream(values())
                .filter(giteeWebHookEvent -> giteeWebHookEvent.event.equals(event))
                .findFirst()
                .orElse(null);
    }

}
