package com.gitee.search.code;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * 用来调试分词器
 * @author Winter Lau<javayou@gmail.com>
 */
public class TokenizerTester {

    public final static char[] beginTokens = {'+','#'};
    public final static char[] endTokens = {'.'};
    private static int currentPos = 0;

    public static void main(String[] args) throws IOException {
        String text = "All rights reserved.";
        try(StringReader input = new StringReader(text)){
            do {
                TechCodeTokenizer.IWord ew = nextWord(input);
                if(ew == null)
                    break;
                System.out.printf("%s (%d -> %d) %s\n", ew.word, ew.startOffset, ew.endOffset, text.substring(ew.startOffset, ew.endOffset));
            } while(true);
        }
    }

    /**
     * 分词开始
     * 源码: Hello.java: public
     * @return
     * @throws IOException
     */
    private static TechCodeTokenizer.IWord nextWord(Reader input) throws IOException {
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
            if(isStopChar(ch)) { //停止词
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
        String token = normalize(word.toString(), drifts);
        if(token.length() == 0)
            return nextWord(input);

        int endOffset = currentPos - stopStep - drifts[1];
        int startOffset = endOffset - token.length();

        return new TechCodeTokenizer.IWord(token, startOffset, endOffset);
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
}
