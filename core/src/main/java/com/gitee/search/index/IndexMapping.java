package com.gitee.search.index;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象索引映射配置
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexMapping {

    private final static JsonFactory jackson = new JsonFactory();

    private String comment;
    private Settings defaultSetting;
    private Map<String, Settings> fields;

    public static void main(String[] args) throws IOException {
        System.out.println(repo);
    }

    private IndexMapping(){}

    public final static IndexMapping repo = parseJson("/mapping-repo.json");
    public final static IndexMapping issue = parseJson("/mapping-issue.json");
    public final static IndexMapping code = parseJson("/mapping-code.json");

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Settings getDefaultSetting() {
        return defaultSetting;
    }

    public void setDefaultSetting(Settings defaultSetting) {
        this.defaultSetting = defaultSetting;
    }

    public Map<String, Settings> getFields() {
        return fields;
    }

    public void setFields(Map<String, Settings> fields) {
        this.fields = fields;
    }

    /**
     * 解析 mapping-xxx.json
     * @param jsonRes
     * @return
     * @throws IOException
     */
    private static IndexMapping parseJson(String jsonRes) {
        IndexMapping im = new IndexMapping();
        try (InputStream stream = IndexMapping.class.getResourceAsStream(jsonRes)) {
            JsonNode json = new ObjectMapper().readTree(stream);
            im.comment = json.get("comment").textValue();
            JsonNode defaultSettingNode = json.get("default-settings");
            im.defaultSetting = new Settings(defaultSettingNode);
            //parse fields
            im.fields = new HashMap<>();
            JsonNode fieldMapping = json.get("fields");
            fieldMapping.fields().forEachRemaining(e -> {
                im.fields.put(e.getKey(), new Settings(e.getValue()));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return im;
    }

    private String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter str = new StringWriter();
        JsonGenerator json = mapper.getFactory().createGenerator(str);
        json.writeStartObject();
        json.useDefaultPrettyPrinter();
        json.writeStringField("comment", this.comment);
        json.writeObjectField("default-settings", this.defaultSetting);
        json.writeObjectField("fields", fields);
        json.writeEndObject();
        json.close();
        return str.toString();
    }

    @Override
    public String toString() {
        try {
            return this.toJson();
        } catch( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字段的索引配置
     */
    public static class Settings {

        private String type;
        private boolean store = true;
        private boolean index = false;

        public Settings(){}
        public Settings(JsonNode node) {
            if(node.get("store") != null)
                this.store = node.get("store").booleanValue();
            if(node.get("index") != null)
                this.index = node.get("index").booleanValue();
            this.type = node.get("type").textValue();
        }

        public boolean isStore() {
            return store;
        }

        public void setStore(boolean store) {
            this.store = store;
        }

        public boolean isIndex() {
            return index;
        }

        public void setIndex(boolean index) {
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
