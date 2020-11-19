package com.gitee.search.index;

import com.gitee.search.queue.QueueTask;
import org.apache.lucene.document.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * body -> document
 * @author Winter Lau<javayou@gmail.com>
 */
public class ObjectMapping {

    /**
     * TODO: 将 task 转成 lucene 文档
     * @param task
     * @return
     */
    public final static List<Document> task2doc(QueueTask task) {
        List<Document> docs = new ArrayList<>();

        return docs;
    }

}
