package com.gitee.search.code;

/**
 * 文件遍历回调接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface FileTraveler {

    /**
     * 产生新的源码文档
     * @param doc  文档信息
     * @param isBinaryFile 是否为二进制文件
     * @return true: 继续下一个文档， false 不再处理下面文档
     */
    boolean newDocument(CodeIndexDocument doc, boolean isBinaryFile);

}
