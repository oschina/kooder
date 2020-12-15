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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public JcsegAnalyzer() {
        long ct = System.currentTimeMillis();
        this.config = new SegmenterConfig(true);
        try {
            this.configForSplit = this.config.clone();//new SegmenterConfig(true);
        } catch (CloneNotSupportedException e) {}
        this.configForSplit.setAppendCJKSyn(false);
        this.configForSplit.setAppendCJKPinyin(false);
        this.dic = DictionaryFactory.createDefaultDictionary(config, false,true);
        try {
            this.loadCustomLexicon(dic);
        } catch (IOException e) {
            log.error("Failed to loading custom lexicon", e);
        }
    }

    /**
     * 加载扩展词库
     * @param dic
     * @throws IOException
     */
    private static void loadCustomLexicon(ADictionary dic) throws IOException {
        CodeSource codeSrc = JcsegAnalyzer.class.getProtectionDomain().getCodeSource();
        String codePath = codeSrc.getLocation().getPath();
        if ( codePath.toLowerCase().endsWith(".jar") ) {
            try(ZipInputStream zip = new ZipInputStream(codeSrc.getLocation().openStream())) {
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null) {
                        break;
                    }
                    String fileName = e.getName();
                    if (fileName.endsWith(".lex") && fileName.startsWith("lexicon/lex-")) {
                        try(InputStream stream = JcsegAnalyzer.class.getResourceAsStream("/" + fileName)) {
                            dic.load(stream);
                        }
                    }
                }
            }
        } else {
            //now, the classpath is an IDE directory
            //  like eclipse ./bin or maven ./target/classes/
            //System.out.println()
            File files = new File(URLDecoder.decode(codeSrc.getLocation().getFile(),"utf-8"));
            dic.loadDirectory(files.getPath() + File.separator + "lexicon");
        }
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        try {
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
