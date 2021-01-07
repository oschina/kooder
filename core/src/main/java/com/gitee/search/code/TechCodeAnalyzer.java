package com.gitee.search.code;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

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
        String text = "源码: Hello.java: This  is a J2Cache demo of the #CodeAnalyzer .NET API. public void main(String[] args){return 0;} C# C++//我是中国人";
        //String text = FileUtils.readFileToString(new File("D:\\test.txt"));
        //String text = "9615    #";
        //String text = "源码:  Hello.java China";
        //System.out.println(text);

        Analyzer analyzer = new TechCodeAnalyzer();
        try (TokenStream stream = analyzer.tokenStream(null, new StringReader(text))){
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                //System.out.print(termAtt.toString() + "\\");
            }
            System.out.println();
            stream.end();
        }
        //测试高亮
        //System.out.println("HLCODE:" + SearchHelper.hlcode(text, "dotnet"));
    }

}
