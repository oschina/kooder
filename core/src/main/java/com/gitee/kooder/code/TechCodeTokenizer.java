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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 源代码分词器
 * @author Winter Lau<javayou@gmail.com>
 */
public final class TechCodeTokenizer extends Tokenizer {

    public final static int MAX_TERM_LENGTH = 128;

    public final static char[] beginTokens = {'+','#'};
    public final static char[] endTokens = {'.'};
    public final static Map<String, String[]> synonymWords = new HashMap(){{
        put(".net", new String[]{"dotnet"});
        put("c#",   new String[]{"csharp"});
        put("c++",  new String[]{"cpp"});
    }};

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private int currentPos = 0;
    private SortedSet<IWord> extendWords = new TreeSet();

    @Override
    public void reset() throws IOException {
        super.reset();
        this.currentPos = 0;
    }

    @Override
    public boolean incrementToken() throws IOException {
        super.clearAttributes();

        EXTEND_WORDS: //处理 .xxx.xxx 的情况
        if(extendWords.size() > 0) {
            IWord ew = extendWords.first();
            if (ew != null) {
                //System.out.printf("%s (%d -> %d)\n", ew.word, ew.startOffset, ew.endOffset);
                this.addTerm(ew);
                extendWords.remove(ew);
                return true;
            }
        }

        IWord ew = this.nextWord();
        if(ew == null)
            return false;

        this.addTerm(ew);
        //处理 .xxx.xxx 的情况，处理完后由 :EXTEND_WORDS 初代码进行返回
        this.computeSubWords(ew);
        //处理同义词
        this.computeSynonym(ew);

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
     * 分词开始
     * 源码: Hello.java: public
     * @return
     * @throws IOException
     */
    public IWord nextWord() throws IOException {
        StringBuffer word = new StringBuffer();
        int stopStep = 0;
        do {
            int ch = input.read();
            currentPos ++; //指针前移
            if(ch == -1) { //读取结束
                if(word.length() == 0)
                    return null;
                stopStep ++;
                break;
            }

            if(Character.isIdeographic(ch)) { //读到中文字符，按字返回
                word.append((char) ch);
                break;
            }
            if(this.isStopChar(ch)) { //停止词
                if(word.length() > 0) {
                    stopStep ++;
                    break;
                }
            }
            else { //常规字符
                word.append((char) ch);
            }
        } while(true);

        //对一些 #+ 开头，.结尾的词汇进行规范化处理
        int[] drifts = {0,0};
        String token = this.normalize(word.toString(), drifts);
        if(token.length() == 0 || token.length() > MAX_TERM_LENGTH)
            return nextWord();

        int endOffset = currentPos - stopStep - drifts[1];
        int startOffset = Math.max(0, endOffset - token.length());

        return new IWord(token, startOffset, endOffset);
    }

    /**
     * 分解点号间隔的词条
     * @param ew
     * @return
     */
    private boolean computeSubWords(IWord ew) {
        int hasSub = 0;
        if(ew.word.indexOf('.', 1)>0) {
            String[] pics = ew.word.split("\\.");
            for(int i=0;i<pics.length;i++) {
                if("".equals(pics[i]))
                    continue;
                int so = ew.word.indexOf(pics[i]) + ew.startOffset;
                IWord iw = new IWord(pics[i], so, so + pics[i].length());
                iw.type = IWord.TYPE_CHILD;
                extendWords.add(iw);
                hasSub ++;
            }
        }
        return hasSub > 0;
    }

    /**
     * 处理同义词
     * @param ew
     * @return
     */
    private boolean computeSynonym(IWord ew) {
        int sync = 0;
        String[] synonyms = synonymWords.get(ew.word);
        if(synonyms != null)
            for(String syn : synonyms) {
                IWord iw = new IWord(syn, ew.startOffset, ew.endOffset);
                iw.type = IWord.TYPE_SYNONY;
                extendWords.add(iw);
            }
        return sync > 0;
    }

    /**
     * 删除一些干扰字符
     * @param word
     * @param drifts
     * @return
     */
    private static String normalize(String word,  int[] drifts) {
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
    private static boolean isStopChar(int ch) {
        return !Character.isLetterOrDigit(ch) && !contains(beginTokens, ch) && !contains(endTokens, ch);
    }

    private static boolean contains(char[] array, int valueToFind) {
        for(char ch : array)
            if(ch == (char)valueToFind)
                return true;
        return false;
    }

    /**
     * 词条信息
     */
    public static class IWord implements Comparable<IWord> {

        public final static byte TYPE_WORD      = 0x01;
        public final static byte TYPE_CHILD     = 0x02;
        public final static byte TYPE_SYNONY    = 0x03;

        public IWord(String word, int start, int end) {
            this.word = word;
            this.startOffset = start;
            this.endOffset = end;
        }
        public String word;
        public int startOffset;
        public int endOffset;
        public int type = TYPE_WORD;

        @Override
        public int compareTo(IWord o) {
            return startOffset - o.startOffset;
        }
    }

}
