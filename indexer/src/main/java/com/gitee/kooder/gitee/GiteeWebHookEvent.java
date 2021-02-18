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
package com.gitee.kooder.gitee;

import java.util.Arrays;

/**
 * @author zhanggx
 */
public enum GiteeWebHookEvent {

    REPO_HOOK("Repo Hook"),
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
