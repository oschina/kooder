package com.gitee.search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.queue.QueueTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * json <-> document
 * @author Winter Lau<javayou@gmail.com>
 */
public class ObjectMapping {

    private final static Logger log = LoggerFactory.getLogger(ObjectMapping.class);

    public final static String FIELD_ID = "id"; //文档的唯一标识
    public final static String FIELD_objects = "objects";

    public final static String FACET_VALUE_EMPTY = "Unknown";

    /**
     * 文档结果返回json
     * @param type
     * @param docs
     * @return
     * @throws IOException
     */
    public final static List<Map<String, Object>> doc2json(String type, List<Document> docs) throws IOException {
        return docs.stream().map(d -> doc2json(type, d)).collect(Collectors.toList());
    }

    /**
     * Lucene 文档转 json
     * @param type
     * @param doc
     * @return
     * @throws IOException
     */
    public final static Map<String, Object> doc2json(String type, Document doc) {
        Map<String, Object> map = new HashMap<>();
        IndexMapping mapping = IndexMapping.get(type);
        doc.forEach( field -> saveFieldToMap(field, map, mapping));
        return map;
    }

    /**
     * 读取 field 信息并保存到 Map
     * @param field
     * @param map
     * @param mapping
     */
    private static void saveFieldToMap(IndexableField field, Map<String, Object> map, IndexMapping mapping) {
        String fname = field.name();
        IndexMapping.Settings setting = mapping.getField(fname);
        Object fvalue = readFieldValue(field, setting);
        String[] names = fname.split("\\.");
        saveFieldToMap(names, fvalue, map);
    }

    private static void saveFieldToMap(String[] names, Object value, Map<String, Object> map) {
        if(names.length == 1)
            map.put(names[0], value);
        else {
            Map<String, Object> subMap = (Map<String, Object>)map.computeIfAbsent(names[0], m -> new HashMap<String, Object>());
            String[] sub_names = new String[names.length - 1];
            System.arraycopy(names, 1, sub_names, 0, names.length - 1);
            saveFieldToMap(sub_names, value, subMap);
        }
    }

    /**
     * 根据 json mapping 来解析文档字段值
     * @param field
     * @param settings
     * @return
     */
    private static Object readFieldValue(IndexableField field, IndexMapping.Settings settings) {
        return settings.isNumber()?field.numericValue().longValue():field.stringValue();
    }

    /**
     * 将 task 转成 lucene 文档
     * @param task
     * @return
     * @exception
     */
    public final static List<Document> task2doc(QueueTask task) throws IOException {
        List<Document> docs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        IndexMapping mapping = IndexMapping.get(task.getType());
        Iterator<JsonNode> objects = mapper.readTree(task.getBody()).withArray(FIELD_objects).elements();
        while(objects.hasNext()) {
            JsonNode obj = objects.next();
            Document doc = new Document();
            parseObjectJson("", obj, doc, mapping);
            docs.add(doc);
        }
        return docs;
    }

    /**
     * 将 object json 转成 lucene 文档
     * @param prefix 字段名前缀
     * @param node
     * @param doc
     * @param mapping
     * @return
     */
    private static void parseObjectJson(String prefix, JsonNode node, Document doc, IndexMapping mapping) {
        node.fields().forEachRemaining( e -> {
            String key = e.getKey();
            String fn = (prefix.length() == 0) ? key : (prefix + "." + key);
            JsonNode field = e.getValue();
            if(field == null || field.isNull())
                return ;
            if(field.isArray()) { //handle array node
                if(field.size() == 0)
                    return ;
                String[] values = new String[field.size()];
                for(int i=0;i<field.size();i++)
                    values[i] = field.get(i).textValue();
                doc.add(new TextField(fn, String.join(",", values),
                        mapping.getField(fn).isStore() ? Field.Store.YES : Field.Store.NO));
            }
            else if(field.isObject()) //nested object node
                parseObjectJson(fn, field, doc, mapping);
            else //simple node
                addSimpleField(doc, fn, field, mapping.getField(fn));
        });
    }

    /**
     * 将 json field 转成 document field
     * @param doc
     * @param fn
     * @param field
     * @param setting
     * @return
     */
    private static void addSimpleField(Document doc, String fn, JsonNode field, IndexMapping.Settings setting) {
        try {
            if(field.isIntegralNumber()){ //整数值
                if(setting.isFacet())
                    doc.add(new FacetField(fn, String.valueOf(field.longValue())));
                doc.add(new NumericDocValuesField(fn, field.longValue()));
                if (setting.isStore())
                    doc.add(new StoredField(fn, field.longValue()));
            }
            else if(field.isFloatingPointNumber()) {
                if(setting.isFacet())
                    doc.add(new FacetField(fn, String.valueOf(field.floatValue())));
                doc.add(new FloatDocValuesField(fn, field.floatValue()));
                if (setting.isStore())
                    doc.add(new StoredField(fn, field.floatValue()));
            }
            else if(field.isTextual()) {//文本内容
                String fnv = field.textValue();
                if(setting.isFacet()) {
                    doc.add(new FacetField(fn, StringUtils.isBlank(fnv)?FACET_VALUE_EMPTY:fnv));
                    if(StringUtils.isNotBlank(fnv))
                        doc.add(new SortedDocValuesField(fn, new BytesRef(fnv)));
                    if(setting.isStore())
                        doc.add(new StringField(fn, fnv, Field.Store.YES));
                }
                else if(setting.isString()){
                    doc.add(new StringField(fn, fnv, setting.isStore() ? Field.Store.YES : Field.Store.NO));
                }
                else {
                    doc.add(new TextField(fn, fnv, setting.isStore() ? Field.Store.YES : Field.Store.NO));
                }
            }
        } catch (Exception e) {
            log.error("Failed to add field("+fn+") to document.", e);
        }
    }

    public static void main(String[] args) throws IOException {
        String body = new String(Files.readAllBytes(Paths.get("D:\\WORKDIR\\Gitee Search\\json\\test-repo-body.json")));
        QueueTask task = new QueueTask();
        task.setBody(body);
        task.setType("repo");
        task.setAction("add");
        task2doc(task).forEach(d -> System.out.println(d));
    }

}
