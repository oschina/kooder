package com.gitee.search.index;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * 对象索引映射配置
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexMapping {

    private final static Logger log = LoggerFactory.getLogger(IndexMapping.class);

    private static Map<String, IndexMapping> mappings ;

    static {
        try {
            mappings = new HashMap() {{
                put(Constants.TYPE_REPOSITORY, parseJson(Constants.TYPE_REPOSITORY, "/mapping-repo.json"));
                put(Constants.TYPE_ISSUE, parseJson(Constants.TYPE_ISSUE,           "/mapping-issue.json"));
                put(Constants.TYPE_CODE, parseJson(Constants.TYPE_CODE,             "/mapping-code.json"));
            }};
        }catch (Exception e) {
            log.error("Failed to load mapping json", e);
        }
    }

    private String type;
    private String comment;
    private Settings defaultSetting;
    private Map<String, Settings> fields;

    private IndexMapping(){}

    public final static IndexMapping get(String type) {
        return mappings.get(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public Settings getField(String field) {
        return fields.computeIfAbsent(field, f -> defaultSetting);
    }

    public boolean isStore(String field) {
        return fields.get(field).isStore();
    }

    public boolean isIndex(String field) { return fields.get(field).isIndex(); }

    public String getType(String field) { return fields.get(field).getType(); }

    /**
     * 罗列所有的 facet 字段
     * @return
     */
    public List<String> listFacetFields() {
        if(Constants.TYPE_CODE.equals(type)){
            return Arrays.asList(Constants.FIELD_LANGUAGE, Constants.FIELD_CODE_OWNER, Constants.FIELD_REPO_NAME, Constants.FIELD_SCM);
        }
        List<String> facets = new ArrayList<>();
        fields.forEach((k,v) -> {
            if(v.isFacet())
                facets.add(k);
        });
        return facets;
    }

    /**
     * 解析 mapping-xxx.json
     * @param type
     * @param jsonRes
     * @return
     * @throws IOException
     */
    private static IndexMapping parseJson(String type, String jsonRes) throws IOException {
        IndexMapping im = new IndexMapping();
        im.setType(type);
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
            log.error("Failed turn mapping object to json.", e);
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

        public boolean isFacet() {
            return "facet".equalsIgnoreCase(type);
        }

        public boolean isNumber() {
            return "long".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type);
        }

        public boolean isText() {
            return "text".equalsIgnoreCase(type);
        }

        public boolean isString() {
            return "string".equalsIgnoreCase(type);
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

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("type=" + type);
            sb.append(",store=" + store);
            sb.append(",index=" + index);
            return sb.toString();
        }

    }
}
