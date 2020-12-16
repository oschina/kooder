package com.gitee.search.queue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.index.IndexManager;

import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
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

    public final static boolean isAvailType(String p_type) {
        return types.contains(p_type.toLowerCase());
    }

    public final static boolean isAvailAction(String p_action) {
        return ACTION_ADD.equalsIgnoreCase(p_action) || ACTION_DELETE.equalsIgnoreCase(p_action) || ACTION_UPDATE.equalsIgnoreCase(p_action);
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
        if(isAvailAction(action) && isAvailType(type))
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
        } catch (IOException e) {
            log.error("Failed to parse json:\n"+json, e);
        }
        return null;
    }

    /**
     * 写入索引库
     * @return
     * @exception
     */
    public int write() throws IOException {
        return IndexManager.write(this);
    }

    /**
     * 用于多线程环境下共享 IndexWriter 写入
     * @param i_writer
     * @param t_writer
     * @return
     * @throws IOException
     */
    public int write(IndexWriter i_writer, TaxonomyWriter t_writer) throws IOException {
        return IndexManager.write(this, i_writer, t_writer);
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

}
