package com.gitee.search.models;

/**
 * 仓库事件
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitlabProjectEvent {

    public final static String E_PROJECT_CREATE     = "project_create";
    public final static String E_PROJECT_DESTROY    = "project_destroy";
    public final static String E_PROJECT_RENAME     = "project_rename";
    public final static String E_PROJECT_TRANSFER   = "project_transfer";
    public final static String E_PROJECT_UPDATE     = "project_update";

}
