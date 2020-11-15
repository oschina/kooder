package com.gitee.search.queue;

import com.gitee.search.core.IndexManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private String key;
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
        this.key = props.getProperty("redis.key", "gitee-search-queue");
        this.username = props.getProperty("username");
        this.password = props.getProperty("password");

        RedisURI uri = RedisURI.create(host,port);
        uri.setDatabase(this.database);
        if(password != null)
            uri.setPassword(password.toCharArray());
        if(username != null)
            uri.setUsername(username);

        this.client = RedisClient.create(uri);

        log.info(String.format("Connected to %s at %s:%d\n", getRedisVersion(), this.host, this.port));

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
    public void push(List<QueueTask> tasks) {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            RedisCommands<String, String> cmd = connection.sync();
            cmd.rpush(this.key, tasks.stream().map(t -> t.json()).toArray(String[]::new));
        }
    }

    @Override
    public List<QueueTask> pop(int fetchCount) {
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            RedisCommands<String, String> cmd = connection.sync();
            List<QueueTask> tasks = new ArrayList<>();
            do{
                String json = cmd.lpop(this.key);
                if(json == null)
                    break;
                QueueTask task = QueueTask.parse(json);
                if(task != null)
                    tasks.add(task);
                else
                    log.error("Parse queue task failed.\n"+json);
            }while(tasks.size() < fetchCount);
            return tasks;
        }
    }

    @Override
    public void close() {
        client.shutdown();
    }
}
