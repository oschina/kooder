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

import java.util.Collection;
import java.util.List;

/**
 * 定义了获取索引任务的队列接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface Queue extends AutoCloseable{

    /**
     * 队列的唯一名称
     * @return
     */
    String type();

    /**
     * 添加任务到队列
     * @param tasks
     */
    void push(Collection<QueueTask> tasks) ;

    /**
     * 从队列获取任务
     * @return
     */
    List<QueueTask> pop(int count) ;

}
