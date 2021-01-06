package com.gitee.search.code;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import java.io.IOException;
import java.io.StringReader;

/**
 * 源码分词器
 * @author Winter Lau<javayou@gmail.com>
 */
public class TechCodeAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new TechCodeTokenizer());
    }

    public static void main(String[] args) throws Exception {
        String text = "Hello.java: This  is a J2Cache demo of the #CodeAnalyzer .NET API. public void main(String[] args){return 0;} C# C++//我是中国人";
        //String text = "Hello     world 你好";
        System.out.println(text);
        /*
        Analyzer analyzer = new TechCodeAnalyzer();
        try (TokenStream stream = analyzer.tokenStream(null, new StringReader(text))){
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                //System.out.print(termAtt.toString() + "\\");
            }
            stream.end();
        }*/
        //测试高亮
        System.out.println(testHighlight(text, "j2cache"));
    }

    /**
     * 测试高亮
     * @param code
     * @param key
     * @return
     * @throws Exception
     */
    private static String testHighlight(String code, String key) throws Exception {
        TechCodeAnalyzer analyzer = new TechCodeAnalyzer();
        QueryParser parser = new QueryParser(null, analyzer);
        Query query = parser.parse(key);
        QueryScorer scorer = new QueryScorer(query);
        Formatter fmt = new SimpleHTMLFormatter("<em class='highlight'>", "</em>");
        Highlighter hig = new Highlighter(fmt, scorer);
        TokenStream tokens = analyzer.tokenStream(null, new StringReader(code));
        return hig.getBestFragment(tokens, code);
    }
}

/**
 * 源代码分词器
 * @author Winter Lau<javayou@gmail.com>
 */
final class TechCodeTokenizer extends Tokenizer {

    public final static char[] beginTokens = {'+','#'};
    public final static char[] endTokens = {'.'};

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private int currentPos = 0;

    @Override
    public void reset() throws IOException {
        super.reset();
        this.currentPos = 0;
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        String word = nextWord();
        if(word == null)
            return false;

        int oldWordLen = word.length();
        int[] drifts = {0,0};
        word = this.normalize(word, drifts);

        termAtt.append(word.toLowerCase());
        termAtt.setLength(word.length());
        offsetAtt.setOffset(
            correctOffset(currentPos - oldWordLen + drifts[0]),
            correctOffset(currentPos - drifts[1])
        );

        //TODO 处理 xxx.xxx 的情况
        //TODO 处理同义词的情况

        //System.out.printf("%s (%d->%d) (%d->%d)\n", word, offsetAtt.startOffset(), offsetAtt.endOffset(), drifts[0], drifts[1]);

        return true;
    }

    /**
     * 分词开始
     * @return
     * @throws IOException
     */
    private String nextWord() throws IOException {
        StringBuffer word = new StringBuffer();
        do {
            int ch = input.read();
            if(ch == -1)
                break;
            currentPos ++;
            if(Character.isIdeographic(ch)) {
                word.append((char) ch);
                break;
            }
            if(this.isStopChar(ch)) {
                if(word.length() > 0) {
                    break;
                }
            }
            else {
                word.append((char) ch);
            }
        } while(true);
        String result = word.toString();
        return result.equals("")?null:result;
    }

    /**
     * 删除一些干扰字符
     * @param word
     * @param drifts
     * @return
     */
    private String normalize(String word,  int[] drifts) {
        for(char rechar : beginTokens) {
            while(word.length() > 0 && word.charAt(0) == rechar) {
                word = word.substring(1);
                drifts[0]++;
            }
        }
        //remove end special symbol
        for(char rechar : endTokens) {
            while(word.length() > 0 && word.charAt(word.length()-1) == rechar) {
                word = word.substring(0, word.length() - 1);
                drifts[1] ++;
            }
        }
        return word.toLowerCase();
    }

    /**
     * 判断是否为停止字符
     * @param ch
     * @return
     */
    private boolean isStopChar(int ch) {
        return !Character.isLetterOrDigit(ch) && !contains(beginTokens, ch) && !contains(endTokens, ch);
    }

    private static boolean contains(char[] array, int valueToFind) {
        for(char ch : array)
            if(ch == (char)valueToFind)
                return true;
        return false;
    }
}
