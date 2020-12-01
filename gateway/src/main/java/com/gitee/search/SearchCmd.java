package com.gitee.search;

import com.gitee.search.action.SearchAction;
import jline.TerminalFactory;
import jline.console.ConsoleReader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

            StringBuilder json = SearchAction.repositories(new HashMap<String, List<String>>() {{
                put("q", Arrays.asList(line));
            }}, null);

            System.out.println("total time: " + (System.currentTimeMillis() - ct) + " ms");

        } while(true);

        reader.shutdown();
        System.exit(0);
    }

}
