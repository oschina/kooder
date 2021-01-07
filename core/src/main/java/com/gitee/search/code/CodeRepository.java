package com.gitee.search.code;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * 代码源的定义
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeRepository {

    public final static String SCM_GIT = "git";
    public final static String SCM_SVN = "svn";
    public final static String SCM_FILE = "file";

    private long   id;          //仓库编号
    private String scm;         //代码源类型：git/svn/file
    private String name;        //仓库名称
    private String url;         //仓库地址，ex: https://gitee.com/ld/J2Cache
    private String username;    //访问仓库的用户名
    private String password;    //访问仓库的密码

    private String path;        //仓库的存放路径
    private String lastCommitId;//最后提交编号

    public long getId() {
        return id;
    }

    public String getIdAsString() {
        return String.valueOf(id);
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean useCredentials() {
        return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password);
    }

    public CredentialsProvider getCredential() {
        return new UsernamePasswordCredentialsProvider(getUsername(), getPassword());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    @Override
    public String toString() {
        return String.format("CodeRepository(%d,%s,%s(%s),%s)", id, name, url, scm, path);
    }
}
