package com.gitee.kooder.models;

/**
 * Code Line
 * used to show results of code search
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeLine {

    private int line;       //Line num
    private String code;    //Code

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
