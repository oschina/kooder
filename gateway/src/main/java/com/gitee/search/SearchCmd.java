package com.gitee.search;

import com.gitee.search.core.Constants;
import com.gitee.search.core.SearchHelper;
import com.gitee.search.models.QueryResult;
import com.gitee.search.query.IQuery;
import com.gitee.search.query.QueryFactory;
import jline.TerminalFactory;
import jline.console.ConsoleReader;

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
            IQuery query = QueryFactory.CODE().setSearchKey(q);
            query.addFacets(Constants.FIELD_LANGUAGE, "Java");
            QueryResult result = query.execute();
            System.out.println(result.toString());

            System.out.println("total time: " + (System.currentTimeMillis() - ct) + " ms");

        } while(true);

        reader.shutdown();
        System.exit(0);
    }

}
