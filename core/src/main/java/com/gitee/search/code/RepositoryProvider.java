package com.gitee.search.code;

import com.gitee.search.utils.TextFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 代码源接口定义
 * @author Winter Lau<javayou@gmail.com>
 */
public interface RepositoryProvider {

    Logger log = LoggerFactory.getLogger(RepositoryProvider.class);

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
     * @return
     */
    RepositoryChanged clone(Repository repo);

    /**
     * 更新仓库
     * @param repo
     * @return
     */
    RepositoryChanged pull(Repository repo);

    /**
     * 读取所有开发者信息
     * @param repo
     * @param fileName
     * @return
     */
    List codeOwners(Repository repo, String fileName);

    /**
     * 遍历所有源码文件
     * @param repo
     * @param changed   变更文件记录，如果 clone = true 则文件列表为空
     * @param traveler
     */
    void travel(Repository repo, RepositoryChanged changed, FileTraveler traveler);

    /**
     * 遍历所有文件
     * @param file
     * @param traveler
     */
    default void travelPath(File file, FileTraveler traveler) {
        String fn = file.getName();
        if(TextFileUtils.ignoreFiles(fn))
            return ;
        if(file.isDirectory()){
            for(File subFile : file.listFiles())
                travelPath(subFile, traveler);
            return ;
        }
        boolean isBinaryFile = TextFileUtils.isBinaryFile(fn);
        CodeIndexDocument doc = this.fileToDocument(file, isBinaryFile);
        if(doc != null)
            traveler.newDocument(doc, isBinaryFile);
    }

    /**
     * 为源码文件生成文档
     * @param file
     * @param isBinaryFile
     * @return
     */
    CodeIndexDocument fileToDocument(File file, boolean isBinaryFile);
}
