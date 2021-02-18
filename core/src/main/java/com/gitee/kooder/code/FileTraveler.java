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

import com.gitee.kooder.models.SourceFile;

/**
 * 文件遍历回调接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface FileTraveler {

    /**
     * 更新源码文档（新文件、更改文件）
     * @param doc  文档信息
     * @return true: 继续下一个文档， false 不再处理下面文档
     */
    void updateDocument(SourceFile doc);

    /**
     * 删除文档
     * @param doc
     * @return
     */
    void deleteDocument(SourceFile doc);

    /**
     * 清空仓库所有文件，以待重建
     * @param repoId
     */
    void resetRepository(long repoId);

}
