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

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * New Source code tokenizer
 * @author Winter Lau<javayou@gmail.com>
 */
public class SourceCodeTokenizer extends Tokenizer {

    private final static String separatorChars = " ~!@#$%^&*()-_+[]{}?/\\<>.;,'\"";
    private final static String uselessChars = "{}()[];,";

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private Iterator<IWord> tokens;

    @Override
    public void reset() throws IOException {
        super.reset();
        String code = IOUtils.toString(input);
        List<IWord> words = tokenizer(code);
        if(words != null)
            tokens = words.iterator();
    }

    @Override
    public boolean incrementToken() throws IOException {
        super.clearAttributes();

        if(!tokens.hasNext())
            return false;

        this.addTerm(tokens.next());

        return true;
    }

    /**
     * Split string with separators and keep separators in the result
     * @param code
     * @return
     */
    private static List<IWord> tokenizer(final String code) {
        if (code == null )
            return null;
        final int len = code.length();
        if (len == 0)
            return Collections.EMPTY_LIST;

        List<IWord> words = new ArrayList<>();

        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        // standard case
        while (i < len) {
            char ch = code.charAt(i);
            if(Character.isIdeographic(ch)) {
                words.add(new IWord(ch, i, i + 1));
                start = i + 1;
            }
            else {
                int idx = separatorChars.indexOf(ch);
                if (idx >= 0) {
                    if (match) {
                        lastMatch = true;
                        words.add(new IWord(code.substring(start, i), start, i));
                        match = false;
                    }
                    char sep = separatorChars.charAt(idx);
                    if (!Character.isWhitespace(sep) && uselessChars.indexOf(sep) < 0)
                        words.add(new IWord(sep, i, i + 1));

                    start = ++i;
                    continue;
                }
            }
            lastMatch = false;
            match = true;
            i++;
        }

        if (i > start && (match || lastMatch))
            words.add(new IWord(code.substring(start, i), start, i));

        return words;
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
     * 词条信息
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
        String text = "中文简介public static void main(String[] args) throws IOException {} // c#";

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
