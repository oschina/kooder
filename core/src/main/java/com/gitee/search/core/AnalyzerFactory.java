package com.gitee.search.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;

import java.io.IOException;

/**
 * Analyzer 工厂类
 * @author Winter Lau (javayou@gmail.com)
 */
public class AnalyzerFactory {

    protected static SegmenterConfig defaultConfig = new SegmenterConfig(true);
    protected static ADictionary dic = DictionaryFactory.createDefaultDictionary(defaultConfig, false, true);

    public final static JcsegAnalyzer INSTANCE = new JcsegAnalyzer(); //Singleton

    public final static Analyzer INSTANCE_FOR_SEARCH = new JcsegForSearchAnalyzer();

}

class JcsegForSearchAnalyzer extends Analyzer {

    private SegmenterConfig config;

    public JcsegForSearchAnalyzer() {
        long ct = System.currentTimeMillis();
        try {
            this.config = AnalyzerFactory.defaultConfig.clone();
        } catch(CloneNotSupportedException e){}
        this.config.setAppendCJKSyn(false);
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        try {
            Tokenizer tokenizer = new JcsegTokenizer(ISegment.Type.NLP, this.config, AnalyzerFactory.dic);
            return new TokenStreamComponents(tokenizer);
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}