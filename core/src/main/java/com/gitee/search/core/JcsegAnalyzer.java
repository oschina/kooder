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

    private SegmenterConfig config;
    private SegmenterConfig configForSearch;
    private ADictionary dic;

    public final static JcsegAnalyzer INSTANCE = new JcsegAnalyzer(); //Singleton

    private JcsegAnalyzer(){
        this.config = new SegmenterConfig(true);
        this.configForSearch = new SegmenterConfig(true);
        this.configForSearch.setAppendCJKSyn(false);
        this.configForSearch.setClearStopwords(true);
        this.dic = DictionaryFactory.createDefaultDictionary(config, false,true);
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        try {
            Tokenizer tokenizer = new JcsegTokenizer(ISegment.Type.COMPLEX, this.config, this.dic);
            return new TokenStreamComponents(tokenizer);
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public SegmenterConfig getSegmenterConfig() {
        return this.config;
    }

    public SegmenterConfig getSegmenterConfigForSearch() {
        return this.configForSearch;
    }

    public ADictionary getDic() {
        return this.dic;
    }
}
