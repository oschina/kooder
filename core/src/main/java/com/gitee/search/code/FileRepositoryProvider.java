package com.gitee.search.code;

import java.io.IOException;

/**
 * TODO： 基于本地文件的仓库源
 * @author Winter Lau<javayou@gmail.com>
 */
public class FileRepositoryProvider implements RepositoryProvider {

    @Override
    public String name() {
        return "file";
    }

    /**
     * 将仓库克隆到指定目录
     *
     * @param repo
     * @param traveler
     * @return
     */
    @Override
    public void clone(CodeRepository repo, FileTraveler traveler) {

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
