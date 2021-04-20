/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.queue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.core.Constants;
import com.gitee.kooder.index.IndexManager;

import com.gitee.kooder.models.Issue;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.models.Searchable;
import com.gitee.kooder.utils.JsonUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列中的任务
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueTask implements Serializable {

    private transient final static Logger log = LoggerFactory.getLogger(QueueTask.class);

    public transient final static List<String> types = Arrays.asList(
            Constants.TYPE_CODE,
            Constants.TYPE_REPOSITORY,
            Constants.TYPE_ISSUE,
            Constants.TYPE_PR,
            Constants.TYPE_COMMIT,
            Constants.TYPE_WIKI,
            Constants.TYPE_USER
    );

    public transient final static String ACTION_ADD            = "add"; //添加
    public transient final static String ACTION_UPDATE         = "update"; //修改
    public transient final static String ACTION_DELETE         = "delete"; //删除

    private String type;    //对象类型
    private String action;  //动作（添加、删除、修改）
    private List<Searchable> objects = new ArrayList<>();    //objects list

    public QueueTask(){}

    public static void push(String type, String action, Searchable...obj){
        QueueTask task = new QueueTask();
        task.type = type;
        task.action = action;
        task.objects.addAll(Arrays.asList(obj));
        QueueFactory.getProvider().queue(type).push(Arrays.asList(task));
    }

    public static void add(String type, Searchable...obj) {
        push(type, ACTION_ADD, obj);
    }

    public static void update(String type, Searchable...obj) {
        push(type, ACTION_UPDATE, obj);
    }

    public static void delete(String type, Searchable...obj) {
        push(type, ACTION_DELETE, obj);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public final static boolean isAvailType(String p_type) {
        return (p_type!=null) && types.contains(p_type.toLowerCase());
    }

    public final static boolean isAvailAction(String p_action) {
        return ACTION_ADD.equalsIgnoreCase(p_action) || ACTION_DELETE.equalsIgnoreCase(p_action) || ACTION_UPDATE.equalsIgnoreCase(p_action);
    }

    public boolean isCodeTask() {
        return Constants.TYPE_CODE.equals(type);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Searchable> getObjects() {
        return objects;
    }

    public void setObjects(List<Searchable> objects) {
        this.objects = objects;
    }

    public void addObject(Searchable obj) {
        objects.add(obj);
    }

    public void setObjects(String json) {
        TypeReference typeRefer;
        switch(type) {
            case Constants.TYPE_CODE:
                typeRefer = new TypeReference<List<CodeRepository>>(){};
                break;
            case Constants.TYPE_REPOSITORY:
                typeRefer = new TypeReference<List<Repository>>() {};
                break;
            case Constants.TYPE_ISSUE:
                typeRefer = new TypeReference<List<Issue>>() {};
                break;
            default:
                throw new IllegalArgumentException("Illegal task type: " + type);
        }
        this.setObjects((List<Searchable>)JsonUtils.readValue(json, typeRefer));
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

    /**
     * 生成 json
     * @return
     */
    public String json() {
        return JsonUtils.toJson(this);
    }

    public static QueueTask parse(String json) {
        return JsonUtils.readValue(json, QueueTask.class);
    }

    @Override
    public String toString() {
        return "QueueTask{" +
                "type='" + type + '\'' +
                ", action='" + action + '\'' +
                ", objects=" + objects +
                '}';
    }
}
