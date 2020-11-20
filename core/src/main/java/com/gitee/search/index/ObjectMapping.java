package com.gitee.search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.gitee.search.queue.QueueTask;
import org.apache.lucene.document.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * body -> document
 * @author Winter Lau<javayou@gmail.com>
 */
public class ObjectMapping {

    public final static String FIELD_ID = "id";

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
        Iterator<JsonNode> objects = mapper.readTree(task.getBody()).withArray("objects").elements();
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
        switch(setting.getType()){
            case "long":
                doc.add(new SortedNumericDocValuesField(fn, field.longValue()));
                if(setting.isStore())
                    doc.add(new StoredField(fn, field.longValue()));
                break;
            case "integer":
                doc.add(new SortedNumericDocValuesField(fn, field.intValue()));
                if(setting.isStore())
                    doc.add(new StoredField(fn, field.intValue()));
                break;
            case "text":
                doc.add(new TextField(fn, field.textValue(), setting.isStore()?Field.Store.YES:Field.Store.NO));
                break;
            default:
                doc.add(new StringField(fn, field.textValue(), setting.isStore()?Field.Store.YES:Field.Store.NO));
        }

        //System.out.printf("%s -> %s {%s}\n", fn, field.toString(), setting.toString());
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
