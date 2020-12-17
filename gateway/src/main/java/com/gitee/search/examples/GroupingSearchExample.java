package com.gitee.search.examples;

import com.gitee.search.storage.StorageFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.*;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * GroupingSearch example
 * @author Winter Lau<javayou@gmail.com>
 */
public class GroupingSearchExample {

    private final static Logger log = LoggerFactory.getLogger(GroupingSearchExample.class);

    public static void main(String[] args) throws IOException {
        Sort sort = new Sort(new SortField("count.star", SortField.Type.INT, true));
        GroupingSearch groupingSearch = new GroupingSearch("lang");
        groupingSearch.setGroupSort(sort);
        groupingSearch.setSortWithinGroup(sort);
        groupingSearch.setGroupDocsLimit(10);
        //进行分组的域上建立的必须是SortedDocValuesField类型
        try (IndexReader reader = StorageFactory.getIndexReader("repo")) {
            IndexSearcher searcher = new IndexSearcher(reader);
            long ct = System.currentTimeMillis();
            TopGroups<BytesRef> result = groupingSearch.search(searcher, new MatchAllDocsQuery(), 0,10);

            log.info("totalHitCount: {}, groupCount:{}, time:{}ms", result.totalHitCount, result.groups.length, System.currentTimeMillis()-ct);

            // 按照分组打印查询结果
            for (GroupDocs<BytesRef> groupDocs : result.groups){
                if (groupDocs != null) {
                    log.info("[{},{}]", groupDocs.groupValue.utf8ToString(), groupDocs.totalHits);

                    for(ScoreDoc scoreDoc : groupDocs.scoreDocs){
                        Document doc = searcher.doc(scoreDoc.doc);
                        log.info("\tdoc:{},id:{},name:{},stars:{},score:{}",
                                scoreDoc.doc, doc.get("id"), doc.get("name"), doc.get("count.star"), scoreDoc.score);
                    }
                }
            }
        }
    }

}
