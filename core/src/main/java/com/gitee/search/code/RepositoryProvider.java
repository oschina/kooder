package com.gitee.search.code;

import java.io.IOException;

/**
 * 代码源接口定义
 * @author Winter Lau<javayou@gmail.com>
 */
public interface RepositoryProvider {

    RepositoryProvider GIT = new GitRepositoryProvider();
    RepositoryProvider GITHUB = new GitRepositoryProvider();
    RepositoryProvider GITLAB = new GitRepositoryProvider();
    RepositoryProvider GITEE = new GitRepositoryProvider();
    RepositoryProvider GOGS = new GitRepositoryProvider();
    RepositoryProvider GITEA = new GitRepositoryProvider();
    RepositoryProvider SVN = new GitRepositoryProvider();

    String name();

    /**
     * 将仓库克隆到指定目录
     * @param repo
     * @param traveler
     * @return
     */
    void clone(CodeRepository repo, FileTraveler traveler);

    /**
     * 更新仓库
     * @param repo
     * @param traveler
     * @return
     */
    void pull(CodeRepository repo, FileTraveler traveler);

    /**
     * 删除仓库
     * @param repo
     * @exception
     */
    void delete(CodeRepository repo) throws IOException ;

}
