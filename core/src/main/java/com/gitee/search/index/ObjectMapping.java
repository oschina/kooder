package com.gitee.search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.gitee.search.queue.QueueTask;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * body -> document
 * @author Winter Lau<javayou@gmail.com>
 */
public class ObjectMapping {

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
        String[] names = fname.split(",");
        if(names.length == 2) {
            Map<String, Object> subMap = (Map<String, Object>)map.computeIfAbsent(names[0], m -> new HashMap<String, Object>());
            subMap.put(names[1], fvalue);
        }
        else {
            map.put(fname, fvalue);
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
            System.out.println(obj.toString());
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
        switch(setting.getType()){
            case "long":
                doc.add(new NumericDocValuesField(fn, field.longValue()));
                break;
            case "integer":
                doc.add(new NumericDocValuesField(fn, field.intValue()));
                break;
            case "text":
                doc.add(new TextField(fn, field.textValue(), setting.isStore()?Field.Store.YES:Field.Store.NO));
                break;
            default:
                doc.add(new StringField(fn, field.textValue(), setting.isStore()?Field.Store.YES:Field.Store.NO));
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
