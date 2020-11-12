package com.gitee.search.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;

import java.io.IOException;

/**
 * Using jcseg analyzer
 * @author Winter Lau (javayou@gmail.com)
 */
public class JcsegAnalyzer extends Analyzer {

    private final static SegmenterConfig config = new SegmenterConfig(true);
    private final static ADictionary dic = DictionaryFactory.createDefaultDictionary(config, false,true);

    public final static JcsegAnalyzer INSTANCE = new JcsegAnalyzer(); //Singleton

    private JcsegAnalyzer(){}

    protected TokenStreamComponents createComponents(String fieldName) {
        try {
            Tokenizer tokenizer = new JcsegTokenizer(ISegment.Type.COMPLEX, this.config, this.dic);
            return new TokenStreamComponents(tokenizer);
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
