/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.code;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * New Source code tokenizer
 * FIXME : 'stream to string' performance is too poor and it takes up too much memory
 * @author Winter Lau<javayou@gmail.com>
 */
public class SourceCodeTokenizer extends Tokenizer {

    private final static String separatorChars = " ~!@#$%^&*()-_+[]{}?/\\<>.:;,'\"\r\n\t";
    private final static String uselessChars = "\r\n\t "; //{}()[];,

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    //private Iterator<IWord> tokens;
    private ReaderTokens tokens;

    @Override
    public void reset() throws IOException {
        super.reset();
        this.tokens = new ReaderTokens(this.input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        super.clearAttributes();

        IWord token = tokens.next();
        if(token == null)
            return false;

        this.addTerm(token);

        return true;
    }

    /**
     * 返回一个有效词条给 lucene
     * @param ew
     */
    private void addTerm(IWord ew) {
        if(ew == null)
            return ;
        termAtt.append(ew.word);
        termAtt.setLength(ew.word.length());
        offsetAtt.setOffset(correctOffset(ew.startOffset),correctOffset(ew.endOffset));
    }

    /**
     * iterate reader to tokens
     */
    public static class ReaderTokens {

        private int pos = 0;
        private boolean match = false;
        private boolean lastMatch = false;
        private Reader reader;

        private List<IWord> lastTokens = new ArrayList<>();

        public ReaderTokens(Reader reader) {
            this.reader = reader;
        }

        public IWord next() throws IOException {
            if(lastTokens.size() > 0)
                return lastTokens.remove(0);

            StringBuffer word = new StringBuffer();
            do {
                int ch = reader.read();
                pos ++;
                if(ch == -1) // end of stream
                    break;
                if(Character.isIdeographic(ch)) { // chinese
                    IWord cur = new IWord(String.valueOf((char)ch), pos - 1, pos);
                    if(word.length() == 0)
                        return cur;
                    else {
                        lastTokens.add(cur);
                        return new IWord(word.toString(), pos - word.length() - 1, pos - 1);
                    }
                }
                else if(separatorChars.indexOf(ch) >= 0) { // ascii
                    IWord cur = new IWord(String.valueOf((char)ch), pos - 1, pos);
                    if(word.length() == 0) {
                        if (!Character.isWhitespace(ch) && uselessChars.indexOf((char)ch) < 0)
                            return cur;
                    }
                    else {
                        if (!Character.isWhitespace(ch) && uselessChars.indexOf((char)ch) < 0)
                            lastTokens.add(cur);
                        return new IWord(word.toString(), pos - word.length() - 1, pos - 1);
                    }
                }
                else {
                    word.append((char)ch);
                }
            } while (true);

            return (word.length()>0)?new IWord(word.toString(), pos - word.length() - 1, pos - 1) : null;
        }

    }

    /**
     * token and position
     */
    public static class IWord implements Comparable<IWord> {

        public IWord(String word, int start, int end) {
            this.word = word;
            this.startOffset = start;
            this.endOffset = end;
        }
        public IWord(char ch, int start, int end) {
            this(String.valueOf(ch), start, end);
        }

        public String word;
        public int startOffset;
        public int endOffset;

        @Override
        public int compareTo(IWord o) {
            return startOffset - o.startOffset;
        }

        @Override
        public String toString() {
            return "{ " +
                    "word='" + word + '\'' +
                    ", startOffset=" + startOffset +
                    ", endOffset=" + endOffset +
                    " }";
        }
    }

    /**
     * 测试入口
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String text = "hello你好";

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                return new TokenStreamComponents(new SourceCodeTokenizer());
            }
        };
        try (TokenStream stream = analyzer.tokenStream(null, new StringReader(text))){
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString());
            }
            stream.end();
        }
    }

}
