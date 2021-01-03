package com.gitee.search.code;

import java.io.IOException;

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
     *
     * @param repo
     * @param traveler
     * @return
     */
    @Override
    public void pull(CodeRepository repo, FileTraveler traveler) {

    }

    /**
     * 删除仓库
     *
     * @param repo
     * @throws
     */
    @Override
    public void delete(CodeRepository repo) throws IOException {

    }
}
