package com.gitee.kooder.code;

import com.gitee.kooder.models.CodeRepository;

/**
 * 代码源接口定义
 * @author Winter Lau<javayou@gmail.com>
 */
public interface RepositoryProvider {

    String name();

    /**
     * 更新仓库
     * @param repo
     * @param traveler
     * @return 返回索引的文件数
     */
    int pull(CodeRepository repo, FileTraveler traveler);

    /**
     * 删除仓库
     * @param repo
     * @exception
     */
    void delete(CodeRepository repo) ;

}
