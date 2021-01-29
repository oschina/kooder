package com.gitee.kooder.code;

import com.gitee.kooder.models.CodeRepository;

/**
 * TODO: SVN 仓库源
 * @author Winter Lau<javayou@gmail.com>
 */
public class SvnRepositoryProvider implements RepositoryProvider {

    @Override
    public String name() {
        return "svn";
    }

    /**
     * 更新仓库
     * @param repo
     * @param traveler
     * @return
     */
    @Override
    public int pull(CodeRepository repo, FileTraveler traveler) {
        return -1;
    }

    /**
     * 删除仓库
     * @param repo
     */
    @Override
    public void delete(CodeRepository repo) {
    }
}
