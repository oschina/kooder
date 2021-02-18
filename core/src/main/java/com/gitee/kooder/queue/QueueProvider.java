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
package com.gitee.kooder.queue;

import com.gitee.kooder.core.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * 定义了获取索引任务的队列接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface QueueProvider extends AutoCloseable {

    List<String> TYPES = Arrays.asList(Constants.TYPE_REPOSITORY, Constants.TYPE_ISSUE, Constants.TYPE_CODE);

    /**
     * Provider 唯一标识
     * @return
     */
    String name();

    /**
     * 获取支持的所有任务类型
     * @return
     */
    default List<String> types() {
        return TYPES;
    }

    /**
     * 获取某个任务类型的队列
     * @param type
     * @return
     */
    Queue queue(String type);

}
