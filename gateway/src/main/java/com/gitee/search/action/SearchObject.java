package com.gitee.search.action;

/**
 * 搜索相关常量定义
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchObject {

    public final static byte RECOMM_NONE    = 0x00; //未被推荐项目（推荐级别定义必须递增）
    public final static byte RECOMM         = 0x01; //推荐项目
    public final static byte RECOMM_GVP     = 0x02; //GVP推荐项目

    public final static byte REPO_TYPE_PUBLIC   = 0x01; //公开仓库
    public final static byte REPO_TYPE_PRIVATE  = 0x02; //私有仓库
    public final static byte REPO_TYPE_INNER    = 0x03; //企业内源仓库

}
