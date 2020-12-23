package com.gitee.search.core;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;

/**
 * Search Toolbox
 * @author Winter Lau (javayou@gmail.com)
 */
public class SearchHelper {

    private final static Logger log = LoggerFactory.getLogger(SearchHelper.class);

    public static void main(String[] args) {
        String text = "Gitee Search是Gitee的搜索引擎服务模块，为Gitee提供仓库、Issue、代码等搜索服务。小程序";
        System.out.println(highlight(text, "gitee search 仓库"));
        //splitKeywords(text).forEach(e -> System.out.println(e));
        System.out.println(cleanupKey("小程序"));
    }

    /**
     * 关键字切分
     *
     * @param sentence 要分词的句子
     * @return 返回分词结果
     */
    public static List<String> splitKeywords(String sentence) {
        return AnalyzerFactory.splitKeywords(sentence);
    }

    /**
     * 重整搜索关键短语
     *
     * @param key
     * @return
     */
    public static String cleanupKey(String key) {
        return String.join(" ", splitKeywords(key));
    }

    /**
     * 对一段文本执行语法高亮处理
     *
     * @param text 要处理高亮的文本
     * @param key  高亮的关键字
     * @return 返回格式化后的HTML文本
     */
    public static String highlight(String text, String key) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(text))
            return text;

        String result = null;

        try {
            Analyzer analyzer = AnalyzerFactory.getInstance(false);
            QueryParser parser = new QueryParser(null, analyzer);
            Query query = parser.parse(key);
            QueryScorer scorer = new QueryScorer(query);
            Formatter fmt = new SimpleHTMLFormatter("<em class='highlight'>", "</em>");
            Highlighter hig = new Highlighter(fmt, scorer);
            TokenStream tokens = analyzer.tokenStream(null, new StringReader(text));
            result = hig.getBestFragment(tokens, text);
        } catch (Exception e) {
            log.error("Unabled to hightlight text", e);
        }

        return (result != null) ? result : text;
    }

}
