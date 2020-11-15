package com.gitee.search.storage;

import java.util.Properties;

/**
 * 索引存储接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface IndexStorage {

    /**
     * 存储的唯一名称
     * @return
     */
    String name();

    /**
     * 初始化配置
     * @param props
     */
    void init(Properties props);
}
