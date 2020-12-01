package com.gitee.search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.gitee.search.queue.QueueTask;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * body -> document
 * TODO 如何在没有定义 mapping 的情况下实现文档的映射
 * @author Winter Lau<javayou@gmail.com>
 */
public class ObjectMapping {

    private final static Logger log = LoggerFactory.getLogger(ObjectMapping.class);

    public final static String FIELD_ID = "id"; //文档的唯一标识
    public final static String FIELD_objects = "objects";

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
        //TODO 增加 gitee search 评分信息
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
            docs.add(parseObjectJson(mapping, obj));
        }
        return docs;
    }

    /**
     * 将 object json 转成 lucene 文档
     * @param mapping
     * @param node
     * @return
     */
    private static Document parseObjectJson(IndexMapping mapping, JsonNode node) {
        Document doc = new Document();
        node.fields().forEachRemaining( e -> {
            String fn = e.getKey();
            JsonNode field = e.getValue();
            JsonNodeType ftype = field.getNodeType();
            if(ftype == JsonNodeType.OBJECT){
                field.fields().forEachRemaining( f -> {
                    String sub_fn = fn + "." + f.getKey();
                    IndexMapping.Settings sub_fs = mapping.getField(sub_fn);
                    addSimpleField(doc, sub_fn, f.getValue(), sub_fs);
                });
            }
            else {
                addSimpleField(doc, fn, field, mapping.getField(fn));
            }
        });
        return doc;
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
        if(field == null || field.isNull())
            return ;
        try {
            switch (setting.getType()) {
                case "long":
                    doc.add(new NumericDocValuesField(fn, field.longValue()));
                    if (setting.isStore())
                        doc.add(new StoredField(fn, field.longValue()));
                    break;
                case "integer":
                    doc.add(new NumericDocValuesField(fn, field.intValue()));
                    if (setting.isStore())
                        doc.add(new StoredField(fn, field.intValue()));
                    break;
                case "text":
                    doc.add(new TextField(fn, getTextValue(field), setting.isStore() ? Field.Store.YES : Field.Store.NO));
                    break;
                default:
                    doc.add(new StringField(fn, getTextValue(field), setting.isStore() ? Field.Store.YES : Field.Store.NO));
            }
        } catch (Exception e) {
            log.error("Failed to add field("+fn+") to document.", e);
        }
    }

    private static String getTextValue(JsonNode field) {
        StringBuilder val = new StringBuilder();
        if(field.isArray()){
            field.elements().forEachRemaining(e -> {
                if(val.length() > 0)
                    val.append(",");
                val.append(e.asText());
            });
        }
        else
            val.append(field.textValue());
        return val.toString();
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
