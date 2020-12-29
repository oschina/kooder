package com.gitee.search.utils;

/**
 * 对用 languages.json 文件中的 quote 字段
 */
public class LanguageQuote {

    public String start;
    public String end;
    public boolean ignoreescape;
    public boolean docstring;

    public LanguageQuote(){}

    public LanguageQuote(String start, String end, boolean ignoreescape, boolean docstring) {
        this.start = start;
        this.end = end;
        this.ignoreescape = ignoreescape;
        this.docstring = docstring;
    }
}
