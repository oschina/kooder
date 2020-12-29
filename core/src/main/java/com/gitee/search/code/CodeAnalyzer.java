package com.gitee.search.code;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;

import java.io.IOException;
import java.io.StringReader;

/**
 * Source Code Analyzer (使用空格对源码进行分词）
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new CodeTokenizer();
        TokenStream result = new LengthFilter(source, 0, 500); // should be enough I hope
        return new TokenStreamComponents(source, result);
    }

    public static void main(String[] args) throws IOException {
        // text to tokenize
        final String text = "This is a demo of the CodeAnalyzer API //我是中国人";

        try (TokenStream stream = new CodeAnalyzer().tokenStream(null, new StringReader(text))){
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            // print all tokens until stream is exhausted
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString());
            }
            stream.end();
        }
    }
}

/**
 * Code tokenizer
 */
class CodeTokenizer extends CharTokenizer {

    public CodeTokenizer() {}

    // TODO possible performance issue here because its called so much,
    //  maybe cache the results so we can test it more quickly
    protected boolean isTokenChar(int c) {
        return !Character.isWhitespace(c);
    }
}

/**
 * Token length filter
 */
class LengthFilter extends FilteringTokenFilter {

    private final CharTermAttribute termAtt;
    private final int min;
    private final int max;

    /**
     * Create a new LengthFilter. This will filter out tokens whose
     * CharTermAttribute is either too short
     * (< min) or too long (> max).
     *
     * @param in  the TokenStream to consume
     * @param min the minimum length
     * @param max the maximum length
     */
    public LengthFilter(TokenStream in, int min, int max) {
        super(in);
        this.min = min;
        this.max = max;
        this.termAtt = addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean accept() {
        final int len = termAtt.length();
        return (len >= min && len <= max);
    }

}

