package com.gitee.search.core;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.*;
import org.lionsoul.jcseg.IWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.lionsoul.jcseg.segmenter.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Search Toolbox
 * @author Winter Lau (javayou@gmail.com)
 */
public class SearchHelper {

    private final static Logger log = LoggerFactory.getLogger(SearchHelper.class);

    public static void main(String[] args) {
        String text = "Gitee Search是Gitee的搜索引擎服务模块，为Gitee提供仓库、Issue、代码等搜索服务。";
        System.out.println(highlight(text, "gitee search 仓库"));
        //splitKeywords(text).forEach(e -> System.out.println(e));
        //System.out.println(cleanupKey(text));
    }

    /**
     * 生成查询条件
     *
     * @param field
     * @param q
     * @param boost
     * @return
     */
    public static Query makeQuery(String field, String q, float boost) {
        QueryParser parser = new QueryParser(field, JcsegAnalyzer.INSTANCE);
        parser.setDefaultOperator(QueryParser.AND_OPERATOR);
        try {
            Query querySinger = parser.parse(q);
            //System.out.println(querySinger.toString());
            return querySinger;
        } catch (Exception e) {
            return new TermQuery(new Term(field, q));
        }
    }

    /**
     * 关键字切分
     *
     * @param sentence 要分词的句子
     * @return 返回分词结果
     */
    public static List<String> splitKeywords(String sentence) {
        List<String> keys = new ArrayList<String>();
        if (StringUtils.isNotBlank(sentence)) {
            StringReader reader = new StringReader(sentence);
            ComplexSeg ikseg = new ComplexSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //NLPSeg ikseg = new NLPSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //DelimiterSeg ikseg = new DelimiterSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //MostSeg ikseg = new MostSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //SimpleSeg ikseg = new SimpleSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //NGramSeg ikseg = new NGramSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            //DetectSeg ikseg = new DetectSeg(JcsegAnalyzer.INSTANCE.getSegmenterConfigForSearch(), JcsegAnalyzer.INSTANCE.getDic());
            try {
                ikseg.reset(reader);
                do {
                    IWord word = ikseg.next();
                    if (word == null)
                        break;
                    String term = word.getValue();
                    //if(term.length() == 1)
                    //    continue;
                    //if(StringUtils.isNumeric(StringUtils.remove(term,'.')))
                    //    continue;
                    keys.add(term);
                } while (true);
            } catch (IOException e) {
                log.error("Unable to split keywords", e);
            }
        }

        return keys.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 重整搜索关键短语
     *
     * @param key
     * @return
     */
    public static String cleanupKey(String key) {
        return String.join(" ", splitKeywords(key));
    }

    /**
     * 对一段文本执行语法高亮处理
     *
     * @param text 要处理高亮的文本
     * @param key  高亮的关键字
     * @return 返回格式化后的HTML文本
     */
    public static String highlight(String text, String key) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(text))
            return text;

        String result = null;

        try {
            QueryParser parser = new QueryParser(null, JcsegAnalyzer.INSTANCE);
            Query query = parser.parse(key);
            QueryScorer scorer = new QueryScorer(query);
            Formatter fmt = new SimpleHTMLFormatter("<em class='highlight'>", "</em>");
            Highlighter hig = new Highlighter(fmt, scorer);
            TokenStream tokens = JcsegAnalyzer.INSTANCE.tokenStream(null, new StringReader(text));
            result = hig.getBestFragment(tokens, text);
        } catch (Exception e) {
            log.error("Unabled to hightlight text", e);
        }

        return (result != null) ? result : text;
    }

    /**
     * get document id
     * @param doc
     * @return
     */
    public static long docid(Document doc) {
        return Long.valueOf(doc.get(SearchObject.FIELD_NAME_ID), 0);
    }

    /**
     * 获取文档对应的对象类
     *
     * @param doc
     * @return
     */
    public static SearchObject doc2obj(Document doc) {
        try {
            long id = docid(doc);
            SearchObject obj = (SearchObject) Class.forName(doc.get(SearchObject.FIELD_NAME_CLASS)).getDeclaredConstructor().newInstance();
            obj.id(id);
            return obj;
        } catch (Exception e) {
            log.error("Unable generate object from document#id=" + doc.toString(), e);
            return null;
        }
    }

    /**
     * turn a SearchObject instance to lucene document
     *
     * @param obj
     * @return
     */
    public static Document obj2doc(SearchObject obj) {
        Document doc = new Document();

        //object id need to be sort and store
        doc.add(new LongPoint(SearchObject.FIELD_NAME_ID, obj.id()));
        doc.add(new StoredField(SearchObject.FIELD_NAME_ID, obj.id()));

        doc.add(new StoredField(SearchObject.FIELD_NAME_CLASS, obj.getClass().getName()));

        //存储字段
        final List<String> fields = obj.storeFields();
        if (fields != null)
            fields.stream().collect(Collectors.toMap(fn -> fn, fv -> readField(obj, fv))).forEach((fn,fv)->addField(doc, fn, fv, true));

        //扩展存储字段
        Map<String, String> esData = obj.extendStoreData();
        if (esData != null)
            esData.entrySet().stream().filter(e -> !fields.contains(e.getKey())).forEach(e -> addField(doc, e.getKey(), e.getValue(), true));

        //索引字段
        List<String> indexFields = obj.indexFields();
        if (fields != null) {
            for (String fn : fields) {
                String fv = (String) readField(obj, fn);
                if (fv != null) {
                    TextField tf = new TextField(fn, fv, Field.Store.NO);
                    doc.add(tf);
                }
            }
        }

        //扩展索引字段
        Map<String, String> eiData = obj.extendIndexData();
        if (eiData != null) {
            for (String fn : eiData.keySet()) {
                if (fields != null && indexFields.contains(fn))
                    continue;
                String fv = eiData.get(fn);
                if (fv != null) {
                    TextField tf = new TextField(fn, fv, Field.Store.NO);
                    doc.add(tf);
                }
            }
        }
        return doc;
    }

    /**
     * 访问对象某个属性的值
     *
     * @param obj   对象
     * @param field 属性名
     * @return Lucene 文档字段
     */
    private static Object readField(Object obj, String field) {
        try {
            return PropertyUtils.getProperty(obj, field);
        } catch (Exception e) {
            log.error("Unabled to get property '" + field + "' of " + obj.getClass().getName(), e);
            return null;
        }

    }

    /**
     * add an object field to document
     * @param doc
     * @param field
     * @param fieldValue
     * @param store
     */
    private static void addField(Document doc, String field, Object fieldValue, boolean store) {
        if (fieldValue == null)
            return ;

        if (fieldValue instanceof Date) //日期
            doc.add(new LongPoint(field, ((Date) fieldValue).getTime()));
        else if (fieldValue instanceof Number) //其他数值
            doc.add(new LongPoint(field, ((Number) fieldValue).longValue()));
        //其他默认当字符串处理
        else {
            doc.add(new StringField(field, (String) fieldValue, store ? Field.Store.YES : Field.Store.NO));
            return;
        }

        if(store)
            doc.add(new StoredField(field, (String) fieldValue));
    }

}
