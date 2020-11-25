package com.gitee.search.core;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.IWord;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Using jcseg analyzer
 * @author lionsoul<chenxin619315@gmail.com>
 * @author Winter Lau (javayou@gmail.com)
 */
public class JcsegAnalyzer extends Analyzer {

    private final static Logger log = LoggerFactory.getLogger(JcsegAnalyzer.class);

    private SegmenterConfig config;
    private SegmenterConfig configForSplit;
    private ADictionary dic;

    public JcsegAnalyzer(){
        long ct = System.currentTimeMillis();
        this.config = new SegmenterConfig(true);
        this.configForSplit = new SegmenterConfig(true);
        this.configForSplit.setAppendCJKSyn(false);
        this.dic = DictionaryFactory.createDefaultDictionary(config, false,true);
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        try {
            //MOST ?
            Tokenizer tokenizer = new JcsegTokenizer(ISegment.Type.MOST, this.config, this.dic);
            return new TokenStreamComponents(tokenizer);
        } catch (IOException e) {
            log.error("Failed to createComponents({})", fieldName, e);
            return null;
        }
    }

    public SegmenterConfig getSegmenterConfig() {
        return this.config;
    }

    public ADictionary getDic() {
        return this.dic;
    }


    /**
     * 关键字切分
     *
     * @param sentence 要分词的句子
     * @return 返回分词结果
     */
    public List<String> splitKeywords(String sentence) {
        List<String> keys = new ArrayList<>();
        if (StringUtils.isNotBlank(sentence)) {
            StringReader reader = new StringReader(sentence);
            ISegment seg = ISegment.NLP.factory.create(this.configForSplit, getDic());
            //ComplexSeg ikseg = new ComplexSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //NLPSeg seg = new NLPSeg(this.configForSplit, getDic());
            //DelimiterSeg ikseg = new DelimiterSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //MostSeg ikseg = new MostSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //SimpleSeg ikseg = new SimpleSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //NGramSeg ikseg = new NGramSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //DetectSeg ikseg = new DetectSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            try {
                IWord word = null;
                seg.reset(reader);
                while((word = seg.next()) != null){
                    keys.add(word.getValue());
                }
            } catch (IOException e) {
                log.error("Unable to split keywords", e);
            }
        }
        //去重
        return keys.stream().distinct().collect(Collectors.toList());
    }

}
