package com.gitee.search.core;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
