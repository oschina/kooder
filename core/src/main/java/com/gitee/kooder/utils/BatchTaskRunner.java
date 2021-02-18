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
package com.gitee.kooder.utils;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

/**
 * Batch task action
 * @author Winter Lau<javayou@gmail.com>
 */
public final class BatchTaskRunner extends RecursiveAction {

    protected int threshold = 5;
    protected List taskList;
    Consumer<List> action;

    /**
     * @param taskList      任务列表
     * @param threshold     每个线程处理的任务数
     */
    private BatchTaskRunner(List taskList, int threshold, Consumer action) {
        this.taskList = taskList;
        this.threshold = threshold;
        this.action = action;
    }

    /**
     * 多线程批量执行任务
     * @param taskList
     * @param threshold
     * @param action
     */
    public static <T> void execute(List<T> taskList, int threshold, Consumer<List<T>> action) {
        new BatchTaskRunner(taskList, threshold, action).invoke();
    }

    @Override
    protected void compute() {
        if (taskList.size() <= threshold) {
            this.action.accept(taskList);
        }
        else {
            this.splitFromMiddle(taskList);
        }
    }

    /**
     * 任务中分
     * @param list
     */
    private void splitFromMiddle(List list) {
        int middle = (int)Math.ceil(list.size() / 2.0);
        List leftList = list.subList(0, middle);
        List RightList = list.subList(middle, list.size());
        BatchTaskRunner left = newInstance(leftList);
        BatchTaskRunner right = newInstance(RightList);
        ForkJoinTask.invokeAll(left, right);
    }

    private BatchTaskRunner newInstance(List taskList) {
        return new BatchTaskRunner(taskList, threshold, action);
    }

}
