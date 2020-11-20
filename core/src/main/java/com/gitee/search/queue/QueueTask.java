package com.gitee.search.queue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.index.ObjectMapping;
import com.gitee.search.storage.StorageFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列中的任务
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueTask {

    private final static Logger log = LoggerFactory.getLogger(QueueTask.class);

    private final static JsonFactory jackson = new JsonFactory().enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

    public final static String TYPE_REPOSITORY    = "repo"; //仓库
    public final static String TYPE_ISSUE         = "issue";
    public final static String TYPE_PR            = "pr";
    public final static String TYPE_COMMIT        = "commit";
    public final static String TYPE_WIKI          = "wiki";
    public final static String TYPE_CODE          = "code";
    public final static String TYPE_USER          = "user";

    public final static List<String> types = Arrays.asList(
            TYPE_REPOSITORY,
            TYPE_ISSUE,
            TYPE_PR,
            TYPE_COMMIT,
            TYPE_WIKI,
            TYPE_CODE,
            TYPE_USER
    );

    public final static String ACTION_ADD            = "add"; //添加
    public final static String ACTION_UPDATE         = "update"; //修改
    public final static String ACTION_DELETE         = "delete"; //删除

    private String type;    //对象类型
    private String action;  //动作（添加、删除、修改）
    private String body;    //操作详情

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public final static boolean isAvailType(String type) {
        return types.contains(type.toLowerCase());
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 检查参数是否有效
     * @return
     */
    public boolean check() {
        boolean actionOk = ACTION_ADD.equals(action) || ACTION_UPDATE.equals(action) || ACTION_DELETE.equals(action);
        if(actionOk && types.contains(type))
            return isValidJSON(body);
        return false;
    }

    /**
     * 合并 JSON
     * @return
     * @exception
     */
    public String json() {
        StringWriter str = new StringWriter();
        JsonGenerator json = null;
        try {
            json = jackson.createGenerator(str);
            json.writeStartObject();
            //json.useDefaultPrettyPrinter(); // enable indentation just to make debug/t
            json.writeStringField("type", this.type);
            json.writeStringField("action", this.action);
            if(body != null) {
                json.writeFieldName("body");
                json.writeRaw(":");
                json.writeRaw(body);
            }
            json.writeEndObject();
        } catch(IOException e) {
            log.error("Failed to generate json", e);
        } finally {
            try {
                json.close();
            } catch (IOException e) {}
        }
        return str.toString();
    }

    /**
     *
     * 解析 JSON 为 Task
     * @param json
     * @return
     */
    public static QueueTask parse(String json) {
        try {
            QueueTask task = new QueueTask();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            task.setType(node.get("type").textValue());
            task.setAction(node.get("action").textValue());
            task.setBody(node.get("body").toString());
            return task;
        } catch (JsonParseException e) {
            log.error("Failed to parse json:\n"+json, e);
        } catch (IOException e) {
            log.error("Failed to parse json:\n"+json, e);
        }
        return null;
    }

    /**
     * 写入索引库
     * @exception
     */
    public void write() throws IOException {
        List<Document> docs = ObjectMapping.task2doc(this);
        if(docs.size() > 0)
        try (IndexWriter writer = StorageFactory.getStorage().getWriter(this.type)) {
            switch(this.action) {
                case ACTION_ADD:
                    writer.addDocuments(docs);
                    break;
                case ACTION_UPDATE:
                    for(Document doc : docs) {
                        writer.updateDocument(new Term(ObjectMapping.FIELD_ID, doc.get(ObjectMapping.FIELD_ID)), doc);
                    }
                    break;
                case ACTION_DELETE:
                    Term[] terms = docs.stream().map(d -> new Term(ObjectMapping.FIELD_ID, d.get(ObjectMapping.FIELD_ID))).toArray(Term[]::new);
                    writer.deleteDocuments(terms);
            }
            log.info(docs.size() + " documents writed to index.");
        }
    }

    public static boolean isValidJSON(final String json) {
        boolean valid = true;
        try{
            new ObjectMapper().readTree(json);
        } catch(JsonProcessingException e){
            valid = false;
        }
        return valid;
    }

    public static void main(String[] args) {
        QueueTask task = new QueueTask();
        task.setType("repo");
        task.setAction("add");
        task.setBody("{\"name\":\"Winter Lau\"}");
        System.out.println(task.check());

        //QueueTask t = QueueTask.parse(task.json());
        //System.out.printf("type:%s,action:%s,body:%s\n", t.type, t.action, t.body);
    }

}
