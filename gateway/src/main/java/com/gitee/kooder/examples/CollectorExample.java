package com.gitee.kooder.examples;

import com.gitee.kooder.storage.StorageFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Collector example
 * @author Winter Lau<javayou@gmail.com>
 */
public class CollectorExample {

    private final static Logger log = LoggerFactory.getLogger(CollectorExample.class);

    public static void main(String[] args) throws IOException {
        try (IndexReader reader = StorageFactory.getIndexReader("repo")) {
            IndexSearcher searcher = new IndexSearcher(reader);
            long ct = System.currentTimeMillis();
            searcher.search(new MatchAllDocsQuery(), new SimpleCollector() {
                @Override
                public ScoreMode scoreMode() {
                    return ScoreMode.COMPLETE;
                }

                @Override
                public void collect(int doc) throws IOException {
                    log.info("{}", doc);
                }
            });
        }
        SimpleCollector d;
    }

}
