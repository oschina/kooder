package com.gitee.search.code;

/**
 * 代码行，用于处理代码搜索结果的显示
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeLine {

    private int line;
    private String code;

    public CodeLine(int line, String code) {
        this.code = code;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public StringBuffer getCode() {
        return new StringBuffer(code);
    }

    public void setCode(String code) {
        this.code = code;
    }
}
