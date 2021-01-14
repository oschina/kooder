package com.gitee.search.core;

import com.gitee.search.code.TechCodeAnalyzer;
import com.gitee.search.jcseg.JcsegAnalyzer;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
 * Analyzer 工厂类
 * @author Winter Lau (javayou@gmail.com)
 */
public class AnalyzerFactory {

    private final static Logger log = LoggerFactory.getLogger(AnalyzerFactory.class);

    private static ADictionary dic;
    private static SegmenterConfig config;
    private static SegmenterConfig configForSplit;
    private static TechCodeAnalyzer codeAnalyzer = new TechCodeAnalyzer();
    private static StandardAnalyzer standardAnalyzer = new StandardAnalyzer();

    static {
        config = new SegmenterConfig(true);
        try {
            configForSplit = config.clone();//new SegmenterConfig(true);
        } catch (CloneNotSupportedException e) {}
        configForSplit.setAppendCJKSyn(false);
        configForSplit.setAppendCJKPinyin(false);
        dic = DictionaryFactory.createDefaultDictionary(config, false,true);
        try {
            addCustomLexicon(dic);
        } catch (IOException e ) {
            log.error("Failed to load custom lexicon", e);
        }
    }

    /**
     * 返回分词器
     * @param forIndexer  true:索引用，false:搜索用
     * @return
     */
    public final static Analyzer getInstance(boolean forIndexer) {
        return forIndexer?new JcsegAnalyzer(ISegment.Type.MOST, config, dic):new JcsegAnalyzer(ISegment.Type.MOST, configForSplit, dic);
    }

    /**
     * 返回高亮分词器
     * @return
     */
    public final static Analyzer getHighlightInstance() {
        return new JcsegAnalyzer(ISegment.Type.COMPLEX, configForSplit, dic);
    }

    /**
     * 代码分词器
     * @return
     */
    public final static Analyzer getCodeAnalyzer() {
        return codeAnalyzer;
    }

    /**
     * 用于一些简单的查询条件的解析器
     * @return
     */
    public final static Analyzer getSimpleAnalyzer() { return standardAnalyzer; }

    /**
     * 加载扩展词库
     * @param dic
     * @throws IOException
     */
    private static void addCustomLexicon(ADictionary dic) throws IOException {
        CodeSource codeSrc = AnalyzerFactory.class.getProtectionDomain().getCodeSource();
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

    /**
     * 关键字切分
     *
     * @param sentence 要分词的句子
     * @return 返回分词结果
     */
    public static List<String> splitKeywords(String sentence) {
        List<String> keys = new ArrayList<>();
        if (StringUtils.isNotBlank(sentence)) {
            StringReader reader = new StringReader(sentence);
            ISegment seg = ISegment.NLP.factory.create(configForSplit, dic);
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
