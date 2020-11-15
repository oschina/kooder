package com.gitee.search.queue;

import com.gitee.search.core.GiteeSearchConfig;

import java.util.Properties;

/**
 * 队列工厂
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueFactory {

    static QueueProvider provider;

    static {
        Properties props = GiteeSearchConfig.getQueueProperties();
        if("redis".equalsIgnoreCase(props.getProperty("type").trim()))
            provider = new RedisQueueProvider(props);
    }

    public final static QueueProvider getProvider() {
        return provider;
    }

}
