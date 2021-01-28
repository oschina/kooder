package com.gitee.kooder.queue;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 使用 Redis 队列
 * @author Winter Lau<javayou@gmail.com>
 */
public class RedisQueueProvider implements QueueProvider {

    private final static Logger log = LoggerFactory.getLogger(RedisQueueProvider.class);

    private String host;
    private int port;
    private int database;
    private String baseKey;
    private String username;
    private String password;

    private RedisClient client;

    /**
     * Connect to redis
     * @param props
     */
    public RedisQueueProvider(Properties props) {
        this.host = props.getProperty("redis.host", "127.0.0.1");
        this.port = NumberUtils.toInt(props.getProperty("redis.port"), 6379);
        this.database = NumberUtils.toInt(props.getProperty("redis.database"), 1);
        this.baseKey = props.getProperty("redis.key", "gsearch-queue");
        this.username = props.getProperty("username");
        this.password = props.getProperty("password");

        RedisURI uri = RedisURI.create(host,port);
        uri.setDatabase(this.database);
        if(password != null)
            uri.setPassword(password.toCharArray());
        if(username != null)
            uri.setUsername(username);

        this.client = RedisClient.create(uri);

        log.info("Connected to {} at {}}:{}}\n", getRedisVersion(), this.host, this.port);

    }

    private String getRedisVersion() {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            RedisCommands<String, String> cmd = connection.sync();
            return cmd.info("redis_version");
        }
    }

    @Override
    public String name() {
        return "redis";
    }

    @Override
    public Queue queue(String type) {
        return new Queue() {

            private String key = type + '@' + baseKey;

            @Override
            public String type() {
                return type;
            }

            @Override
            public void push(Collection<QueueTask> tasks) {
                try (StatefulRedisConnection<String, String> connection = client.connect()) {
                    RedisCommands<String, String> cmd = connection.sync();
                    cmd.rpush(key, tasks.stream().map(t -> t.json()).toArray(String[]::new));
                }
            }

            @Override
            public List<QueueTask> pop(int count) {
                String json = null;
                List<QueueTask> tasks = new ArrayList<>();
                try (StatefulRedisConnection<String, String> connection = client.connect()) {
                    RedisCommands<String, String> cmd = connection.sync();
                    do{
                        json = cmd.lpop(key);
                        if(json == null)
                            break;
                        QueueTask task = QueueTask.parse(json);
                        if(task != null)
                            tasks.add(task);
                    }while(tasks.size() < count);
                }
                return tasks;
            }

            @Override
            public void close() {}
        };
    }

    @Override
    public void close() {
        client.shutdown();
    }
}
