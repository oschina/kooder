package com.gitee.search;

import com.gitee.search.action.SearchAction;
import com.gitee.search.core.SearchHelper;
import com.gitee.search.index.IndexManager;
import com.gitee.search.query.QueryHelper;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.server.Request;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

/**
 * 测试搜索的控制台程序
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchCmd {

    static {
        jline.TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, jline.UnsupportedTerminal.class);
    }

    public static void main(String[] args) throws Exception {
        ConsoleReader reader = new ConsoleReader();
        do {
            String line = reader.readLine("> ");
            if (line == null || "quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line))
                break;

            long ct = System.currentTimeMillis();

            String q = SearchHelper.cleanupKey(line);
            Query query = QueryHelper.buildRepoQuery(q, null, 0);

            String json = IndexManager.searchAfter(QueueTask.TYPE_REPOSITORY, null, query, 10);
            System.out.println(json);

            System.out.println("total time: " + (System.currentTimeMillis() - ct) + " ms");

        } while(true);

        reader.shutdown();
        System.exit(0);
    }

}
