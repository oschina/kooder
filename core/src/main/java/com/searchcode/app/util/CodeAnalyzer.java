/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Custom implementation of Lucene Analyzer where we limit to strings 100 characters log.
 * A few other things going on in here.
 * TODO add more details about whats going on
 */
public class CodeAnalyzer extends Analyzer {

    public CodeAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new CodeTokenizer();
        //TokenStream result = new LengthFilter(source, 0, Integer.MAX_VALUE);
        TokenStream result = new LengthFilter(source, 0, 500); // should be enough I hope
        return new TokenStreamComponents(source, result);
    }


    public static void main(String[] args) throws IOException {
        // text to tokenize
        final String text = "This is a demo of the TokenStream API //我是中国人";

        CodeAnalyzer analyzer = new CodeAnalyzer();
        TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

        // get the CharTermAttribute from the TokenStream
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        try {
            stream.reset();

            // print all tokens until stream is exhausted
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString());
            }

            stream.end();
        } finally {
            stream.close();
        }
    }
}

final class CodeTokenizer extends CharTokenizer {
    public CodeTokenizer() {
    }

    public CodeTokenizer(AttributeFactory factory) {
        super(factory);
    }

    // TODO possible performance issue here because its called so much, maybe cache the results so we can test it more quickly
    protected boolean isTokenChar(int c) {
        return !Character.isWhitespace(c);
    }
}

final class LengthFilter extends FilteringTokenFilter {

    private final int min;
    private final int max;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

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
    }

    @Override
    public boolean accept() {
        final int len = termAtt.length();
        return (len >= min && len <= max);
    }

}
