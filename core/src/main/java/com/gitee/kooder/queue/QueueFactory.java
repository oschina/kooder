package com.gitee.kooder.queue;

import com.gitee.kooder.core.GiteeSearchConfig;

import java.util.Properties;

/**
 * 队列工厂
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueFactory {

    static QueueProvider provider;

    static {
        Properties props = GiteeSearchConfig.getQueueProperties();
        String type = props.getProperty("provider").trim();
        if("redis".equalsIgnoreCase(type))
            provider = new RedisQueueProvider(props);
        else if("embed".equalsIgnoreCase(type))
            provider = new EmbedQueueProvider(props);
    }

    public final static QueueProvider getProvider() {
        return provider;
    }

}
