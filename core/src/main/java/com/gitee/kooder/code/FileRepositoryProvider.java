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

/**
 * TODO： 基于本地文件的仓库源
 * @author Winter Lau<javayou@gmail.com>
 */
public class FileRepositoryProvider implements RepositoryProvider {

    @Override
    public String name() {
        return "file";
    }

    /**
     * 更新仓库
     * @param repo
     * @param traveler
     * @return
     */
    @Override
    public int pull(CodeRepository repo, FileTraveler traveler) {
        return -1;
    }

    /**
     * 删除仓库
     * @param repo
     */
    @Override
    public void delete(CodeRepository repo) {
    }
}
