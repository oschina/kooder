package com.gitee.search.action;

/**
 * 搜索相关常量定义
 * @author Winter Lau<javayou@gmail.com>
 */
public class Constants {

    public final static byte RECOMM_NONE    = 0x00; //未被推荐项目（推荐级别定义必须递增）
    public final static byte RECOMM         = 0x01; //推荐项目
    public final static byte RECOMM_GVP     = 0x02; //GVP推荐项目

    public final static byte REPO_TYPE_PRIVATE  = 0x00; //私有仓库
    public final static byte REPO_TYPE_PUBLIC   = 0x01; //公开仓库
    public final static byte REPO_TYPE_INNER    = 0x02; //企业内源仓库

    public final static byte REPO_BLOCK_YES = 0x01;
    public final static byte REPO_BLOCK_NO = 0x00;

    public final static byte REPO_FORK_NO = 0x00;
    public final static byte REPO_FORK_YES = 0x01;

    public final static byte ISSUE_PUBLIC = 1;

}
