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
import com.gitee.kooder.core.KooderConfig;
import com.gitee.kooder.gitea.GiteaException;
import com.gitee.kooder.server.Action;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle Gitea webhook
 * http://localhost:8080/gitea   web hook  (new project/project updated etc)
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteaAction implements Action {

    String SECRET_TOKEN = KooderConfig.getProperty("gitea.secret_token", Constants.DEFAULT_SECRET_TOKEN);

    /**
     * Gitea webhook handler
     * @param context
     */
    public void index(RoutingContext context) throws GiteaException {
        GiteaSystemHookManager.handleEvent(SECRET_TOKEN, context);
    }

}
