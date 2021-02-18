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
