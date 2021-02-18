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
package com.gitee.kooder.code;

import com.gitee.kooder.models.CodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 各种仓库源的管理
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepositoryFactory {

    private final static Logger log = LoggerFactory.getLogger(RepositoryFactory.class);

    private final static Map<String, RepositoryProvider> providers = new HashMap(){{
        put(CodeRepository.SCM_GIT,     new GitRepositoryProvider());
        put(CodeRepository.SCM_SVN,     new SvnRepositoryProvider());
        put(CodeRepository.SCM_FILE,    new FileRepositoryProvider());
    }};

    /**
     * 根据 scm 获取仓库操作类实例
     * @param scm
     * @return
     */
    public final static RepositoryProvider getProvider(String scm) {
        return providers.get(scm);
    }

}
