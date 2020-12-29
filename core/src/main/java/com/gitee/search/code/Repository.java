package com.gitee.search.code;

import com.searchcode.app.config.Values;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 代码源的定义
 * @author Winter Lau<javayou@gmail.com>
 */
public class Repository {

    private long   id;          //仓库编号
    private String scm;         //代码源类型：gitee/gitlab/gogs/gitea/github/svn/file
    private String name;        //仓库名称
    private String url;         //仓库地址，ex: https://gitee.com/ld/J2Cache
    private String username;    //访问仓库的用户名
    private String password;    //访问仓库的密码

    private String path;        //仓库的存放路径

    // Use this in order to determine checkout directory as otherwise
    // it may be invalid on the filesystem
    public String getDirectoryName() {
        // Must check if name is different and if so append hash to avoid issue of collisions
        String toReturn = this.name.replaceAll("\\W+", Values.EMPTYSTRING);
        if (!toReturn.equals(this.name)) {
            toReturn += "_" + DigestUtils.sha1Hex(this.name);
        }
        return toReturn;
    }


    public long getId() {
        return id;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
