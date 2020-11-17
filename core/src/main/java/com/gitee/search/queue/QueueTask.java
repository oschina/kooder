package com.gitee.search.queue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列中的任务
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueTask {

    private final static Logger log = LoggerFactory.getLogger(QueueTask.class);

    private final static JsonFactory jackson = new JsonFactory();

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
            json.writeFieldName("body");
            json.writeRaw(":");
            json.writeRaw(body);
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
     * TODO: 写入索引库
     * @exception
     */
    public void write() throws IOException {
        System.out.println("task writed to index");
    }

    public static void main(String[] args) {
        QueueTask task = new QueueTask();
        task.setType("repo");
        task.setAction("add");
        task.setBody("{\"name\":\"Winter Lau\"}");
        System.out.println(task.json());

        QueueTask t = QueueTask.parse(task.json());
        System.out.printf("type:%s,action:%s,body:%s\n", t.type, t.action, t.body);
    }

}
