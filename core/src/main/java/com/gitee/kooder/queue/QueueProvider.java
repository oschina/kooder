package com.gitee.kooder.queue;

import com.gitee.kooder.core.GiteeSearchConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 定义了获取索引任务的队列接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface QueueProvider extends AutoCloseable {

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
        Properties props = GiteeSearchConfig.getQueueProperties();
        String type = props.getProperty("types").trim();
        return Arrays.asList(type.split(","));
    }

    /**
     * 获取某个任务类型的队列
     * @param type
     * @return
     */
    Queue queue(String type);

}
