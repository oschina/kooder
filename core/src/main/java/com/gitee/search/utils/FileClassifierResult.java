package com.gitee.search.utils;

/**
 * 对应 languages.json 中的语言定义
 */
public class FileClassifierResult {

    public String[] extensions;
    public String[] extensionfile;
    public String[] line_comment;
    public String[] complexitychecks;
    public String[][] multi_line;
    public LanguageQuote[] quotes;
    public boolean nestedmultiline;
    public String[] keywords; // Used to identify languages that share extensions
    public String[] filenames;
    public String comment;

    public FileClassifierResult(){}

    public FileClassifierResult(String extensions) {
        this.extensions = extensions.toLowerCase().split(",");
    }

    public FileClassifierResult(String extensions, String keywords) {
        this.extensions = extensions.toLowerCase().split(",");
        this.keywords = keywords.toLowerCase().split(",");
    }
}
