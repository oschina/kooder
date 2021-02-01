package com.gitee.kooder.core;

/**
 * 搜索相关常量定义
 * @author Winter Lau<javayou@gmail.com>
 */
public interface Constants {

    String PRODUCT_NAME = "RedCode";    //产品名称

    String EMPTYSTRING          = "";
    String EMPTYJSON            = "{}";

    int VISIBILITY_PRIVATE  = 0;
    int VISIBILITY_INTERNAL = 1;
    int VISIBILITY_PUBLIC   = 2;

    String TYPE_REPOSITORY      = "repo";     //仓库
    String TYPE_ISSUE           = "issue";    //Issue
    String TYPE_PR              = "pr";       //Pull Requests
    String TYPE_COMMIT          = "commit";   //Commits
    String TYPE_WIKI            = "wiki";     //WIKI
    String TYPE_CODE            = "code";     //Source Code
    String TYPE_USER            = "user";     //Users
    String TYPE_METADATA        = "_metadata";

    byte RECOMM_NONE            = 0x00; //未被推荐项目（推荐级别定义必须递增）
    byte RECOMM                 = 0x01; //推荐项目
    byte RECOMM_GVP             = 0x02; //GVP推荐项目

    byte REPO_BLOCK_YES         = 0x01;
    byte REPO_BLOCK_NO          = 0x00;

    byte REPO_FORK_NO           = 0x00;
    byte REPO_FORK_YES          = 0x01;

    byte ISSUE_PUBLIC           = 1;

    String FIELD_OBJECTS        = "objects";    //json 数据中的对象数字字段名称

    String FACET_VALUE_EMPTY    = "Unknown";    //空 Facet 字段对应的默认值

    /* 文档字段定义 */
    String FIELD_LICENSE        = "license";
    String FIELD_ID             = "id";
    String FIELD_IDENT          = "ident";

    /* 代码相关的字段定义 */
    String FIELD_UUID           = "uuid";
    String FIELD_LANGUAGE       = "lang";
    String FIELD_NAME           = "name";
    String FIELD_TITLE          = "title";
    String FIELD_DISPLAY_NAME   = "display.name";
    String FIELD_DESC           = "desc";
    String FIELD_URL            = "url";
    String FIELD_README         = "readme";
    String FIELD_FORK           = "fork";
    String FIELD_CREATED_AT     = "createdAt";
    String FIELD_UPDATED_AT     = "updatedAt";
    String FIELD_CLOSED_AT      = "closedAt";
    String FIELD_STAR_COUNT     = "starCount";
    String FIELD_FORK_COUNT     = "forkCount";
    String FIELD_G_INDEX        = "gindex";
    String FIELD_TAGS           = "tags";
    String FIELD_CATALOGS       = "catalogs";
    String FIELD_BRANCH         = "branch";

    String FIELD_REPO_ID        = "repo.id";
    String FIELD_REPO_NAME      = "repo.name";
    String FIELD_REPO_PATH      = "repo.path";
    String FIELD_REPO_URL       = "repo.url";

    String FIELD_FILE_NAME      = "file.name";
    String FIELD_FILE_LOCATION  = "file.location";

    //Gitee Enterprise
    String FIELD_ENTERPRISE_ID      = "e.id";
    String FIELD_ENTERPRISE_NAME    = "e.name";
    String FIELD_ENTERPRISE_URL     = "e.url";

    //Gitee Enterprise Program
    String FIELD_PROGRAM_ID         = "p.id";
    String FIELD_PROGRAM_NAME       = "p.name";
    String FIELD_PROGRAM_URL        = "p.url";

    //User
    String FIELD_USER_ID            = "u.id";
    String FIELD_USER_NAME          = "u.name";
    String FIELD_USER_URL           = "u.url";

    String FIELD_CODE_OWNER     = "owner";
    String FIELD_FILE_HASH      = "file.hash";
    String FIELD_SOURCE         = "source";

    String FIELD_RECOMM         = "recomm";
    String FIELD_BLOCK          = "block";
    String FIELD_VISIBILITY     = "visibility";
    String FIELD_LAST_INDEX     = "modified";
    String FIELD_REVISION       = "revision";
    String FIELD_SCM            = "scm";
    String FIELD_STATUS         = "status";
    String FIELD_TIMESTAMP      = "timestamp";

    String FIELD_LINES_TOTAL    = "lines.total";
    String FIELD_LINES_CODE     = "lines.code";
    String FIELD_LINES_BLANK    = "lines.blank";
    String FIELD_LINES_COMMENT  = "lines.comment";
    String FIELD_COMPLEXITY     = "complexity";
}
