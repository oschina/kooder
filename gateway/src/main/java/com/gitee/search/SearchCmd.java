package com.gitee.search;

import com.gitee.search.action.SearchAction;
import com.gitee.search.server.Request;
import jline.TerminalFactory;
import jline.console.ConsoleReader;

/**
 * 测试搜索的控制台程序
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchCmd {

    public static void main(String[] args) throws Exception {

        jline.TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, jline.UnsupportedTerminal.class);

        ConsoleReader reader = new ConsoleReader();
        do {
            String line = reader.readLine("> ");

            if (line == null || "quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line))
                break;

            long ct = System.currentTimeMillis();

            Request req = new Request(){
                @Override
                public String param(String name) {
                    return "q".equals(name)?line:super.param(name);
                }

                {

            }};

            String json = SearchAction.repositories(req);

            System.out.println("total time: " + (System.currentTimeMillis() - ct) + " ms");

        } while(true);

        reader.shutdown();
        System.exit(0);
    }

}
